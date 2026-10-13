package com.parkjonghwan.meal_auto.dto;

import com.parkjonghwan.meal_auto.domain.Member;

// 회원 정보를 화면에 보낼 때 사용 (비밀번호는 절대 포함하지 않음)
public record MemberResponse(
    String username, double targetKcal, double targetCarbohydrate,
    double targetProtein, double targetFat) {

  public static MemberResponse from(Member m) {
    return new MemberResponse(m.getUsername(), m.getTargetKcal(),
        m.getTargetCarbohydrate(), m.getTargetProtein(), m.getTargetFat());
  }
}