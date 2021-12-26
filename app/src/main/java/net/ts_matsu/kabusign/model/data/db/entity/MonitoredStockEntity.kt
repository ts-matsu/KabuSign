package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_monitored_stock")
data class  MonitoredStockEntity(
    @PrimaryKey @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "memo") val memo: String,
    @ColumnInfo(name = "view_position") val viewPosition: Int,
    @ColumnInfo(name = "enable_price_designation") val enablePriceDesignation: Boolean,
    @ColumnInfo(name = "enable_candle") val enableCandle: Boolean,

    // ここからが課金対象（現状思いつくのは、移動平均線のみ）
    // RSI/MACD も入れたいところではある。 todo
    @ColumnInfo(name = "enable_moving_average") val enableMovingAverage: Boolean
    )
