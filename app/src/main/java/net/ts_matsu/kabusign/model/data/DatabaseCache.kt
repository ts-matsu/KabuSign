package net.ts_matsu.kabusign.model.data

import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity
import net.ts_matsu.kabusign.model.data.db.entity.ConditionKindEntity
import net.ts_matsu.kabusign.model.data.db.entity.MonitoredStockEntity
import net.ts_matsu.kabusign.model.data.db.entity.PriceDesignationEntity

// データベース保存時のキャッシュクラス
class DatabaseCache {
    // 各テーブルのキャッシュは、シングルトンとする
    private companion object {
        val priceDesignationEntityList = mutableListOf<PriceDesignationEntity>()
        val monitoredStockEntityList = mutableListOf<MonitoredStockEntity>()
        val conditionKindEntityList = mutableListOf<ConditionKindEntity>()
        val candleConditionEntityList = mutableListOf<CandleConditionEntity>()
    }

    fun addPriceDesignationEntity(entity: PriceDesignationEntity) {
        priceDesignationEntityList.add(entity)
    }
    fun addMonitoredStockEntity(entity: MonitoredStockEntity) {
        monitoredStockEntityList.add(entity)
    }
    fun addConditionKindEntity(entity: ConditionKindEntity) {
        conditionKindEntityList.add(entity)
    }
    fun addCandleConditionEntity(entity: CandleConditionEntity) {
        candleConditionEntityList.add(entity)
    }

    fun getPriceDesignationEntityList(): MutableList<PriceDesignationEntity> {
        return priceDesignationEntityList;
    }
    fun getMonitoredStockEntityList(): MutableList<MonitoredStockEntity> {
        return monitoredStockEntityList;
    }
    fun getConditionKindEntityList(): MutableList<ConditionKindEntity> {
        return conditionKindEntityList;
    }
    fun getCandleConditionEntityList(): MutableList<CandleConditionEntity> {
        return candleConditionEntityList;
    }

    fun clearPriceDesignationEntityList() {
        priceDesignationEntityList.clear()
    }
    fun clearMonitoredStockEntityList() {
        monitoredStockEntityList.clear()
    }
    fun clearConditionKindEntityList() {
        conditionKindEntityList.clear()
    }
    fun clearCandleConditionEntityList() {
        candleConditionEntityList.clear()
    }
}