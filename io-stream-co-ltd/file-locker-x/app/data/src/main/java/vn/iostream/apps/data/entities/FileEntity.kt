package vn.iostream.apps.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @ColumnInfo(name = "path") var path: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}