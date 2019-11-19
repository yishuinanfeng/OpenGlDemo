package com.example.opengldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.orthoM;

public class WlRender implements GLSurfaceView.Renderer {

    private Context context;
    private int program;
    private int avPosition;
    //纹理坐标
    private int afPosition;
    private int red;
    private int green;
    private int blue;
    private int sTexture;
    private int textureId;
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private int vboId;
    //private int afColor;

    private final float[] vertexData = {
//            -1f, -1f,
//            1f, -1f,
//            -1f, 1f,
//            1f, 1f

            -1f, 1f,
            1f, 1f,
            -1f, -1f,
            1f, -1f
//            0f, 1f,
//            0f, -1f,
//            1f, 0f
    };

    private final float[] textureData = {
//            0f, 1f,
//            1f, 1f,
//            0f, 0f,
//            1f, 0f


            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f

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
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        try {
            String vertexSource = WlShaderUtil.readRawTExt(context, R.raw.vertex_shader);
            String fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader);
            program = WlShaderUtil.createProgram(vertexSource, fragmentSource);
            if (program > 0) {
                avPosition = GLES20.glGetAttribLocation(program, "av_Position");
                //afColor = GLES20.glGetUniformLocation(program, "af_Color");
                afPosition = GLES20.glGetAttribLocation(program, "af_Position");
                uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix");
                red = GLES20.glGetAttribLocation(program, "red");
                green = GLES20.glGetAttribLocation(program, "green");
                blue = GLES20.glGetAttribLocation(program, "blue");
                //vbo的创建和绑定
                int[] vbos = new int[1];
                glGenBuffers(1, vbos, 0);
                vboId = vbos[0];
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, vertexData.length * 4 + textureData.length * 4, null, GL_STATIC_DRAW);
                glBufferSubData(GL_ARRAY_BUFFER, 0, vertexData.length * 4, vertexBuffer);
                glBufferSubData(GL_ARRAY_BUFFER, vertexData.length * 4, textureData.length * 4, textureBuffer);
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                // sTexture = GLES20.glGetUniformLocation(program, "sTexture");

                int[] textureIds = new int[1];
                //生成纹理ID。offset表示？
                GLES20.glGenTextures(1, textureIds, 0);

                if (textureIds[0] == 0) {
                    return;
                }

                textureId = textureIds[0];
                //将生成的ID绑定到纹理通道
                GLES20.glBindBuffer(GLES20.GL_TEXTURE_2D, textureId);
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

                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.a);
                if (bitmap == null) {
                    return;
                }
                //将Bitmap对象与当前纹理通道绑定，而当前纹理通道已经绑定好了ID，从而达到了ID与纹理的间接绑定
                // level?border?将Bitmap对象与当前纹理通道绑定，而当前纹理通道已经绑定好了ID，从而达到了ID与纹理的间接绑定
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                //当bitmap的数据已经绑定到纹理单元后，就可以释放空间
                bitmap.recycle();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //正交投影，将显示区域的偏小的边设为1，偏大的边看作大边和小边的比例
        final float aspectRatio = width > height ?
                (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glUseProgram(program);

        //GLES20.glUniform4f(afColor, 1f, 1f, 0f, 1f);

        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(avPosition);
        GLES20.glVertexAttribPointer(avPosition, 2, GLES20.GL_FLOAT, false, 8
                , 0);

        GLES20.glEnableVertexAttribArray(afPosition);
        GLES20.glVertexAttribPointer(afPosition, 2, GLES20.GL_FLOAT, false, 8
                , vertexData.length * 4 );

        GLES20.glVertexAttrib1f(red, r);
        GLES20.glVertexAttrib1f(green, g);
        GLES20.glVertexAttrib1f(blue, b);
        //从缓存的数组的0开始绘制4个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }


    private float r;
    private float g;
    private float b;

    public void changeRed(float r) {
        this.r = r;
    }

    public void changeGreen(float g) {
        this.g = g;
    }

    public void changeBlue(float b) {
        this.b = b;
    }
}
