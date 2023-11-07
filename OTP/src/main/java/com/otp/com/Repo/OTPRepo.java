package com.otp.com.Repo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.otp.com.pojo.OTP;

import reactor.core.publisher.Mono;

@Repository
public interface OTPRepo extends ReactiveCrudRepository<OTP, Long>{

	Mono<OTP> getByEmailAddress(String emailAddress);

}
