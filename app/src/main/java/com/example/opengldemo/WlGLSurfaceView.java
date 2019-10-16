package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class WlGLSurfaceView extends GLSurfaceView {
    private WlRender wlRender;

    public WlGLSurfaceView(Context context) {
        super(context);
    }

    public WlGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使用OpenGl版本
        setEGLContextClientVersion(2);
        wlRender = new WlRender(context);
        setRenderer(wlRender);
    }

    public void changeRed(float r) {
        wlRender.changeRed(r);
    }

    public void changeGreen(float g) {
        wlRender.changeGreen(g);
    }

    public void changeBlue(float b) {
        wlRender.changeBlue(b);
    }

}
