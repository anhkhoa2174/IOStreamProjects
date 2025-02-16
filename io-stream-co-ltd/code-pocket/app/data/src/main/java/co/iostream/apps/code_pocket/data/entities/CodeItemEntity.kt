package co.iostream.apps.code_pocket.data.entities

import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone


@Entity(tableName = "codes")
data class CodeItemEntity(
    @ColumnInfo(name = "code") var code: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "label") var label: CodeItemLabel,
    @ColumnInfo(name = "created_at") var createdAt: Long,
    @ColumnInfo(name = "zone") var zone: CodeItemZone,
    @ColumnInfo(name = "logo_file_path") var logoFileName: String,
    @ColumnInfo(name = "is_deleted") var isDeleted: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun getColorByLabel(): Color {
        when (label) {
            CodeItemLabel.RED -> {
                return Color.Red
            }
            CodeItemLabel.BLUE -> {
                return Color.Blue
            }
            CodeItemLabel.GREEN -> {
                return Color.Green
            }
            CodeItemLabel.YELLOW -> {
                return Color.Yellow
            }
            CodeItemLabel.VIOLET -> {
                return Color.Cyan
            }
            else -> {
                return Color.White
            }
        }
}}