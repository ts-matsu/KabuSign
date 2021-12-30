package net.ts_matsu.kabusign.viewmodel

import android.media.Image
import androidx.lifecycle.MutableLiveData
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.util.CommonInfo

class ConditionItemCandleViewModel(_code: String) {
    private val cName = ConditionItemCandleViewModel::class.java.simpleName
    private val code = _code

    enum class CandleInputMode {
        SHAPE_INPUT,
        PATTERN_INPUT
    }

    // ローソク足アイコンテーブル
    private val shapeIconTable = mutableListOf<Int>(R.drawable.ic_candle_daiyousen, R.drawable.ic_candle_daiinsen,
                                                    R.drawable.ic_candle_shouyousen, R.drawable.ic_candle_shouinsen,
                                                    R.drawable.ic_candle_uwakageyousen, R.drawable.ic_candle_uwakageinsen,
                                                    R.drawable.ic_candle_shitakageyousen, R.drawable.ic_candle_shitakageinsen,
                                                    R.drawable.ic_candle_jujisen)
    // ローソク足パターンアイコンテーブル
    private val patternIconTable = mutableListOf<Int>(R.drawable.ic_candle_pattern_tsutsumiyousen, R.drawable.ic_candle_pattern_tsutsumiinsen,
                                                    R.drawable.ic_candle_pattern_haramiyousen, R.drawable.ic_candle_pattern_haramiinsen,
                                                    R.drawable.ic_candle_pattern_kabuse, R.drawable.ic_candle_pattern_kirikomi)
    val enable = MutableLiveData(true)                                  // 条件全体の有効・無効
    val candleShapeIcon = MutableLiveData(R.drawable.ic_candle_daiyousen)    // ローソク足形状アイコン
    val shapeSpinnerPosition = MutableLiveData(0)                       // ローソク足形状スピナーのポジション
    val candlePatternIcon = MutableLiveData(R.drawable.ic_candle_pattern_tsutsumiyousen)    // ローソク足パターンアイコン
    val patternSpinnerPosition = MutableLiveData(0)                                    // ローソク足パターンスピナーのポジション
    val isCandleShapeMode = MutableLiveData(true)                                      // 入力モード（表示切り替え用）
    var candleInputMode = CandleInputMode.SHAPE_INPUT      // 入力モード（価格指定、基準値差分指定）
    val shapeTitleColor = MutableLiveData(R.color.colorWhite)                               // タイトル系の表示色
    val patternTitleColor = MutableLiveData(R.color.colorWhite)                             // タイトル系の表示色
    val shapeContentsTitleColor = MutableLiveData(R.color.colorWhite)                       // コンテンツタイトル（形状）の表示色
    val patternContentsTitleColor = MutableLiveData(R.color.colorWhite)                     // コンテンツタイトル（パターン）の表示色
    val shapeIconBackgroundColor = MutableLiveData(R.color.colorWhite)                      // アイコンの背景色
    val patternIconBackgroundColor = MutableLiveData(R.color.colorWhite)                    // アイコンの背景色


    fun onEnableStatusSelected(_enable: Boolean, dbUpdate: Boolean = true) {
        CommonInfo.debugInfo("$cName onEnableStatusSelected($enable)")
        enable.value = _enable

        // DBキャッシュの値更新
        if(dbUpdate){
        }

        // 表示色の更新
        setColorAll(_enable)
    }

    fun onRequireInputMode(mode: CandleInputMode, dbUpdate: Boolean = true){
        CommonInfo.debugInfo("$cName onRequireInputMode: $mode")
        if(enable.value!!){
            candleInputMode = mode
            isCandleShapeMode.value = true
            if(mode != CandleInputMode.SHAPE_INPUT){
                isCandleShapeMode.value = false
            }
            setColorByMode(mode)

            if(dbUpdate) {
                // DBキャッシュ更新
            }
        }
    }

    fun onCandleShapeSelected(position: Int, dbUpdate: Boolean) {
        if(enable.value!!) {
            CommonInfo.debugInfo("$cName onReferenceKindSelected($position), ${shapeSpinnerPosition.value}")
            shapeSpinnerPosition.value = position

            // アイコン変更
            candleShapeIcon.value = shapeIconTable[position]

            // DBキャッシュの値更新
            if(dbUpdate){
            }
        }
    }

    fun onCandlePatternSelected(position: Int, dbUpdate: Boolean) {
        if(enable.value!!) {
            CommonInfo.debugInfo("$cName onReferenceKindSelected($position), ${patternSpinnerPosition.value}")
            patternSpinnerPosition.value = position

            // アイコン変更
            candlePatternIcon.value = patternIconTable[position]

            // DBキャッシュの値更新
            if(dbUpdate){
            }
        }
    }

    private fun setColorByMode(mode: CandleInputMode) {
        if(mode == CandleInputMode.SHAPE_INPUT) {
            shapeTitleColor.value = R.color.colorWhite
            shapeContentsTitleColor.value = R.color.colorWhite
            shapeIconBackgroundColor.value = R.color.colorWhite
            patternTitleColor.value = R.color.colorGray
            patternContentsTitleColor.value = R.color.colorBlue2
            patternIconBackgroundColor.value = R.color.colorLightGray
        }
        else {
            shapeTitleColor.value = R.color.colorGray
            shapeContentsTitleColor.value = R.color.colorBlue2
            shapeIconBackgroundColor.value = R.color.colorLightGray
            patternTitleColor.value = R.color.colorWhite
            patternContentsTitleColor.value = R.color.colorWhite
            patternIconBackgroundColor.value = R.color.colorWhite
        }
    }

    private fun setColorAll(enable: Boolean) {
        if(!enable) {
            shapeTitleColor.value = R.color.colorGray
            shapeContentsTitleColor.value = R.color.colorBlue2
            shapeIconBackgroundColor.value = R.color.colorLightGray
            patternTitleColor.value = R.color.colorGray
            patternContentsTitleColor.value = R.color.colorBlue2
            patternIconBackgroundColor.value = R.color.colorLightGray
        }
        else {
            setColorByMode(candleInputMode)
        }
    }

}