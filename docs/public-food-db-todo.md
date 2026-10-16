# 식약처 공공 DB 등록 — 남은 작업 (2026-10-16 기준)

## 진행 상황

| 항목 | 상태 |
|---|---|
| 코드 (`public_food` 테이블·검색·가져오기·넣기 도구) | ✅ 커밋 `7028fb3`까지 완료, 원격과 같음 |
| 테스트 (`./gradlew test`) | ✅ 전부 통과 |
| 식약처 데이터 받기 (TSV) | ❌ `public-foods.tsv`가 머리글 한 줄뿐, 데이터 0건 |
| 운영 DB에 넣기 | ⏸ 데이터가 없어서 아직 못 함 |

관련 코드
- `scripts/export-public-foods.js`: API에서 전체를 받아 TSV로 저장 (한국 PC에서만 됨, 이어받기 가능)
- `PublicFoodImporter`: `--public-food.import=파일`로 실행하면 기존 내용을 지우고 다시 넣은 뒤 서버를 끝냄 (한 트랜잭션)
- `PublicFood`, `PublicFoodRepository`, `FoodService.searchPublic / hasPublicFoods / importPublic`

## 막힌 원인

API를 불러 보면 이렇게 거절됨:
```
403 SERVICE_KEY_IS_NOT_REGISTERED_ERROR (등록되지 않은 서비스키)
```
키를 인코딩하든 안 하든 같음. 그래서 코드 문제가 아니라 이 키로 이 API(`FoodNtrCpntDbInfo02`)를 활용 신청하지 않았거나 승인이 아직 반영되지 않은 상태.

## 작업 순서 (Windows PowerShell)

### 0. 키 준비
- data.go.kr → "식품의약품안전처_식품영양성분DB정보" → **활용신청**
- 마이페이지에서 **일반 인증키(Decoding)** 복사 (승인 직후에는 1~2시간 뒤에야 동작하기도 함)
- 아래 결과에 `"resultCode":"00"`이 보이면 준비 끝
  ```powershell
  $env:FOOD_API_KEY="<키>"
  node -e "fetch('https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02?serviceKey='+encodeURIComponent(process.env.FOOD_API_KEY)+'&pageNo=1&numOfRows=1&type=json').then(r=>r.text()).then(console.log)"
  ```

### 1. 전체 데이터 받기 (한국 PC, 오래 걸림)
```powershell
node scripts/export-public-foods.js public-foods.tsv
```
- 중간에 끊기면 마지막으로 찍힌 페이지 다음 번호부터 이어받기: `node scripts/export-public-foods.js public-foods.tsv 151`
- 건수 확인: `(Get-Content public-foods.tsv | Measure-Object -Line).Lines`

### 2. 로컬 DB(H2, `./data`)에 넣어서 확인
```powershell
./gradlew bootRun --args="--public-food.import=public-foods.tsv"
```
- 끝나면 서버가 저절로 꺼짐
- `./gradlew bootRun` + `npm run dev` → 관리 화면에서 "사과"를 검색하고 가져오기까지 확인

### 3. 운영 DB에 넣기
```powershell
$env:DB_URL="postgres://아이디:비밀번호@호스트/DB이름"   # Render/Neon 대시보드 주소 그대로
./gradlew bootRun --args="--public-food.import=public-foods.tsv"
Remove-Item Env:DB_URL   # 끝나면 바로 지우기
```
- 기존 `public_food` 내용을 모두 지우고 새로 넣음, 중간에 실패하면 원래 데이터가 그대로 남음
- 테이블은 `ddl-auto=update`가 만들어 줌, 다시 배포할 필요 없음

## 주의
- README의 `FOOD_API_KEY=... node ...`는 bash용 형식이라 PowerShell에서는 위의 `$env:` 형식을 써야 함
- 3단계에서는 로컬 코드가 운영 DB에 붙음 → 로컬이 배포된 커밋과 같은지 먼저 확인 (다르면 `ddl-auto=update`가 운영 스키마를 바꿈)
- 운영 DB 용량 확인: Neon 무료 0.5GB, Render 무료 1GB (수십만 건이면 수십~100MB 예상)
- 검색이 `%키워드%` 방식이라 데이터가 많아지면 느릴 수 있음 → 그때 PostgreSQL `pg_trgm` 인덱스 추가 (`PublicFoodRepository`의 shortcut 주석)
