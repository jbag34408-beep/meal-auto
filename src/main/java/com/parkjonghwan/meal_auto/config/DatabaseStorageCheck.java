package com.parkjonghwan.meal_auto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 배포 환경(Render)에서 H2 파일 DB를 쓰고 있으면 크게 경고
// Render 무료 인스턴스는 재배포·재시작 때 디스크가 초기화돼서 ./data 의 H2 파일이 사라짐
// → 회원·기록이 매번 지워지므로 외부 PostgreSQL(Render PostgreSQL, Neon 등)을 DB_URL로 연결해야 함
@Component
public class DatabaseStorageCheck {
  private static final Logger log = LoggerFactory.getLogger(DatabaseStorageCheck.class);

  private final String datasourceUrl;

  public DatabaseStorageCheck(@Value("${spring.datasource.url:}") String datasourceUrl) {
    this.datasourceUrl = datasourceUrl;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void warnIfDataIsTemporary() {
    // Render는 모든 서비스에 RENDER=true 환경 변수를 넣어 줌
    boolean onRender = "true".equalsIgnoreCase(System.getenv("RENDER"));
    if (onRender && datasourceUrl.startsWith("jdbc:h2:")) {
      log.warn("==========================================================================");
      log.warn(" Render에서 H2 DB({})를 쓰고 있어요.", datasourceUrl);
      log.warn(" 재배포·재시작 때 디스크가 초기화돼서 회원과 기록이 모두 사라져요.");
      log.warn(" Render PostgreSQL이나 Neon의 접속 주소를 환경 변수 DB_URL에 넣어 주세요.");
      log.warn("==========================================================================");
    }
  }
}
