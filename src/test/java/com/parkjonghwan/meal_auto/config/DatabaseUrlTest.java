package com.parkjonghwan.meal_auto.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DatabaseUrlTest {

  @AfterEach
  void clearProperties() {
    System.clearProperty("spring.datasource.url");
    System.clearProperty("spring.datasource.username");
    System.clearProperty("spring.datasource.password");
  }

  @Test
  void Render_PostgreSQL_주소를_JDBC_주소와_아이디_비밀번호로_나눈다() {
    var jdbc = DatabaseUrl.fromUri(
        "postgres://meal_user:s3cret@dpg-abc123-a.singapore-postgres.render.com:5432/mealdb").orElseThrow();

    assertThat(jdbc.url()).isEqualTo("jdbc:postgresql://dpg-abc123-a.singapore-postgres.render.com:5432/mealdb");
    assertThat(jdbc.username()).isEqualTo("meal_user");
    assertThat(jdbc.password()).isEqualTo("s3cret");
  }

  @Test
  void Neon_주소는_포트가_없고_sslmode가_붙어도_그대로_옮긴다() {
    var jdbc = DatabaseUrl.fromUri(
        "postgresql://neondb_owner:p%40ss%2Fword@ep-cool-1234.ap-southeast-1.aws.neon.tech/neondb?sslmode=require")
        .orElseThrow();

    assertThat(jdbc.url()).isEqualTo("jdbc:postgresql://ep-cool-1234.ap-southeast-1.aws.neon.tech/neondb?sslmode=require");
    assertThat(jdbc.username()).isEqualTo("neondb_owner");
    assertThat(jdbc.password()).isEqualTo("p@ss/word");   // %인코딩된 비밀번호도 원래대로
  }

  @Test
  void JDBC_주소나_빈_값은_바꾸지_않는다() {
    assertThat(DatabaseUrl.fromUri("jdbc:h2:file:./data/mealdb")).isEmpty();
    assertThat(DatabaseUrl.fromUri("jdbc:postgresql://localhost:5432/mealdb")).isEmpty();
    assertThat(DatabaseUrl.fromUri(null)).isEmpty();
  }

  @Test
  void 환경_변수의_주소를_Spring_설정으로_넣고_따로_넣은_아이디는_우선한다() {
    DatabaseUrl.applyFromEnvironment(Map.of(
        "DATABASE_URL", "postgres://u:p@db.example.com/meal",
        "DB_USERNAME", "override"));

    assertThat(System.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://db.example.com/meal");
    assertThat(System.getProperty("spring.datasource.username")).isNull();   // DB_USERNAME이 그대로 쓰임
    assertThat(System.getProperty("spring.datasource.password")).isEqualTo("p");
  }

  @Test
  void JDBC_주소면_아무_설정도_건드리지_않는다() {
    DatabaseUrl.applyFromEnvironment(Map.of("DB_URL", "jdbc:postgresql://localhost/meal"));

    assertThat(System.getProperty("spring.datasource.url")).isNull();
  }
}
