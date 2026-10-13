package com.parkjonghwan.meal_auto.controller;

import com.parkjonghwan.meal_auto.domain.Food;
import com.parkjonghwan.meal_auto.dto.FoodRequest;
import com.parkjonghwan.meal_auto.dto.FoodSearchResult;
import com.parkjonghwan.meal_auto.service.FoodService;
import com.parkjonghwan.meal_auto.service.PublicFoodClient;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foods")
public class FoodController {
  private final FoodService foodService;
  private final PublicFoodClient publicFoodClient;

  public FoodController(FoodService foodService, PublicFoodClient publicFoodClient) {
    this.foodService = foodService;
    this.publicFoodClient = publicFoodClient;
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

  // GET /foods/public?keyword=사과 → 식약처 DB 검색 (저장은 안 함)
  @GetMapping("/public")
  public List<FoodSearchResult> searchPublic(@RequestParam String keyword) {
    return publicFoodClient.search(keyword.trim());
  }
}