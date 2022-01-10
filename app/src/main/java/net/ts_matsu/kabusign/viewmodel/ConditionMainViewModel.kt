package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.view.AdapterInfo
import net.ts_matsu.kabusign.view.ConditionItemCandle
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation

class ConditionMainViewModel(): ViewModel() {
    private val cName = ConditionMainViewModel::class.java.simpleName

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、フラグメントを閉じることを通知
    val requireOk = MutableLiveData(false)      // OKがタップされて、フラグメントを閉じることを通知

    val adapterList = mutableListOf<AdapterInfo>()

    // CANCELボタン処理
    fun onCancelButtonClick() {
        requireClose.value = true
    }

    // OKボタン処理
    fun onOkButtonClick() {
        viewModelScope.launch {
            updateData()
        }
        requireOk.value = true
    }

    // DB 保存処理
    private suspend fun updateData() {
        withContext(Dispatchers.IO) {
            val databaseCache = DatabaseCache()
            // 価格指定データを更新
            for(entity in databaseCache.getPriceDesignationEntityList()) {
                val dao = ResourceApp.database.priceDesignationDao()
                dao.update(entity)
            }
            databaseCache.clearPriceDesignationEntityList()

            // ローソク足指定データを更新
            for(entity in databaseCache.getCandleConditionEntityList()) {
                val dao = ResourceApp.database.candleConditionDao()
                dao.update(entity)
            }
            databaseCache.clearCandleConditionEntityList()
        }
    }

    // アダプタを設定する
    fun createAdapter(code: String) {
        adapterList.add(AdapterInfo("価格指定1", ConditionItemPriceDesignation(0, code)))
        adapterList.add(AdapterInfo("価格指定2", ConditionItemPriceDesignation(1, code)))
        adapterList.add(AdapterInfo("ローソク足", ConditionItemCandle(code)))
    }

    init {
        // DB を読み出して、サポートしている指定種別を読み出し、Adapterに反映させる todo
    }
}