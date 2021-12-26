package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_price_designation")
data class PriceDesignationEntity (
    @PrimaryKey @ColumnInfo(name = "revision") val revision: Int,           // リビジョン番号
    @ColumnInfo(name = "number") val number: Int,                           // 価格指定番号（0 or 1）
    @ColumnInfo(name = "code") val code: String,                            // 銘柄コード
    @ColumnInfo(name = "enable") var enable: Boolean,                       // 有効・無効
    @ColumnInfo(name = "mode") var mode: Int,                               // 価格指定種別　0:価格、1:差分
    @ColumnInfo(name = "designation_price") var designationPrice: Int,      // 指定価格
    @ColumnInfo(name = "reference_kind") var referenceKind: Int,            // 差分指定の基準値種別
    @ColumnInfo(name = "difference_kind") var differenceKind: Int,          // 差分種別　0:価格、1:比率
    @ColumnInfo(name = "difference_price") var differencePrice: Int,        // 差分価格
    @ColumnInfo(name = "difference_ratio") var differenceRatio: Float,      // 差分比率
    )
