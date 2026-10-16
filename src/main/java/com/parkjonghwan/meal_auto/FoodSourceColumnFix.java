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

// food.source·food.category 열이 처음 만들 때의 enum 값만 받도록 묶여 있는 문제를 풀어 줌
// (예: source가 SEED, AI만 허용돼서 USER·PUBLIC 저장이 실패)
// - ddl-auto=update는 이미 있는 열의 허용 값을 바꾸지 않아서, 새 값(PUBLIC, USER)을 저장하면 오류가 남
// - H2: 열 타입이 ENUM('AI','SEED') → 일반 문자열로 바꿈
// - PostgreSQL: food_source_check 제약 → 지움
// 어느 쪽이든 이미 풀려 있으면 아무것도 안 함 (서버를 켤 때마다 실행해도 안전)
// DataInitializer(기본 음식에 category 값을 씀)보다 먼저 실행
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

    // source(출처)와 category(상차림 분류) 둘 다 enum 값 목록에 묶이지 않게 풀어 줌
    for (String column : new String[] {"source", "category"}) {
      if (db.equalsIgnoreCase("H2")) {
        Integer enumColumns = jdbc.queryForObject(
            "select count(*) from information_schema.columns"
                + " where table_name = 'FOOD' and column_name = ? and data_type = 'ENUM'",
            Integer.class, column.toUpperCase());
        if (enumColumns != null && enumColumns > 0) {
          jdbc.execute("alter table food alter column " + column + " set data type varchar(20)");
          log.info("food.{} 열을 문자열로 바꿨어요 (H2)", column);
        }
      } else if (db.equalsIgnoreCase("PostgreSQL")) {
        jdbc.execute("alter table food drop constraint if exists food_" + column + "_check");
      }
    }
  }
}
