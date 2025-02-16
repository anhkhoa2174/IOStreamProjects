package vn.iostream.apps.core.iofile

open class ImageFormat(
    family: ImageUtils.Family,
    name: String,
    type: FileUtils.Type,
    var isRaw: Boolean,
    var extensions: Array<String>
) : FileFormat(name, type) {
    var family: ImageUtils.Family = family
        private set

    val extension: String?
        get() = extensions.firstOrNull()

    constructor(format: ImageFormat) : this(
        format.family,
        format.name,
        format.type,
        format.isRaw,
        format.extensions.clone()
    ) {
        this.family = format.family
        this.isRaw = format.isRaw
        this.extensions = format.extensions.clone()
    }
}


class ImageUtils {
    enum class Family {
        Null,

        // Standard
        Avif,
        Eps,
        Heic,
        Mpo,
        Psd,
        Qoi,
        Sfw,
        Svg,
        Tga,
        Tiff,
        Webp,
        Xpm,

        Bmp,
        Gif,
        Ico,
        Jpg,
        Png,

        Png8,    // Output only

        Pbm,
        Pcx,
        Wbmp,

        // Raw
        Arw,
        Cr2,
        Dcr,
        Dng,
        Erf,
        Mef,
        Nef,
        Orf,
        Pef,
        Raf,
        Raw,
        Rw2
    }

    companion object {
        val IMAGE_FORMATS: HashMap<Family, ImageFormat> = hashMapOf(
            // Standard
            Family.Avif to ImageFormat(
                Family.Avif,
                "AVIF",
                FileUtils.Type.Image,
                false,
                arrayOf(".avif")
            ),
            Family.Eps to ImageFormat(
                Family.Eps,
                "EPS",
                FileUtils.Type.Image,
                false,
                arrayOf(".eps")
            ),
            Family.Heic to ImageFormat(
                Family.Heic,
                "HEIC",
                FileUtils.Type.Image,
                false,
                arrayOf(".heic", ".heif")
            ),
            Family.Mpo to ImageFormat(
                Family.Mpo,
                "MPO",
                FileUtils.Type.Image,
                false,
                arrayOf(".mpo")
            ),
            Family.Psd to ImageFormat(
                Family.Psd,
                "PSD",
                FileUtils.Type.Image,
                false,
                arrayOf(".psb", ".psd")
            ),
            Family.Qoi to ImageFormat(
                Family.Qoi,
                "QOI",
                FileUtils.Type.Image,
                false,
                arrayOf(".qoi"),
            ),
            Family.Sfw to ImageFormat(
                Family.Sfw,
                "Seattle FilmWorks",
                FileUtils.Type.Image,
                false,
                arrayOf(".pwp", ".sfw"),
            ),
            Family.Svg to ImageFormat(
                Family.Svg,
                "SVG",
                FileUtils.Type.Image,
                false,
                arrayOf(".svg", ".svgz"),
            ),
            Family.Tga to ImageFormat(
                Family.Tga,
                "TGA",
                FileUtils.Type.Image,
                false,
                arrayOf(".icb", ".tga", ".vda", ".vst"),
            ),
            Family.Tiff to ImageFormat(
                Family.Tiff,
                "TIFF",
                FileUtils.Type.Image,
                false,
                arrayOf(".tif", ".tiff"),
            ),
            Family.Webp to ImageFormat(
                Family.Webp,
                "WEBP",
                FileUtils.Type.Image,
                false,
                arrayOf(".webp"),
            ),
            Family.Xpm to ImageFormat(
                Family.Xpm,
                "XPM",
                FileUtils.Type.Image,
                false,
                arrayOf(".xbm", ".xpm"),
            ),

            Family.Bmp to ImageFormat(
                Family.Bmp,
                "BMP",
                FileUtils.Type.Image,
                false,
                arrayOf(".bmp", ".rle", ".dib"),
            ),
            Family.Gif to ImageFormat(
                Family.Gif,
                "GIF",
                FileUtils.Type.Image,
                false,
                arrayOf(".gif"),
            ),
            Family.Ico to ImageFormat(
                Family.Ico,
                "ICO",
                FileUtils.Type.Image,
                false,
                arrayOf(".ico"),
            ),
            Family.Jpg to ImageFormat(
                Family.Jpg,
                "JPG",
                FileUtils.Type.Image,
                false,
                arrayOf(".jpg", ".jpeg", ".jpe", ".jfif"),
            ),
            Family.Png to ImageFormat(
                Family.Png,
                "PNG",
                FileUtils.Type.Image,
                false,
                arrayOf(".png"),
            ),

            Family.Png8 to ImageFormat(
                Family.Png8,
                "PNG8",
                FileUtils.Type.Image,
                false,
                arrayOf(".png"),
            ), // Output Only

            Family.Pbm to ImageFormat(
                Family.Pbm,
                "PBM",
                FileUtils.Type.Image,
                false,
                arrayOf(".pbm", ".pgm", ".ppm", ".pnm", ".pfm", ".pam"),
            ),
            Family.Pcx to ImageFormat(
                Family.Pcx,
                "PCX",
                FileUtils.Type.Image,
                false,
                arrayOf(".pcx"),
            ),
            Family.Wbmp to ImageFormat(
                Family.Wbmp,
                "WBMP",
                FileUtils.Type.Image,
                false,
                arrayOf(".wbm", ".wbmp"),
            ),

            // Raw
            Family.Arw to ImageFormat(
                Family.Arw,
                "ARW",
                FileUtils.Type.Image,
                true,
                arrayOf(".arw", ".srf", ".sr2"),
            ),
            Family.Cr2 to ImageFormat(
                Family.Cr2,
                "CR2",
                FileUtils.Type.Image,
                true,
                arrayOf(".cr2", ".cr3", ".crw"),
            ),
            Family.Dcr to ImageFormat(
                Family.Dcr,
                "DCR",
                FileUtils.Type.Image,
                true,
                arrayOf(".dcr", ".kdc", ".k25"),
            ),
            Family.Dng to ImageFormat(
                Family.Dng,
                "DNG",
                FileUtils.Type.Image,
                true,
                arrayOf(".dng"),
            ),
            Family.Erf to ImageFormat(
                Family.Erf,
                "ERF",
                FileUtils.Type.Image,
                true,
                arrayOf(".erf"),
            ),
            Family.Mef to ImageFormat(
                Family.Mef,
                "MEF",
                FileUtils.Type.Image,
                true,
                arrayOf(".mef"),
            ),
            Family.Nef to ImageFormat(
                Family.Nef,
                "NEF",
                FileUtils.Type.Image,
                true,
                arrayOf(".nef", ".nrw"),
            ),
            Family.Orf to ImageFormat(
                Family.Orf,
                "ORF",
                FileUtils.Type.Image,
                true,
                arrayOf(".orf"),
            ),
            Family.Pef to ImageFormat(
                Family.Pef,
                "PEF",
                FileUtils.Type.Image,
                true,
                arrayOf(".pef"),
            ),
            Family.Raf to ImageFormat(
                Family.Raf,
                "RAF",
                FileUtils.Type.Image,
                true,
                arrayOf(".raf"),
            ),
            Family.Raw to ImageFormat(
                Family.Raw,
                "RAW",
                FileUtils.Type.Image,
                true,
                arrayOf(".raw"),
            ),
            Family.Rw2 to ImageFormat(
                Family.Rw2,
                "RW2",
                FileUtils.Type.Image,
                true,
                arrayOf(".rw2"),
            )
        )

        val AUTO_DETECT_FORMAT_FAMILIES = arrayOf(
            Family.Heic,
            Family.Svg,
            Family.Tga,
            Family.Tiff,
            Family.Webp,
            Family.Bmp,
            Family.Gif,
            Family.Ico,
            Family.Jpg,
            Family.Png,
            Family.Png8
        )
    }
}