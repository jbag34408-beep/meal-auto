package com.parkjonghwan.meal_auto;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// 식약처 식품영양성분 DB(TSV 파일)를 public_food 테이블에 한꺼번에 넣고 서버를 끝냄 (기존 내용은 지우고 새로)
// 실행: DB_URL을 넣을 DB로 잡고  ./gradlew bootRun --args="--public-food.import=파일경로"
// 파일: scripts/export-public-foods.js가 만든 TSV (머리글 한 줄 + name maker basis kcal carbohydrate protein fat)
// 한 트랜잭션으로 넣어서 중간에 실패하면 기존 데이터가 그대로 남음
@Component
@Order(2)   // DataInitializer 다음
@ConditionalOnProperty("public-food.import")
public class PublicFoodImporter implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(PublicFoodImporter.class);
  private static final int BATCH = 1000;
  private static final String INSERT = "insert into public_food"
      + " (name, maker, basis, kcal, carbohydrate, protein, fat) values (?, ?, ?, ?, ?, ?, ?)";

  private final JdbcTemplate jdbc;
  private final TransactionTemplate transactionTemplate;
  private final ConfigurableApplicationContext context;
  private final String path;

  public PublicFoodImporter(JdbcTemplate jdbc, PlatformTransactionManager transactionManager,
                            ConfigurableApplicationContext context,
                            @Value("${public-food.import}") String path) {
    this.jdbc = jdbc;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.context = context;
    this.path = path;
  }

  @Override
  public void run(String... args) {
    int count = importFile(Path.of(path));
    log.info("식약처 DB {}건을 넣었어요. 서버를 끝낼게요.", count);
    System.exit(SpringApplication.exit(context, () -> 0));
  }

  int importFile(Path file) {
    return transactionTemplate.execute(status -> {
      jdbc.update("delete from public_food");
      int count = 0;
      List<Object[]> batch = new ArrayList<>();
      try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
        reader.readLine();   // 머리글
        String line;
        int lineNo = 1;
        while ((line = reader.readLine()) != null) {
          lineNo++;
          if (line.isBlank()) {
            continue;
          }
          String[] c = line.split("\t", -1);
          if (c.length != 7) {
            throw new IllegalStateException(file + " " + lineNo + "번째 줄의 칸 수가 7개가 아니에요: " + line);
          }
          batch.add(new Object[] {c[0], c[1], c[2], Double.parseDouble(c[3]), Double.parseDouble(c[4]),
              Double.parseDouble(c[5]), Double.parseDouble(c[6])});
          if (batch.size() == BATCH) {
            count += flush(batch);
          }
        }
      } catch (IOException e) {
        throw new IllegalStateException(file + "를 읽지 못했어요", e);
      }
      return count + flush(batch);
    });
  }

  private int flush(List<Object[]> batch) {
    jdbc.batchUpdate(INSERT, batch);
    int size = batch.size();
    batch.clear();
    return size;
  }
}
