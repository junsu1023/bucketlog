package com.bucketlog.presentation.common

import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.category_challenge
import bucketlog.shared.generated.resources.category_health
import bucketlog.shared.generated.resources.category_hobby
import bucketlog.shared.generated.resources.category_learning
import bucketlog.shared.generated.resources.category_other
import bucketlog.shared.generated.resources.category_relationship
import bucketlog.shared.generated.resources.category_travel
import bucketlog.shared.generated.resources.filter_archived
import bucketlog.shared.generated.resources.filter_completed
import bucketlog.shared.generated.resources.filter_in_progress
import com.bucketlog.domain.model.Category
import com.bucketlog.domain.model.GoalStatus
import org.jetbrains.compose.resources.StringResource

fun Category.labelRes(): StringResource = when (this) {
    Category.TRAVEL -> Res.string.category_travel
    Category.HOBBY -> Res.string.category_hobby
    Category.RELATIONSHIP -> Res.string.category_relationship
    Category.CHALLENGE -> Res.string.category_challenge
    Category.LEARNING -> Res.string.category_learning
    Category.HEALTH -> Res.string.category_health
    Category.OTHER -> Res.string.category_other
}

fun GoalStatus.filterLabelRes(): StringResource = when (this) {
    GoalStatus.IN_PROGRESS -> Res.string.filter_in_progress
    GoalStatus.COMPLETED -> Res.string.filter_completed
    GoalStatus.ARCHIVED -> Res.string.filter_archived
}
