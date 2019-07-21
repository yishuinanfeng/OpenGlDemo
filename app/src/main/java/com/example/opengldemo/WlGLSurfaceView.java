package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class WlGLSurfaceView extends GLSurfaceView {

    public WlGLSurfaceView(Context context) {
        super(context);
    }

    public WlGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使用OpenGl版本
        setEGLContextClientVersion(2);
        setRenderer(new WlRender(context));
    }


}
