package com.otp.com.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "otpverification")

public class OTP {	
	@Id
	private Long id;
	private String emailAddress;
	private Long otpNumber;
	private String status="activate";
}
