package vn.iostream.apps.file_locker_x.configs

import vn.iostream.apps.core.iofile.FileUtils

class Profile {
    companion object {
        private val _INPUT_FILES: Map<AppTypes.FileType, AppTypes.FileTypeRecord> = mapOf(
            AppTypes.FileType.Image         to AppTypes.FileTypeRecord(true, FileUtils.EXTENSIONS[FileUtils.Type.Image]!!),
            AppTypes.FileType.Video         to AppTypes.FileTypeRecord(true, FileUtils.EXTENSIONS[FileUtils.Type.Video]!!),
            AppTypes.FileType.Audio         to AppTypes.FileTypeRecord(true, FileUtils.EXTENSIONS[FileUtils.Type.Audio]!!),
            AppTypes.FileType.Document      to AppTypes.FileTypeRecord(true, FileUtils.EXTENSIONS[FileUtils.Type.Document]!!),
            AppTypes.FileType.Pdf           to AppTypes.FileTypeRecord(true, arrayOf(".pdf")),
        )
    }
}