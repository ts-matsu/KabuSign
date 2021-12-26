package net.ts_matsu.kabusign.model.data.db.dao

import androidx.room.*
import net.ts_matsu.kabusign.model.data.db.entity.MonitoredStockEntity

@Dao
interface MonitoredStockDao {

    @Query("SELECT * FROM tbl_monitored_stock")
    fun getAll(): List<MonitoredStockEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: MonitoredStockEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: MonitoredStockEntity)

    @Delete
    fun delete(entity: MonitoredStockEntity)
}