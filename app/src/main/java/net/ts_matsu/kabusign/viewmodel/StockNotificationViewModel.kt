package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import net.ts_matsu.kabusign.service.StockNotificationManager
import net.ts_matsu.kabusign.util.ResourceApp
import java.util.concurrent.TimeUnit

class StockNotificationViewModel: ViewModel() {

    val requireClose = MutableLiveData(false)   // OK/CANCEL // がタップされて、ダイアログを閉じることを通知

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        // Dialogを閉じる
        requireClose.value = true
    }

    // OKボタン処理
    fun onPositiveButtonClick() {
        // WorkManager再起動
        val workRequest1 = PeriodicWorkRequestBuilder<StockNotificationManager>(24, TimeUnit.HOURS)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(workDataOf("id" to 1))
            .build()
        WorkManager.getInstance(ResourceApp.instance).enqueue(workRequest1)

        val workRequest2 = PeriodicWorkRequestBuilder<StockNotificationManager>(24, TimeUnit.HOURS)
            .setInitialDelay(15, TimeUnit.SECONDS)
            .setInputData(workDataOf("id" to 2))
            .build()
        WorkManager.getInstance(ResourceApp.instance).enqueue(workRequest2)

        // Dialogを閉じる
        requireClose.value = true
    }

}