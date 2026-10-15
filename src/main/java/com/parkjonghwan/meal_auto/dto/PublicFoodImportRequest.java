package com.parkjonghwan.meal_auto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 공공데이터 검색 결과에서 "가져오기"할 음식 (영양 값은 받지 않고 서버가 다시 조회)
public record PublicFoodImportRequest(
    @NotBlank(message = "음식 이름은 필수예요")
    @Size(max = 100, message = "음식 이름이 너무 길어요")
    String name,

    String maker   // 같은 이름이 여러 제조사에 있을 때 구분 (일반 식품은 비어 있음)
) {
}
