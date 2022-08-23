package com.game.queist.spectrum.activities

import android.graphics.Point
import android.os.Build
import android.view.*
import androidx.appcompat.app.AppCompatActivity

public abstract class BaseSpectrumActivity : AppCompatActivity() {

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

    protected fun <T : ViewGroup> restrictResolution(layout: T) { //TODO
        val params = layout.layoutParams
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        params.height = size.y.coerceAtMost(size.x * 9 / 16)
        params.width = 16 * params.height / 9
        layout.x = size.x.toFloat() / 2 - params.width.toFloat() / 2
        layout.y = size.y.toFloat() / 2 - params.height.toFloat() / 2
    }
}