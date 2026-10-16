package com.parkjonghwan.meal_auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MealEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id")
  @JsonIgnore   // 응답 JSON에 회원 정보(비밀번호 해시 포함)가 섞여 나가지 않게
  private Member member;

  @Column(name = "meal_date")
  private LocalDate date;

  @Enumerated(EnumType.STRING)
  private MealType mealType;

  @ManyToOne
  @JoinColumn(name = "food_id")
  private Food food;

  private double grams;

  protected MealEntry() {}

  public MealEntry(Member member, LocalDate date, MealType mealType, Food food, double grams) {
    this.member = member;
    this.date = date;
    this.mealType = mealType;
    this.food = food;
    this.grams = grams;
  }

  // 회원 정보 없이 만드는 생성자 (계산 테스트용)
  public MealEntry(LocalDate date, MealType mealType, Food food, double grams) {
    this(null, date, mealType, food, grams);
  }

  public Long getId() { return id; }
  public Member getMember() { return member; }
  public LocalDate getDate() { return date; }
  public MealType getMealType() { return mealType; }
  public Food getFood() { return food; }
  public double getGrams() { return grams; }
}