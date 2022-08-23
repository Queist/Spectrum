package com.game.queist.spectrum.activities

import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.os.Bundle
import com.game.queist.spectrum.R
import android.content.Intent
import android.graphics.Point
import com.game.queist.spectrum.BuildConfig
import com.game.queist.spectrum.activities.LoadingScreen
import com.game.queist.spectrum.activities.StartScreen
import androidx.core.app.ActivityOptionsCompat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import kotlin.math.min

class Logo : AppCompatActivity() {
    var handler: Handler? = null
    var linearLayout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logo)
        handler = Handler(Looper.getMainLooper())
        for (i in 0..249) {
            handler!!.postDelayed(
                { linearLayout!!.alpha = (250 - i).toFloat() / 250 },
                (750 + i).toLong()
            )
        }
        handler!!.postDelayed({
            val intent: Intent
            intent = if (BuildConfig.DEV_MODE) Intent(this, LoadingScreen::class.java) else Intent(
                this,
                StartScreen::class.java
            )
            val bundle = ActivityOptionsCompat.makeCustomAnimation(
                this,
                0, 0
            ).toBundle()
            startActivity(intent, bundle)
        }, 1000)
        linearLayout = findViewById(R.id.loadingLayout)
        val params = linearLayout!!.layoutParams
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        params.height = min(size.y, size.x * 9 / 16)
        params.width = 16 * params.height / 9
        linearLayout?.layoutParams = LinearLayout.LayoutParams(params)
        linearLayout?.x = size.x.toFloat() / 2 - params.width.toFloat() / 2
        linearLayout?.y = size.y.toFloat() / 2 - params.height.toFloat() / 2
    }

    override fun onResume() {
        if (Build.VERSION.SDK_INT < 30) {
            var uiOption = window.decorView.systemUiVisibility
            uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
            uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            uiOption = uiOption or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            uiOption = uiOption or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = uiOption
        } else {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (Build.VERSION.SDK_INT < 30) {
            var uiOption = window.decorView.systemUiVisibility
            uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
            uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            uiOption = uiOption or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            uiOption = uiOption or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = uiOption
        } else {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        if (Build.VERSION.SDK_INT > 27) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        super.onWindowFocusChanged(hasFocus)
    }
}