package net.ts_matsu.kabusign.model.data.db.dao

import androidx.room.*
import net.ts_matsu.kabusign.model.data.db.entity.AlarmEntity

@Dao
interface AlarmDao {
    @Query("SELECT * FROM tbl_alarm WHERE code = :code")
    fun get(code: String): List<AlarmEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: AlarmEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: AlarmEntity)
}