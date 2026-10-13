package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.service.FoodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foods")   // 이 클래스의 모든 주소 앞에 /foods 가 붙음
public class FoodController {
  private final FoodService foodService;

  public FoodController(FoodService foodService) {
    this.foodService = foodService;
  }

  // GET /foods  또는  GET /foods?keyword=닭
  @GetMapping
  public List<Food> search(@RequestParam(required = false) String keyword) {
    return foodService.search(keyword);
  }

  // POST /foods → 201 Created
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Food create(@Valid @RequestBody FoodRequest request) {
    return foodService.create(request);
  }
}