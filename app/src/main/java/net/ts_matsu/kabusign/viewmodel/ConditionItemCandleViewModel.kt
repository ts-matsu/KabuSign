package net.ts_matsu.kabusign.viewmodel

import android.media.Image
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.model.data.db.entity.CandleConditionEntity
import net.ts_matsu.kabusign.model.data.db.entity.PriceDesignationEntity
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp

class ConditionItemCandleViewModel(_code: String): ViewModel() {
    private val cName = ConditionItemCandleViewModel::class.java.simpleName
    private val code = _code
    private val number = 0    // ローソク足指定のタブ番号（現状は0のみ）
    private val databaseCache = DatabaseCache()
    private lateinit var entity: CandleConditionEntity

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
    private val shapeOffIconTable = mutableListOf<Int>(R.drawable.ic_candle_daiyousen_off, R.drawable.ic_candle_daiinsen_off,
                                                    R.drawable.ic_candle_shouyousen_off, R.drawable.ic_candle_shouinsen_off,
                                                    R.drawable.ic_candle_uwakageyousen_off, R.drawable.ic_candle_uwakageinsen_off,
                                                    R.drawable.ic_candle_shitakageyousen_off, R.drawable.ic_candle_shitakageinsen_off,
                                                    R.drawable.ic_candle_jujisen_off)
    // ローソク足パターンアイコンテーブル
    private val patternIconTable = mutableListOf<Int>(R.drawable.ic_candle_pattern_tsutsumiyousen, R.drawable.ic_candle_pattern_tsutsumiinsen,
                                                    R.drawable.ic_candle_pattern_haramiyousen, R.drawable.ic_candle_pattern_haramiinsen,
                                                    R.drawable.ic_candle_pattern_kabuse, R.drawable.ic_candle_pattern_kirikomi)
    private val patternOffIconTable = mutableListOf<Int>(R.drawable.ic_candle_pattern_tsutsumiyousen_off, R.drawable.ic_candle_pattern_tsutsumiinsen_off,
                                                    R.drawable.ic_candle_pattern_haramiyousen_off, R.drawable.ic_candle_pattern_haramiinsen_off,
                                                    R.drawable.ic_candle_pattern_kabuse_off, R.drawable.ic_candle_pattern_kirikomi_off)
    val enable = MutableLiveData(false)                                  // 条件全体の有効・無効
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

    init {
        // DBから設定を読み出して、表示に反映させる
        viewModelScope.launch {
            runBlocking {
                if(!initialize()){
                    // デフォルト値で設定する
                    entity = CandleConditionEntity(
                        revision = getRecordCount(),
                        number = number,
                        code = code,
                        enable = enable.value!!,
                        mode = candleInputMode.ordinal,
                        shapeKind = shapeSpinnerPosition.value!!,
                        patternKind = patternSpinnerPosition.value!!
                    )
                    insertData(entity)
                }
            }

            // 表示を設定する
            entity?.let {
                CommonInfo.debugInfo("$cName  init: entity setting.")
                CommonInfo.debugInfo("${entity.enable}, ${entity.mode}, ${entity.shapeKind}, ${entity.patternKind}")
                onEnableStatusSelected(entity.enable, dbUpdate = false)
                onRequireInputMode(CandleInputMode.values()[entity.mode], dbUpdate = false)
                onCandleShapeSelected(entity.shapeKind, dbUpdate = false)
                onCandlePatternSelected(entity.patternKind, dbUpdate = false)
            }
        }
    }

    private suspend fun initialize(): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.candleConditionDao()
            val tmpEntity = dao.get(number, code)
            tmpEntity?.let {
                entity = tmpEntity
                result = true
            }
        }
        return result
    }
    private suspend fun getRecordCount(): Int {
        var result = 0
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.candleConditionDao()
            result = dao.count()
        }
        return result
    }
    private suspend fun insertData(data: CandleConditionEntity) {
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.candleConditionDao()
            val result = dao.insert(data)
            CommonInfo.debugInfo("$cName insertData($result)")
        }
    }
    private fun updateData(data: CandleConditionEntity) {
        databaseCache.addCandleConditionEntity(data)
    }

    fun onEnableStatusSelected(_enable: Boolean, dbUpdate: Boolean = true) {
        CommonInfo.debugInfo("$cName onEnableStatusSelected($enable)")
        enable.value = _enable

        // DBキャッシュの値更新
        if(dbUpdate){
            entity.enable = enable.value!!
            updateData(entity)
        }

        // 表示色の更新
        setColorAll(_enable)
    }

    fun onRequireInputMode(mode: CandleInputMode, dbUpdate: Boolean = true){
        CommonInfo.debugInfo("$cName onRequireInputMode: $mode")
        candleInputMode = mode
        isCandleShapeMode.value = true
        if(mode != CandleInputMode.SHAPE_INPUT){
            isCandleShapeMode.value = false
        }
        setColorAll(enable.value!!)

        if(dbUpdate) {
            // DBキャッシュ更新
            entity.mode = candleInputMode.ordinal
            updateData(entity)
        }
    }

    fun onCandleShapeSelected(position: Int, dbUpdate: Boolean) {
        CommonInfo.debugInfo("$cName onCandleShapeSelected($position), ${shapeSpinnerPosition.value}")
        shapeSpinnerPosition.value = position

        // アイコン変更
        if(isCandleShapeMode.value!! && enable.value!!){
            candleShapeIcon.value = shapeIconTable[position]
        }
        else {
            candleShapeIcon.value = shapeOffIconTable[position]
        }

        // DBキャッシュの値更新
        if(dbUpdate){
            entity.shapeKind = position
            updateData(entity)
        }
    }

    fun onCandlePatternSelected(position: Int, dbUpdate: Boolean) {
        CommonInfo.debugInfo("$cName onCandlePatternSelected($position), ${patternSpinnerPosition.value}")
        patternSpinnerPosition.value = position

        // アイコン変更
        if(!isCandleShapeMode.value!! && enable.value!!){
            candlePatternIcon.value = patternIconTable[position]
        }
        else {
            candlePatternIcon.value = patternOffIconTable[position]
        }

        // DBキャッシュの値更新
        if(dbUpdate){
            entity.patternKind = position
            updateData(entity)
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