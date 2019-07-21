package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WlRender implements GLSurfaceView.Renderer {

    private Context context;
    private int program;
    private int avPosition;
    private int afColor;

    private final float[] vertexData = {
            -1f, 0f,
            0f, -1f,
            0f, 1f,
            1f, 0f
//            0f, 1f,
//            0f, -1f,
//            1f, 0f
    };

    private FloatBuffer vertexBuffer;

    public WlRender(Context context) {
        this.context = context;
        this.vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);

        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        try {
            String vertexSource = WlShaderUtil.readRawTExt(context, R.raw.vertex_shader);
            String fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader);
            program = WlShaderUtil.createProgram(vertexSource, fragmentSource);
            if (program > 0) {
                avPosition = GLES20.glGetAttribLocation(program, "av_Position");
                afColor = GLES20.glGetUniformLocation(program, "af_Color");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glUseProgram(program);

        GLES20.glUniform4f(afColor, 1f, 1f, 0f, 1f);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8
                , vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
