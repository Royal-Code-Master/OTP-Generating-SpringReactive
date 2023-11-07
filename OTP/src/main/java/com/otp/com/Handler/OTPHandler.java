package com.otp.com.Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.otp.com.Repo.OTPRepo;
import com.otp.com.pojo.OTP;

import reactor.core.publisher.Mono;

@Service
public class OTPHandler {
	@Autowired
	private OTPRepo otpRepo;

	@Autowired
	private ReactiveMongoTemplate reactiveMongoTemplate;

	@Autowired
	private JavaMailSender javaMailSender;

	// save
	public Mono<ServerResponse> saveUserData(ServerRequest serverRequest) {
		Mono<OTP> monoOTP = serverRequest.bodyToMono(OTP.class);
		return monoOTP.flatMap(saveuser -> {
			String emailExist = saveuser.getEmailAddress();
			Long userIdExist = saveuser.getId();
			return otpRepo.getByEmailAddress(emailExist).flatMap(emailAlreadyExist -> {
				return ServerResponse.ok().bodyValue("This Email " + emailExist + " Already Verified");
			}).switchIfEmpty(otpRepo.findById(userIdExist).flatMap(userById -> {
				return ServerResponse.ok().bodyValue("This User ID Exists: " + userIdExist);
			}).switchIfEmpty(Mono.defer(() -> {
				generateOTP(saveuser);
				return otpRepo.save(saveuser).flatMap(savedUser -> ServerResponse.ok().bodyValue(savedUser));
			})));
		});
	}

	// find by email id
	public Mono<OTP> getEmailAddress(String emailAddress) {
		return otpRepo.getByEmailAddress(emailAddress);
	}

	// create OTP
	private void generateOTP(OTP otp) {
		long minNumber = 100000;
		long maxNumber = 999999;
		long getOTP = (long) (Math.random() * (maxNumber - minNumber)) + minNumber;
		otp.setOtpNumber(getOTP);
		sendEmail(otp);

	}

	// validate OTP and email address
	public Mono<ServerResponse> validateOTP(ServerRequest serverRequest) {
		return serverRequest.bodyToMono(OTP.class).flatMap(requestedOTP -> {
			return otpRepo.findById(Long.parseLong(serverRequest.pathVariable("id"))).flatMap(existingUser -> {
				if (existingUser != null) {

					String existingEmail = existingUser.getEmailAddress();
					Long existingOtp = existingUser.getOtpNumber();

					String requestedEmail = requestedOTP.getEmailAddress();
					Long requestedOtp = requestedOTP.getOtpNumber();

					if (existingUser.getStatus().equals("deactivated") && existingEmail.equals(requestedEmail)) {
						return ServerResponse.ok()
								.bodyValue("Already This " + existingEmail + " is verified with OTP : " + existingOtp
										+ " --> " + existingUser.getStatus().toUpperCase());
					}

					else {
						if (existingEmail.equals(requestedEmail) && existingOtp.equals(requestedOtp)) {
							Query query = new Query(Criteria.where("id").is(existingUser.getId()));
							Update update = new Update().set("status", "deactivated");
							return reactiveMongoTemplate.updateFirst(query, update, OTP.class)
									.flatMap(result -> ServerResponse.ok()
											.bodyValue("Your Email Address " + existingEmail + " is Verified"));
						}
					}
				}
				return ServerResponse.ok().bodyValue("not match");
			}).switchIfEmpty(
					ServerResponse.ok().bodyValue(Long.parseLong(serverRequest.pathVariable("id")) + " is Not Found"));
		});
	}

	// send OTP to email
	private void sendEmail(OTP otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("youremail@gmail.com");
		message.setTo(otp.getEmailAddress());
		message.setText("Your OTP Number : " + otp.getOtpNumber());
		message.setSubject("OTP Verification");

		javaMailSender.send(message);

		System.out.println("Mail sent Successfully::");
	}
	
	
	// re-send OTP to email
	public Mono<ServerResponse> resendOTP(ServerRequest serverRequest){
		Mono<OTP> monoOTP = serverRequest.bodyToMono(OTP.class);
		return monoOTP.flatMap(checkuser->{
			String checkEmail = checkuser.getEmailAddress();
			
			return otpRepo.findById(Long.parseLong(serverRequest.pathVariable("id"))).flatMap(idExist->{
				return otpRepo.getByEmailAddress(checkEmail).flatMap(emailExist ->{
					generateOTP(checkuser);
					return otpRepo.save(checkuser).flatMap(checkedUser -> ServerResponse.ok().bodyValue("OTP Re-Send Successfully"));
				}).switchIfEmpty(ServerResponse.ok().bodyValue("This Email : "+checkEmail +" Not Exist"));
			}).switchIfEmpty(ServerResponse.ok().bodyValue("This Id : "+serverRequest.pathVariable("id") +" Not Exist"));
		}).switchIfEmpty(ServerResponse.ok().bodyValue("Data Not Found"));
	}
	
	// get latest
}
