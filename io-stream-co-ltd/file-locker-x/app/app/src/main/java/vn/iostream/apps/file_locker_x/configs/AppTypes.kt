package vn.iostream.apps.file_locker_x.configs

import vn.iostream.apps.file_locker_x.R

class AppTypes {
    companion object {
        val DOCS: Map<String, String> = mapOf()
        val FILES: Map<FileType, String> = mapOf(
            FileType.All to "All",
            FileType.Video to "Video",
            FileType.Audio to "Audio",
            FileType.Image to "Image",
        )

        val SORT_TYPES: Map<SortType, Int> = mapOf(
            SortType.AZ to R.string.a2z,
            SortType.ZA to R.string.z2a,
            SortType.Newest to R.string.newest,
            SortType.Oldest to R.string.oldest,
        )
    }

    enum class FileType {
        All, Video, Audio, Image, Document, Pdf,
    }

    enum class SortType {
        AZ, ZA, Newest, Oldest,
    }

    class FileTypeRecord(var IsSupported: Boolean, var Extensions: Array<String>)
}