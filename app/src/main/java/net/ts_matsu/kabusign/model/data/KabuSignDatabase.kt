package net.ts_matsu.kabusign.model.data

import androidx.room.Database
import androidx.room.RoomDatabase
import net.ts_matsu.kabusign.model.data.db.dao.*
import net.ts_matsu.kabusign.model.data.db.entity.*

@Database(
    entities = [CandleConditionEntity::class, MonitoredStockEntity::class, ConditionKindEntity::class, PriceDesignationEntity::class, AlarmEntity::class],
    version = 1, exportSchema = false)
abstract class KabuSignDatabase: RoomDatabase() {
    abstract fun candleConditionDao(): CandleConditionDao
    abstract fun monitoredStockDao(): MonitoredStockDao
    abstract fun conditionKindDao(): ConditionKindDao
    abstract fun priceDesignationDao(): PriceDesignationDao
    abstract fun alarmDao(): AlarmDao
}