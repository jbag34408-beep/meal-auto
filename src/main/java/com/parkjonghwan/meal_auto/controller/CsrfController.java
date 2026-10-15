package com.parkjonghwan.meal_auto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 화면이 CSRF 토큰 쿠키(XSRF-TOKEN)를 아직 못 받았을 때 부르는 주소
// 토큰은 필요할 때 만들어지므로(지연 생성) 여기서 꺼내 봐야 응답에 쿠키가 실림
// 로그인·로그아웃 때 토큰이 새로 바뀌면 화면이 다시 부름
@RestController
public class CsrfController {

  @GetMapping("/csrf")
  public ResponseEntity<Void> csrf(CsrfToken token) {
    token.getToken();   // 토큰을 만들어서 쿠키로 내려 보내게 함
    return ResponseEntity.noContent().build();
  }
}
