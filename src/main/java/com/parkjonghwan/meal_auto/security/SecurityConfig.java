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
        // 학습용으로 CSRF 보호는 끔 (실서비스에서는 켜는 것이 원칙)
        .csrf(csrf -> csrf.disable())

        // 로그인 없이 접근 가능한 주소만 열고, 나머지는 모두 로그인 필요
        .authorizeHttpRequests(auth -> auth
          .requestMatchers("/index.html", "/landing.html", "/login.html", "/signup", "/login", "/error", "/favicon.ico").permitAll()
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