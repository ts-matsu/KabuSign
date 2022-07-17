package net.ts_matsu.kabusign.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.model.StockDataManager
import net.ts_matsu.kabusign.model.StockFile
import net.ts_matsu.kabusign.model.StockTodayData
import net.ts_matsu.kabusign.model.data.DatabaseCache
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.ResourceApp
import net.ts_matsu.kabusign.util.StockInfo
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class ConditionAlarmViewModel() : ViewModel() {
    private val cName = ConditionAlarmViewModel::class.java.simpleName

    private var code = ""
    private var name = ""
    private val stockList: List<StockInfo>
    private lateinit var stockTodayData: StockTodayData

    val requireClose = MutableLiveData(false)   // CANCELがタップされて、フラグメントを閉じることを通知
    val requireOk = MutableLiveData(false)      // OKがタップされて、フラグメントを閉じることを通知
    val mainCode = MutableLiveData("")          // 最上部の銘柄コード表示
    val mainTitle = MutableLiveData("")         // 最上部の銘柄名表示
    val todayDate = MutableLiveData("")         // 日付
    val todayTime = MutableLiveData("")         // 時刻
    val todayValue = MutableLiveData("")        // 本日(現在)の株価
    val todayDiffValue = MutableLiveData("")    // 前日比(価格)
    val todayDiffRatio = MutableLiveData("")    // 前日比(比率)

    val tvCodeColor = MutableLiveData(R.color.colorBlack)
    val tvTitleColor = MutableLiveData(R.color.colorBlack)
    val tvTimeColor = MutableLiveData(R.color.colorBlack)
    val tvValueColor = MutableLiveData(R.color.colorRed)

    // チャート更新時のプログレスバー表示（LiveDataはこの形にすべきなのかな？？）
    private val _isUpdateProgress = MutableLiveData<Boolean>(false)
    val isUpdateProgress: LiveData<Boolean> get() = _isUpdateProgress

    // 本日の日付（通知設定）
    private var alarmDateToday = ""
    private val _alarmDateTodayString = MutableLiveData<String>("")
    val alarmDateTodayString: LiveData<String> get() = _alarmDateTodayString
    // 明日の日付（通知設定）
    private var alarmDateTomorrow = ""
    private val _alarmDateTomorrowString = MutableLiveData<String>("")
    val alarmDateTomorrowString: LiveData<String> get() = _alarmDateTomorrowString
    // 一週間の日付（通知設定）
    private var alarmDateOneWeekEnd = ""
    private val _alarmDateOneWeekString = MutableLiveData<String>("")
    val alarmDateOneWeekString: LiveData<String> get() = _alarmDateOneWeekString
    // 期間指定の日付
    private var alarmDatePeriodStart = ""
    private var alarmDatePeriodEnd = ""
    private val _alarmDatePeriodStart = MutableLiveData<String>("")
    val alarmDatePeriodStartString: LiveData<String> get() = _alarmDatePeriodStart
    private val _alarmDatePeriodEnd = MutableLiveData<String>("")
    val alarmDatePeriodEndString: LiveData<String> get() = _alarmDatePeriodEnd

    // 一週間の日付選択時のダイアログ表示
    val requireOneWeekDate = MutableLiveData(false)
    // 期間指定の日付選択時のダイアログ表示
    val requirePeriodStartDate = MutableLiveData(false)
    val requirePeriodEndDate = MutableLiveData(false)
    val requirePeriodDateToast = MutableLiveData(false)

    init {
        // 銘柄リスト読み込み
        val stockFile = StockFile()
        stockList = stockFile.getSupportedStockList()

        val df = SimpleDateFormat("yyyyMMdd")
        val date = Date()
        alarmDateToday = df.format(date)
        _alarmDateTodayString.value = " " + ResourceApp.instance.getString(R.string.label_today) +
                "　" + alarmDateToday.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateToday.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateToday.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DATE, 1)
        alarmDateTomorrow = df.format(cal.time)
        _alarmDateTomorrowString.value = " " + ResourceApp.instance.getString(R.string.label_tomorrow) +
                "　" + alarmDateTomorrow.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateTomorrow.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateTomorrow.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
        // DB データを使用するまでの仮処理
        cal.add(Calendar.DATE, 5)
        alarmDateOneWeekEnd = df.format(cal.time)
        _alarmDateOneWeekString.value = alarmDateToday.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateToday.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateToday.substring(6,8) + ResourceApp.instance.getString(R.string.label_days) +
                " ～ " +
                alarmDateOneWeekEnd.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateOneWeekEnd.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateOneWeekEnd.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)

        alarmDatePeriodStart = alarmDateToday
        alarmDatePeriodEnd = alarmDateToday
        _alarmDatePeriodStart.value = alarmDateToday.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateToday.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateToday.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
        _alarmDatePeriodEnd.value = _alarmDatePeriodStart.value

    }

    fun setCode(_code: String) {
        code = _code

        // 銘柄名を設定する
        for(info in stockList) {
            if(code == info.code) {
                name = info.name
            }
        }
        mainCode.value = code
        mainTitle.value = name
    }
    fun getCode(): String = code

    fun getTodayData() {
        // 本日データ取得
        viewModelScope.launch {
            val stockDataManager = StockDataManager()
            _isUpdateProgress.value = true
            stockTodayData = stockDataManager.getTodayData(code)

            // 時刻更新
            todayDate.value = "${stockTodayData.date.substring(4,6)}/${stockTodayData.date.substring(6)}"
            todayTime.value = stockTodayData.time

            // 株価に関しては、小数点以下が0で無い場合のみ、少数を表示させる
            CommonInfo.debugInfo("$cName todayValue: ${1 % stockTodayData.close}")
            if(stockTodayData.close % 1 != 0f){
                todayValue.value = "%,.d".format(stockTodayData.close)
            }
            else {
                todayValue.value = "%,d".format(stockTodayData.close.toInt())
            }

            // 前日比(価格)
            if(stockTodayData.ratio1 % 1 != 0f){
                todayDiffValue.value = stockTodayData.ratio1.toString()
            }
            else {
                todayDiffValue.value = stockTodayData.ratio1.toInt().toString()
            }

            // (%)は、本来xmlファイル側で付けたいが、そうすると、初期状態からのデータ読み込み中表示で、
            // "(%)"と表示されて不細工なので、ここで付けることにする
            todayDiffRatio.value = "(${stockTodayData.ratio2}%)"

            // 前日比が＋の場合は、意図的に"+"を付加する（±0の場合は何も付加しない）
            if(stockTodayData.ratio1 > 0f){
                todayDiffValue.value = "+${todayDiffValue.value}"
                todayDiffRatio.value = todayDiffRatio.value!!.replace("(", "(+")
            }

            // 価格の色設定
            // 前日比がマイナスなら緑、プラスなら赤、±0なら黒とする
            when {
                stockTodayData.ratio1 > 0 -> tvValueColor.value = R.color.colorRed
                stockTodayData.ratio1 < 0 -> tvValueColor.value = R.color.colorGreen
                else -> tvValueColor.value = R.color.colorBlack
            }
            _isUpdateProgress.value = false
        }
    }

    // DB 保存処理
    private suspend fun updateData() {
        withContext(Dispatchers.IO) {
            val databaseCache = DatabaseCache()
        }
    }

    // 一週間の日付関連
    fun onOneWeekDateClick() {
        requireOneWeekDate.value = true
    }
    fun clearOneWeekDateDialog() {
        requireOneWeekDate.value = false
    }
    fun setOnWeekDate(year: Int, month: Int, day: Int) {
        val df = SimpleDateFormat("yyyyMMdd")
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day)
        cal.add(Calendar.DATE, 6)
        alarmDateOneWeekEnd = df.format(cal.time)
        _alarmDateOneWeekString.value = "$year" + ResourceApp.instance.getString(R.string.label_year) +
                "%02d".format(month) + ResourceApp.instance.getString(R.string.label_month) +
                "%02d".format(day) + ResourceApp.instance.getString(R.string.label_days) +
                " ～ " +
                alarmDateOneWeekEnd.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                alarmDateOneWeekEnd.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                alarmDateOneWeekEnd.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
    }

    // 期間指定の日付関連
    fun onPeriodStartDateClick() {
        requirePeriodStartDate.value = true
    }
    fun onPeriodEndDateClick() {
        requirePeriodEndDate.value = true
    }
    fun clearPeriodDateDialog() {
        requirePeriodStartDate.value = false
        requirePeriodEndDate.value = false
    }
    fun setPeriodStartDate(year: Int, month: Int, day: Int) {
        // LocalDate.of の month は、1 ~ 12
        val localDateStart = LocalDate.of(year,month,day)
        val localDateEnd = LocalDate.of(alarmDatePeriodEnd.substring(0,4).toInt(),
            alarmDatePeriodEnd.substring(4,6).toInt(),
            alarmDatePeriodEnd.substring(6,8).toInt())
        if(localDateStart.isAfter(localDateEnd)) {
            // 終了日より後の日が指定された場合は、Toast表示させる
            requirePeriodDateToast.value = true
        }
        else {
            alarmDatePeriodStart = "%d%02d%02d".format(year, month, day)
            _alarmDatePeriodStart.value = alarmDatePeriodStart.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                    alarmDatePeriodStart.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                    alarmDatePeriodStart.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
        }
    }
    fun setPeriodEndDate(year: Int, month: Int, day: Int) {
        val localDateStart = LocalDate.of(alarmDatePeriodStart.substring(0,4).toInt(),
            alarmDatePeriodStart.substring(4,6).toInt(),
            alarmDatePeriodStart.substring(6,8).toInt())
        val localDateEnd = LocalDate.of(year,month,day)
        if(localDateEnd.isBefore(localDateStart)) {
            // 開始日より前の日が指定された場合は、Toast表示させる
            requirePeriodDateToast.value = true
        }
        else {
            alarmDatePeriodEnd = "%d%02d%02d".format(year, month, day)
            _alarmDatePeriodEnd.value = alarmDatePeriodEnd.substring(0,4) + ResourceApp.instance.getString(R.string.label_year) +
                    alarmDatePeriodEnd.substring(4,6) + ResourceApp.instance.getString(R.string.label_month) +
                    alarmDatePeriodEnd.substring(6,8) + ResourceApp.instance.getString(R.string.label_days)
        }
    }
    fun clearPeriodToast() {
        requirePeriodDateToast.value = false
    }

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

}