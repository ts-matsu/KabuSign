package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_alarm")
data class AlarmEntity (
    @PrimaryKey @ColumnInfo(name = "revision") val revision: Int,   // リビジョン
    @ColumnInfo(name = "code") val code: String,                    // 銘柄コード
    @ColumnInfo(name = "enable") val enable: Boolean,               // 有効無効
    @ColumnInfo(name = "mode") val mode: Int,                       // 期間モード（0：当日、1：翌日、2：一週間、3：期間指摘）
    @ColumnInfo(name = "start") val start: String,                  // 開始日時
    @ColumnInfo(name = "end") val end: String,                      // 終了日時
    @ColumnInfo(name = "vibration") val vibration: Boolean,         // バイブレーション（false：無し、true：あり）
)