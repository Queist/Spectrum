package com.game.queist.spectrum.activities

import android.Manifest
import android.content.Intent
import android.widget.LinearLayout
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.game.queist.spectrum.R
import com.bumptech.glide.Glide
import android.content.pm.PackageManager
import android.graphics.Point
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import com.game.queist.spectrum.utils.Utility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.*

class LoadingScreen : BaseSpectrumActivity() {
    var linearLayout: LinearLayout? = null
    var loadingIcon: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            MODE_PRIVATE
        )
        setContentView(R.layout.loading)
        linearLayout = findViewById(R.id.loadingLayout)
        loadingIcon = findViewById(R.id.loadingIcon)
        Glide.with(this).load(R.drawable.loading).into(loadingIcon!!)
        val params = linearLayout!!.layoutParams
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        params.height = size.y.coerceAtMost(size.x * 9 / 16)
        params.width = 16 * params.height / 9
        linearLayout?.layoutParams = LinearLayout.LayoutParams(params)
        linearLayout?.x = size.x.toFloat() / 2 - params.width.toFloat() / 2
        linearLayout?.y = size.y.toFloat() / 2 - params.height.toFloat() / 2
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        finishAndRemoveTask()
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val result = CoroutineScope(Dispatchers.IO).async {
                        loadResources()
                    }.await()
                    if (result) {
                        startMainActivity()
                    }
                    else finishAndRemoveTask()
                }
            }
            else -> finishAndRemoveTask()
        }
    }

    private fun loadResources(): Boolean {
        Utility.writeFile(this, String.format("song_list"), "raw")
        try {
            val inputStream = resources.openRawResource(R.raw.song_list)
            val bufferedReader = BufferedReader(
                InputStreamReader(
                    inputStream,
                    StandardCharsets.UTF_8
                )
            )
            var readLine: String?
            while (bufferedReader.readLine().also { readLine = it } != null) {
                val attr = readLine!!.split("\t").toTypedArray()
                val name = attr[0].lowercase(Locale.getDefault()).replace(" ", "_")
                Utility.writeFile(this, String.format("%s", name), "raw")
                Utility.writeFile(
                    this,
                    String.format("%s_thumb", name),
                    "raw"
                )
                Utility.writeFile(
                    this,
                    String.format("%s", name),
                    "drawable"
                )
                val difficultyValue = attr[4].split(",").toTypedArray()
                var i = 0
                while (i < SongSelect.DIFF_NUM) {
                    if (difficultyValue[i].toInt() != -1) {
                        Utility.writeFile(
                            this,
                            String.format(
                                "%s_%s",
                                name,
                                Utility.difficultyToString(i)
                            ),
                            "raw"
                        )
                    }
                    i++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun startMainActivity() {
        val intent = Intent(this, StartScreen::class.java)
        val bundle = ActivityOptionsCompat.makeCustomAnimation(
            this,
            0, 0
        ).toBundle()
        startActivity(intent, bundle)
    }
}