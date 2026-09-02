# COSMAS 검사 기록 조회 API

대시보드에서 검사 기록과 키트 이미지를 불러오기 위한 API 명세입니다.

```
BASE = https://43-201-180-210.sslip.io
```

- **인증 없음** — 토큰이나 헤더 없이 바로 호출할 수 있습니다.
- **CORS 전체 허용** (`Access-Control-Allow-Origin: *`) — 브라우저에서 `fetch()`로 직접 호출 가능합니다.
- 응답은 모두 `application/json`, 인코딩은 UTF-8입니다.

> 시연용 임시 서버입니다. 정식 배포 시에는 주소가 바뀌고 인증이 추가됩니다. 자세한 내용은 맨 아래 [주의사항](#주의사항) 참고.

---

## 1. 검사 목록

```
GET /api/v1/captures
```

### 쿼리 파라미터

| 이름 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| `limit` | int | `50` | 한 번에 가져올 개수. 1~200 범위로 제한됩니다. |
| `offset` | int | `0` | 건너뛸 개수. 페이지네이션에 사용합니다. |
| `positive_only` | bool | `false` | `true` 면 양성(POSITIVE) 건만 반환합니다. |

정렬은 최근 촬영순(내림차순) 고정입니다.

### 요청 예시

```bash
curl "https://43-201-180-210.sslip.io/api/v1/captures?limit=50&offset=0&positive_only=false"
```

### 응답 예시

```json
{
  "total": 1,
  "limit": 50,
  "offset": 0,
  "items": [
    {
      "capture_id": "470cd121-3ca5-42d5-a4b0-3ad05001f4c5",
      "captured_at": "2026-09-02T02:08:52.538000+00:00",
      "device_id": "COSMAS-1000",
      "test_type": "Multi-Panel Drug Test",
      "sync_status": "COMPLETE",
      "person": {
        "person_id": "e5b0b9d9-c676-4e54-97eb-80eadd33f59d",
        "name": "박경찰",
        "date_of_birth": "1988-03-14",
        "email": "park@police.go.kr",
        "phone_number": "010-2841-7702",
        "organization": "경찰청 마약수사대"
      },
      "result": "POSITIVE",
      "thumbnail_url": "https://mark1-captures-593484868437.s3.ap-northeast-2.amazonaws.com/captures/470cd121-....png?X-Amz-Algorithm=...&X-Amz-Signature=..."
    }
  ]
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|---|---|---|
| `total` | int | 필터를 적용한 전체 건수 (페이지네이션용) |
| `items[].capture_id` | string | 검사 고유 ID. 상세 조회에 사용합니다. |
| `items[].captured_at` | string \| null | 촬영 시각 (ISO 8601, UTC) |
| `items[].device_id` | string | 촬영에 사용한 리더기 모델 |
| `items[].test_type` | string | 검사 종류 |
| `items[].sync_status` | string | 서버 처리 상태. 정상 완료 시 `COMPLETE` |
| `items[].person` | object \| null | 대상자 정보. 등록 정보가 없으면 `null` |
| `items[].result` | string \| null | `POSITIVE` / `NEGATIVE`. 분석 결과가 없으면 `null` |
| `items[].thumbnail_url` | string \| null | 키트 사진 링크 (**15분 만료**, 아래 참고) |

---

## 2. 검사 상세

```
GET /api/v1/captures/{capture_id}
```

목록의 필드에 더해 원본 이미지 링크와 분석 수치가 포함됩니다.

### 요청 예시

```bash
curl "https://43-201-180-210.sslip.io/api/v1/captures/470cd121-3ca5-42d5-a4b0-3ad05001f4c5"
```

### 응답 예시

```json
{
  "capture_id": "470cd121-3ca5-42d5-a4b0-3ad05001f4c5",
  "captured_at": "2026-09-02T02:08:52.538000+00:00",
  "device_id": "COSMAS-1000",
  "test_type": "Multi-Panel Drug Test",
  "sync_status": "COMPLETE",
  "image_sha256": "288411968dd4f700ff9950086649467a5dfa975a9f577616ea40e6e13c9aaa7a",
  "image_url": "https://mark1-captures-593484868437.s3.ap-northeast-2.amazonaws.com/captures/470cd121-....png?X-Amz-Algorithm=...&X-Amz-Signature=...",
  "person": {
    "person_id": "e5b0b9d9-c676-4e54-97eb-80eadd33f59d",
    "name": "박경찰",
    "date_of_birth": "1988-03-14",
    "email": "park@police.go.kr",
    "phone_number": "010-2841-7702",
    "organization": "경찰청 마약수사대"
  },
  "analysis": {
    "result": "POSITIVE",
    "t_detected": true,
    "t_snr": 3.0454,
    "c_snr": 2.1081,
    "num_peaks": 11,
    "analyzed_at": "2026-09-02T02:35:06.002517+00:00"
  }
}
```

### 추가 필드 설명

| 필드 | 타입 | 설명 |
|---|---|---|
| `image_sha256` | string \| null | 촬영 직후 앱이 계산한 원본 해시. 무결성 검증용 |
| `image_url` | string \| null | 키트 원본 사진 링크 (**15분 만료**) |
| `analysis` | object \| null | 분석 결과. 분석 전이면 `null` |

#### `analysis` 내부

| 필드 | 타입 | 설명 |
|---|---|---|
| `result` | string | `POSITIVE` / `NEGATIVE` |
| `t_detected` | bool | T라인 검출 여부. 이 값이 `result` 를 결정합니다. |
| `t_snr` | float \| null | T라인 신호대잡음비 |
| `c_snr` | float \| null | C라인(대조선) 신호대잡음비 |
| `num_peaks` | int | 검출된 피크 개수 |
| `analyzed_at` | string \| null | 분석 시각 (ISO 8601, UTC) |

> 이 외의 분석 수치(`t_weak`, 라인 위치, `noise_sigma`, ROI, 채널명, 원본 해상도 등)는 서버 DB에 그대로 보관돼 있습니다. 대시보드에서 필요해지면 응답에 추가할 수 있으니 말씀해 주세요.

---

## 이미지 사용 시 주의

`thumbnail_url` 과 `image_url` 은 **15분 후 만료되는 S3 서명 링크**입니다.

```html
<!-- OK: 응답에서 받은 URL을 그대로 사용 -->
<img src="{{ item.thumbnail_url }}" alt="키트 사진">
```

- ✅ API 응답에서 받은 URL을 **그때그때** `<img src>` 에 사용
- ❌ 이 URL을 DB에 저장하거나 프론트에서 캐싱 — 15분 뒤 깨집니다
- ❌ URL에서 파일 경로만 떼어내 직접 조합 — S3 버킷이 비공개라 접근 불가

화면을 다시 열거나 새로고침할 때마다 API를 호출해 새 URL을 받아 쓰면 됩니다.

---

## 에러 응답

| 상태 | 상황 | 본문 |
|---|---|---|
| `404` | 존재하지 않는 `capture_id` | `{"detail": "unknown capture"}` |
| `405` | 잘못된 메서드 (예: 목록 경로에 POST) | `{"detail": "Method Not Allowed"}` |

---

## 연동 예시

```js
const BASE = "https://43-201-180-210.sslip.io";

// 목록
async function fetchCaptures({ limit = 50, offset = 0, positiveOnly = false } = {}) {
  const params = new URLSearchParams({
    limit: String(limit),
    offset: String(offset),
    positive_only: String(positiveOnly),
  });
  const res = await fetch(`${BASE}/api/v1/captures?${params}`);
  if (!res.ok) throw new Error(`목록 조회 실패: ${res.status}`);
  return res.json();
}

// 상세
async function fetchCapture(captureId) {
  const res = await fetch(`${BASE}/api/v1/captures/${captureId}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`상세 조회 실패: ${res.status}`);
  return res.json();
}
```

---

## 주의사항

**주소가 바뀔 수 있습니다.** `43-201-180-210.sslip.io` 는 도메인 없이 HTTPS를 쓰기 위해 서버 IP를 그대로 호스트명에 넣은 형태입니다. 서버가 재시작되어 IP가 바뀌면 주소도 함께 바뀝니다. 코드에 하드코딩하지 말고 환경변수로 빼두시길 권합니다.

**나중에 인증이 추가됩니다.** 지금은 시연 편의를 위해 인증 없이 열어두었지만, 실제 대상자 정보를 다루는 단계에서는 로그인 토큰이 필요해집니다. 요청 헤더를 끼워 넣을 수 있는 구조로 잡아두시면 그때 수정 범위가 작아집니다.

**쓰기(POST)는 인증이 걸려 있습니다.** 검사 기록 업로드는 앱 전용이며, 대시보드에서는 조회만 가능합니다.
