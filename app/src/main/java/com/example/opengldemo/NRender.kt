package com.example.opengldemo

import android.opengl.GLES20
import android.util.Log

class NRender:CustomGlSurfaceView.CustomRender {

    override fun onSurfaceCreated() {
        Log.d("NRender", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.d("NRender", "onSurfaceChanged")
    }

    override fun onDrawFrame() {
        Log.d("NRender", "onDrawFrame")
        //清除颜色缓冲以及深度缓冲
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)
    }
}