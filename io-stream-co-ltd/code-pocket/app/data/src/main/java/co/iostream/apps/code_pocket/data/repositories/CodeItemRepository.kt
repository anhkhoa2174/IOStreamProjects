package co.iostream.apps.code_pocket.data.repositories

import kotlinx.coroutines.flow.Flow
import co.iostream.apps.code_pocket.data.daos.CodeItemDao
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone
import javax.inject.Inject
import javax.inject.Singleton

interface ICodeItemRepository {
    fun getAll(): Flow<List<CodeItemEntity>>
    fun getByZone(zone: CodeItemZone): Flow<List<CodeItemEntity>>
    fun getByLabelAndZone(
        zone: CodeItemZone,
        label: CodeItemLabel
    ): Flow<List<CodeItemEntity>>

    fun getById(id: Long): Flow<CodeItemEntity>
    suspend fun insert(item: CodeItemEntity): Long
    suspend fun update(item: CodeItemEntity): Int
    suspend fun delete(item: CodeItemEntity): Int
    suspend fun deleteAll()
}

@Singleton
class CodeItemRepository @Inject constructor(private val codeItemDao: CodeItemDao) : ICodeItemRepository {
    override fun getAll(): Flow<List<CodeItemEntity>> {
        return codeItemDao.getAll()
    }

    override fun getByZone(zone: CodeItemZone): Flow<List<CodeItemEntity>> {
        return codeItemDao.getByZone(zone)
    }

    override fun getByLabelAndZone(
        zone: CodeItemZone,
        label: CodeItemLabel
    ): Flow<List<CodeItemEntity>> {
        return codeItemDao.getByLabelAndZone(zone, label)
    }

    override fun getById(id: Long): Flow<CodeItemEntity> {
        return codeItemDao.getById(id)
    }

    override suspend fun insert(item: CodeItemEntity): Long {
        return codeItemDao.insert(item)
    }

    override suspend fun update(item: CodeItemEntity): Int {
        return codeItemDao.update(item)
    }

    override suspend fun delete(item: CodeItemEntity): Int {
        return codeItemDao.delete(item)
    }

    override suspend fun deleteAll() {
        return codeItemDao.deleteAll()
    }
}