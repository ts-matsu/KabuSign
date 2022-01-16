package net.ts_matsu.kabusign.viewmodel.library

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.ts_matsu.kabusign.util.ResourceApp
import kotlin.math.abs
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation

//
class PriceInputViewModel(_isDot: Boolean=false, _isMinus: Boolean=false): ViewModel() {
    private val cName = ConditionItemPriceDesignation::class.java.simpleName

    private val isDotShow = _isDot
    private val isMinusShow = _isMinus
    private var isMinusPrice = false
    private var isDotPrice = false

    val requireClose = MutableLiveData(false)   // OK がタップされて、ダイアログを閉じることを通知

    // 表示価格
    private val MAX_PRICE_LENGTH = 6
    private val MAX_DECIMAL_LENGTH = 1
    private val MAX_RATIO = 100
    private val MIN_PRICE = 0
    var designationPriceInt = 0
    var designationPriceFloat = 0f
    val designationPrice = MutableLiveData("")
    val minusTextColor = MutableLiveData<Int>(R.color.colorBlack)
    val dotTextColor = MutableLiveData(R.color.colorBlack)

    init {
        if(!isMinusShow) {
            minusTextColor.value = R.color.colorLightGray
        }
        if(!isDotShow) {
            dotTextColor.value = R.color.colorLightGray
        }
    }

    fun convertFloatValue(): Float {
        var resultString = "0"
        designationPrice.value?.let {
            if(it.isNotBlank()){
                resultString = it.replace(",", "")
                if(resultString.contains('.')){
                    if(resultString.last() == '.') {
                        resultString += '0'
                    }
                }
            }
        }
        return resultString.toFloat()
    }
    // 整数側の桁数取得
    private fun getLength(): Int {
        var result = 0
        designationPrice.value?.let {
            if (it.isNotBlank()) {
                result = it.replace("-", "").replace(",", "").length
                if (it.contains('.')) {
                    result = it.replace("-", "").replace(",", "").split('.')[0].length
                }
            }
        }
        return result
    }
    // 少数側の桁数取得
    private fun getDecimalLength(): Int {
        var result = 0
        designationPrice.value?.let {
            if (it.isNotBlank()) {
                if (it.contains('.') && (it.last() != '.')) {
                    result = it.split('.')[1].length
                }
            }
        }
        return result
    }
    fun setValue(value: String) {
        CommonInfo.debugInfo("$cName: setValue($value)")
        designationPrice.value?.let {
            designationPrice.value = value
        }
    }
    private fun addValue(value: String) {
        designationPrice.value?.let {
            designationPrice.value += value
        }
        setDigitSeparator()
    }
    private fun addValueToTop(value: String) {
        designationPrice.value?.let {
            var tmp = value + designationPrice.value
            designationPrice.value = tmp
        }
    }
    private fun setDigitSeparator(){
        designationPrice.value?.let {
            if(it.isNotBlank() && !it.contains(".") && (it != "-")){
                val tmp = it.replace(",", "")
                var tmpInt = tmp.toInt()
                if(Math.abs(tmpInt) >= 1000) {
                    designationPrice.value =  "%,d".format(tmpInt)
                }
                else {
                    designationPrice.value =  "%d".format(tmpInt)
                }
            }
            else {
                // 小数点を含む場合は、100未満のため、桁区切り入れない
            }
        }
    }
    private fun getLastValue(): String {
        var result = ""
        designationPrice.value?.let {
            if(it.isNotBlank()){    // BlankもしくはEmptyでlast().toString()を呼ぶと落ちる
                result = it.last().toString()
            }
        }
        return result
    }
    fun delValue() {
        designationPrice.value?.let {
            setValue(it.dropLast(1))
        }
        setDigitSeparator()
    }

    // 数値ボタンクリック
    fun onPriceValueClick(value: Int) {
        if((getLength() < MAX_PRICE_LENGTH) && (getDecimalLength() < MAX_DECIMAL_LENGTH)) {
            // 整数側の桁数がMAX未満、かつ少数側の桁数が1未満の場合のみ値をセットできる
            if(designationPrice.value == "0") {
                // 0の場合は、"01"とかにならないよう、そのまま設定する
                setValue(value.toString())
            }
            else {
                if(isDotShow &&
                    (getDecimalLength() == 0) && (getLastValue() != ".") &&
                    (getLength() >= 2)){
                    // 小数点ありの場合(比率）は、MAX値が100までなので、入力できない
                }
                else {
                    addValue(value.toString())
                }
            }
        }
    }

    // クリアボタンクリック
    fun onClearClick(){
        setValue("")
    }

    // Delete(Back)ボタンクリック
    fun onDeleteClick(){
        delValue()
    }

    // 小数点ボタンクリック
    fun onDotClick(){
        if(isDotShow) {
            designationPrice.value?.let {
                if(!it.contains('.')){
                    addValue(".")
                }
            }
        }
    }

    // マイナスボタンクリック
    fun onMinusClick(){
        if(isMinusShow){
            designationPrice.value?.let {
                if(!it.contains('-')){
                    if(designationPrice.value == "0" || designationPrice.value == "0.0") {
                        // 0の場合は、"01"とかにならないよう、そのまま設定する
                        setValue("-")
                    }
                    else {
                        addValueToTop("-")
                    }
                }
            }
        }
    }

    // OKボタンクリック
    fun onOkClick(){
        // マイナスのみの場合は、空に変換する
        if(designationPrice.value == "-") {
            designationPrice.value = ""
        }
        // ダイアログ終了
        // Fragment側に通知して、設定されている値(designationPriceInt)を、
        // 呼び出し元のFragmentに通知する
        requireClose.value = true
    }
}