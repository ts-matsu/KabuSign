package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_condition_kind")
data class ConditionKindEntity(
    @PrimaryKey @ColumnInfo(name = "kind") val kind: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "enable") val enable: Boolean
    )
