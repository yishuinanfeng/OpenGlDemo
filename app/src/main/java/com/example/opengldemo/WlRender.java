package com.example.opengldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;

public class WlRender implements CustomGlSurfaceView.CustomRender {

    private static final String TAG = WlRender.class.getSimpleName();
    private int fboWidth = 720;
    private int fboHeight = 1280;

    private Context context;
    private int program;
    private int avPosition;
    //纹理坐标
    private int afPosition;
    private int width;
    private int height;

    private int textureId;
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private int vboId;
    private int fboId;
    private int imageTextureId;
    private int imageTextureId2;
    private FboRender fboRender;
    private OnTextureCreateListener onTextureCreateListener;

    public void setOnTextureCreateListener(OnTextureCreateListener onTextureCreateListener) {
        this.onTextureCreateListener = onTextureCreateListener;
    }

    //private int afColor;

    private final float[] vertexData = {
//            -1f, -1f,
//            1f, -1f,
//            -1f, 1f,
//            1f, 1f

            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f,

            -0.5f, 0.5f,
            0.5f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
//            0f, 1f,
//            0f, -1f,
//            1f, 0f
    };

    private final float[] textureData = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f

//
//            0f, 0f,
//            1f, 0f,
//            0f, 1f,
//            1f, 1f

//            0f, 0f,
//            1.5f, 0f,
//            0f, 1.5f,
//            1.5f, 1.5f

//            0f, 0f,
//            0f, 0.5f,
//            0.5f, 0.5f,
//            0.5f, 0f


//            0f,0f,
//            0.5f,0f,
//            0f,1f,
//            0.5f,1f

    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    public WlRender(Context context) {
        this.context = context;
        fboRender = new FboRender(context);
        this.vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);

        vertexBuffer.position(0);

        this.textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);

        textureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        try {
            fboRender.onCreate();
            String vertexSource = WlShaderUtil.readRawTExt(context, R.raw.vertex_shader);
            String fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader);
            program = WlShaderUtil.createProgram(vertexSource, fragmentSource);
            if (program > 0) {
                avPosition = GLES20.glGetAttribLocation(program, "av_Position");
                //afColor = GLES20.glGetUniformLocation(program, "af_Color");
                afPosition = GLES20.glGetAttribLocation(program, "af_Position");
                uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");

                //vbo的创建和绑定,将顶点和纹理坐标数组放入显存，提高性能
                int[] vbos = new int[1];
                glGenBuffers(1, vbos, 0);
                vboId = vbos[0];
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, vertexData.length * 4 + textureData.length * 4, null, GL_STATIC_DRAW);
                glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
                glBufferSubData(GL_ARRAY_BUFFER, vertexData.length * 4, textureData.length * 4, textureBuffer);
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                int[] fbos = new int[1];
                glGenBuffers(1, fbos, 0);
                fboId = fbos[0];
                Log.e(TAG, "fboId=" + fboId);
                //绑定FrameBuffer到当前的绘制环境。缓冲绑定到fbo上。后面绑定到纹理目标的纹理对象才可以绑定到该fbo对象
                //GL_FRAMEBUFFER为帧缓冲的目标
                glBindFramebuffer(GL_FRAMEBUFFER, fboId);


                // sTexture = GLES20.glGetUniformLocation(program, "sTexture");

                int[] textureIds = new int[1];
                //生成纹理ID。offset表示？
                GLES20.glGenTextures(1, textureIds, 0);

                if (textureIds[0] == 0) {
                    return;
                }

                textureId = textureIds[0];
                //将生成的ID绑定到纹理通道
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                //纹理坐标超出1的部分对纹理的显示处理（GL_REPEAT表示纹理重复显示）
                //横向
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
                //纵向
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
                //当视频尺寸和屏幕尺寸不一致的时候，如何进行缩放的配置
                //GL_LINEAR表示使用距离渲染像素中心最近的4个纹理像素加权平均值
                //GL_TEXTURE_MIN_FILTER表示缩小的情况，GL_TEXTURE_MAG_FILTER表示放大的情况
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                //设置fbo大小（需要在纹理对象绑定到纹理目标之后）
                //尺寸为缓冲区显示大小，显示出来的纹理图片会以这个尺寸为全尺寸按比例显示？
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, fboWidth, fboHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
                //纹理对象绑定到fbo，后面对fbo的操作就是对纹理的操作（将纹理对象挂载到FrameBuffer上，纹理对象会存储绘制到FrameBuffer的颜色信息）
                //GL_COLOR_ATTACHMENT0表示采样颜色缓冲
                //textureId是被离屏渲染的纹理id
                glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
                if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                    Log.e(TAG, "fbo fail");
                } else {
                    Log.e(TAG, "fbo success");
                }

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                glBindFramebuffer(GL_FRAMEBUFFER, 0);

                imageTextureId = loadTexture(R.drawable.a);
                imageTextureId2 = loadTexture(R.drawable.girl);
                Log.e(TAG, "imageTextureId=" + imageTextureId);

                if (onTextureCreateListener != null) {
                    onTextureCreateListener.onTextureCreate(textureId);
                }
                //当bitmap的数据已经绑定到纹理单元后，就可以释放空间
                //    bitmap.recycle();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int loadTexture(int src) {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), src);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textureIds[0];
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //GLES20.glViewport(0, 0, width, height);
        //正交投影，将显示区域的偏小的边设为1，偏大的边看作大边和小边的比例
        this.width = width;
        this.height = height;
        width = fboWidth;
        height = fboHeight;
        final float aspectRatio = width > height ?
                (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

        //   rotateM(projectionMatrix, 0, 180, 0, 0, 1);
        rotateM(projectionMatrix, 0, 180, 1, 0, 0);

        fboRender.onChange(this.width, this.height);

    }

    @Override
    public void onDrawFrame() {
        //绑定FrameBuffer到当前的绘制环境。后面的渲染都会渲染到fboId绑定的纹理对象上（离屏渲染）
        GLES20.glViewport(0, 0, fboWidth, fboHeight);

        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glUseProgram(program);

        //GLES20.glUniform4f(afColor, 1f, 1f, 0f, 1f);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        //后面对imageTextureId纹理对象的渲染会渲染到和fbo绑定的TextureId纹理对象
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        //绘制第一张纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imageTextureId);
        //绑定了vbo之后，后面的操作都是基于vbo的
        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8
                , 0);

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8
                , vertexData.length * 4);

        //从缓存的数组的0开始绘制4个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //绘制第二张纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imageTextureId2);
        //绑定了vbo之后，后面的操作都是基于vbo的
        GLES20.glEnableVertexAttribArray(avPosition);

        //这里最后一个参数传入的是对应的顶点数组的偏移量4 * 8
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8
                , 4 * 8);

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8
                , vertexData.length * 4);

        //从缓存的数组的glVertexAttribPointer指定的偏移量开始绘制4个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //只有解绑帧缓冲对象才可以绘制到屏幕（The value zero is reserved to represent the default framebuffer provided by the windowing system）
        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        fboRender.onDraw(textureId);
    }

    interface OnTextureCreateListener {
        /**
         * @param textureId 离屏渲染id
         */
        void onTextureCreate(int textureId);
    }

}
