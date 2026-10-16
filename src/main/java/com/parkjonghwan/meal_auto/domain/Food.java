package com.parkjonghwan.meal_auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

  // 1인분(g): 기본 음식에만 있음 (추천할 때 최대 2인분까지만 권함, 비어 있으면 제한 없음)
  private Double servingGrams;

  // 상차림 자리 (밥·국·주반찬·나물·김치): 기본 음식에만 있음, 비어 있으면 식단 추천에 안 씀
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private FoodCategory category;

  // 주인: 비어 있으면 모두가 쓰는 공용 음식(기본 음식·공공데이터),
  // 있으면 그 회원만 보고 쓰는 개인 음식(직접 등록·AI 추정)
  // 화면에는 회원 정보를 내보내지 않음 (대신 shared 값으로 공용 여부만 알려 줌)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  @JsonIgnore
  private Member owner;

  protected Food() {}   // JPA용 빈 생성자

  // 공용 기본 음식 (DataInitializer, 테스트)
  public Food(String name, double kcalPer100g, double carbohydrate,
              double protein, double fat) {
    this(name, kcalPer100g, carbohydrate, protein, fat, FoodSource.SEED, null);
  }

  // owner가 null이면 공용, 있으면 그 회원의 개인 음식
  public Food(String name, double kcalPer100g, double carbohydrate,
              double protein, double fat, FoodSource source, Member owner) {
    this.name = name;
    this.kcalPer100g = kcalPer100g;
    this.carbohydrate = carbohydrate;
    this.protein = protein;
    this.fat = fat;
    this.source = source;
    this.owner = owner;
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

  public Double getServingGrams() { return servingGrams; }
  public FoodCategory getCategory() { return category; }

  @JsonIgnore
  public Member getOwner() { return owner; }

  // JSON에는 "shared": true/false 로 나감
  public boolean isShared() { return owner == null; }

  // 기본 음식의 1인분 양 지정 (DataInitializer)
  public Food withServingGrams(Double servingGrams) {
    this.servingGrams = servingGrams;
    return this;
  }

  // 기본 음식의 상차림 자리 지정 (DataInitializer)
  public Food withCategory(FoodCategory category) {
    this.category = category;
    return this;
  }

}
