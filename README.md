# BucketLog (버킷로그)

> 올해 하고 싶은 작은 것들을 적고, 해나가는 과정을 사진과 메모로 기록하고, 나중에 돌아보는 앱.
> Kotlin Multiplatform + Compose Multiplatform · Android / iOS

---

## 이게 뭔가요

할 일 목록이 아니라 **추억 아카이브**입니다.

```
🎯 제주도 한 달 살기
   ├─ 03.12  "숙소 알아보는 중"
   ├─ 04.02  "항공권 예약 완료!"          📷
   ├─ 05.20  "짐 싸는 중, 설렌다"         📷📷
   └─ 06.01  ✅ 완료 — "드디어 왔다"      📷📷📷
              가장 기억에 남는 순간은?
              → 생각보다 조용했고, 그래서 좋았다
```

기존 버킷리스트 앱에는 목록만 있습니다. 여기엔 **과정**이 있습니다.

---

## 특징

**기록의 마찰이 3단계** — 사진 꺼내기 귀찮은 날에도 한 줄은 남길 수 있어야 합니다.

```
① 퀵 체크인   한 줄 (5초)
② 진행 기록   사진 + 메모
③ 완료 인증   사진 + 회고
```

**접어두기** — 못 지킨 목표를 "실패"가 아니라 "정리"로 다룹니다. 목록에 쌓이지 않고, 언제든 다시 꺼낼 수 있습니다.

**스마트 넛지** — "앱을 확인하세요"가 아니라 목표 이름을 부릅니다. *"제주도 한 달 살기 — 마지막 기록 후 32일이 지났어요. 요즘 어때요?"* 탭하면 한 줄 입력창이 바로 열립니다.

**알림 예산** — 전체 알림 합계가 주 1회를 넘지 않습니다. 목표가 30개여도 마찬가지입니다.

---

## 문서

| 문서 | 내용 |
|---|---|
| [CLAUDE.md](./CLAUDE.md) | 프로젝트 컨텍스트 (Claude Code 진입점) |
| [docs/MVP-SCOPE.md](./docs/MVP-SCOPE.md) | **MVP 범위** — 무엇을 만들고 무엇을 안 만드는가 |
| [docs/PRD.md](./docs/PRD.md) | 문제 정의, 타겟, 경쟁 환경, 지표 |
| [docs/DATA-MODEL.md](./docs/DATA-MODEL.md) | 스키마, 상태 전이, 백업 포맷 |
| [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) | KMP 구조, expect/actual, 이미지 파이프라인 |
| [docs/NOTIFICATIONS.md](./docs/NOTIFICATIONS.md) | 알림 예산 규칙 — 알림 작업 전 필독 |
| [docs/DESIGN.md](./docs/DESIGN.md) | 톤 가이드, 금지어, 컬러, 타임라인 UI |
| [docs/ROADMAP.md](./docs/ROADMAP.md) | Phase 0~3 |
| [docs/MONETIZATION.md](./docs/MONETIZATION.md) | 구독 티어, 가격, 전환 설계 |

---

## 기술 스택

```
Kotlin Multiplatform
├── Compose Multiplatform    UI
├── Koin                     DI
├── Room (KMP)               로컬 DB
├── Coil 3                   이미지
├── kotlinx-datetime         날짜
└── expect/actual            카메라 · 이미지 · 알림 · 파일 · 권한
    ├── Android : CameraX, WorkManager, NotificationManagerCompat
    └── iOS     : AVFoundation, UNUserNotificationCenter
```

**MVP는 네트워크 의존성이 없습니다.** 완전한 로컬 앱으로 출시합니다.

---

## 기술적으로 흥미로운 지점

**이미지 파이프라인** — 촬영 → EXIF 보정 → 1080px 리사이즈 → 썸네일 생성 → EXIF 스트립 → 로컬 저장을 양 플랫폼에서 공통 인터페이스로. 원본을 저장하지 않아 450장 기준 1.8GB → 135MB.

**알림 예산 아키텍처** — 모든 알림이 단일 `NotificationBudget`을 통과합니다. 개별 기능이 스케줄러를 직접 호출할 수 없는 구조로 총량을 강제합니다.

**상태 전이의 무손실 설계** — 완료를 되돌려도 완료 인증 사진과 회고가 사라지지 않습니다. 유저의 기록은 어떤 경로로도 소실되지 않아야 합니다.

---

## 상태

**Phase 0 (MVP) 착수 전 — 기획 완료**

진행 상황은 [docs/ROADMAP.md](./docs/ROADMAP.md) 참조.
