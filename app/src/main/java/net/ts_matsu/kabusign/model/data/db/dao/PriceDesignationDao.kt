package net.ts_matsu.kabusign.model.data.db.dao

import androidx.room.*
import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity
import net.ts_matsu.kabusign.model.data.db.entity.MonitoredStockEntity
import net.ts_matsu.kabusign.model.data.db.entity.PriceDesignationEntity

@Dao
interface PriceDesignationDao {
    @Query("SELECT * FROM tbl_price_designation")
    fun getAll(): List<PriceDesignationEntity>?

    @Query("SELECT * FROM tbl_price_designation WHERE code = :code AND number = :number")
    fun get(number: Int, code: String): PriceDesignationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: PriceDesignationEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: PriceDesignationEntity)

    @Query("SELECT count(*) FROM tbl_price_designation")
    fun count(): Int
}
