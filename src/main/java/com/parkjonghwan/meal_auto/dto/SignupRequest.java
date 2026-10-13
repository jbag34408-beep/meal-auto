package com.parkjonghwan.meal_auto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "아이디는 필수예요")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문·숫자 4~20자여야 해요")
    String username,

    @NotBlank(message = "비밀번호는 필수예요")
    @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 해요")
    String password
) {
}