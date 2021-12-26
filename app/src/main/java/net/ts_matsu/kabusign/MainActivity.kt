package net.ts_matsu.kabusign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import net.ts_matsu.kabusign.util.CommonInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // フォントライブラリ（InflationX Calligraphy）のインストール
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
//                            .setDefaultFontPath("fonts/rounded-mgenplus-1c-black.ttf") // Add Line
                            .setFontAttrId(R.attr.fontPath)                 // Add Line
                            .build()
                    )
                )
                .build()
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        CommonInfo.debugInfo("onBackPressed.")
    }

    override fun onDestroy() {
        super.onDestroy()
        CommonInfo.debugInfo("onDestroy.")
    }

}
