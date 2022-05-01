package net.ts_matsu.kabusign.model.data

import net.ts_matsu.kabusign.model.data.db.entity.*

// データベース保存時のキャッシュクラス
class DatabaseCache {
    // 各テーブルのキャッシュは、シングルトンとする
    private companion object {
        val priceDesignationEntityList = mutableListOf<PriceDesignationEntity>()
        val monitoredStockEntityList = mutableListOf<MonitoredStockEntity>()
        val conditionKindEntityList = mutableListOf<ConditionKindEntity>()
        val candleConditionEntityList = mutableListOf<CandleConditionEntity>()
        val alarmEntityList = mutableListOf<AlarmEntity>()
    }

    fun addPriceDesignationEntity(entity: PriceDesignationEntity) {
        var isUpdate = false
        for((i,list) in priceDesignationEntityList.withIndex()) {
            if(list.revision == entity.revision) {
                priceDesignationEntityList[i] = entity
                isUpdate = true
            }
        }
        if(!isUpdate){
            priceDesignationEntityList.add(entity)
        }
    }
    fun addMonitoredStockEntity(entity: MonitoredStockEntity) {
        monitoredStockEntityList.add(entity)
    }
    fun addConditionKindEntity(entity: ConditionKindEntity) {
        conditionKindEntityList.add(entity)
    }
    fun addCandleConditionEntity(entity: CandleConditionEntity) {
        var isUpdate = false
        for((i,list) in candleConditionEntityList.withIndex()) {
            if(list.revision == entity.revision) {
                candleConditionEntityList[i] = entity
                isUpdate = true
            }
        }
        if(!isUpdate){
            candleConditionEntityList.add(entity)
        }
    }
    fun addAlarmEntity(entity: AlarmEntity) {
        alarmEntityList.add(entity)
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
    fun getAlarmEntityList(): MutableList<AlarmEntity> {
        return alarmEntityList;
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
    fun clearAlarmEntityList() {
        alarmEntityList.clear()
    }
}