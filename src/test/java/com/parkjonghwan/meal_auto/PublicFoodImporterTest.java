package com.parkjonghwan.meal_auto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.parkjonghwan.meal_auto.repository.PublicFoodRepository;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

// TSV를 public_food에 넣는지, 틀린 줄이 있으면 기존 데이터를 남기는지 확인 (메모리 H2)
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:public-food-importer-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "gemini.api-key="
})
class PublicFoodImporterTest {

  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired ConfigurableApplicationContext context;
  @Autowired PublicFoodRepository repository;

  @TempDir Path dir;

  @Test
  void TSV를_넣고_다시_넣으면_새_내용으로_바뀌며_틀린_파일이면_기존_내용이_남는다() throws Exception {
    PublicFoodImporter importer = new PublicFoodImporter(jdbc, transactionManager, context, "unused");
    String header = "name\tmaker\tbasis\tkcal\tcarbohydrate\tprotein\tfat\n";

    Path first = Files.writeString(dir.resolve("a.tsv"), header
        + "사과\t\t100g\t52\t14\t0.3\t0.2\n"
        + "초코과자, 딸기맛\t오리온\t100g\t480\t60\t6\t24\n", StandardCharsets.UTF_8);
    assertThat(importer.importFile(first)).isEqualTo(2);
    assertThat(repository.findFirstByNameAndMaker("초코과자, 딸기맛", "오리온")).isPresent();

    Path second = Files.writeString(dir.resolve("b.tsv"), header + "바나나\t\t100g\t90\t23\t1\t0.3\n",
        StandardCharsets.UTF_8);
    assertThat(importer.importFile(second)).isEqualTo(1);
    assertThat(repository.findTop20ByNameContainingOrderByNameAsc("사과")).isEmpty();

    Path broken = Files.writeString(dir.resolve("c.tsv"), header + "포도\t\t100g\t60\n", StandardCharsets.UTF_8);
    assertThatThrownBy(() -> importer.importFile(broken)).hasMessageContaining("칸 수");
    assertThat(repository.findTop20ByNameContainingOrderByNameAsc("바나나")).hasSize(1);
    assertThat(repository.existsByIdNotNull()).isTrue();
  }
}
