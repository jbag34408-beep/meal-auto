package com.parkjonghwan.meal_auto.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

// Render PostgreSQL·Neon이 주는 접속 주소를 Spring이 쓰는 JDBC 형식으로 바꿈
//   postgres://아이디:비밀번호@호스트:5432/DB이름?sslmode=require
//   → spring.datasource.url      = jdbc:postgresql://호스트:5432/DB이름?sslmode=require
//     spring.datasource.username = 아이디
//     spring.datasource.password = 비밀번호
// 대시보드에서 복사한 주소를 DB_URL(또는 DATABASE_URL)에 그대로 넣으면 됨
// jdbc:로 시작하는 주소는 손대지 않음 (기존처럼 DB_USERNAME·DB_PASSWORD와 함께 사용)
public final class DatabaseUrl {

  public record Jdbc(String url, String username, String password) {}

  private DatabaseUrl() {}

  // postgres:// 또는 postgresql:// 주소면 JDBC 정보로 바꿔서 돌려줌
  public static Optional<Jdbc> fromUri(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String trimmed = value.trim();
    if (!trimmed.startsWith("postgres://") && !trimmed.startsWith("postgresql://")) {
      return Optional.empty();
    }

    URI uri = URI.create(trimmed);
    String host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("DB 주소에 호스트가 없어요: " + mask(trimmed));
    }

    StringBuilder url = new StringBuilder("jdbc:postgresql://").append(host);
    if (uri.getPort() != -1) {
      url.append(':').append(uri.getPort());
    }
    url.append(uri.getRawPath() == null ? "" : uri.getRawPath());
    if (uri.getRawQuery() != null) {
      url.append('?').append(uri.getRawQuery());
    }

    String username = null;
    String password = null;
    String userInfo = uri.getRawUserInfo();
    if (userInfo != null) {
      int colon = userInfo.indexOf(':');
      username = decode(colon < 0 ? userInfo : userInfo.substring(0, colon));
      password = colon < 0 ? null : decode(userInfo.substring(colon + 1));
    }
    return Optional.of(new Jdbc(url.toString(), username, password));
  }

  // 환경 변수에 postgres:// 주소가 있으면 Spring 설정(시스템 속성)으로 넣음
  // 시스템 속성은 application.properties보다 우선이라 ${DB_URL} 자리를 덮어씀
  // DB_USERNAME·DB_PASSWORD를 따로 넣었다면 그 값을 우선함
  public static void applyFromEnvironment(Map<String, String> env) {
    String raw = firstNonBlank(env.get("DB_URL"), env.get("DATABASE_URL"));
    fromUri(raw).ifPresent(jdbc -> {
      System.setProperty("spring.datasource.url", jdbc.url());
      setIfAbsent(env, "DB_USERNAME", "spring.datasource.username", jdbc.username());
      setIfAbsent(env, "DB_PASSWORD", "spring.datasource.password", jdbc.password());
    });
  }

  private static void setIfAbsent(Map<String, String> env, String envName, String property, String value) {
    String explicit = env.get(envName);
    if ((explicit == null || explicit.isBlank()) && value != null) {
      System.setProperty(property, value);
    }
  }

  private static String firstNonBlank(String a, String b) {
    if (a != null && !a.isBlank()) {
      return a;
    }
    return b != null && !b.isBlank() ? b : null;
  }

  private static String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  // 오류 메시지에 비밀번호가 찍히지 않도록 가림
  private static String mask(String value) {
    return value.replaceAll("://[^@/]*@", "://***@");
  }
}
