package com.parkjonghwan.meal_auto.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MealEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "meal_date")      // date는 DB 예약어와 겹칠 수 있어 컬럼명 변경
  private LocalDate date;

  @Enumerated(EnumType.STRING)     // enum을 "LUNCH" 글자로 저장
  private MealType mealType;

  @ManyToOne                       // 여러 기록 → 음식 하나
  @JoinColumn(name = "food_id")
  private Food food;

  private double grams;

  protected MealEntry() {}

  public MealEntry(LocalDate date, MealType mealType, Food food, double grams) {
    this.date = date;
    this.mealType = mealType;
    this.food = food;
    this.grams = grams;
  }

  public Long getId() { return id; }
  public LocalDate getDate() { return date; }
  public MealType getMealType() { return mealType; }
  public Food getFood() { return food; }
  public double getGrams() { return grams; }
}
