package com.parkjonghwan.meal_auto.security;

import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security가 로그인할 때 "이 아이디의 회원 정보"를 가져가는 곳
@Service
public class MemberDetailsService implements UserDetailsService {
  private final MemberRepository memberRepository;

  public MemberDetailsService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Member member = memberRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
    // 입력한 비밀번호와 DB의 암호화된 비밀번호 비교는 Spring Security가 알아서 함
    return User.withUsername(member.getUsername())
        .password(member.getPassword())
        .roles("USER")
        .build();
  }
}