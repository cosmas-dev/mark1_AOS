# mark1_native — MARK1 Android 앱

COSMAS 리더기(Wi-Fi/Socket)로 진단 스트립을 촬영하고 OpenCV(JNI)로 분석하는
Kotlin / Jetpack Compose 앱.

관련 프로젝트 (별도 저장소로 관리):
- 백엔드 서버: `~/Desktop/mark1-server` (FastAPI + PostgreSQL + MinIO)
- 관리자 대시보드: `~/Desktop/mark1-dashboard` (React + TypeScript)

## 데이터 흐름

```
[COSMAS 리더기] --Wi-Fi/Socket--> [이 앱]
    촬영 → OpenCV(JNI) 분석 → Room DB 저장 (네트워크와 무관하게 항상 먼저 저장)
                                   │ WorkManager (네트워크 연결 시, 실패하면 지수 백오프 재시도)
                                   ▼
                          [mark1-server] → PostgreSQL / MinIO → [mark1-dashboard]
```

## 저장/동기화 계층 (기존 촬영·분석 흐름은 무변경)

- **파일명 개인정보 제거**: 이미지 파일명은 UUID (`550e8400-….jpg`). 성명/검사정보는 파일명에 절대 포함하지 않음
- **Room DB**: `persons` / `captures` / `analysis_results` 3테이블. 촬영+분석 완료 즉시 트랜잭션 저장 (`ExamRepository.saveExam`)
- **syncStatus**: `LOCAL → UPLOAD_PENDING → UPLOADING → SYNCED / FAILED`
- **WorkManager 동기화** (`SyncWorker`): 네트워크 연결 제약 + 지수 백오프. 앱 종료·네트워크 장애 후에도 재전송. COSMAS Wi-Fi(인터넷 없음) 연결 중에는 자동 대기
- **PersonInfo**: `AddDiagnosisDetailsScreen` 의 5개 입력(name/dateOfBirth/email/phoneNumber/company)이 전부 저장됨
- **SHA-256**: 촬영 직후 원본 이미지 해시를 계산해 서버 무결성 검증에 사용

## 서버 설정

`SyncConfig` (SharedPreferences) — 기본값 `http://10.0.2.2:8000` (에뮬레이터→호스트),
계정 `device-sync`. 운영 배포 시 HTTPS 주소로 변경하고
`AndroidManifest.xml` 의 `usesCleartextTraffic` 를 제거할 것.

## 빌드

`local.properties` 에 `OpenCV_DIR` 필요 (OpenCV Android SDK 경로).

```bash
./gradlew :app:assembleDebug
```
