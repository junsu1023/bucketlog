# BucketLog (버킷로그) — 프로젝트 컨텍스트

> Claude Code가 세션 시작 시 자동으로 읽는 파일입니다.
> 작업 전 이 문서를 기준으로 판단하고, 상세가 필요하면 아래 인덱스의 문서만 선택적으로 읽으세요.

---

## 1. 한 줄 요약

**"올해 안에 하고 싶은 작은 것들"을 적고, 해나가는 과정을 사진과 메모로 기록해, 나중에 돌아보는 앱.**

할 일 목록이 아니라 **추억 아카이브**입니다. Android / iOS 동시 출시 (KMP + Compose Multiplatform).

---

## 2. 이 앱의 유일한 승부처

버킷리스트 앱이 죽는 이유는 기능 부족이 아닙니다.

> **목표를 적고 나면 앱을 열 이유가 없다.**

1월에 20개 적고 3월에 삭제합니다. 그래서 이 제품의 설계 목표는 하나입니다.

> ### "적기"와 "완료하기" 사이의 공백을 채운다.

**기능을 추가·삭제·변경할 때 항상 이 기준으로 판단하세요.**
이 공백을 메우지 않는 기능은 넣지 않습니다.

공백을 메우는 장치 6개 (전부 MVP):

| 장치 | 역할 |
|---|---|
| **진행 기록** | 완료 전에도 남길 게 있다 (사진 + 메모) |
| **퀵 체크인** | 한 줄만 남기는 최소 마찰 기록 |
| **접어두기** | 죄책감을 구조적으로 제거 |
| **스마트 넛지** | 정체된 목표 하나를 콕 집어 물어본다 |
| **월간 회고** | 앱이 먼저 말을 건다 |
| **작년 오늘** | 과거 기록이 스스로 돌아온다 |

---

## 3. 문서 인덱스

**전부 읽지 마세요.** 작업에 필요한 것만 읽습니다.

| 문서 | 언제 읽나 |
|---|---|
| `docs/MVP-SCOPE.md` | **지금 뭘 만들어야 하는지** — 가장 자주 참조 |
| `docs/PRD.md` | 제품 배경, 타겟, 전체 기능 명세, 지표 |
| `docs/DATA-MODEL.md` | 스키마, 엔티티, 파생 규칙 |
| `docs/ARCHITECTURE.md` | 모듈 구조, expect/actual 경계, 이미지 파이프라인 |
| `docs/NOTIFICATIONS.md` | 알림 관련 작업 시 **필수** — 알림 예산 규칙 있음 |
| `docs/DESIGN.md` | UI 톤, 문구, 색상, 컴포넌트 |
| `docs/ROADMAP.md` | Phase 정의 및 종료 조건 |
| `docs/MONETIZATION.md` | 결제, 구독 티어 |

---

## 4. 현재 상태

| 항목 | 값 |
|---|---|
| 단계 | **Phase 0 (MVP) 진행 중** — 4주차까지 완료(1~4주차 상세는 이전 기록 참고) + 6주차 일부: 다크모드
  수동 전환(M-01) + 완료 카드 공유(S-01/S-02, 9:16 규격 겸용, Android Canvas/iOS Core Graphics
  네이티브 렌더링) + 전체 데이터 초기화(M-03). 이번 세션엔 버그 찾기 겸 실기기 회귀 테스트를
  진행하며 미구현 P0 스펙 3개(G-06 반복형 완료 제안, E-04 기록 날짜 수정, E-05 기록 수정/삭제)를
  발견해 함께 구현. 반복형 목표 진행 기록 시 메모/사진 없이 카운트만으로 저장이 막히던 버그도 수정.
  알림(5주차)도 스마트 넛지·월간 회고·목표별 리마인더·알림 설정·백업복원까지 먼저 구현됨. 전부
  Android 실기기/에뮬레이터 검증, iOS는 컴파일만 확인(`test.md` 참고) |
| 앱 이름 | **가칭** BucketLog / 버킷로그 |
| 백엔드 | **없음** (MVP는 로컬 전용. Phase 1에 도입) |
| 로그인 | **없음** (Phase 1) |
| 다음 작업 | `docs/MVP-SCOPE.md` §4의 6주차 잔여분 — 남은 버그 있는지 계속 확인, 스토어 제출 준비(개발자
  계정·서명키·스토어 리스팅 등 사용자가 직접 해야 하는 외부 작업 위주). 5주차 알림 중 남은
  N-04(마감임박, G-09 마감일 UI 선행)·N-05(연말회고, G-12 연말이월 선행)는 각 선행 기능과 묶어
  이후 진행 |

> 미확정 항목에 대해 코드를 생성해야 하면 **임의로 결정하지 말고 먼저 물어보세요.**

---

## 5. 기술 스택

```
Kotlin Multiplatform
├── commonMain    도메인 · 데이터 · ViewModel · UI(Compose Multiplatform)
├── androidMain   카메라, 이미지 처리, 알림, 파일 IO, 권한
└── iosMain       동일 (actual 구현)
```

| 영역 | 선택 |
|---|---|
| UI | Compose Multiplatform |
| DI | Koin |
| 비동기 | Coroutines + Flow |
| 로컬 DB | Room 3.0 (`androidx.room3` — 2026년 신규 groupId, 기존 `androidx.room`과 다름) |
| 이미지 로딩 | Coil 3 |
| 날짜 | kotlinx-datetime |
| 직렬화 | kotlinx-serialization (백업/복원용) |
| 아키텍처 | Clean Architecture + MVVM |

**MVP에는 네트워크 라이브러리가 필요 없습니다.** Ktor를 미리 넣지 마세요.

---

## 6. 절대 규칙

1. **스트릭(연속일) 개념을 절대 넣지 마세요.** 넣는 순간 습관 트래커가 되고, 그 시장은 완전히 포화입니다. 이건 추억 아카이브입니다.
2. **"포기"라는 단어를 UI에 쓰지 마세요.** `접어두기` / `이건 안 하기로 하기`를 씁니다. 자세한 문구 규칙은 `docs/DESIGN.md`.
3. **기록에 필수 입력을 만들지 마세요.** 사진만, 메모만, 둘 다도 허용. 필수가 생기는 순간 기록이 끊깁니다.
4. **알림은 주 1회를 넘기지 않습니다.** 예외 없음. `docs/NOTIFICATIONS.md`의 알림 예산 규칙을 반드시 따르세요.
5. **비즈니스 로직은 `commonMain`에.** `expect/actual`은 카메라·이미지·알림·파일·권한만.
6. **기록은 유저의 기억입니다.** destructive migration 금지, 삭제는 항상 확인 후, 백업 기능은 MVP 필수.
7. **매일 반복 리마인더를 허용하지 마세요.** 최소 단위는 주 1회입니다 (규칙 1과 같은 이유).

---

## 7. 코딩 컨벤션

- 패키지: `com.bucketlog.<layer>.<feature>` (예: `com.bucketlog.domain.goal`)
- 화면 단위로 `UiState` 단일 data class + `Intent` sealed interface
- ViewModel은 `StateFlow<UiState>`만 노출
- Composable은 상태 없는 순수 함수를 기본으로, 상태는 호이스팅
- 문자열 하드코딩 금지 (문구는 제품의 차별점 — `docs/DESIGN.md` 참조)
- 주석은 "왜"를 설명

---

## 8. 작업 요청 시 기대 동작

- 기능 구현 → `docs/MVP-SCOPE.md`에서 해당 ID의 명세를 먼저 확인
- 스키마 변경 → `docs/DATA-MODEL.md`를 함께 수정 (문서-코드 동기화)
- 알림 관련 → `docs/NOTIFICATIONS.md`의 예산 규칙 위반 여부 확인
- UI 문구 작성 → `docs/DESIGN.md`의 톤 가이드 준수
- 라이브러리 추가 → KMP 지원 여부 먼저 확인
- 큰 작업은 착수 전 계획 제시

---

## 9. 자주 쓰는 명령어

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS 프레임워크 (시뮬레이터)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# 테스트
./gradlew :shared:allTests

# 린트
./gradlew ktlintCheck
```

> 실제 모듈 구성: `androidApp`(얇은 진입점) + `shared`(로직·UI 전부) + `iosApp`(Xcode 프로젝트, Gradle 모듈 아님).
> 자세한 내용은 `docs/ARCHITECTURE.md` §2.
