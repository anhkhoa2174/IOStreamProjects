package co.iostream.apps.code_pocket.domain.models

enum class CodeItemZone {
    OTHERS, ME, PROMOTION
}

enum class CodeItemLabel {
    RED, YELLOW, GREEN, BLUE, VIOLET, WHITE
}

data class CodeLabel(var label: CodeItemLabel, var colorName: String)

class CodeItem constructor(
    var code: String, var description: String, var label: CodeItemLabel, var zone: CodeItemZone, var isDeleted: Boolean
) {
    /*
    Basic properties
    * */
    var createdAt: Long = System.currentTimeMillis()

    /*
    Media properties
    * */
    var logoFilePath: String = ""
}
