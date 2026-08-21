package com.bucketlog.presentation.theme

import com.bucketlog.notification.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 앱 전역 다크모드 설정. [SettingsStore]는 suspend 함수만 있고 Flow가 아니라서,
 * 설정 화면(쓰기)과 App() 루트(읽기)가 즉시 동기화되려면 이 얇은 반응형 레이어가 필요하다.
 * Koin 싱글턴이라 앱 전체가 같은 인스턴스를 공유한다.
 */
class ThemeModeStore(private val settings: SettingsStore) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    init {
        scope.launch {
            val saved = settings.getString(KEY, null)
            _mode.value = ThemeMode.entries.find { it.name == saved } ?: ThemeMode.SYSTEM
        }
    }

    fun setMode(mode: ThemeMode) {
        _mode.value = mode
        scope.launch { settings.setString(KEY, mode.name) }
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}
