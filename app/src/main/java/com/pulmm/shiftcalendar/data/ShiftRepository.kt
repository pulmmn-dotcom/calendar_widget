package com.pulmm.shiftcalendar.data

import com.pulmm.shiftcalendar.data.entity.DayOverride
import com.pulmm.shiftcalendar.data.entity.Memo
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.PatternDefinition
import com.pulmm.shiftcalendar.logic.ShiftResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ShiftRepository(
    private val db: AppDatabase,
    private val onDataChanged: DataChangeListener = DataChangeListener {}
) {

    fun observeShiftTypes(): Flow<List<ShiftType>> = db.shiftTypeDao().observeAll()

    fun observePatterns() = db.shiftPatternDao().observeAllWithItems()

    suspend fun getShiftTypesOnce(): List<ShiftType> = observeShiftTypes().first()

    fun observeDayInfoRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DayInfo>> =
        combine(
            db.shiftPatternDao().observeAllWithItems(),
            db.dayOverrideDao().observeRange(startEpochDay, endEpochDay),
            db.memoDao().observeRange(startEpochDay, endEpochDay)
        ) { patternRows, overrides, memos ->
            val patterns = patternRows.map { row ->
                PatternDefinition(
                    startEpochDay = row.pattern.startEpochDay,
                    shiftTypeIdsInOrder = row.items.sortedBy { it.orderIndex }.map { it.shiftTypeId }
                )
            }
            val overrideMap = overrides.associate { it.epochDay to it.shiftTypeId }
            val memoMap = memos.associateBy { it.epochDay }
            (startEpochDay..endEpochDay).map { day ->
                DayInfo(
                    epochDay = day,
                    shiftTypeId = ShiftResolver.resolveShiftTypeId(patterns, overrideMap, day),
                    memoText = memoMap[day]?.text
                )
            }
        }

    suspend fun getDayInfoOnce(epochDay: Long): DayInfo =
        observeDayInfoRange(epochDay, epochDay).first().first()

    suspend fun setOverride(epochDay: Long, shiftTypeId: Long?) {
        if (shiftTypeId == null) db.dayOverrideDao().delete(epochDay)
        else db.dayOverrideDao().upsert(DayOverride(epochDay, shiftTypeId))
        onDataChanged.onDataChanged()
    }

    suspend fun setMemo(epochDay: Long, text: String) {
        if (text.isBlank()) db.memoDao().delete(epochDay) else db.memoDao().upsert(Memo(epochDay, text))
        onDataChanged.onDataChanged()
    }

    suspend fun addShiftType(name: String, colorArgb: Int, sortOrder: Int): Long {
        val id = db.shiftTypeDao().insert(ShiftType(name = name, colorArgb = colorArgb, sortOrder = sortOrder))
        onDataChanged.onDataChanged()
        return id
    }

    suspend fun updateShiftType(shiftType: ShiftType) {
        db.shiftTypeDao().update(shiftType)
        onDataChanged.onDataChanged()
    }

    suspend fun deleteShiftType(shiftType: ShiftType) {
        db.shiftTypeDao().delete(shiftType)
        onDataChanged.onDataChanged()
    }

    suspend fun savePattern(name: String, startEpochDay: Long, shiftTypeIdsInOrder: List<Long>) {
        val patternId = db.shiftPatternDao().insertPattern(ShiftPattern(name = name, startEpochDay = startEpochDay))
        db.shiftPatternDao().insertItems(
            shiftTypeIdsInOrder.mapIndexed { index, shiftTypeId ->
                ShiftPatternItem(patternId = patternId, shiftTypeId = shiftTypeId, orderIndex = index)
            }
        )
        onDataChanged.onDataChanged()
    }

    suspend fun deletePattern(pattern: ShiftPattern) {
        db.shiftPatternDao().deletePattern(pattern)
        onDataChanged.onDataChanged()
    }
}
