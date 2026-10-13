package com.parkjonghwan.meal_auto.service;

import com.parkjonghwan.meal_auto.domain.Member;
import com.parkjonghwan.meal_auto.dto.SignupRequest;
import com.parkjonghwan.meal_auto.dto.TargetRequest;
import com.parkjonghwan.meal_auto.exception.DuplicateMemberException;
import com.parkjonghwan.meal_auto.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public Member signup(SignupRequest request) {
    if (memberRepository.existsByUsername(request.username())) {
      throw new DuplicateMemberException(request.username());
    }
    // 비밀번호는 원문 대신 암호화한 값만 저장
    return memberRepository.save(
        new Member(request.username(), passwordEncoder.encode(request.password())));
  }

  @Transactional(readOnly = true)
  public Member get(String username) {
    return memberRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalStateException("로그인 정보를 찾을 수 없어요"));
  }

  @Transactional
  public Member updateTargets(String username, TargetRequest request) {
    Member member = get(username);
    member.updateTargets(request.kcal(), request.carbohydrate(),
        request.protein(), request.fat());
    return member;   // 트랜잭션이 끝날 때 변경 내용이 자동으로 DB에 반영됨 (변경 감지)
  }
}