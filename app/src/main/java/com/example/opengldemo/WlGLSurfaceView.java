package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class WlGLSurfaceView extends CustomGlSurfaceView {
    private WlRender wlRender;

    public WlGLSurfaceView(Context context) {
        super(context);
    }

    public WlGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使用OpenGl版本
        // setEGLContextClientVersion(2);
        wlRender = new WlRender(context);
        setRender(wlRender);
        setRenderMode(CustomGlSurfaceView.RENDERMODE_CONTINUOUSLY);
        //   setRenderMode(CustomGlSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public WlRender getRender() {
        return wlRender;
    }

}
