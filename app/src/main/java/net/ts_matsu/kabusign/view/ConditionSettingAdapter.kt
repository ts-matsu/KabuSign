package net.ts_matsu.kabusign.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.ts_matsu.kabusign.util.CommonInfo

data class AdapterInfo(val name: String, val fragment: Fragment)
class ConditionSettingAdapter(fragment: Fragment, adapterList: MutableList<AdapterInfo>) : FragmentStateAdapter(fragment) {
    private val cName = ConditionSettingAdapter::class.java.simpleName
    private val fragment = adapterList

    override fun getItemCount(): Int = fragment.size

    override fun createFragment(position: Int): Fragment {
        val fg = fragment[position].fragment

//        fg.arguments = Bundle().apply {
//            putInt("test", position + 1)
//            CommonInfo.debugInfo("$cName 111")
//        }
        return fg
    }

    fun getName(position: Int): String {
        var result = ""
        if(position < fragment.size) {
            result = fragment[position].name
        }
        return result
    }
}