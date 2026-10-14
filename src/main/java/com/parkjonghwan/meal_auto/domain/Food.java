package com.parkjonghwan.meal_auto.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity   // 이 클래스 = DB 테이블 하나
public class Food {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private double kcalPer100g;
  private double carbohydrate;
  private double protein;
  private double fat;

  // DB에 숫자가 아니라 "SEED", "AI" 글자로 저장 (enum 순서가 바뀌어도 안전)
  @Enumerated(EnumType.STRING)
  private FoodSource source;

  protected Food() {}   // JPA용 빈 생성자

  // 기존 생성자: DataInitializer·FoodService가 그대로 쓰고, 출처는 자동으로 SEED
  public Food(String name, double kcalPer100g, double carbohydrate,
              double protein, double fat) {
    this(name, kcalPer100g, carbohydrate, protein, fat, FoodSource.SEED);
  }

  // AI로 만든 음식은 FoodSource.AI를 넘겨서 생성
  public Food(String name, double kcalPer100g, double carbohydrate,
              double protein, double fat, FoodSource source) {
    this.name = name;
    this.kcalPer100g = kcalPer100g;
    this.carbohydrate = carbohydrate;
    this.protein = protein;
    this.fat = fat;
    this.source = source;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public double getKcalPer100g() { return kcalPer100g; }
  public double getCarbohydrate() { return carbohydrate; }
  public double getProtein() { return protein; }
  public double getFat() { return fat; }

  // 출처 기능 이전에 저장된 음식은 source가 비어 있으므로 SEED로 취급
  public FoodSource getSource() {
    return source != null ? source : FoodSource.SEED;
  }
}
