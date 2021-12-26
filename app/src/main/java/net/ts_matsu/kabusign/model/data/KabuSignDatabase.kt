package net.ts_matsu.kabusign.model.data

import androidx.room.Database
import androidx.room.RoomDatabase
import net.ts_matsu.kabusign.model.data.db.dao.CandleConditionDao
import net.ts_matsu.kabusign.model.data.db.dao.ConditionKindDao
import net.ts_matsu.kabusign.model.data.db.dao.MonitoredStockDao
import net.ts_matsu.kabusign.model.data.db.dao.PriceDesignationDao
import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity
import net.ts_matsu.kabusign.model.data.db.entity.ConditionKindEntity
import net.ts_matsu.kabusign.model.data.db.entity.MonitoredStockEntity
import net.ts_matsu.kabusign.model.data.db.entity.PriceDesignationEntity

@Database(
    entities = [CandleConditionEntity::class, MonitoredStockEntity::class, ConditionKindEntity::class, PriceDesignationEntity::class],
    version = 1, exportSchema = false)
abstract class KabuSignDatabase: RoomDatabase() {
    abstract fun candleConditionDao(): CandleConditionDao
    abstract fun monitoredStockDao(): MonitoredStockDao
    abstract fun conditionKindDao(): ConditionKindDao
    abstract fun priceDesignationDao(): PriceDesignationDao
}