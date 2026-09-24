package com.pulmm.shiftcalendar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pulmm.shiftcalendar.data.dao.DayOverrideDao
import com.pulmm.shiftcalendar.data.dao.MemoDao
import com.pulmm.shiftcalendar.data.dao.ShiftPatternDao
import com.pulmm.shiftcalendar.data.dao.ShiftTypeDao
import com.pulmm.shiftcalendar.data.entity.DayOverride
import com.pulmm.shiftcalendar.data.entity.Memo
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import com.pulmm.shiftcalendar.data.entity.ShiftType

@Database(
    entities = [ShiftType::class, ShiftPattern::class, ShiftPatternItem::class, DayOverride::class, Memo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftTypeDao(): ShiftTypeDao
    abstract fun shiftPatternDao(): ShiftPatternDao
    abstract fun dayOverrideDao(): DayOverrideDao
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shift_calendar.db"
                ).build().also { instance = it }
            }
    }
}
