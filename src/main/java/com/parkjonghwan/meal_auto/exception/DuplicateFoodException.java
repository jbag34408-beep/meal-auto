package com.parkjonghwan.meal_auto.exception;

public class DuplicateFoodException extends RuntimeException {
  public DuplicateFoodException(String name) {
    super("이미 등록된 음식이에요: " + name);
  }
}