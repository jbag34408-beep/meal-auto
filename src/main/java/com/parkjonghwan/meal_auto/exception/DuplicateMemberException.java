package com.parkjonghwan.meal_auto.exception;

public class DuplicateMemberException extends RuntimeException {
  public DuplicateMemberException(String username) {
    super("이미 사용 중인 아이디예요: " + username);
  }
}