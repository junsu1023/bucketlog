# 데이터 모델 — BucketLog

> 스키마를 변경하면 이 문서도 함께 수정하세요. 문서와 코드가 어긋나면 안 됩니다.
> 버전 0.1 · 2026-08-17

---

## 1. 개념 모델

```
   Goal (목표)
     │ 1
     │
     │ N
   Entry (기록)      ← 퀵 체크인 / 진행 기록 / 완료 인증이 모두 여기
     │
     │ N
   Photo (사진)
```

**핵심 설계: 기록 3종을 하나의 테이블로 다룹니다.** 퀵 체크인과 진행 기록은 저장 구조가 같고 **입력 UI의 마찰만 다릅니다.** 별도 테이블로 나누면 타임라인 조회가 복잡해지고 얻는 게 없습니다.

---

## 2. 도메인 모델 (commonMain)

```kotlin
data class Goal(
    val id: String,                  // UUID
    val title: String,
    val note: String?,               // "왜 하고 싶은지"
    val category: Category,
    val type: GoalType,
    val targetCount: Int?,           // REPEATABLE만. ONE_TIME이면 null
    val status: GoalStatus,
    val bucketYear: Int?,            // 2026 / null = "언젠가"
    val dueDate: LocalDate?,
    val coverEntryId: String?,       // 대표 사진이 담긴 Entry
    val reminderRule: ReminderRule?, // 목표별 리마인더 (기본 null = 꺼짐)
    val createdAt: Instant,
    val completedAt: Instant?,
    val retrospect: String?,         // 완료 회고
    val archivedAt: Instant?,
    val archiveReason: String?,      // 접어둔 이유
    val nudgeSnoozedUntil: Instant?, // 넛지 무응답 시 제외 기간
)

enum class GoalType { ONE_TIME, REPEATABLE }

enum class GoalStatus { IN_PROGRESS, COMPLETED, ARCHIVED }

enum class Category {
    TRAVEL, HOBBY, RELATIONSHIP, CHALLENGE, LEARNING, HEALTH, OTHER
}

/** 목표별 리마인더. 최소 주기는 주 1회 — 매일 옵션은 존재하지 않는다 */
data class ReminderRule(
    val interval: ReminderInterval,
    val enabled: Boolean,
)

enum class ReminderInterval { WEEKLY, BIWEEKLY, MONTHLY }
```

```kotlin
data class Entry(
    val id: String,
    val goalId: String,
    val kind: EntryKind,
    val memo: String?,
    val photos: List<Photo>,         // 0~5
    val countDelta: Int,             // REPEATABLE 진행량
    val recordedAt: Instant,         // 유저가 수정 가능 (소급 기록)
    val createdAt: Instant,          // 실제 생성 시각 (수정 불가)
)

enum class EntryKind {
    CHECK_IN,     // 퀵 체크인 — 한 줄. countDelta = 0
    PROGRESS,     // 진행 기록 — 사진 + 메모. countDelta = 1 (조정 가능)
    COMPLETION,   // 완료 인증 — 목표당 1개
}

data class Photo(
    val id: String,
    val entryId: String,
    val path: String,                // 표시용 (최대 1080px)
    val thumbnailPath: String,       // 320px
    val order: Int,
    val width: Int,
    val height: Int,
)
```

### 설계 노트

**`recordedAt` vs `createdAt`을 분리한 이유** — 사람들은 일을 하고 며칠 뒤에 앱을 엽니다. 타임라인 정렬은 `recordedAt`을, 넛지의 "마지막 활동" 판정은 `createdAt`을 씁니다. (3일 전 일을 오늘 기록해도 유저는 오늘 활동한 것이므로 넛지 대상에서 빠져야 합니다.)

**`countDelta`가 `CHECK_IN`에서 0인 이유** — 반복형 목표에 "아직 못 갔다"를 체크인할 수 있어야 하기 때문입니다. 카운트를 올리려면 유저가 명시적으로 토글합니다.

**`nudgeSnoozedUntil`** — 같은 목표를 반복해서 넛지하면 앱을 지웁니다. 2회 연속 무응답 시 4주간 제외 (`docs/NOTIFICATIONS.md` §2 N-02).

---

## 3. 로컬 스키마 (Room)

### `goals`

| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | TEXT PK | |
| `title` | TEXT NOT NULL | |
| `note` | TEXT | |
| `category` | TEXT NOT NULL | |
| `type` | TEXT NOT NULL | |
| `target_count` | INTEGER | |
| `status` | TEXT NOT NULL | |
| `bucket_year` | INTEGER | null = "언젠가" |
| `due_date` | INTEGER | epoch day |
| `cover_entry_id` | TEXT | |
| `reminder_interval` | TEXT | null = 꺼짐 |
| `reminder_enabled` | INTEGER | |
| `created_at` | INTEGER NOT NULL | |
| `completed_at` | INTEGER | |
| `retrospect` | TEXT | |
| `archived_at` | INTEGER | |
| `archive_reason` | TEXT | |
| `nudge_snoozed_until` | INTEGER | |

### `entries`

| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | TEXT PK | |
| `goal_id` | TEXT NOT NULL | FK → goals.id, ON DELETE CASCADE |
| `kind` | TEXT NOT NULL | |
| `memo` | TEXT | |
| `count_delta` | INTEGER NOT NULL DEFAULT 0 | |
| `recorded_at` | INTEGER NOT NULL | |
| `created_at` | INTEGER NOT NULL | |

### `photos`

| 컬럼 | 타입 | 비고 |
|---|---|---|
| `id` | TEXT PK | |
| `entry_id` | TEXT NOT NULL | FK → entries.id, ON DELETE CASCADE |
| `path` | TEXT NOT NULL | |
| `thumbnail_path` | TEXT NOT NULL | |
| `order_index` | INTEGER NOT NULL | |
| `width` / `height` | INTEGER NOT NULL | 레이아웃 점프 방지 |

### 인덱스

```sql
-- 홈: 진행 중 목표를 연도별로
CREATE INDEX idx_goals_status_year   ON goals(status, bucket_year);

-- 보관함
CREATE INDEX idx_goals_completed_at  ON goals(completed_at);

-- 목표 상세 타임라인
CREATE INDEX idx_entries_goal_recorded ON entries(goal_id, recorded_at DESC);

-- 전체 타임라인 · "작년 오늘"
CREATE INDEX idx_entries_recorded_at ON entries(recorded_at);

-- 넛지 대상 선정
CREATE INDEX idx_entries_goal_created ON entries(goal_id, created_at DESC);

CREATE INDEX idx_photos_entry_id     ON photos(entry_id, order_index);
```

**사진 파일은 DB에 넣지 않습니다.** 경로만 저장하고 실제 파일은 앱 전용 디렉토리에 둡니다. 자세한 내용은 `docs/ARCHITECTURE.md` §5.

---

## 4. 파생 규칙

DB에 저장하지 않고 계산하는 값들입니다. **집계 컬럼을 만들지 마세요** — 동기화 버그의 원천입니다. 데이터 규모가 작아(목표 수십 개, 기록 수백 개) 실시간 계산으로 충분합니다.

```kotlin
/** 반복형 진행 카운트 */
fun progressCount(goalId: String) =
    entries.filter { it.goalId == goalId }.sumOf { it.countDelta }

/** 완료 제안 조건 */
fun canSuggestCompletion(goal: Goal) =
    goal.type == REPEATABLE &&
    goal.targetCount != null &&
    progressCount(goal.id) >= goal.targetCount

/** 대표 사진 */
fun coverPhoto(goal: Goal) =
    goal.coverEntryId?.let { photoOf(it) }
        ?: entriesOf(goal.id).sortedByDescending { it.recordedAt }
            .firstNotNullOfOrNull { it.photos.firstOrNull() }

/** 넛지 판정용 마지막 활동 (recordedAt이 아니라 createdAt) */
fun lastActivityAt(goalId: String) =
    entries.filter { it.goalId == goalId }.maxOfOrNull { it.createdAt }

/** "작년 오늘" — 같은 월/일의 과거 기록 */
fun onThisDay(today: LocalDate) =
    entries.filter {
        val d = it.recordedAt.toLocalDate()
        d.month == today.month && d.dayOfMonth == today.dayOfMonth && d.year < today.year
    }

/** 연도별 요약 */
fun yearSummary(year: Int) = YearSummary(
    total     = goals.count { it.bucketYear == year },
    completed = goals.count { it.bucketYear == year && it.status == COMPLETED },
    archived  = goals.count { it.bucketYear == year && it.status == ARCHIVED },
    entryCount = entries.count { it.recordedAt.year == year },
)
```

---

## 5. 상태 전이

```
                  ┌─────────────────┐
                  │  IN_PROGRESS    │ ← 생성 시 기본
                  └────┬───────┬────┘
              완료하기 │       │ 접어두기
                       ▼       ▼
              ┌────────────┐ ┌────────────┐
              │ COMPLETED  │ │  ARCHIVED  │
              └─────┬──────┘ └─────┬──────┘
                    │  되돌리기      │  다시 꺼내기
                    └───────┬───────┘
                            ▼
                      IN_PROGRESS
```

### 전이 규칙

| 전이 | 부수 효과 |
|---|---|
| → `COMPLETED` | `COMPLETION` Entry 생성, `completedAt` 기록, `retrospect` 저장, 예약된 리마인더 취소 |
| → `ARCHIVED` | `archivedAt`·`archiveReason` 기록, 홈에서 제거, 예약된 리마인더 취소 |
| `COMPLETED` → `IN_PROGRESS` | `COMPLETION` Entry **삭제하지 않음** — `kind`를 `PROGRESS`로 강등. 사진과 메모는 유저의 기억이므로 보존 |
| `ARCHIVED` → `IN_PROGRESS` | `archivedAt`·`archiveReason`을 null로. **이유 텍스트는 보존하지 않음** (다시 하기로 했으므로) |

> 되돌리기에서 데이터를 지우지 마세요. 유저가 실수로 완료를 눌렀다가 되돌렸을 때 사진이 사라지면 복구가 불가능합니다.

---

## 6. 연말 이월 (G-12)

12월에 실행되는 배치성 플로우입니다. **자동으로 옮기지 않습니다** — 유저가 목표별로 고릅니다.

```
대상: bucketYear == 올해 && status == IN_PROGRESS

각 목표에 대해 유저가 선택:
  ├─ 내년으로     → bucketYear = 올해 + 1   (기록 전부 유지)
  ├─ 언젠가로     → bucketYear = null
  ├─ 접어두기     → status = ARCHIVED
  └─ 그대로 두기  → 변경 없음 (지난 해에 남음)
```

**기록은 절대 삭제하거나 초기화하지 않습니다.** 2026년에 3번 기록한 목표를 2027년으로 옮겨도 그 3개의 기록은 그대로 따라갑니다. 이월은 "새로 시작"이 아니라 "계속하기"입니다.

---

## 7. 백업 / 복원 (M-02)

MVP에 클라우드 동기화가 없으므로 이것이 유일한 데이터 보호 수단입니다. **반드시 사진을 포함해야 합니다.**

### 포맷

```
bucketlog-backup-20261231.zip
├── data.json          # goals + entries + photos 메타
├── version.txt        # 스키마 버전
└── photos/
    ├── {photoId}.jpg
    └── {photoId}_thumb.jpg
```

```json
{
  "schemaVersion": 1,
  "exportedAt": "2026-12-31T10:00:00Z",
  "goals": [ ... ],
  "entries": [ ... ],
  "photos": [ { "id": "...", "file": "photos/abc.jpg", ... } ]
}
```

### 복원 규칙

- 기존 데이터와 **병합**이 기본. `id` 충돌 시 백업본 우선
- 사진 파일이 없는 메타는 건너뛰되 Entry 자체는 살림 (메모라도 보존)
- `schemaVersion`이 현재보다 높으면 거부하고 앱 업데이트 안내
- 복원 전 현재 데이터를 임시 백업 (복원 실패 시 롤백)

---

## 8. 마이그레이션 원칙

- **destructive migration 절대 금지.** 유저의 기록은 사진과 기억입니다
- 컬럼 추가는 nullable 또는 DEFAULT 필수
- 마이그레이션마다 테스트 작성
- 스키마 버전 올릴 때 백업 포맷의 `schemaVersion`도 함께 관리
