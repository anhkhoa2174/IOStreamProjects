package vn.iostream.apps.data.repositories

import vn.iostream.apps.data.daos.FileDao
import vn.iostream.apps.data.entities.FileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(private val fileDao: FileDao) {
    suspend fun getAll(): List<FileEntity> {
        return fileDao.getAll()
    }

    fun getById(id: Long): FileEntity {
        return fileDao.getById(id)
    }

    fun insert(item: FileEntity): Long {
        return fileDao.insert(item)
    }

    suspend fun getByPath(path: String): List<FileEntity> {
        return fileDao.getByPath(path)
    }

    suspend fun insertAsync(item: FileEntity): Long {
        return fileDao.insertAsync(item)
    }

    suspend fun update(item: FileEntity): Int {
        return fileDao.update(item)
    }

    suspend fun deleteByPath(path: String): Int {
        return fileDao.deleteByPath(path)
    }

    suspend fun deleteAll(): Int {
        return fileDao.deleteAll()
    }
}