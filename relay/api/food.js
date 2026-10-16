// 식약처 식품영양성분 API 중계 (서울 리전)
// data.go.kr이 해외 IP 요청을 막아서, 싱가포르의 Render 서버 대신 여기서 호출해 그대로 돌려줌
// 주소는 고정이라 다른 곳으로는 못 넘김, 인증키(serviceKey)는 Render 서버가 쿼리로 보냄 (여기엔 저장 안 함)
const TARGET = "https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02";

module.exports = async (req, res) => {
  if (req.method !== "GET") {
    res.status(405).end();
    return;
  }
  const query = req.url.includes("?") ? req.url.slice(req.url.indexOf("?")) : "";
  try {
    const upstream = await fetch(TARGET + query, { signal: AbortSignal.timeout(8000) });
    res.status(upstream.status)
      .setHeader("content-type", upstream.headers.get("content-type") || "application/json")
      .send(await upstream.text());
  } catch (e) {
    res.status(502).json({ message: "식품영양성분 API에 연결하지 못했어요" });
  }
};
