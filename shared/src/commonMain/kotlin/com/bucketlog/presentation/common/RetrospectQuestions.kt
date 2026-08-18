package com.bucketlog.presentation.common

import bucketlog.shared.generated.resources.Res
import bucketlog.shared.generated.resources.retrospect_question_challenge
import bucketlog.shared.generated.resources.retrospect_question_common_1
import bucketlog.shared.generated.resources.retrospect_question_common_2
import bucketlog.shared.generated.resources.retrospect_question_common_3
import bucketlog.shared.generated.resources.retrospect_question_common_4
import bucketlog.shared.generated.resources.retrospect_question_learning
import bucketlog.shared.generated.resources.retrospect_question_relationship
import bucketlog.shared.generated.resources.retrospect_question_travel
import com.bucketlog.domain.model.Category
import org.jetbrains.compose.resources.StringResource

/** E-08 회고 프롬프트. docs/DESIGN.md §회고 프롬프트 참고. */
private val commonQuestions: List<StringResource> = listOf(
    Res.string.retrospect_question_common_1,
    Res.string.retrospect_question_common_2,
    Res.string.retrospect_question_common_3,
    Res.string.retrospect_question_common_4,
)

private val categoryQuestions: Map<Category, StringResource> = mapOf(
    Category.TRAVEL to Res.string.retrospect_question_travel,
    Category.CHALLENGE to Res.string.retrospect_question_challenge,
    Category.LEARNING to Res.string.retrospect_question_learning,
    Category.RELATIONSHIP to Res.string.retrospect_question_relationship,
)

/** 완료 시 랜덤으로 하나 제시한다. [exclude]를 넘기면 "다른 질문 보기" 재추첨에서 같은 질문을 뺀다. */
fun randomRetrospectQuestion(category: Category, exclude: StringResource? = null): StringResource {
    val pool = (commonQuestions + listOfNotNull(categoryQuestions[category]))
        .filter { it != exclude }
        .ifEmpty { commonQuestions }
    return pool.random()
}
