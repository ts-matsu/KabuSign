package net.ts_matsu.kabusign.model.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_alarm")
data class AlarmEntity (
    @PrimaryKey @ColumnInfo(name = "revision") val revision: Int,   // リビジョン
    @ColumnInfo(name = "code") val code: String,                    // 銘柄コード
    @ColumnInfo(name = "enable") val enable: Boolean,               // 有効無効
    @ColumnInfo(name = "time") val time: String,                    // 時刻
    @ColumnInfo(name = "repeat") val repeat: Int,                   // リピート（0：無し、1：あり）
    @ColumnInfo(name = "vibration") val vibration: Boolean,         // バイブレーション（false：無し、true：あり）
)