package com.parkjonghwan.meal_auto.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;

// 공용 기본 음식 목록 (src/main/resources/seed/foods.csv)
// 한 줄: 이름,kcal,탄수화물,단백질,지방,1인분(g)  — 모두 100g 기준, '#'으로 시작하는 줄은 설명
public final class SeedFoodCatalog {
  public static final String PATH = "seed/foods.csv";

  public record SeedFood(String name, double kcalPer100g, double carbohydrate,
                         double protein, double fat, double servingGrams) {}

  private static final List<SeedFood> FOODS = load();

  private SeedFoodCatalog() {}

  public static List<SeedFood> foods() {
    return FOODS;
  }

  public static Set<String> names() {
    return FOODS.stream().map(SeedFood::name).collect(Collectors.toUnmodifiableSet());
  }

  private static List<SeedFood> load() {
    List<SeedFood> foods = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        new ClassPathResource(PATH).getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      int lineNo = 0;
      while ((line = reader.readLine()) != null) {
        lineNo++;
        String trimmed = line.strip();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        String[] cols = trimmed.split(",");
        if (cols.length != 6) {
          throw new IllegalStateException(PATH + " " + lineNo + "번째 줄의 칸 수가 6개가 아니에요: " + line);
        }
        foods.add(new SeedFood(cols[0].strip(), num(cols[1]), num(cols[2]), num(cols[3]),
            num(cols[4]), num(cols[5])));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(PATH + "를 읽지 못했어요", e);
    }
    return List.copyOf(foods);
  }

  private static double num(String value) {
    return Double.parseDouble(value.strip());
  }
}
