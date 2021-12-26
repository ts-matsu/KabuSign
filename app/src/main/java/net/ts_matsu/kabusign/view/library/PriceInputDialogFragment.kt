package net.ts_matsu.kabusign.view.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentCandleInfoDialogBinding
import net.ts_matsu.kabusign.databinding.FragmentPriceInputDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.view.CandleInfoDialogFragment
import net.ts_matsu.kabusign.view.CandleInfoDialogFragmentArgs
import net.ts_matsu.kabusign.viewmodel.CandleInfoViewModel
import net.ts_matsu.kabusign.viewmodel.library.PriceInputViewModel

class PriceInputDialogFragment(_isDot: Boolean=false, _isMinus: Boolean=false) : DialogFragment() {
    private val cName = PriceInputDialogFragment::class.java.simpleName

    private lateinit var binding: FragmentPriceInputDialogBinding
    private val viewModel = PriceInputViewModel(_isDot, _isMinus)
    private var requestKey = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPriceInputDialogBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 親画面から指定された値を設定する
        arguments?.getString("requestKey")?.let {
            CommonInfo.debugInfo("$cName: requestKey(${it})")
            requestKey = it
        }
        arguments?.getString("value")?.let {
            CommonInfo.debugInfo("$cName: value(${it})")
            viewModel.setValue(it)
        }

        // OK、CANCELボタンを監視する
        viewModel!!.requireClose.observe(viewLifecycleOwner) {
            if(it) {
                // CandleInfoDialogFragment に、設定された株価を渡す
                val value = viewModel!!.convertFloatValue()
                CommonInfo.debugInfo("$cName: OK($value)")
                setFragmentResult(requestKey, bundleOf("value" to value.toString()))

                // ダイアログ終了
                dismiss()
            }
        }
    }

}