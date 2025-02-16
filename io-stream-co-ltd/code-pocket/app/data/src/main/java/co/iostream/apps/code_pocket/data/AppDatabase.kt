package co.iostream.apps.code_pocket.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import co.iostream.apps.code_pocket.data.daos.CodeItemDao
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity

@Database(version = 4, entities = [CodeItemEntity::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun codeItemDao(): CodeItemDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

//        val migration = Migration(1, 2) {
//            // Your migration strategy here
//            it.execSQL("ALTER TABLE code_items ADD COLUMN label INTEGER")
//        }

        fun getInst(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, context.packageName)
                    .fallbackToDestructiveMigration()
                    .build().also {
                        instance = it
                    }
            }
        }
    }
}