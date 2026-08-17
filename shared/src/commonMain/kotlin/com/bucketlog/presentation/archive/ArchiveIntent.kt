package com.bucketlog.presentation.archive

sealed interface ArchiveIntent {
    data class SelectTab(val tab: ArchiveTab) : ArchiveIntent
}
