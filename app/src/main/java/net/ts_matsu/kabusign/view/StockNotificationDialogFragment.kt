package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.room.InvalidationTracker
import net.ts_matsu.kabusign.R
import net.ts_matsu.kabusign.databinding.FragmentStockNotificationDialogBinding
import net.ts_matsu.kabusign.util.CommonInfo
import net.ts_matsu.kabusign.viewmodel.StockNotificationViewModel

class StockNotificationDialogFragment : DialogFragment() {
    private val cName = StockNotificationDialogFragment::class.java.simpleName
    private lateinit var binding: FragmentStockNotificationDialogBinding
    private val viewModel = StockNotificationViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        CommonInfo.debugInfo("$cName  onCreateView()")
        binding = FragmentStockNotificationDialogBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        CommonInfo.debugInfo("$cName  onViewCreated()")

        // OK/CANCEL ボタンがタップされたらダイアログを閉じる
        binding.viewModel!!.requireClose.observe(viewLifecycleOwner, Observer {
            if(it) dismiss()
        })
    }
}