package net.ts_matsu.kabusign.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.viewmodel.ChartDisplayDialogViewModel
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import net.ts_matsu.kabusign.databinding.ChartDisplayDialogFragmentBinding
import net.ts_matsu.kabusign.databinding.FragmentStockNotificationDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo

class ChartDisplayDialogFragment : DialogFragment() {
    private val cName = ConditionMainFragment::class.java.simpleName

    private val viewModel = ChartDisplayDialogViewModel()
    private lateinit var binding: ChartDisplayDialogFragmentBinding
    private val args: ChartDisplayDialogFragmentArgs by navArgs()

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
        // 銘柄名、株価の2行の高さの、全体の高さに対する割合
        // ひとまず 80dp としている
        val topMarginPixel = 80 * resources.displayMetrics.density

        // ダイアログの位置を調整
        dialog?.window?.attributes.also {
            it?.gravity = Gravity.TOP
            it?.verticalMargin = topMarginPixel/resources.displayMetrics.heightPixels
        }

        CommonInfo.debugInfo("aaaa: ${topMarginPixel/resources.displayMetrics.heightPixels}")

        // ダイアログの高さ/幅を設定
        // 幅は、Styleで設定できるが、高さがうまく設定できないので、ここで設定する
        dialog?.window?.setLayout(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels - topMarginPixel.toInt())


        // 戻ってきたときかどうかをどうやって区別するのか？
        // （デフォルト値が""なので、それで区別できる）
        if(args.code.isNotEmpty()){
            viewModel.setStockItem(args.code)
        }

        // チャートデータの監視
        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            showChart()
        })

        // ConbinedChartの初期設定
        val combinedChart = binding.combinedChart
        combinedChart.apply {
            setTouchEnabled(true)
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            drawOrder = arrayOf(CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE)               // 移動平均線を後から描画する
            setNoDataText("")

            // チャートタップ時の、横線表示
            setOnTouchListener { view, motionEvent ->
                view.performClick()     // これをコールしろとメッセージが出るためコールしている

                // タップ時のみ横線描画
                if(motionEvent.action == MotionEvent.ACTION_DOWN){
                    val pointY = getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(motionEvent.x, motionEvent.y).y.toFloat()
                    showLimitLine(pointY)
                }
                // true を返すと、ここの処理しかされなくて、Zoom処理とかが行えなかった。
                false
            }
        }

    }

    // MPAndroidChartは、データバインディングサポートしてないので、
    // ここにチャート表示ロジックを記載する必要がある。
    private fun showChart(){
        CommonInfo.debugInfo("$cName: showChart")
        val combinedChart = binding.combinedChart
        combinedChart.apply {
            combinedChart.description.text = ""
            combinedChart.legend.isEnabled = false                      // 凡例は表示しない
            combinedChart.xAxis.position = XAxis.XAxisPosition.BOTTOM   // X軸ラベルは下側
            combinedChart.axisLeft.setDrawLabels(false)                 // Y軸ラベルは右側のみ
            combinedChart.xAxis.setLabelCount(5,false)      // X軸の表示ラベル数
            combinedChart.data = viewModel.chartData.value
            combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(viewModel.dateData.value)
            combinedChart.notifyDataSetChanged()
            combinedChart.invalidate()
        }
    }

    private fun showLimitLine(yValue: Float){
        val value = yValue.toInt().toFloat()    // 整数に変換
        viewModel.getLimitLine(value)?.let {
            val combinedChart = binding.combinedChart
            val axisRight = combinedChart.axisRight
            axisRight.removeAllLimitLines()
            axisRight.addLimitLine(it)
            combinedChart.notifyDataSetChanged()
            combinedChart.invalidate()
        }
    }
}