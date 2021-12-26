package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.ts_matsu.kabusign.view.AdapterInfo
import net.ts_matsu.kabusign.view.ConditionItemCandle
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation

class ConditionMainViewModel(): ViewModel() {
    private val cName = ConditionMainViewModel::class.java.simpleName

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、フラグメントを閉じることを通知
    val requireOk = MutableLiveData(false)      // OKがタップされて、フラグメントを閉じることを通知

    val adapterList = mutableListOf<AdapterInfo>()

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        requireClose.value = true
    }

    // アダプタを設定する
    fun createAdapter(code: String) {
        adapterList.add(AdapterInfo("価格指定1", ConditionItemPriceDesignation(0, code)))
        adapterList.add(AdapterInfo("価格指定2", ConditionItemPriceDesignation(1, code)))
        adapterList.add(AdapterInfo("ローソク足", ConditionItemCandle()))
    }

    init {
        // DB を読み出して、サポートしている指定種別を読み出し、Adapterに反映させる todo
    }
}