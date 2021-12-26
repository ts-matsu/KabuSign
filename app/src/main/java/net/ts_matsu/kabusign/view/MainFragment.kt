package net.ts_matsu.kabusign.view

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.DrawFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginLeft
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentMainBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.util.StockInfo
import net.ts_matsu.kabusign.viewmodel.MainViewModel
import net.ts_matsu.kabusign.viewmodel.StockSelectViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private val cName = MainFragment::class.java.simpleName

    private lateinit var binding : FragmentMainBinding
    private val viewModel = MainViewModel()
    private val args: MainFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // StockSelectDialogFragment起動
        binding.ibStockSelect.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToMonitoredStockFragment("")
            findNavController().navigate(action)
        }
        // CandleInfoDialogFragment起動
        binding.ibCandleInfo.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToCandleInfoDialogFragment(code = viewModel.currentStockCode)
            findNavController().navigate(action)
        }
        // StockNotificationFragment起動
        binding.ibNotification.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToStockNotificationDialogFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 戻ってきたときかどうかをどうやって区別するのか？
        // （デフォルト値が""なので、それで区別できる）
        if(args.item.isNotEmpty()){
            viewModel.setStockItem(args.item)
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
            combinedChart.xAxis.valueFormatter =IndexAxisValueFormatter(viewModel.dateData.value)
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