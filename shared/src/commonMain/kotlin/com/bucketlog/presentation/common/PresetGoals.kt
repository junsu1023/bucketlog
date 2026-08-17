package com.bucketlog.presentation.common

import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.preset_challenge_1
import bucketlog.shared.generated.resources.preset_challenge_2
import bucketlog.shared.generated.resources.preset_challenge_3
import bucketlog.shared.generated.resources.preset_challenge_4
import bucketlog.shared.generated.resources.preset_challenge_5
import bucketlog.shared.generated.resources.preset_health_1
import bucketlog.shared.generated.resources.preset_health_2
import bucketlog.shared.generated.resources.preset_health_3
import bucketlog.shared.generated.resources.preset_health_4
import bucketlog.shared.generated.resources.preset_hobby_1
import bucketlog.shared.generated.resources.preset_hobby_2
import bucketlog.shared.generated.resources.preset_hobby_3
import bucketlog.shared.generated.resources.preset_hobby_4
import bucketlog.shared.generated.resources.preset_hobby_5
import bucketlog.shared.generated.resources.preset_learning_1
import bucketlog.shared.generated.resources.preset_learning_2
import bucketlog.shared.generated.resources.preset_learning_3
import bucketlog.shared.generated.resources.preset_learning_4
import bucketlog.shared.generated.resources.preset_other_1
import bucketlog.shared.generated.resources.preset_other_2
import bucketlog.shared.generated.resources.preset_other_3
import bucketlog.shared.generated.resources.preset_other_4
import bucketlog.shared.generated.resources.preset_relationship_1
import bucketlog.shared.generated.resources.preset_relationship_2
import bucketlog.shared.generated.resources.preset_relationship_3
import bucketlog.shared.generated.resources.preset_relationship_4
import bucketlog.shared.generated.resources.preset_travel_1
import bucketlog.shared.generated.resources.preset_travel_2
import bucketlog.shared.generated.resources.preset_travel_3
import bucketlog.shared.generated.resources.preset_travel_4
import bucketlog.shared.generated.resources.preset_travel_5
import com.bucketlog.domain.model.Category
import org.jetbrains.compose.resources.StringResource

/** 온보딩(O-01)과 홈 빈 상태(H-06)가 함께 쓰는 프리셋 목표. docs/MVP-SCOPE.md §2.7 참고. */
data class PresetGoal(val titleRes: StringResource, val category: Category)

val presetGoals: List<PresetGoal> = listOf(
    PresetGoal(Res.string.preset_travel_1, Category.TRAVEL),
    PresetGoal(Res.string.preset_travel_2, Category.TRAVEL),
    PresetGoal(Res.string.preset_travel_3, Category.TRAVEL),
    PresetGoal(Res.string.preset_travel_4, Category.TRAVEL),
    PresetGoal(Res.string.preset_travel_5, Category.TRAVEL),
    PresetGoal(Res.string.preset_hobby_1, Category.HOBBY),
    PresetGoal(Res.string.preset_hobby_2, Category.HOBBY),
    PresetGoal(Res.string.preset_hobby_3, Category.HOBBY),
    PresetGoal(Res.string.preset_hobby_4, Category.HOBBY),
    PresetGoal(Res.string.preset_hobby_5, Category.HOBBY),
    PresetGoal(Res.string.preset_relationship_1, Category.RELATIONSHIP),
    PresetGoal(Res.string.preset_relationship_2, Category.RELATIONSHIP),
    PresetGoal(Res.string.preset_relationship_3, Category.RELATIONSHIP),
    PresetGoal(Res.string.preset_relationship_4, Category.RELATIONSHIP),
    PresetGoal(Res.string.preset_challenge_1, Category.CHALLENGE),
    PresetGoal(Res.string.preset_challenge_2, Category.CHALLENGE),
    PresetGoal(Res.string.preset_challenge_3, Category.CHALLENGE),
    PresetGoal(Res.string.preset_challenge_4, Category.CHALLENGE),
    PresetGoal(Res.string.preset_challenge_5, Category.CHALLENGE),
    PresetGoal(Res.string.preset_learning_1, Category.LEARNING),
    PresetGoal(Res.string.preset_learning_2, Category.LEARNING),
    PresetGoal(Res.string.preset_learning_3, Category.LEARNING),
    PresetGoal(Res.string.preset_learning_4, Category.LEARNING),
    PresetGoal(Res.string.preset_health_1, Category.HEALTH),
    PresetGoal(Res.string.preset_health_2, Category.HEALTH),
    PresetGoal(Res.string.preset_health_3, Category.HEALTH),
    PresetGoal(Res.string.preset_health_4, Category.HEALTH),
    PresetGoal(Res.string.preset_other_1, Category.OTHER),
    PresetGoal(Res.string.preset_other_2, Category.OTHER),
    PresetGoal(Res.string.preset_other_3, Category.OTHER),
    PresetGoal(Res.string.preset_other_4, Category.OTHER),
)
