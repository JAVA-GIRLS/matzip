package com.itwill.matzip.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.itwill.matzip.domain.Member;
import com.itwill.matzip.domain.enums.MemberRole;


public class MemberSecurityDto extends User implements OAuth2User {
	
	
	private static final long serialVersionUID = 1L;
	
	private Long userId;
	private String nickname;
	private String img;

	public MemberSecurityDto(Long userId,String nickname, String img, String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.userId = userId;
		this.nickname = nickname;
		this.img = img;
	}
	
	public static MemberSecurityDto fromEntity(Member entity) {
		
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		
		for(MemberRole role : entity.getRoles()) {
			SimpleGrantedAuthority auth = new SimpleGrantedAuthority(role.getAuthority());
			authorities.add(auth);
		}
		
		return new MemberSecurityDto(entity.getId(), entity.getNickname(), entity.getImg(), entity.getUsername(), entity.getPassword(), authorities);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}
	

	public Long getUserid() {
		return this.userId;
	}
	
	public String getNickname() {
		return this.nickname;
	}
	

	public String getImg() {
		return this.img;
	}
	
	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}	
	
	public void updateImg(String img) {
		this.img = img;
	}
}
