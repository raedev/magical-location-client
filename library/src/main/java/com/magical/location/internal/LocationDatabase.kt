package com.magical.location.internal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.magical.location.LocationOptions
import com.magical.location.model.LocationPoint
import com.magical.location.model.LocationTrace
import java.io.File

/**
 * 位置数据库
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Database(
    entities = [LocationPoint::class, LocationTrace::class],
    version = 1,
    exportSchema = false
)
internal abstract class LocationDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context, options: LocationOptions): LocationDatabase {
            var name = "locations-${options.userId}.db"
            options.databasePath?.let {
                name = File(it, name).path
            }
            return Room.databaseBuilder(context, LocationDatabase::class.java, name).build()
        }
    }

    /**
     * 位置数据库操作
     */
    abstract fun locationDao(): LocationDao
}