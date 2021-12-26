package net.ts_matsu.kabusign.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentMonitoredStockBinding
import net.ts_matsu.kabusign.databinding.FragmentStockSelectDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.MonitoredStockViewModel
import net.ts_matsu.kabusign.viewmodel.StockSelectViewModel

class MonitoredStockFragment : Fragment() {
    private val cName = MonitoredStockFragment::class.java.simpleName

    private lateinit var binding: FragmentMonitoredStockBinding
    private val viewModel = MonitoredStockViewModel()
    private val args: MonitoredStockFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMonitoredStockBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 銘柄選択ダイアログ遷移
        binding.ibStockSelect.setOnClickListener {
            val action =
                MonitoredStockFragmentDirections.actionMonitoredStockFragmentToStockSelectDialogFragment()
            findNavController().navigate(action)
        }

        // 銘柄選択ダイアログから銘柄コードを取得
        if(args.item.isNotEmpty()){
            CommonInfo.debugInfo("$cName  code:${args.item!!.split(",")[0]}, name:${args.item!!.split(",")[1]}")
            binding.viewModel!!.registerMonitoredStock(args.item)
        }

        // 監視銘柄選択で、メイン画面へ遷移
        binding.viewModel!!.selectedCode.observe(viewLifecycleOwner) {
            if(it.isNotEmpty()){
                val action = MonitoredStockFragmentDirections.actionMonitoredStockFragmentToMainFragment(it)
                findNavController().navigate(action)
            }
        }

        // 条件設定フラグメントへ遷移
        binding.viewModel!!.selectedConditionKind.observe(viewLifecycleOwner) {
            if(it) {
                val action = MonitoredStockFragmentDirections.actionMonitoredStockFragmentToConditionMainFragment(
                    code = binding.viewModel!!.selectingCode
                )
                findNavController().navigate(action)
                binding.viewModel!!.selectedConditionKind.value = false
            }
        }

        // Cancelボタンを監視する
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) findNavController().popBackStack()
        }
    }
}