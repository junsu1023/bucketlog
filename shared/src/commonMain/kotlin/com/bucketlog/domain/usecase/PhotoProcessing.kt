package com.bucketlog.domain.usecase

import com.bucketlog.domain.model.Photo
import com.bucketlog.domain.util.newId
import com.bucketlog.platform.FileStorage
import com.bucketlog.platform.ImageProcessor

/**
 * 원본 바이트(카메라/갤러리 결과) → 리사이즈/압축 → 로컬 저장까지 처리해 Photo 목록을 만든다.
 * AddProgressEntryUseCase/CompleteGoalUseCase가 공유한다. docs/ARCHITECTURE.md §5 처리 흐름.
 */
internal suspend fun processPhotosForEntry(
    entryId: String,
    photoBytes: List<ByteArray>,
    imageProcessor: ImageProcessor,
    fileStorage: FileStorage,
): List<Photo> = photoBytes.mapIndexed { index, bytes ->
    val processed = imageProcessor.process(bytes)
    val photoId = newId()
    val paths = fileStorage.writePhoto(photoId, processed.display, processed.thumbnail)
    Photo(
        id = photoId,
        entryId = entryId,
        path = paths.displayPath,
        thumbnailPath = paths.thumbnailPath,
        order = index,
        width = processed.width,
        height = processed.height,
    )
}
