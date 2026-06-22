(() => {
    const root = document.getElementById("repositoryDetail");
    if (!root || !root.dataset.repositoryUrl) return;

    const repositoryPath = root.dataset.repositoryUrl
        .replace(/^https:\/\/github\.com\//, "")
        .replace(/\.git\/?$/, "")
        .replace(/\/$/, "");
    const apiBase = `https://api.github.com/repos/${repositoryPath}`;
    const branchSelect = document.getElementById("githubBranch");
    const directoryInput = document.getElementById("githubDirectory");
    const tree = document.getElementById("githubFileTree");
    const fileTitle = document.getElementById("githubFileTitle");
    const fileContent = document.getElementById("githubFileContent");
    const commitList = document.getElementById("githubCommitList");
    const errorMessage = document.getElementById("githubError");

    const getJson = async (path) => {
        const response = await fetch(`${apiBase}${path}`, {
            headers: { Accept: "application/vnd.github+json" }
        });
        if (!response.ok) throw new Error("GitHub API request failed");
        return response.json();
    };

    const createTreeItem = (entry, branch, directory) => {
        const item = document.createElement("a");
        item.className = entry.type === "dir" ? "repository-tree-folder" : "";
        item.href = "#";
        item.innerHTML = `<i class="fa ${entry.type === "dir" ? "fa-folder" : "fa-file-code-o"}"></i><span></span>`;
        item.querySelector("span").textContent = entry.name;
        if (entry.type === "dir") {
            item.addEventListener("click", (event) => {
                event.preventDefault();
                loadRepository(branch, entry.path);
            });
        } else {
            item.addEventListener("click", (event) => {
                event.preventDefault();
                loadFile(entry.path, branch);
            });
        }
        return item;
    };

    const renderCommits = (commits, branch) => {
        commitList.replaceChildren();
        commits.forEach((commit) => {
            const row = document.createElement("tr");
            [commit.sha.slice(0, 8), branch, commit.commit.message,
                commit.commit.author?.email || "-", commit.commit.author?.date || "-"].forEach((value) => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });
            commitList.appendChild(row);
        });
    };

    const loadFile = async (path, branch) => {
        fileTitle.textContent = path;
        fileContent.textContent = "파일 내용을 불러오는 중입니다.";
        try {
            const file = await getJson(`/contents/${path}?ref=${encodeURIComponent(branch)}`);
            const binary = atob((file.content || "").replace(/\s/g, ""));
            const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
            fileContent.textContent = new TextDecoder().decode(bytes);
        } catch {
            fileContent.textContent = "이 파일의 내용을 GitHub에서 불러오지 못했습니다.";
        }
    };

    const loadRepository = async (branch, directory = "") => {
        try {
            const repository = await getJson("");
            const selectedBranch = branch || repository.default_branch;
            const [branches, files, commits] = await Promise.all([
                getJson("/branches?per_page=100"),
                getJson(`/contents${directory ? `/${directory}` : ""}?ref=${encodeURIComponent(selectedBranch)}`),
                getJson(`/commits?sha=${encodeURIComponent(selectedBranch)}&per_page=10")
            ]);
            branchSelect.replaceChildren();
            branches.forEach((item) => {
                const option = new Option(item.name, item.name, item.name === selectedBranch, item.name === selectedBranch);
                branchSelect.add(option);
            });
            directoryInput.value = directory || "/";
            tree.replaceChildren();
            if (directory) {
                const parent = directory.includes("/") ? directory.substring(0, directory.lastIndexOf("/")) : "";
                const up = document.createElement("a");
                up.className = "repository-tree-folder";
                up.href = "#";
                up.innerHTML = '<i class="fa fa-level-up"></i><span>..</span>';
                up.addEventListener("click", (event) => { event.preventDefault(); loadRepository(selectedBranch, parent); });
                tree.appendChild(up);
            }
            files.forEach((file) => tree.appendChild(createTreeItem(file, selectedBranch, directory)));
            renderCommits(commits, selectedBranch);
            fileTitle.textContent = "파일을 선택하세요.";
            fileContent.textContent = "왼쪽 목록에서 파일을 선택하세요.";
            if (errorMessage) errorMessage.hidden = true;
        } catch {
            if (errorMessage) errorMessage.hidden = false;
        }
    };

    branchSelect.addEventListener("change", () => loadRepository(branchSelect.value));
    loadRepository();
})();
