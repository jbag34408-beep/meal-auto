package com.parkjonghwan.meal_auto.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "members")   // member는 일부 DB에서 예약어라 복수형으로
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String username;

  @Column(nullable = false)
  private String password;   // BCrypt로 암호화된 값만 저장

  // 회원별 하루 목표 (가입 시 기본값)
  private double targetKcal = 2000;
  private double targetCarbohydrate = 300;
  private double targetProtein = 60;
  private double targetFat = 50;

  protected Member() {}

  public Member(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public void updateTargets(double kcal, double carbohydrate, double protein, double fat) {
    this.targetKcal = kcal;
    this.targetCarbohydrate = carbohydrate;
    this.targetProtein = protein;
    this.targetFat = fat;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getPassword() { return password; }
  public double getTargetKcal() { return targetKcal; }
  public double getTargetCarbohydrate() { return targetCarbohydrate; }
  public double getTargetProtein() { return targetProtein; }
  public double getTargetFat() { return targetFat; }
}