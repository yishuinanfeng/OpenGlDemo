package com.example.opengldemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.widget.LinearLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = WlRender::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        surfaceview.setOnClickListener {
            surfaceview.requestRender()
        }

        surfaceview.render.setOnTextureCreateListener { textureId ->
            runOnUiThread {

                Log.d(TAG, "OnTextureCreate")

                if (bottomMenu.childCount > 0) {
                    bottomMenu.removeAllViews()
                }

                val mutilSurfaceView = MutilSurfaceView(this)
                mutilSurfaceView.setSurfaceAndEglContext(null, surfaceview.getEglContext())
                mutilSurfaceView.setTextureId(textureId)

                val lp = LinearLayout.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
             //   lp.weight = 1f
                mutilSurfaceView.layoutParams = lp

                Log.d(TAG, "addView mutilSurfaceView")
                bottomMenu.addView(mutilSurfaceView)
            }
        }

    }
}
