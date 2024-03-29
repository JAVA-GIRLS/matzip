package com.itwill.matzip.dto;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.itwill.matzip.domain.Member;
import com.itwill.matzip.domain.enums.Gender;

import lombok.Data;

@Data
public class MemberSignupRequestDto {
	private String username;
	private String password;
	private String email;
	private String nickname;
	private LocalDate birth;
	private Gender gender;
	
	public Member toEntity(PasswordEncoder encoder) {
		return Member.builder()
										.username(username)
										.password(encoder.encode(password))
										.email(email)
										.nickname(nickname)
										.birth(birth)
										.gender(gender)
										.img("https://domain-web-storage.s3.ap-northeast-2.amazonaws.com/KakaoTalk_20240219_111445259.jpg")
										.build();
	}
}
