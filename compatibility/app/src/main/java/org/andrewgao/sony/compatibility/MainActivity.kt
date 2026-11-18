package org.andrewgao.sony.compatibility

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.github.ma1co.openmemories.framework.CameraKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = MainScope()
    private var logScroll: ScrollView? = null
    private var previewHolder: SurfaceHolder? = null
    private var started = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val surface = findViewById(R.id.preview_surface) as SurfaceView
        surface.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                previewHolder = holder
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                previewHolder = holder
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if (previewHolder == holder) previewHolder = null
            }
        })
        val progressGroup = findViewById(R.id.progress_group) as View
        val progressText = findViewById(R.id.progress_text) as TextView
        val progressBar = findViewById(R.id.progress_bar) as ProgressBar
        val logScroll = findViewById(R.id.log_scroll) as ScrollView
        this.logScroll = logScroll
        val mainText = findViewById(R.id.main_text) as TextView
        progressGroup.tag = mainText
        progressText.tag = progressBar
    }

    override fun onResume() {
        super.onResume()
        if (started) return
        started = true
        val progressGroup = findViewById(R.id.progress_group) as View
        val progressText = findViewById(R.id.progress_text) as TextView
        val progressBar = progressText.tag as ProgressBar
        val mainText = progressGroup.tag as TextView
        val logScroll = logScroll ?: return
        scope.launch(Dispatchers.Default) {
            val report = ApiReport(this@MainActivity, { previewHolder }) { name, index, total ->
                runOnUiThread {
                    progressText.text = name + "\n" + index + " / " + total + " APIs tested"
                    progressBar.max = total
                    progressBar.progress = index
                }
            }
            report.run()
            report.lines().forEach { line -> Log.i(TAG, line) }
            val saved = report.writeToSdcard()
            Log.i(TAG, saved)
            runOnUiThread {
                progressGroup.visibility = View.GONE
                logScroll.visibility = View.VISIBLE
                mainText.text = report.summary() + "\n" + saved + "\n\n" + report.text()
                logScroll.post { logScroll.requestFocus() }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.i(TAG, "key code=" + keyCode + " scan=" + event.scanCode)
        if (keyCode == KeyEvent.KEYCODE_MENU || event.scanCode == CameraKeys.MENU) {
            finish()
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return true
        }
        val scroll = logScroll
        if (scroll != null && scroll.visibility == View.VISIBLE && scrollLog(scroll, keyCode, event.scanCode)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun scrollLog(scroll: ScrollView, keyCode: Int, scanCode: Int): Boolean {
        val down = keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            scanCode == CameraKeys.DOWN ||
            scanCode == CameraKeys.DIAL_1_CLOCKWISE ||
            scanCode == CameraKeys.DIAL_2_CLOCKWISE ||
            scanCode == CameraKeys.RING_CLOCKWISE
        val up = keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            scanCode == CameraKeys.UP ||
            scanCode == CameraKeys.DIAL_1_COUNTERCLOCKWISE ||
            scanCode == CameraKeys.DIAL_2_COUNTERCLOCKWISE ||
            scanCode == CameraKeys.RING_COUNTERCLOCKWISE
        val step = if (scroll.height > 0) scroll.height / 3 else 48
        if (down) {
            scroll.scrollBy(0, step)
            return true
        }
        if (up) {
            scroll.scrollBy(0, -step)
            return true
        }
        return false
    }

    companion object {
        const val TAG = "ApiCompat"
    }
}
