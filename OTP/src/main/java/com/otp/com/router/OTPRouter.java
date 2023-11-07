package com.otp.com.router;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.otp.com.Handler.OTPHandler;

@Configuration
public class OTPRouter {
	@Autowired
	private OTPHandler otpHandler;
	
	@Bean
	RouterFunction<ServerResponse> saveUser(){
		return RouterFunctions
				.route().POST("/save", otpHandler::saveUserData).build()
				.andRoute(RequestPredicates.GET("/validate/{id}"), otpHandler::validateOTP)
				.andRoute(RequestPredicates.POST("/resend/{id}"), otpHandler::resendOTP);
	}
	
}
