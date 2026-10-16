package com.parkjonghwan.meal_auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// 식약처 식품영양성분 DB 한 건 (검색용으로 내장, 가져오기 하면 공용 Food가 만들어짐)
// 데이터는 PublicFoodImporter로 TSV 파일에서 한꺼번에 넣음
@Entity
public class PublicFood {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false, length = 100)
  private String maker;   // 제조사 (일반 식품은 빈 문자열)

  @Column(length = 50)
  private String basis;   // 영양 성분 기준량 (예: 100g)

  private double kcal;
  private double carbohydrate;
  private double protein;
  private double fat;

  protected PublicFood() {}

  public PublicFood(String name, String maker, String basis,
                    double kcal, double carbohydrate, double protein, double fat) {
    this.name = name;
    this.maker = maker;
    this.basis = basis;
    this.kcal = kcal;
    this.carbohydrate = carbohydrate;
    this.protein = protein;
    this.fat = fat;
  }

  public String getName() { return name; }
  public String getMaker() { return maker; }
  public String getBasis() { return basis; }
  public double getKcal() { return kcal; }
  public double getCarbohydrate() { return carbohydrate; }
  public double getProtein() { return protein; }
  public double getFat() { return fat; }
}
