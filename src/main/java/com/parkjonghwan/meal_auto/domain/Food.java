package com.parkjonghwan.meal_auto.domain;

import jakarta.persistence.Entity;
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

  protected Food() {}   // JPA용 빈 생성자

  public Food(String name, double kcalPer100g, double carbohydrate,
              double protein, double fat) {
    this.name = name;
    this.kcalPer100g = kcalPer100g;
    this.carbohydrate = carbohydrate;
    this.protein = protein;
    this.fat = fat;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public double getKcalPer100g() { return kcalPer100g; }
  public double getCarbohydrate() { return carbohydrate; }
  public double getProtein() { return protein; }
  public double getFat() { return fat; }
}
