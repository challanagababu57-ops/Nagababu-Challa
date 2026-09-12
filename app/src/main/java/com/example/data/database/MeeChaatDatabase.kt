package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CallDao
import com.example.data.dao.ChannelDao
import com.example.data.dao.ChatDao
import com.example.data.dao.MessageDao
import com.example.data.dao.StatusDao
import com.example.data.entity.CallEntity
import com.example.data.entity.ChannelEntity
import com.example.data.entity.ChatEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.StatusEntity

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        StatusEntity::class,
        CallEntity::class,
        ChannelEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MeeChaatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun statusDao(): StatusDao
    abstract fun callDao(): CallDao
    abstract fun channelDao(): ChannelDao

    companion object {
        @Volatile
        private var INSTANCE: MeeChaatDatabase? = null

        fun getDatabase(context: Context): MeeChaatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeeChaatDatabase::class.java,
                    "meechaat_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
