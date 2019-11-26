package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class WlGLSurfaceView extends CustomGlSurfaceView {
    private NRender wlRender;

    public WlGLSurfaceView(Context context) {
        super(context);
    }

    public WlGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使用OpenGl版本
       // setEGLContextClientVersion(2);
        wlRender = new NRender();
        setRender(wlRender);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

//    public void changeRed(float r) {
//        wlRender.changeRed(r);
//    }
//
//    public void changeGreen(float g) {
//        wlRender.changeGreen(g);
//    }
//
//    public void changeBlue(float b) {
//        wlRender.changeBlue(b);
//    }

}
