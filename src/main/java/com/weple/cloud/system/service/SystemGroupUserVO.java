package com.weple.cloud.system.service;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class SystemGroupUserVO {
	private String user_code;                 //NOT NULL
	private Integer company_id;               //NOT NULL
	private String login_id;                  //NOT NULL
	private String password;                  //NOT NULL
	private String user_name;                 //NOT NULL
	private String email;                     //NOT NULL
	private String phone_number;              //NOT NULL
	private String approval;                  //CHECK
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date create_date;
	private String status;                    //NOT NULL
	private String profile_image;
	private Integer owner_yn;                 //NOT NULL
	private Integer admin_yn;
	private String web_notification_yn;
	private String email_notification_yn;     //PK
	private String notification_atea;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date last_login_time;             //FK
	private Integer group_id;                 //FK
}
