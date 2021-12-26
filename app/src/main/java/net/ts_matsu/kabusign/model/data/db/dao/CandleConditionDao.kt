package net.ts_matsu.kabusign.model.data.db.dao

import androidx.room.*
import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity

@Dao
interface CandleConditionDao {

    @Query("SELECT * FROM tbl_candle_condition")
    fun getAll(): List<CandleConditionEntity>?

    @Query("SELECT * FROM tbl_candle_condition WHERE code = :code")
    fun get(code: String) : CandleConditionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: CandleConditionEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: CandleConditionEntity)

    @Delete
    fun delete(entity: CandleConditionEntity)
}