package com.weple.cloud;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WepleApplicationTests {

    @Autowired
    @Qualifier("jasyptStringEncryptor")
    private StringEncryptor jasypt;

 @Test
void 대칭키_암호화_테스트() {

    String plainText = "my-secret-db-password";

String encryptedText = jasypt.encrypt(plainText);

 System.out.println("Encrypted Result:");
 System.out.println(encryptedText);
  }
    
@Test
 void checkBean() {
      System.out.println(jasypt);
  }
  
    @Test
    void db암호화값생성() {

    	System.out.println("class 이름 암호화:");
        System.out.println(jasypt.encrypt("oracle.jdbc.OracleDriver"));
        
        System.out.println("url 암호화:");
        System.out.println(jasypt.encrypt("jdbc:oracle:thin:@3.34.91.19:1521:xe"));
    	
        System.out.println("username 암호화:");
        System.out.println(jasypt.encrypt("memo"));

        System.out.println("password 암호화:");
        System.out.println(jasypt.encrypt("memo"));
    }
}