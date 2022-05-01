package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentConditionMainBinding
import net.ts_matsu.kabusign.databinding.FragmentMonitoredStockBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.ConditionMainViewModel
import net.ts_matsu.kabusign.viewmodel.MonitoredStockViewModel

class ConditionMainFragment : Fragment() {
    private val cName = ConditionMainFragment::class.java.simpleName

    private lateinit var binding: FragmentConditionMainBinding
    private val viewModel = ConditionMainViewModel()
    private val args: ConditionMainFragmentArgs by navArgs()

    private lateinit var conditionSettingAdapter: ConditionSettingAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConditionMainBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(args.code.isNotEmpty()) {
            viewModel.setCode(args.code)
            viewModel.getTodayData()
            viewModel.createAdapter(args.code)
        }
        conditionSettingAdapter = ConditionSettingAdapter(this,
            viewModel.adapterList)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = conditionSettingAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = conditionSettingAdapter.getName(position)
        }.attach()


        // Cancelボタンを監視する
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) findNavController().popBackStack()
        }

        // OKボタンを監視する
        binding.viewModel!!.requireOk.observe(viewLifecycleOwner) {
            if(it) findNavController().popBackStack()
        }

        // PriceInputDialog の結果を取得する
        setFragmentResultListener("DesignationPrice") {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener(${bundle.getInt("value")})")
        }

        // チャート表示
        viewModel.chartDisplay.observe(viewLifecycleOwner) {
            if (it) {
                val action = ConditionMainFragmentDirections.actionConditionMainFragmentToChartDisplayDialogFragment(viewModel.getCode())
                findNavController().navigate(action)
            }
        }

    }

}