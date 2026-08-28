package com.bucketlog.presentation.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * 재설계 모션 시스템. sinasamaki 계열의 스프링·공유요소·gooey 인디케이터 모션을 가져오되
 * "애니메이션을 보여주기 위한 애니메이션"은 배제한다 — 상태 변화를 이해시키는 데까지만.
 *
 * 원칙:
 *  - 등장엔 살짝 오버슈트하는 스프링, 사라짐엔 스프링을 쓰지 않는다(조용히 나간다).
 *  - 인디케이터(탭/내비)는 이동 방향으로 늘어났다가 뭉치는 gooey 감을 위해 낮은 damping.
 *  - 금지(그대로 유지): 스트릭·진척바 증가·confetti·독려 진동.
 *
 * 제네릭 함수로 노출하는 이유: SpringSpec/TweenSpec은 애니메이션 대상 타입(Dp, Color, Float…)에
 * 묶여 있어 상수 val로 둘 수 없다. 호출부에서 `BucketLogMotion.enter<Dp>()`처럼 쓴다.
 */
object BucketLogMotion {

    /** 화면·카드·시트 등장. 살짝 오버슈트 후 정착. */
    fun <T> enter(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)

    /** 탭·하단 내비 인디케이터의 gooey wobble — 낮은 damping으로 이동 방향에 늘어졌다 뭉친다. */
    fun <T> indicator(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)

    /** 사라짐 — 조용히 나간다. 스프링 금지. */
    fun <T> exit(): FiniteAnimationSpec<T> =
        tween(durationMillis = 180)

    /** 사진 blur-up(초점이 맞는 느낌) / 크로스페이드 지속시간(ms). */
    const val PhotoRevealMillis: Int = 420
}
