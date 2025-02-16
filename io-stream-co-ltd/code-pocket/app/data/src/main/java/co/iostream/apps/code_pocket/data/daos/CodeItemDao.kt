package co.iostream.apps.code_pocket.data.daos

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone

@Dao
interface CodeItemDao {
    @Query("SELECT * FROM codes")
    fun getAll(): Flow<List<CodeItemEntity>>

    @Query("SELECT * FROM codes WHERE zone = :zone")
    fun getByZone(zone: CodeItemZone): Flow<List<CodeItemEntity>>

    @Query("SELECT * FROM codes WHERE label = :label AND zone = :zone")
    fun getByLabelAndZone(zone: CodeItemZone, label: CodeItemLabel): Flow<List<CodeItemEntity>>

    @Query("SELECT * FROM codes WHERE id = :id")
    fun getById(id: Long): Flow<CodeItemEntity>

    @Insert
    suspend fun insert(item: CodeItemEntity): Long

    @Update
    suspend fun update(item: CodeItemEntity): Int

    @Delete
    suspend fun delete(item: CodeItemEntity): Int

    @Query("DELETE FROM codes")
    suspend fun deleteAll()
}