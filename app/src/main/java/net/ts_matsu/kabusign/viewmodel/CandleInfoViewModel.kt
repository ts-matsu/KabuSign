package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp

class CandleInfoViewModel: ViewModel() {
    private val cName = CandleInfoViewModel::class.java.simpleName

    private var stockCode: String = ""
    private var inputMode: String = "open"

    val candleBodyInfo = MutableLiveData(0)     // ローソク足の形状
    val candleShadowsInfo = MutableLiveData(0)  // ローソク足のヒゲ

    val openValue = MutableLiveData("")         // 始値の価格
    val closeValue = MutableLiveData("")        // 終値の価格
    val highValue = MutableLiveData("")         // 高値の価格
    val lowValue = MutableLiveData("")          // 安値の価格

    val openCondition = MutableLiveData(0)      // 始値の条件
    val closeCondition = MutableLiveData(0)     // 終値の条件
    val highCondition = MutableLiveData(0)      // 高値の条件
    val lowCondition = MutableLiveData(0)       // 安値の条件

    val requireClose = MutableLiveData(false)   // OK/CANCEL がタップされて、ダイアログを閉じることを通知

    private val _valueClick = MutableLiveData(false)
    val valueClick: LiveData<Boolean> get() = _valueClick
    var defaultValue = "0"

    private val _isEnable = MutableLiveData(true)
    val isEnable: LiveData<Boolean> get() = _isEnable

    init {
        CommonInfo.debugInfo("$cName  init")
    }

    fun setStockCode(code: String){
        stockCode = code
        CommonInfo.debugInfo("$cName  setStockCode($stockCode)")

        // 設定情報を残っていれば、読み出して設定する
        var enable = false
        viewModelScope.launch {
            enable = setInfo(stockCode)
            _isEnable.value = enable
        }
    }

    private suspend fun setInfo(code: String): Boolean {
        var enable = false
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.candleConditionDao()
            val entity = dao.get(stockCode)
            enable = postData(entity)
        }
        return enable
    }

    fun setStockValue(value: Int) {
        CommonInfo.debugInfo("$cName: setStockValue($value)")
        when(inputMode){
            "open" -> openValue.value = value.toString()
            "close" -> closeValue.value = value.toString()
            "high" -> highValue.value = value.toString()
            "low" -> lowValue.value = value.toString()
        }
    }

    // StockValuePickerDialogで入力中の株価設定
    fun setInputOpen() {inputMode = "open"}
    fun setInputClose() {inputMode = "close"}
    fun setInputHigh() {inputMode = "high"}
    fun setInputLow() {inputMode = "low"}

    // 有効スイッチ
    fun onCheckedChange(isChecked: Boolean){
        _isEnable.value = isChecked
    }

    // CANCELボタン処理
    fun onNegativeButtonClick() {
        requireClose.value = true
    }
    // OKボタン処理
    fun onPositiveButtonClick() {
        CommonInfo.debugInfo("$cName  Code:${stockCode}, 始値:${openValue.value}, 終値:${closeValue.value}, 高値:${highValue.value}, 安値:${lowValue.value}")
        CommonInfo.debugInfo("$cName  ローソク足[${candleBodyInfo.value}]")

        if(stockCode.isNotEmpty()){
            // 設定情報をDBに保存する
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    val dao = ResourceApp.database.candleConditionDao()
                    CommonInfo.debugInfo("$cName  ローソク足2[${candleBodyInfo.value}]")

                    val entity = CandleConditionEntity(
                        stockCode, _isEnable.value!!,
                        candleBodyInfo.value!!, candleShadowsInfo.value!!,
                        openValue.value!!, closeValue.value!!, highValue.value!!, lowValue.value!!,
                        openCondition.value!!, closeCondition.value!!, highCondition.value!!, lowCondition.value!!
                    )
                    dao.insert(entity)
                    CommonInfo.debugInfo("${dao.getAll()}")
                }
            }
        }
        requireClose.value = true
    }

    // 始値クリック
    fun setOnOpenValueClick() {
        if(_isEnable.value!!){
            inputMode = "open"
            defaultValue = openValue.value!!
            _valueClick.value = true
        }
    }
    // 終値クリック
    fun setOnCloseValueClick() {
        if(_isEnable.value!!){
            inputMode = "close"
            defaultValue = closeValue.value!!
            _valueClick.value = true
        }
    }
    // 高値クリック
    fun setOnHighValueClick() {
        if(_isEnable.value!!){
            inputMode = "high"
            defaultValue = highValue.value!!
            _valueClick.value = true
        }
    }
    // 安値クリック
    fun setOnLowValueClick() {
        if(_isEnable.value!!){
            inputMode = "low"
            defaultValue = lowValue.value!!
            _valueClick.value = true
        }
    }

    // ローソク足情報を設定する
    private fun postData(entity: CandleConditionEntity?): Boolean {
        var enable = false
        entity?.let {
            CommonInfo.debugInfo("$cName  postValue() ${it.candleBody}")
//            if(it.isEnabled){
                enable = it.isEnabled
                candleBodyInfo.postValue(it.candleBody)
                candleShadowsInfo.postValue(it.candleShadows)

                openValue.postValue(it.openValue)
                closeValue.postValue(it.closeValue)
                highValue.postValue(it.highValue)
                lowValue.postValue(it.lowValue)

                openCondition.postValue(it.openCondition)
                closeCondition.postValue(it.closeCondition)
                highCondition.postValue(it.highCondition)
                lowCondition.postValue(it.lowCondition)
//            }
        }
        return enable
    }
}