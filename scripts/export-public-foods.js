// 식약처 식품영양성분 DB 전체를 API로 받아 TSV로 저장 (PublicFoodImporter가 읽는 형식)
// data.go.kr이 해외 IP를 막으므로 한국에 있는 PC에서 실행
//   FOOD_API_KEY=<일반 인증키> node scripts/export-public-foods.js public-foods.tsv [시작 페이지]
// 중간에 끊기면 마지막으로 찍힌 페이지 다음부터 다시 실행하면 이어서 붙임
const fs = require("fs");

const BASE = "https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02";
const ROWS = 100;
const [file, startArg] = process.argv.slice(2);
const key = process.env.FOOD_API_KEY;
if (!file || !key) {
  console.error("사용법: FOOD_API_KEY=... node scripts/export-public-foods.js <저장할 파일> [시작 페이지]");
  process.exit(1);
}

// 탭·줄바꿈이 섞이면 TSV 칸이 어긋나서 공백으로 바꿈
const text = (v) => String(v ?? "").replace(/[\t\r\n]+/g, " ").trim();
// "52", "1,234", "-", "" → 숫자 (못 바꾸면 0, 소수 첫째 자리까지)
const num = (v) => {
  const n = parseFloat(String(v ?? "").replace(/,/g, ""));
  return Number.isFinite(n) ? Math.round(n * 10) / 10 : 0;
};

async function page(no) {
  const url = `${BASE}?serviceKey=${encodeURIComponent(key)}&pageNo=${no}&numOfRows=${ROWS}&type=json`;
  for (let attempt = 1; ; attempt++) {
    try {
      const res = await fetch(url, { signal: AbortSignal.timeout(30000) });
      const body = await res.text();
      const json = JSON.parse(body);   // 키 오류 등은 JSON이 아니라 여기서 걸림
      const authError = json.OpenAPI_ServiceResponse?.cmmMsgHeader;   // 키 오류는 다른 모양으로 옴
      if (authError) throw new Error(`인증 오류: ${authError.returnAuthMsg} (${authError.errMsg})`);
      const code = json.header?.resultCode;
      if (code && code !== "00") throw new Error(`API 오류 ${code}: ${json.header.resultMsg}`);
      let items = json.body?.items ?? [];
      if (items.item) items = items.item;
      return { total: json.body.totalCount, items: Array.isArray(items) ? items : [items] };
    } catch (e) {
      if (attempt >= 3) throw e;
      console.warn(`${no}페이지 실패, 다시 시도 (${e.message.slice(0, 120)})`);
      await new Promise((r) => setTimeout(r, 2000 * attempt));
    }
  }
}

(async () => {
  const start = Number(startArg ?? 1);
  if (start === 1) fs.writeFileSync(file, "name\tmaker\tbasis\tkcal\tcarbohydrate\tprotein\tfat\n");
  let pages = Infinity;
  for (let no = start; no <= pages; no++) {
    const { total, items } = await page(no);
    pages = Math.ceil(total / ROWS);
    const lines = items
      .filter((i) => text(i.FOOD_NM_KR))
      .map((i) => [text(i.FOOD_NM_KR), text(i.MAKER_NM), text(i.NUTRI_AMOUNT_SERVING),
        num(i.AMT_NUM1), num(i.AMT_NUM6), num(i.AMT_NUM3), num(i.AMT_NUM4)].join("\t"));
    fs.appendFileSync(file, lines.map((l) => l + "\n").join(""));
    if (no % 50 === 0 || no === pages) console.log(`${no} / ${pages} 페이지`);
  }
  console.log("끝");
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
