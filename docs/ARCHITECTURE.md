# 아키텍처 — BucketLog

> KMP / CMP 모듈 구조와 설계 원칙
> 버전 0.1 · 2026-08-17

---

## 1. 설계 원칙

1. **로직은 공유하고, 감각기관만 분리한다.** 카메라·이미지·알림·파일·권한만 플랫폼 코드로 내립니다.
2. **MVP는 네트워크가 없다.** Ktor를 미리 넣지 마세요. Repository 인터페이스만 잘 두면 Phase 1에서 붙일 수 있습니다.
3. **레이어 단방향 의존.** `presentation → domain ← data`
4. **사진이 곧 제품이다.** 이미지 파이프라인 성능이 UX의 8할입니다.

---

## 2. 모듈 구조

> Android Studio의 Kotlin Multiplatform 마법사(Share UI)로 생성한 실제 구조입니다.
> 아래 세 모듈로 구성되며, **`shared`가 사실상 이 앱의 전부**입니다. `androidApp`/`iosApp`은
> 각 플랫폼 진입점만 갖는 얇은 껍데기입니다 — 화면(UI)까지 포함해 로직은 전부 `shared`에 있습니다.

```
bucketlog/
├── androidApp/                 얇은 Android 진입점 (Gradle 모듈)
│   └── src/main/kotlin/com/bucketlog/
│       └── MainActivity.kt     shared의 App()을 setContent로 띄우기만 함
│
├── iosApp/                     Xcode 프로젝트 (Gradle 모듈 아님)
│   └── iosApp/
│       ├── iOSApp.swift        앱 진입점
│       └── ContentView.swift   shared가 만든 Shared.framework의 MainViewController() 호출
│
├── shared/                     핵심 공유 모듈 (Gradle 모듈)
│   └── src/
│       ├── commonMain/kotlin/com/bucketlog/
│       │   ├── domain/
│       │   │   ├── model/          Goal, Entry, Photo, Category... ✅ 생성됨
│       │   │   ├── repository/     인터페이스만
│       │   │   └── usecase/        AddEntry, CompleteGoal, ArchiveGoal,
│       │   │                       PickNudgeTarget, GetOnThisDay...
│       │   ├── data/
│       │   │   ├── local/          Room DAO, 엔티티
│       │   │   ├── file/           사진 저장/삭제
│       │   │   ├── backup/         zip 내보내기/가져오기
│       │   │   ├── mapper/
│       │   │   └── repository/     구현체
│       │   ├── presentation/
│       │   │   ├── home/
│       │   │   ├── goaldetail/
│       │   │   ├── entryeditor/
│       │   │   ├── completion/
│       │   │   ├── archive/
│       │   │   ├── onboarding/
│       │   │   └── settings/
│       │   ├── notification/       스케줄러 추상화, NotificationBudget
│       │   ├── designsystem/       색상, 타이포, 공통 컴포넌트
│       │   ├── platform/           expect 선언
│       │   └── App.kt              최상위 Composable (마법사 기본 생성)
│       ├── androidMain/kotlin/com/bucketlog/     actual (CameraX, WorkManager...)
│       └── iosMain/kotlin/com/bucketlog/         actual (AVFoundation, UNUserNotificationCenter...)
│
└── docs/
```

**모듈이 물리적으로 나뉘지 않은 것은 문제가 아닙니다.** 핵심은 `commonMain` 안에서
`domain/data/presentation`이 패키지 단위로 명확히 분리되는 것이고, 위 구조가 이미 그렇게 되어 있습니다.

---

## 3. expect / actual 경계

**이 목록 외에는 추가하지 마세요.** 새로 필요하다고 판단되면 먼저 물어보세요.

| 인터페이스 | Android | iOS |
|---|---|---|
| `CameraController` | CameraX | AVFoundation |
| `PhotoPicker` | Photo Picker API | PHPickerViewController |
| `ImageProcessor` | Bitmap 리사이즈/압축 | UIImage / Core Graphics |
| `FileStorage` | `Context.filesDir` | `NSFileManager` documents |
| `NotificationScheduler` | WorkManager + NotificationManagerCompat | UNUserNotificationCenter |
| `PermissionManager` | ActivityResult API | 각 프레임워크 권한 API |
| `ShareHandler` | `Intent.ACTION_SEND` | UIActivityViewController |
| `AppSettings` | DataStore | NSUserDefaults |
| `ZipArchiver` | java.util.zip | libarchive / NSFileCoordinator |

```kotlin
// commonMain/platform/ImageProcessor.kt
expect class ImageProcessor {
    /** 원본 → 표시용(1080px) + 썸네일(320px) */
    suspend fun process(source: ByteArray): ProcessedImage
}

data class ProcessedImage(
    val display: ByteArray,
    val thumbnail: ByteArray,
    val width: Int,
    val height: Int,
)
```

---

## 4. 레이어 규칙

```
presentation ──► domain ◄── data
      │                      │
      └──── DI (Koin) ───────┘
```

- `domain`은 Kotlin 표준 라이브러리 + coroutines + kotlinx-datetime 외에 아무것도 의존하지 않습니다
- `presentation`은 `data`를 직접 참조하지 않습니다 (반드시 UseCase 경유)
- `data`의 Entity/DTO가 `presentation`까지 새어나오면 안 됩니다

### 화면 패턴

모든 화면에 동일하게 적용합니다.

```kotlin
class GoalDetailViewModel(
    private val observeGoal: ObserveGoalUseCase,
    private val addEntry: AddEntryUseCase,
    private val completeGoal: CompleteGoalUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    fun onIntent(intent: GoalDetailIntent) = when (intent) {
        is GoalDetailIntent.QuickCheckIn -> quickCheckIn(intent.memo)
        is GoalDetailIntent.Complete     -> complete(intent.retrospect)
        // ...
    }
}

data class GoalDetailUiState(
    val goal: Goal? = null,
    val entries: List<Entry> = emptyList(),
    val progress: ProgressInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

---

## 5. 이미지 파이프라인 ⚠️ 가장 먼저 만들 것

사진이 이 앱 콘텐츠의 전부입니다. **다른 코드를 쓰기 전에 이 파이프라인부터 PoC로 검증하세요.**

### 저장 정책

| 항목 | 정책 |
|---|---|
| 원본 | **저장하지 않음** |
| 표시용 | 최대 1080px (긴 변 기준), JPEG 품질 80 |
| 썸네일 | 320px, JPEG 품질 70 |
| 저장 위치 | 앱 전용 디렉토리 (갤러리에 노출 안 함) |
| 파일명 | `{photoId}.jpg` / `{photoId}_thumb.jpg` |
| EXIF | 방향만 적용 후 제거 (위치정보 제거 — 프라이버시) |

원본을 저장하지 않는 것이 핵심입니다. 목표 30개 × 기록 5개 × 사진 3장 = 450장. 원본 4MB면 1.8GB, 리사이즈본 300KB면 135MB입니다.

### 처리 흐름

```
촬영/선택
   │
   ▼
ImageProcessor.process()      ← actual. 백그라운드 디스패처
   │  ├─ EXIF 방향 보정
   │  ├─ 1080px 리사이즈 + 압축
   │  ├─ 320px 썸네일 생성
   │  └─ EXIF 스트립
   ▼
FileStorage.write()
   │
   ▼
photos 테이블에 경로 + width/height 저장
```

**`width`/`height`를 DB에 저장하는 이유** — 타임라인에서 이미지 로드 전에 자리를 잡아둘 수 있어 레이아웃 점프가 없어집니다. 사진이 많은 화면에서 체감 차이가 큽니다.

### 렌더링

- 목록/그리드: **썸네일만**
- 상세/뷰어: 표시용
- Coil 3의 메모리·디스크 캐시 사용
- 타임라인은 `LazyColumn` + `key` 지정 필수

### 삭제

Entry/Goal 삭제 시 파일도 함께 지웁니다. DB만 지우면 고아 파일이 쌓입니다.

```kotlin
// 앱 시작 시 주 1회 정도 고아 파일 청소
suspend fun cleanupOrphanPhotos() {
    val known = photoDao.allPaths().toSet()
    fileStorage.listPhotos().filterNot { it in known }.forEach { fileStorage.delete(it) }
}
```

---

## 6. 알림 구조

전체 규칙은 `docs/NOTIFICATIONS.md`. 아키텍처 관점의 핵심만 적습니다.

```
UseCase (ScheduleMonthlyRecap, ScheduleNudge, ...)
   │
   ▼
NotificationBudget         ← 모든 알림이 반드시 통과. 주 1회 상한
   │
   ▼
NotificationScheduler      ← expect/actual
   │
   ├─ Android : WorkManager
   └─ iOS     : UNUserNotificationCenter
```

**개별 기능이 `NotificationScheduler`를 직접 호출하는 코드를 만들지 마세요.** 반드시 `NotificationBudget`을 거칩니다. 이 규칙이 깨지면 알림 총량 제어가 불가능해집니다.

### 딥링크

```
bucketlog://goal/{goalId}?focus=checkin
bucketlog://archive?month=2026-09
bucketlog://retrospect/2026
```

`focus=checkin`은 목표 상세로 이동한 뒤 **퀵 체크인 필드에 포커스 + 키보드 표시**까지 수행합니다. 넛지 알림의 효과가 여기서 결정됩니다.

---

## 7. 백그라운드 작업

| 작업 | 주기 | 내용 |
|---|---|---|
| 넛지 평가 | 주 1회 | 정체 목표 선정 후 알림 예약 |
| 알림 재예약 | 앱 실행 시 | iOS 64개 제한 대응 — 가까운 미래분만 |
| 고아 파일 청소 | 주 1회 | 참조 없는 사진 파일 삭제 |

Android는 WorkManager, iOS는 `BGAppRefreshTask`를 쓰되, **iOS 백그라운드 실행은 보장되지 않으므로** 앱 실행 시점에도 같은 로직을 한 번 돌립니다.

---

## 8. 테스트 전략

| 대상 | 우선순위 | 비고 |
|---|---|---|
| `PickNudgeTarget` | **최상** | 로직이 미묘하고 잘못되면 유저가 앱을 지움 |
| `NotificationBudget` | **최상** | 주 1회 상한이 실제로 지켜지는지 |
| 상태 전이 (완료/접어두기/되돌리기) | 상 | 데이터 손실 위험 |
| 백업 / 복원 왕복 | 상 | export → import 후 동일성 검증 |
| 파생 규칙 (progressCount, onThisDay) | 중 | |
| Mapper | 중 | |
| UI | 낮음 | 핵심 플로우만 |

`commonTest`에서 대부분 커버 가능합니다. 플랫폼 코드는 수동 검증으로 충분합니다.

---

## 9. Phase 1 대비

지금 만들지는 않되, **막지는 않도록** 해둘 것들.

- Repository 인터페이스는 `suspend` / `Flow` 기반으로 — 나중에 원격 소스를 끼워도 시그니처가 안 바뀌게
- 모든 엔티티에 `updatedAt`을 두면 나중에 동기화 충돌 해결이 쉬워집니다 (지금은 안 써도 컬럼만 만들어두기)
- 사진 경로를 절대 경로로 저장하지 마세요. 상대 경로 + 루트 조합. 클라우드 이전 시 재작성 부담이 없어집니다
