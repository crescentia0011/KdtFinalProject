package com.weple.cloud.file;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 업로드: savedName은 S3 안에서의 키(파일명)로 사용
    public String uploadFile(MultipartFile file, String savedName) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(savedName)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        // S3 퍼블릭 URL 반환
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + savedName;
    }

    // 다운로드: InputStream 반환
    public ResponseInputStream<GetObjectResponse> downloadFile(String savedName) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(savedName)
                .build();
        return s3Client.getObject(request);
    }

    // 삭제
    public void deleteFile(String savedName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(savedName)
                .build();
        s3Client.deleteObject(request);
    }
}