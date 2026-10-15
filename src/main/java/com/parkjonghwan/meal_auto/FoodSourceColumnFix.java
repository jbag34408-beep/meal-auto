package com.parkjonghwan.meal_auto;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// food.source 열이 처음 만들 때의 enum 값(SEED, AI)만 받도록 묶여 있는 문제를 풀어 줌
// - ddl-auto=update는 이미 있는 열의 허용 값을 바꾸지 않아서, 새 값(PUBLIC, USER)을 저장하면 오류가 남
// - H2: 열 타입이 ENUM('AI','SEED') → 일반 문자열로 바꿈
// - PostgreSQL: food_source_check 제약 → 지움
// 어느 쪽이든 이미 풀려 있으면 아무것도 안 함 (서버를 켤 때마다 실행해도 안전)
// DataInitializer(주인 정리 때 USER 값을 씀)보다 먼저 실행
@Component
@Order(0)
public class FoodSourceColumnFix implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(FoodSourceColumnFix.class);

  private final DataSource dataSource;
  private final JdbcTemplate jdbc;

  public FoodSourceColumnFix(DataSource dataSource) {
    this.dataSource = dataSource;
    this.jdbc = new JdbcTemplate(dataSource);
  }

  @Override
  public void run(String... args) throws SQLException {
    String db;
    try (Connection connection = dataSource.getConnection()) {
      db = connection.getMetaData().getDatabaseProductName();
    }

    if (db.equalsIgnoreCase("H2")) {
      Integer enumColumns = jdbc.queryForObject(
          "select count(*) from information_schema.columns"
              + " where table_name = 'FOOD' and column_name = 'SOURCE' and data_type = 'ENUM'",
          Integer.class);
      if (enumColumns != null && enumColumns > 0) {
        jdbc.execute("alter table food alter column source set data type varchar(20)");
        log.info("food.source 열을 문자열로 바꿨어요 (H2)");
      }
    } else if (db.equalsIgnoreCase("PostgreSQL")) {
      jdbc.execute("alter table food drop constraint if exists food_source_check");
    }
  }
}
