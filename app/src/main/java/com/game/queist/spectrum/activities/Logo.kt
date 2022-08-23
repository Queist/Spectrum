package com.game.queist.spectrum.activities

import android.widget.LinearLayout
import android.os.Bundle
import com.game.queist.spectrum.R
import android.content.Intent
import android.graphics.Point
import com.game.queist.spectrum.BuildConfig
import androidx.core.app.ActivityOptionsCompat
import android.os.Handler
import android.os.Looper

class Logo : BaseSpectrumActivity() {
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
            (if (BuildConfig.DEV_MODE) Intent(this, LoadingScreen::class.java)
            else Intent(this, StartScreen::class.java)).also { intent = it }
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
        params.height = size.y.coerceAtMost(size.x * 9 / 16)
        params.width = 16 * params.height / 9
        linearLayout?.x = size.x.toFloat() / 2 - params.width.toFloat() / 2
        linearLayout?.y = size.y.toFloat() / 2 - params.height.toFloat() / 2
    }
}