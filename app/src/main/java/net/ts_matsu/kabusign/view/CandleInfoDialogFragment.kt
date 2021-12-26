package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_candle_info_dialog.*
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentCandleInfoDialogBinding
import net.ts_matsu.kabusign.databinding.FragmentStockSelectDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.CandleInfoViewModel
import net.ts_matsu.kabusign.viewmodel.MainViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [CandleInfoDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CandleInfoDialogFragment : DialogFragment() {
    private val cName = CandleInfoDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentCandleInfoDialogBinding
    private val viewModel = CandleInfoViewModel()
    private val args: CandleInfoDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        CommonInfo.debugInfo("$cName: onCreateView.")
        binding = FragmentCandleInfoDialogBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CommonInfo.debugInfo("$cName: onViewCreated.")

        // StockValuePickerDialog の結果を取得する
        setFragmentResultListener("stockValue") {requestKey, bundle ->
            CommonInfo.debugInfo("$cName: setFragmentResultListener(${bundle.getInt("value")})")
            if(bundle.getInt("value") > 0){
                viewModel.setStockValue(bundle.getInt("value"))
            }
        }

        // 対象の銘柄コードを取得
        if(args.code.isNotEmpty()){
            viewModel.setStockCode(args.code)
        }

        // OK、CANCELボタンを監視する
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) {
                dismiss()
            }
        }

        // 株価設定ダイアログへの遷移
        binding.viewModel!!.valueClick.observe(viewLifecycleOwner) {
            if(it) {
                val action = CandleInfoDialogFragmentDirections.actionCandleInfoDialogFragmentToStockValuePickerDialogFragment(viewModel.defaultValue)
                findNavController().navigate(action)
            }
        }
    }
}