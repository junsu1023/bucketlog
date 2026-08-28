package com.bucketlog.platform

import androidx.compose.runtime.Composable

/**
 * 시스템 뒤로가기(Android 하드웨어/제스처 백)를 가로채 앱 내부 네비게이션으로 처리한다.
 * enabled가 false면 시스템 기본 동작(액티비티 종료 등)을 그대로 따른다.
 */
@Composable
expect fun AppBackHandler(enabled: Boolean, onBack: () -> Unit)

/**
 * 하단 탭(홈/보관함/검색/설정)처럼 "더 돌아갈 곳이 없는" 화면에서 뒤로가기를 한 번 누르면
 * [message]를 안내하고, 짧은 시간(2초) 안에 다시 누르면 그때 앱을 종료한다.
 * iOS에는 하드웨어 백버튼이 없어 아무 동작도 하지 않는다.
 */
@Composable
expect fun ExitOnDoubleBackHandler(enabled: Boolean, message: String)
