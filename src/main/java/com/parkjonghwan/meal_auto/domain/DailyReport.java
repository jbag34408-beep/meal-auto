package com.parkjonghwan.meal_auto.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "report_date"}))
public class DailyReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id")
  @JsonIgnore   // 응답 JSON에 회원 정보(비밀번호 해시 포함)가 섞여 나가지 않게
  private Member member;

  @Column(name = "report_date")
  private LocalDate date;

  private int mealCount;
  private double totalKcal;
  private double totalCarbohydrate;
  private double totalProtein;
  private double totalFat;

  @ManyToOne
  @JoinColumn(name = "recommended_food_id")
  private Food recommendedFood;   // 목표를 다 채운 날은 null

  private Double recommendedGrams;   // recommendedFood가 null이면 같이 null

  private LocalDateTime createdAt;   // 최초 생성 시각 (갱신 때도 유지)

  protected DailyReport() {}

  public DailyReport(Member member, LocalDate date, int mealCount, double totalKcal,
                      double totalCarbohydrate, double totalProtein, double totalFat,
                      Food recommendedFood, Double recommendedGrams, LocalDateTime createdAt) {
    this.member = member;
    this.date = date;
    this.createdAt = createdAt;
    updateFrom(mealCount, totalKcal, totalCarbohydrate, totalProtein, totalFat,
        recommendedFood, recommendedGrams);
  }

  // 같은 회원·같은 날짜 리포트가 이미 있으면 새로 만들지 않고 이 메서드로 덮어씀
  public void updateFrom(int mealCount, double totalKcal, double totalCarbohydrate,
                          double totalProtein, double totalFat,
                          Food recommendedFood, Double recommendedGrams) {
    this.mealCount = mealCount;
    this.totalKcal = totalKcal;
    this.totalCarbohydrate = totalCarbohydrate;
    this.totalProtein = totalProtein;
    this.totalFat = totalFat;
    this.recommendedFood = recommendedFood;
    this.recommendedGrams = recommendedGrams;
  }

  public Long getId() { return id; }
  public Member getMember() { return member; }
  public LocalDate getDate() { return date; }
  public int getMealCount() { return mealCount; }
  public double getTotalKcal() { return totalKcal; }
  public double getTotalCarbohydrate() { return totalCarbohydrate; }
  public double getTotalProtein() { return totalProtein; }
  public double getTotalFat() { return totalFat; }
  public Food getRecommendedFood() { return recommendedFood; }
  public Double getRecommendedGrams() { return recommendedGrams; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
