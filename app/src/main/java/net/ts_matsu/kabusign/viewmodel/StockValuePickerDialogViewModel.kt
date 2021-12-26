package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.StockValuePickerDialogFragment

class StockValuePickerDialogViewModel: ViewModel() {
    private val cName = StockValuePickerDialogViewModel::class.java.simpleName

    val npValue1 = MutableLiveData<Int>(0)  // 1の位
    val npValue2 = MutableLiveData<Int>(0)  // 10の位
    val npValue3 = MutableLiveData<Int>(0)  // 100の位
    val npValue4 = MutableLiveData<Int>(0)  // 1000の位
    val npValue5 = MutableLiveData<Int>(0)  // 10000の位

    var stockValue = 0

    private val _requireCancel = MutableLiveData<Boolean>(false)
    val requireCancel: LiveData<Boolean> get() = _requireCancel

    private val _requireOk = MutableLiveData<Boolean>(false)
    val requireOk: LiveData<Boolean> get() = _requireOk

    fun setDefaultValue(value: String){
        val default = value.toIntOrNull()
        if(default != null){
            npValue1.value = default % 10
            npValue2.value = (default / 10) % 10
            npValue3.value = (default / 100) % 10
            npValue4.value = (default / 1000) % 10
            npValue5.value = default / 10000
        }
    }
    // CANCELボタン処理
    fun onNegativeButtonClick() {
        _requireCancel.value = true
    }

    // OKボタン処理
    fun onPositiveButtonClick() {
        // NumberPickerの値を変換する
        stockValue = npValue1.value!! +
                    npValue2.value!!*10 +
                    npValue3.value!!*100 +
                    npValue4.value!!*1000 +
                    npValue5.value!!*10000
        CommonInfo.debugInfo("$cName onPositiveButtonClick($stockValue): ${npValue5.value!!}${npValue4.value!!}${npValue3.value!!}${npValue2.value!!}${npValue1.value!!}")

        _requireOk.value = true
    }

}