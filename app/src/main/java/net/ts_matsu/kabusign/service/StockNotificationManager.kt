package net.ts_matsu.kabusign.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import net.ts_matsu.kabusign.model.data.KabuSignDatabase
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.viewmodel.CandleInfoViewModel

class StockNotificationManager(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {
    private val cName = StockNotificationManager::class.java.simpleName
    private val context = appContext


    override fun doWork(): Result {

        // ローソク足、移動平均線の設定を、DBから読み出す
        val dao = ResourceApp.database.candleConditionDao()
        val candleList = dao.getAll()
        val enableCandle = mutableListOf<String>()

        // ローソク足設定が有効になっている銘柄を抽出する
        candleList?.let {
            for(d in candleList) {
                if(d.isEnabled){
                    enableCandle.add(d.code)
                }
            }
        }
        // 移動平均設定が有効になっている銘柄を抽出する


        // 対象銘柄の時系列データを取得する


        // 通知する
        val channelId = "channel_id_${inputData.getInt("id", 0)}"
        val channel = NotificationChannel(channelId, "test_notification", NotificationManager.IMPORTANCE_HIGH)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("サイン通知:$channelId")
            .setContentText("XXXX買いサイン")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val notification = builder.build()
        NotificationManagerCompat.from(context)
            .notify(inputData.getInt("id", 0), notification)


        CommonInfo.debugInfo("$cName: doWork(${inputData.getInt("id", 0)})")
        return Result.success()
    }
}