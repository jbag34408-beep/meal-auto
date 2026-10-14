import { useEffect, useMemo, useRef, useState } from "react"
import { Canvas, useFrame } from "@react-three/fiber"
import { Environment } from "@react-three/drei"
import * as THREE from "three"

export type AuraTheme = "light" | "dark"

type Props = {
  theme: AuraTheme
  reducedMotion: boolean
  onReady?: () => void
}

// 3차원 simplex noise (Ashima Arts, MIT) — 정점을 굽이치게 미는 데 사용
const SIMPLEX_NOISE = /* glsl */ `
vec3 mod289(vec3 x){return x-floor(x*(1.0/289.0))*289.0;}
vec4 mod289(vec4 x){return x-floor(x*(1.0/289.0))*289.0;}
vec4 permute(vec4 x){return mod289(((x*34.0)+10.0)*x);}
vec4 taylorInvSqrt(vec4 r){return 1.79284291400159-0.85373472095314*r;}
float snoise(vec3 v){
  const vec2 C=vec2(1.0/6.0,1.0/3.0);
  const vec4 D=vec4(0.0,0.5,1.0,2.0);
  vec3 i=floor(v+dot(v,C.yyy));
  vec3 x0=v-i+dot(i,C.xxx);
  vec3 g=step(x0.yzx,x0.xyz);
  vec3 l=1.0-g;
  vec3 i1=min(g.xyz,l.zxy);
  vec3 i2=max(g.xyz,l.zxy);
  vec3 x1=x0-i1+C.xxx;
  vec3 x2=x0-i2+C.yyy;
  vec3 x3=x0-D.yyy;
  i=mod289(i);
  vec4 p=permute(permute(permute(i.z+vec4(0.0,i1.z,i2.z,1.0))+i.y+vec4(0.0,i1.y,i2.y,1.0))+i.x+vec4(0.0,i1.x,i2.x,1.0));
  float n_=0.142857142857;
  vec3 ns=n_*D.wyz-D.xzx;
  vec4 j=p-49.0*floor(p*ns.z*ns.z);
  vec4 x_=floor(j*ns.z);
  vec4 y_=floor(j-7.0*x_);
  vec4 x=x_*ns.x+ns.yyyy;
  vec4 y=y_*ns.x+ns.yyyy;
  vec4 h=1.0-abs(x)-abs(y);
  vec4 b0=vec4(x.xy,y.xy);
  vec4 b1=vec4(x.zw,y.zw);
  vec4 s0=floor(b0)*2.0+1.0;
  vec4 s1=floor(b1)*2.0+1.0;
  vec4 sh=-step(h,vec4(0.0));
  vec4 a0=b0.xzyw+s0.xzyw*sh.xxyy;
  vec4 a1=b1.xzyw+s1.xzyw*sh.zzww;
  vec3 p0=vec3(a0.xy,h.x);
  vec3 p1=vec3(a0.zw,h.y);
  vec3 p2=vec3(a1.xy,h.z);
  vec3 p3=vec3(a1.zw,h.w);
  vec4 norm=taylorInvSqrt(vec4(dot(p0,p0),dot(p1,p1),dot(p2,p2),dot(p3,p3)));
  p0*=norm.x;p1*=norm.y;p2*=norm.z;p3*=norm.w;
  vec4 m=max(0.5-vec4(dot(x0,x0),dot(x1,x1),dot(x2,x2),dot(x3,x3)),0.0);
  m=m*m;
  return 105.0*dot(m*m,vec4(dot(p0,x0),dot(p1,x1),dot(p2,x2),dot(p3,x3)));
}
uniform float uTime;
uniform float uAmp;
uniform float uFreq;
// 구 표면을 바깥쪽으로 밀어 내는 높이 (큰 물결 + 작은 물결)
float auraField(vec3 p){
  vec3 q=p*uFreq;
  return (snoise(q+vec3(0.0,0.0,uTime))+0.18*snoise(q*2.0-vec3(uTime*0.6,0.0,0.0)))*uAmp;
}
`

// 테마별 금속 색과 주변 빛
// 금속 구는 정면에서 카메라 쪽(+z)을, 가장자리에서 뒤쪽(-z)을 비춤.
// 그래서 "뒤쪽에서 잰 각도"에 따라 색을 칠한 둥근 환경을 만들면
// 흰 덩어리 → 회청색 테두리 → 그 안의 밝은 띠가 경계 없이 부드럽게 이어짐
type Stop = [angle: number, color: string]
const LOOK: Record<AuraTheme, { metal: string; roughness: number; stops: Stop[] }> = {
  light: {
    metal: "#ffffff",
    roughness: 0.26,
    stops: [
      [0, "#aab3c4"],
      [30, "#9ba6b9"],
      [47, "#8d99af"],
      [58, "#e4ebf7"],
      [68, "#97a3b8"],
      [88, "#c5cdd9"],
      [112, "#e8ebef"],
      [180, "#f1f3f6"],
    ],
  },
  dark: {
    metal: "#d4d5d8",
    roughness: 0.3,
    stops: [
      [0, "#3a3b3e"],
      [30, "#1d1e20"],
      [47, "#0e0e10"],
      [58, "#9a9ea6"],
      [68, "#141517"],
      [88, "#3b3c40"],
      [112, "#65676c"],
      [180, "#717378"],
    ],
  },
}

function hexToRgb(hex: string) {
  const n = parseInt(hex.slice(1), 16)
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255]
}

// 각도 → 색 (구간 사이는 부드럽게 섞음)
function sampleStops(stops: Stop[], angle: number) {
  for (let i = 1; i < stops.length; i++) {
    const [a1, c1] = stops[i]
    if (angle > a1) continue
    const [a0, c0] = stops[i - 1]
    const t = (angle - a0) / (a1 - a0)
    const k = t * t * (3 - 2 * t)
    const p = hexToRgb(c0)
    const q = hexToRgb(c1)
    return [0, 1, 2].map((j) => p[j] + (q[j] - p[j]) * k)
  }
  return hexToRgb(stops[stops.length - 1][1])
}

// 등장방형(가로 = 경도, 세로 = 위도) 환경 지도를 직접 칠함
function makeEnvironment(theme: AuraTheme) {
  const W = 512
  const H = 256
  const data = new Uint8Array(W * H * 4)
  const { stops } = LOOK[theme]
  for (let y = 0; y < H; y++) {
    const lat = ((y + 0.5) / H - 0.5) * Math.PI
    for (let x = 0; x < W; x++) {
      const lon = ((x + 0.5) / W - 0.5) * Math.PI * 2
      const dz = Math.cos(lat) * Math.sin(lon)
      // 뒤쪽(-z)에서 잰 각도, 경도에 따라 살짝 흔들어 띠가 유기적으로 휘게
      const angle = (Math.acos(Math.max(-1, Math.min(1, -dz))) * 180) / Math.PI + Math.sin(lon * 2 + lat * 3) * 4
      const [r, g, b] = sampleStops(stops, angle)
      // 위쪽이 아주 조금 더 밝게
      const lift = 1 + Math.max(0, Math.sin(lat)) * 0.05
      const i = (y * W + x) * 4
      data[i] = Math.min(255, r * lift)
      data[i + 1] = Math.min(255, g * lift)
      data[i + 2] = Math.min(255, b * lift)
      data[i + 3] = 255
    }
  }
  const texture = new THREE.DataTexture(data, W, H, THREE.RGBAFormat)
  texture.mapping = THREE.EquirectangularReflectionMapping
  texture.colorSpace = THREE.SRGBColorSpace
  texture.magFilter = THREE.LinearFilter
  texture.minFilter = THREE.LinearFilter
  texture.needsUpdate = true
  return texture
}

function useFlowingMaterial() {
  const uniforms = useMemo(
    () => ({ uTime: { value: 0 }, uAmp: { value: 0.24 }, uFreq: { value: 0.45 } }),
    []
  )
  const onBeforeCompile = useMemo(
    () => (shader: THREE.WebGLProgramParametersWithUniforms) => {
      Object.assign(shader.uniforms, uniforms)
      shader.vertexShader = shader.vertexShader
        .replace("#include <common>", `#include <common>\n${SIMPLEX_NOISE}`)
        // 높이의 기울기로 법선을 구함 (분기 없이 어디서나 매끈 → 반사가 끊기지 않음)
        .replace(
          "#include <beginnormal_vertex>",
          /* glsl */ `
          vec3 auraN = normalize(position);
          float auraF = auraField(auraN);
          const float auraE = 0.01;
          vec3 auraG = vec3(
            auraField(auraN + vec3(auraE, 0.0, 0.0)) - auraField(auraN - vec3(auraE, 0.0, 0.0)),
            auraField(auraN + vec3(0.0, auraE, 0.0)) - auraField(auraN - vec3(0.0, auraE, 0.0)),
            auraField(auraN + vec3(0.0, 0.0, auraE)) - auraField(auraN - vec3(0.0, 0.0, auraE))
          ) / (2.0 * auraE);
          vec3 objectNormal = normalize(auraN * (1.0 + auraF) - (auraG - dot(auraG, auraN) * auraN));
          vec3 auraPos = auraN * (1.0 + auraF);
          `
        )
        .replace("#include <begin_vertex>", "vec3 transformed = auraPos;")
    },
    [uniforms]
  )
  return { uniforms, onBeforeCompile }
}

function Blob({ theme, reducedMotion }: Omit<Props, "onReady">) {
  const mesh = useRef<THREE.Mesh>(null)
  const { uniforms, onBeforeCompile } = useFlowingMaterial()
  const look = LOOK[theme]

  useFrame((state, delta) => {
    if (!reducedMotion) uniforms.uTime.value += Math.min(delta, 0.05) * 0.07 // 한 굽이 약 15초

    // 화면 비율에 맞춰 덩어리 위치·크기 조정
    const { width, height } = state.viewport
    const m = mesh.current
    if (!m) return
    if (width / height < 0.8) {
      // 세로 화면(모바일): 카드 뒤 위쪽
      const r = Math.max(width * 0.85, 1.6)
      m.scale.setScalar(r)
      m.rotation.z = 0
      m.position.set(0, height * 0.3, 0)
    } else {
      // 가로 화면: 세로로 긴 덩어리를 살짝 기울여 왼쪽~가운데를 채우고,
      // 오른쪽 가장자리가 위에서 아래로 갈수록 카드 뒤로 휘어 들어가게
      const r = height * 0.56
      m.scale.set(r * 0.95, r * 1.3, r)
      m.rotation.z = 0.42
      m.position.set(width * 0.1 - r * 0.97, 0, 0)
    }
  })

  return (
    <mesh ref={mesh}>
      <sphereGeometry args={[1, 224, 224]} />
      <meshPhysicalMaterial
        color={look.metal}
        metalness={1}
        roughness={look.roughness}
        clearcoat={1}
        clearcoatRoughness={0.25}
        onBeforeCompile={onBeforeCompile}
        customProgramCacheKey={() => "aura-blob"}
      />
    </mesh>
  )
}

// 마우스를 따라 시점이 살짝 움직임
function Parallax({ enabled }: { enabled: boolean }) {
  const target = useRef({ x: 0, y: 0 })
  useEffect(() => {
    if (!enabled) return
    const move = (e: PointerEvent) => {
      target.current.x = (e.clientX / window.innerWidth) * 2 - 1
      target.current.y = (e.clientY / window.innerHeight) * 2 - 1
    }
    window.addEventListener("pointermove", move, { passive: true })
    return () => window.removeEventListener("pointermove", move)
  }, [enabled])

  useFrame(({ camera }, delta) => {
    const k = 1 - Math.exp(-Math.min(delta, 0.05) * 2.5)
    camera.position.x += (target.current.x * 0.35 - camera.position.x) * k
    camera.position.y += (-target.current.y * 0.25 - camera.position.y) * k
    camera.lookAt(0, 0, 0)
  })
  return null
}

function Lights({ theme }: { theme: AuraTheme }) {
  const map = useMemo(() => makeEnvironment(theme), [theme])
  useEffect(() => () => map.dispose(), [map])
  return <Environment map={map} />
}

// 로그인 배경의 액체 크롬 덩어리 (지연 로딩되는 모듈)
export default function ChromeBlob({ theme, reducedMotion, onReady }: Props) {
  const [visible, setVisible] = useState(() => document.visibilityState === "visible")

  useEffect(() => {
    const sync = () => setVisible(document.visibilityState === "visible")
    document.addEventListener("visibilitychange", sync)
    return () => document.removeEventListener("visibilitychange", sync)
  }, [])

  // 탭이 숨겨지면 멈춤, 움직임 줄이기 설정이면 필요할 때만 그림
  const frameloop = !visible ? "never" : reducedMotion ? "demand" : "always"

  return (
    <Canvas
      frameloop={frameloop}
      dpr={[1, 2]}
      camera={{ position: [0, 0, 6], fov: 40, near: 0.1, far: 50 }}
      gl={{ antialias: true, alpha: true, powerPreference: "high-performance", toneMapping: THREE.NeutralToneMapping }}
      onCreated={() => onReady?.()}
      style={{ position: "absolute", inset: 0 }}
      aria-hidden
    >
      <Lights theme={theme} />
      <Blob theme={theme} reducedMotion={reducedMotion} />
      <Parallax enabled={!reducedMotion} />
    </Canvas>
  )
}
