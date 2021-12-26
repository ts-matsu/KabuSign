package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_candle_condition")
data class CandleConditionEntity(
    @PrimaryKey val code: String,
    val isEnabled: Boolean,
    val candleBody: Int,
    val candleShadows: Int,
    val openValue: String,
    val closeValue: String,
    val highValue: String,
    val lowValue: String,
    val openCondition: Int,
    val closeCondition: Int,
    val highCondition: Int,
    val lowCondition: Int
)
