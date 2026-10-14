package com.parkjonghwan.meal_auto.exception;

// 조회 기간이 잘못됨 (시작일이 종료일보다 늦거나, 허용 기간을 넘음)
public class InvalidDateRangeException extends RuntimeException {
  public InvalidDateRangeException(String message) {
    super(message);
  }
}
