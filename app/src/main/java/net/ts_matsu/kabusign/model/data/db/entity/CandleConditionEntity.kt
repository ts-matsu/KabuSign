package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_candle_condition")
data class CandleConditionEntity(
    @PrimaryKey @ColumnInfo(name = "revision") val revision: Int,           // リビジョン番号
    @ColumnInfo(name = "number") val number: Int,                           // ローソク足指定番号（現状は 0 固定）
    @ColumnInfo(name = "code") val code: String,                            // 銘柄コード
    @ColumnInfo(name = "enable") var enable: Boolean,                       // 有効・無効
    @ColumnInfo(name = "mode") var mode: Int,                               // 種別　0:ローソク足の形状、1:ローソク足パターン
    @ColumnInfo(name = "shape_kind") var shapeKind: Int,                    // ローソク足の形状の選択種別（0 ～）
    @ColumnInfo(name = "pattern_kind") var patternKind: Int                 // ローソク足パターンの選択種別（0 ～）
)
