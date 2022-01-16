package net.ts_matsu.kabusign.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.model.data.db.entity.PriceDesignationEntity
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.view.ConditionItemPriceDesignation

class ConditionItemPriceDesignationViewModel(_number: Int, _code: String): ViewModel() {
    private val cName = ConditionItemPriceDesignationViewModel::class.java.simpleName
    private val code = _code        // 銘柄コード
    private val number = _number    // 価格指定番号
    private val databaseCache = DatabaseCache()
    private lateinit var entity: PriceDesignationEntity

    enum class DesignationMode {
        PRICE_DESIGNATION,
        REFERENCE_DESIGNATION
    }
    enum class InputDialogMode {
        PRICE_INPUT,
        RATIO_INPUT
    }
    enum class ReferenceInputMode {
        REFERENCE_PRICE,
        REFERENCE_RATIO
    }

    var designationMode = DesignationMode.PRICE_DESIGNATION      // 入力モード（価格指定、基準値差分指定）
    var referenceInputMode = ReferenceInputMode.REFERENCE_PRICE  // 基準値差分指定のモード（価格/比率）
    var inputDialogMode = InputDialogMode.PRICE_INPUT                        // 基準値入力種別（価格、比率）

    val enable = MutableLiveData(false)                     // 条件全体の有効・無効
    val isPriceDesignationMode = MutableLiveData(true)      // 入力モード（表示切り替え用）
    val requireInputDialog = MutableLiveData(false)         // OK がタップされて、入力ダイアログを閉じることを通知
    val informationMessage = MutableLiveData("")            // 下段のインフォメーションメッセージ
    val kindSpinnerPosition = MutableLiveData(0)            // 基準値スピナーのポジション（設定値）

    val rbPriceDesignationColor = MutableLiveData(R.color.colorWhite)       // "価格指定"の表示色
    val rbReferenceDesignationColor = MutableLiveData(R.color.colorWhite)   // "基準値差分指定"の表示色
    val tvLabelDesignationPriceColor = MutableLiveData(R.color.colorWhite)  // 価格指定の"価格"の表示色
    val tvDesignationPriceColor = MutableLiveData(R.color.colorBlack)       // 価格指定の価格の表示色
    val tvReferenceLabelColor = MutableLiveData(R.color.colorWhite)         // 基準値差分指定の各ラベルの表示色
    val tvTargetPriceColor = MutableLiveData(R.color.colorBlack)            // 基準値差分指定の"価格"の値の表示色
    val tvTargetRatioColor = MutableLiveData(R.color.colorBlack)            // 基準値差分指定の"比率"の値の表示色
    val referenceInfoIcon = MutableLiveData(R.drawable.ic_baseline_info_20) // 基準値差分指定のメッセージアイコン
    val tvReferenceInfoColor = MutableLiveData(R.color.colorBlack)          // 基準値差分指定のメッセージの表示色

    val designationPrice = MutableLiveData("0")             // 価格指定の価格値
    val targetPrice = MutableLiveData("0")                  // 基準値差分指定の価格値
    val targetRatio = MutableLiveData("0")                  // 基準値差分指定の比率値
    val isTargetPriceMode = MutableLiveData(true)           // 基準値差分指定の価格か比率か

    // 基準値差分指定で、価格が入力可能か、比率が入力可能か
    val enableTargetPriceInput = MutableLiveData(!isPriceDesignationMode.value!! && isTargetPriceMode.value!!)
    val enableTargetRatioInput = MutableLiveData(!isPriceDesignationMode.value!! && !isTargetPriceMode.value!!)

    init {
        CommonInfo.debugInfo("$cName: init start")

        // DBから設定を読み出して、表示に反映させる
        viewModelScope.launch {
            runBlocking {
                if(!initialize()) {
                    CommonInfo.debugInfo("$cName init number:$number code:$code")
                    // デフォルト値で設定する
                    entity = PriceDesignationEntity(
                        revision = getRecordCount(),
                        number = number,
                        code = code,
                        enable = false,
                        mode = DesignationMode.PRICE_DESIGNATION.ordinal,
                        designationPrice = 0,
                        referenceKind = 0,
                        differenceKind = ReferenceInputMode.REFERENCE_PRICE.ordinal,
                        differencePrice = 0,
                        differenceRatio = 0f
                    )
                    insertData(entity)
                }
            }
            // 表示値を設定する
            entity?.let {
                onEnableStatusSelected(entity.enable, dbUpdate = false)
                onRequireInputMode(DesignationMode.values()[entity.mode], dbUpdate = false)
                CommonInfo.debugInfo("$cName init kind:${it.referenceKind}")
                setPrice(it.designationPrice.toString(), dbUpdate = false)
                onRequireTargetModeChange(it.differenceKind, dbUpdate = false)
                setReferencePrice(it.differencePrice.toString(), dbUpdate = false)
                setReferenceRatio(it.differenceRatio.toString(), dbUpdate = false)
                onReferenceKindSelected(it.referenceKind, false)
            }
            // インフォメーションメッセージ更新
            changeInformationMessage()

            CommonInfo.debugInfo("$cName: init end")
        }
    }

    private suspend fun initialize(): Boolean {
        var result = false
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.priceDesignationDao()
            CommonInfo.debugInfo("$cName initialize($number, $code)")
            val _entity = dao.get(number, code)
            _entity?.let {
                CommonInfo.debugInfo("$cName initialize2($number, $code)")
                entity = _entity
                result = true
            }
        }
        return result
    }
    private suspend fun getRecordCount(): Int {
        var result = 0
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.priceDesignationDao()
            result = dao.count()
        }
        return result
    }
    private suspend fun insertData(data: PriceDesignationEntity) {
        withContext(Dispatchers.IO) {
            val dao = ResourceApp.database.priceDesignationDao()
            val result = dao.insert(data)
            CommonInfo.debugInfo("$cName insertData($result)")
        }
    }
    private fun updateData(data: PriceDesignationEntity) {
        databaseCache.addPriceDesignationEntity(data)
    }

    fun onRequireInputMode(mode: DesignationMode, dbUpdate: Boolean = true){
        designationMode = mode
        isPriceDesignationMode.value = false
        if(designationMode == DesignationMode.PRICE_DESIGNATION){
            isPriceDesignationMode.value = true
        }
        setColorAll(enable.value!!)

        if(dbUpdate) {
            // DBキャッシュ更新
            entity.mode = mode.ordinal
            updateData(entity)
        }
    }
    fun onRequireInputDialogMode(mode: InputDialogMode){
        if(enable.value!!) {
            inputDialogMode = mode
            requireInputDialog.value = true
        }
    }
    fun clearInputDialog(){
        requireInputDialog.value = false
    }

    private fun setColorByMode(isPriceDesignation: Boolean) {
        if(isPriceDesignation) {
            rbPriceDesignationColor.value = R.color.colorWhite
            tvLabelDesignationPriceColor.value = R.color.colorWhite
            tvDesignationPriceColor.value = R.color.colorBlack
            rbReferenceDesignationColor.value = R.color.colorGray
            tvReferenceLabelColor.value = R.color.colorBlue2
            tvTargetPriceColor.value = R.color.colorLightGray
            tvTargetRatioColor.value = R.color.colorLightGray
            referenceInfoIcon.value = R.drawable.ic_baseline_info_off_20
            tvReferenceInfoColor.value = R.color.colorLightGray
        }
        else {
            rbPriceDesignationColor.value = R.color.colorGray
            tvLabelDesignationPriceColor.value = R.color.colorBlue2
            tvDesignationPriceColor.value = R.color.colorLightGray
            rbReferenceDesignationColor.value = R.color.colorWhite
            tvReferenceLabelColor.value = R.color.colorWhite
            referenceInfoIcon.value = R.drawable.ic_baseline_info_20
            tvReferenceInfoColor.value = R.color.colorBlack
            onRequireTargetModeChange(referenceInputMode, false)
        }
        enableTargetPriceInput.value = !isPriceDesignationMode.value!! && isTargetPriceMode.value!!
        enableTargetRatioInput.value = !isPriceDesignationMode.value!! && !isTargetPriceMode.value!!
    }

    private fun setColorAll(enable: Boolean) {
        if(!enable) {
            rbPriceDesignationColor.value = R.color.colorGray
            tvLabelDesignationPriceColor.value = R.color.colorBlue2
            tvDesignationPriceColor.value = R.color.colorLightGray
            rbReferenceDesignationColor.value = R.color.colorGray
            tvReferenceLabelColor.value = R.color.colorBlue2
            tvTargetPriceColor.value = R.color.colorLightGray
            tvTargetRatioColor.value = R.color.colorLightGray
            referenceInfoIcon.value = R.drawable.ic_baseline_info_off_20
            tvReferenceInfoColor.value = R.color.colorLightGray
        }
        else {
            setColorByMode(isPriceDesignationMode.value!!)
        }
    }

    // 価格指定モード時の価格
    fun setPrice(value: String?, dbUpdate: Boolean = true) {
        value?.let {
            designationPrice.value = "%,d".format(value.toFloat().toInt())

            if(dbUpdate) {
                // DBキャッシュに値を保存
                entity.designationPrice = value.toFloat().toInt()
                updateData(entity)
            }
        }
    }
    fun getPrice(): String {
        var result = ""
        designationPrice.value?.let {
            if(it.isNotBlank()){
                result = it!!.replace(",", "")      // 桁区切りを除去
            }
        }
        return result
    }

    // 基準値差分指定時の価格、比率
    fun onRequireTargetModeChange(mode: ReferenceInputMode, dbUpdate: Boolean = true) {
        tvTargetPriceColor.value = R.color.colorLightGray
        tvTargetRatioColor.value = R.color.colorLightGray
        referenceInputMode = mode
        if(mode == ReferenceInputMode.REFERENCE_PRICE) {
            isTargetPriceMode.value = true
            enableTargetPriceInput.value = !isPriceDesignationMode.value!! && isTargetPriceMode.value!!
            enableTargetRatioInput.value = !isPriceDesignationMode.value!! && !isTargetPriceMode.value!!
            if(enable.value!! && designationMode == DesignationMode.REFERENCE_DESIGNATION){
                tvTargetPriceColor.value = R.color.colorBlack
            }
        }
        else {
            isTargetPriceMode.value = false
            enableTargetPriceInput.value = !isPriceDesignationMode.value!! && isTargetPriceMode.value!!
            enableTargetRatioInput.value = !isPriceDesignationMode.value!! && !isTargetPriceMode.value!!
            if(enable.value!! && designationMode == DesignationMode.REFERENCE_DESIGNATION){
                tvTargetRatioColor.value = R.color.colorBlack
            }
        }

        if(dbUpdate) {
            // DBキャッシュに値を保存
            entity.differenceKind = mode.ordinal
            updateData(entity)
        }
        // インフォメーションメッセージ更新
        changeInformationMessage()
    }
    fun onRequireTargetModeChange(mode: Int, dbUpdate: Boolean = true) {
        var referenceInputMode = ReferenceInputMode.REFERENCE_PRICE
        if(mode != referenceInputMode.ordinal) {
            referenceInputMode = ReferenceInputMode.REFERENCE_RATIO
        }
        onRequireTargetModeChange(referenceInputMode, dbUpdate)
    }

    fun setReferencePrice(value: String?, dbUpdate: Boolean = true) {
        value?.let {
            targetPrice.value = "%,d".format(it.toFloat().toInt())

            if(dbUpdate) {
                // DBキャッシュに値を保存
                entity.differencePrice = it.toFloat().toInt()
                updateData(entity)
            }
            // インフォメーションメッセージ更新
            changeInformationMessage()
        }
    }
    fun getReferencePrice(): String {
        var result = ""
        targetPrice.value?.let {
            if(it.isNotBlank()){
                result = it
            }
        }
        return result
    }
    fun setReferenceRatio(value: String?, dbUpdate: Boolean = true) {
        value?.let {
            targetRatio.value = it

            if(dbUpdate) {
                // DBキャッシュに値を保存
                entity.differenceRatio = it.toFloat()
                updateData(entity)
            }
            // インフォメーションメッセージ更新
            changeInformationMessage()
        }
    }
    fun getReferenceRatio(): String {
        var result = ""
        targetRatio.value?.let {
            if(it.isNotBlank()) {
                result = it
                if(result == "0.0"){
                    // 0.0 の場合は、あえて0にする
                    // 入力ダイアログの制約
                    result = "0"
                }
            }
        }
        return result
    }
    fun onReferenceKindSelected(position: Int, dbUpdate: Boolean) {
        CommonInfo.debugInfo("$cName onReferenceKindSelected($position), ${kindSpinnerPosition.value}")
        kindSpinnerPosition.value = position

        // DBキャッシュの値更新
        if(dbUpdate){
            entity.referenceKind = position
            CommonInfo.debugInfo("$cName onReferenceKindSelected(${entity.referenceKind})")
            updateData(entity)
        }
        // インフォメーションメッセージ更新
        changeInformationMessage()
    }

    fun onEnableStatusSelected(_enable: Boolean, dbUpdate: Boolean = true) {
        CommonInfo.debugInfo("$cName onEnableStatusSelected($enable)")
        enable.value = _enable

        // DBキャッシュの値更新
        if(dbUpdate){
            entity.enable = _enable
            updateData(entity)
        }

        // 表示色の更新
        setColorAll(_enable)
    }
    private fun changeInformationMessage() {
        val differenceKind = entity.differenceKind
        var valueMessage = ""
        if(differenceKind == ReferenceInputMode.REFERENCE_PRICE.ordinal) {
            if(entity.differencePrice >= 0) {
                valueMessage = "+" + entity.differencePrice.toString() + " 円"
            }
            else {
                valueMessage = entity.differencePrice.toString() + " 円"
            }
        }
        else {
            if(entity.differenceRatio >= 0) {
                valueMessage = "+" + entity.differenceRatio.toString() + " ％"
            }
            else {
                valueMessage = entity.differenceRatio.toString() + " ％"
            }
        }
        var preferenceMessage = "「" +
                ResourceApp.instance.resources.getStringArray(R.array.reference_kind)[entity.referenceKind] +
                "」"

        // 基準値差分のインフォメーションメッセージ
        informationMessage.value =
                    preferenceMessage +
                    ResourceApp.instance.getString(R.string.price_designation_message_prefix) +
                    " " + valueMessage + " " +
                    ResourceApp.instance.getString(R.string.price_designation_message_suffix)
    }
}