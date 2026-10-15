package com.parkjonghwan.meal_auto.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 보호: 세션 쿠키로 로그인하므로 다른 사이트가 몰래 보낸 요청을 막아야 함
        // spa(): 토큰을 XSRF-TOKEN 쿠키(JS가 읽을 수 있음)로 내려 주고,
        //        화면은 POST·PUT·DELETE 때 그 값을 X-XSRF-TOKEN 헤더로 돌려보냄 (frontend/src/lib/csrf.ts)
        // 로그인·회원가입·로그아웃도 모두 토큰이 있어야 처리됨
        .csrf(csrf -> csrf.spa())

        // 로그인 없이 접근 가능한 주소만 열고, 나머지는 모두 로그인 필요
        .authorizeHttpRequests(auth -> auth
          .requestMatchers("/", "/landing.html", "/login.html", "/assets/**", "/signup", "/login", "/csrf", "/error", "/favicon.ico").permitAll()
            .anyRequest().authenticated())

        // 로그인: 화면은 login.html, 처리 주소는 POST /login
        // 페이지 이동 대신 상태 코드만 돌려줘서 JS가 결과를 판단
        .formLogin(form -> form
            .loginPage("/login.html")
            .loginProcessingUrl("/login")
            .successHandler((req, res, auth) -> res.setStatus(200))
            .failureHandler((req, res, ex) -> res.setStatus(401)))

        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler((req, res, auth) -> res.setStatus(200)))

        // 로그인 안 된 요청: 화면 요청이면 로그인 페이지로, API 요청이면 401
        .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
          String accept = req.getHeader("Accept");
          if (accept != null && accept.contains("text/html")) {
            res.sendRedirect("/login.html");
          } else {
            res.setStatus(401);
          }
        }));

    return http.build();
  }
}