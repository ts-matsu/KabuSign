package net.ts_matsu.kabusign.model.data.db.dao

import androidx.annotation.CheckResult
import androidx.room.*
import net.ts_matsu.kabusign.model.data.db.entity.ConditionKindEntity

@Dao
interface ConditionKindDao {
    @Query("SELECT * FROM tbl_condition_kind")
    fun getAll(): List<ConditionKindEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ConditionKindEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: ConditionKindEntity)

    @Delete
    fun delete(entity: ConditionKindEntity)
}