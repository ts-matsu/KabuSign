package net.ts_matsu.kabusign.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.Runnable
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.ChartDisplayDialogFragmentBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.ChartDisplayDialogViewModel

class ChartDisplayDialogFragment : DialogFragment() {
    private val cName = ConditionMainFragment::class.java.simpleName

    private val viewModel = ChartDisplayDialogViewModel()
    private lateinit var binding: ChartDisplayDialogFragmentBinding
    private val args: ChartDisplayDialogFragmentArgs by navArgs()

    private val flickHandler = Handler(Looper.getMainLooper())
    private val flickRunner = object : Runnable {
        override fun run() {
            CommonInfo.debugInfo("flickRunner")
            // 前回と位置が変わっていなければ終了
            if(binding.combinedChart.lowestVisibleX != binding.volumeChart.lowestVisibleX){
                binding.volumeChart.moveViewToX(binding.combinedChart.lowestVisibleX)
                flickHandler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.StockChartDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChartDisplayDialogFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 銘柄名、株価の2行の高さ。ひとまず 88dp としている
        val topMarginPixel = 88 * resources.displayMetrics.density

        // ダイアログの位置を調整
        dialog?.window?.attributes.also {
            it?.gravity = Gravity.TOP
            it?.verticalMargin = topMarginPixel/resources.displayMetrics.heightPixels
        }

        // ダイアログの高さ/幅を設定
        // 幅は、Styleで設定できるが、高さがうまく設定できないので、ここで設定する
        dialog?.window?.setLayout(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels - topMarginPixel.toInt())

        // 戻ってきたときかどうかをどうやって区別するのか？
        // （デフォルト値が""なので、それで区別できる）
        if(args.code.isNotEmpty()){
            viewModel.setStockItem(args.code)
        }

        // 横線アイコンボタンの監視
        binding.viewModel!!.ivLineEditState.observe(viewLifecycleOwner) {
            val ivLineEdit = view.findViewById<ImageView>(R.id.ivLineEdit)
            ivLineEdit.drawable.level = it
        }

        // チャートデータの監視
        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            showChart()
            showVolumeChar()
        })

        // CombinedChartの初期設定
        val combinedChart = binding.combinedChart
        var volumeScaleX = 1.0f
        var combinedScaleX = 1.0f
        combinedChart.apply {
            setTouchEnabled(true)
            description.text = ""
            legend.isEnabled = false                      // 凡例は表示しない
            xAxis.position = XAxis.XAxisPosition.BOTTOM   // X軸ラベルは下側
            xAxis.setLabelCount(5,false)        // X軸の表示ラベル数(表示はしないが、出来高チャートとグリッド線を合わせるために設定しておく）
            xAxis.setDrawLabels(false)                    // X軸ラベルなし
            axisLeft.setDrawLabels(false)                 // Y軸ラベルは右側のみ
            axisRight.maxWidth = viewModel.yAxisLabelWidth * resources.displayMetrics.density
            axisRight.minWidth = viewModel.yAxisLabelWidth * resources.displayMetrics.density
            axisRight.textSize = 4.0f * resources.displayMetrics.scaledDensity
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            isDoubleTapToZoomEnabled = false
            drawOrder = arrayOf(CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE)               // 移動平均線を後から描画する
            setNoDataText("")

            // チャートタップ時の、横線表示
            setOnTouchListener { view, motionEvent ->
                var result = false
                view.performClick()     // これをコールしろとメッセージが出るためコールしている

                // タップ時のみ横線描画
                if(motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_MOVE){
                    val pointY = getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(motionEvent.x, motionEvent.y).y.toFloat()
                    result = showLimitLine(pointY)

                }

                // フリック動作用処理
                // 離した時点から、100ms 毎にローソク足チャートの位置をチェックし、
                // 出来高チャートに反映させる
                if(motionEvent.action == MotionEvent.ACTION_UP){
                    flickHandler.postDelayed(flickRunner, 100)
                }

                // 出来高チャートにX軸のズーム値を設定する
                volumeScaleX = (scaleX / combinedScaleX)
                combinedScaleX = scaleX
                binding.volumeChart.zoom(volumeScaleX, 0f ,0f, 0f)
                binding.volumeChart.moveViewToX(binding.combinedChart.lowestVisibleX)

                // 横線移動の場合は、resultがtureになることで、グラフ移動を抑止する
                // true を返すと、ここの処理しかされなくて、Zoom処理とかが行えなかったため、それを利用
                result
            }

            // チャートタップ時に、その日の価格等の表示を切り替える
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        val dateList = viewModel.dateData.value
                        CommonInfo.debugInfo("aaaaaaa: ${e.x.toInt()}")
                        viewModel.setStockDataAt(e.x.toInt())
                    }
                }
            })
        }

        // 出来高チャートの初期設定
        val volumeChart = binding.volumeChart
        volumeChart.apply {
            setNoDataText("")
            description.text = ""
            legend.isEnabled = false                      // 凡例は表示しない
            xAxis.position = XAxis.XAxisPosition.BOTTOM   // X軸ラベルは下側
            xAxis.setLabelCount(5,false)       // X軸の表示ラベル数
            axisRight.maxWidth = viewModel.yAxisLabelWidth * resources.displayMetrics.density
            axisRight.minWidth = viewModel.yAxisLabelWidth * resources.displayMetrics.density
            axisRight.textSize = 3.0f * resources.displayMetrics.scaledDensity
            axisRight.setLabelCount(3, false)  // Y軸のラベル数
            axisLeft.setDrawLabels(false)                 // Y軸ラベルは右側のみ
            isScaleXEnabled = true
            setTouchEnabled(false)
        }

        // Cancelボタンを監視する
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) findNavController().popBackStack()
        }
    }

    // MPAndroidChartは、データバインディングサポートしてないので、
    // ここにチャート表示ロジックを記載する必要がある。
    private fun showChart(){
        CommonInfo.debugInfo("$cName: showChart")
        val combinedChart = binding.combinedChart
        combinedChart.apply {
            data = viewModel.chartData.value

            // 当日、開始日 のローソク足が半分表示になるのを回避する設定
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = (viewModel.dateData.value!!.size - 1) + 0.5f

            // X軸の表示フォーマット設定
            xAxis.valueFormatter = IndexAxisValueFormatter(viewModel.dateData.value)

            setVisibleXRangeMinimum(viewModel.minChartData)    // 表示する最小データ数を10にしておく（データセットするタイミングで設定する必要があるっぽい）
            notifyDataSetChanged()
            invalidate()
        }
    }

    // 出来高チャート表示
    private fun showVolumeChar()  {
        val barChart = binding.volumeChart
        barChart.apply {
            data = viewModel.volumeChartData.value

            // X軸の表示フォーマット設定
            xAxis.valueFormatter = IndexAxisValueFormatter(viewModel.dateData.value)

            setVisibleXRangeMinimum(viewModel.minChartData)    // 表示する最小データ数を10にしておく（データセットするタイミングで設定する必要があるっぽい）
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun showLimitLine(yValue: Float): Boolean {
        var result = false
        val value = yValue.toInt().toFloat()    // 整数に変換

        // 横線情報設定（アイコンが ADD の時のみ）
        viewModel.setLimitLine(value)

        // すでに表示されている横線削除して、今の値で追加（アイコンが MOVE の場合のみ）
        viewModel.getLimitLine(value)?.let {
            val combinedChart = binding.combinedChart
            val axisRight = combinedChart.axisRight
            result = viewModel.removeAndSetLimitLine(it.limit, value)   // MOVE の場合のみ処理される
            axisRight.removeLimitLine(it)
        }

        // 横線表示
        viewModel.getLimitLine(value)?.let {
            val combinedChart = binding.combinedChart
            val axisRight = combinedChart.axisRight
            axisRight.addLimitLine(it)
            combinedChart.notifyDataSetChanged()
            combinedChart.invalidate()
        }
        return result
    }
}