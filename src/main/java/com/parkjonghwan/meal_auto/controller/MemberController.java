package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.dto.MemberResponse;
import com.parkjonghwan.meal_auto.dto.SignupRequest;
import com.parkjonghwan.meal_auto.dto.TargetRequest;
import com.parkjonghwan.meal_auto.service.MemberService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class MemberController {
  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  // POST /signup → 회원가입 (로그인 없이 접근 가능)
  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public MemberResponse signup(@Valid @RequestBody SignupRequest request) {
    return MemberResponse.from(memberService.signup(request));
  }

  // GET /me → 내 정보와 목표 (Principal = 지금 로그인한 사람)
  @GetMapping("/me")
  public MemberResponse me(Principal principal) {
    return MemberResponse.from(memberService.get(principal.getName()));
  }

  // PUT /me/targets → 내 목표 수정
  @PutMapping("/me/targets")
  public MemberResponse updateTargets(Principal principal,
                                      @Valid @RequestBody TargetRequest request) {
    return MemberResponse.from(memberService.updateTargets(principal.getName(), request));
  }
}