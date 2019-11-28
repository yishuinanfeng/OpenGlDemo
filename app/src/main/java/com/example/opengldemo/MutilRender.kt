package com.example.opengldemo

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix.orthoM
import android.opengl.Matrix.rotateM
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 多个共享Texture的SurfaceView使用的Render
 */
class MutilRender(val context: Context) : CustomGlSurfaceView.CustomRender {

    private val projectionMatrix = FloatArray(16)
    private var uMatrixLocation: Int = 0

    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer

    private val vertexData = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    private val fragmentData = floatArrayOf(
        //            0f, 1f,
        //            1f, 1f,
        //            0f, 0f,
        //            1f, 0f,

        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    private var program: Int = 0
    private var vPosition: Int = 0
    private var fPosition: Int = 0
    private var textureId: Int = 0
    private val sampler: Int = 0

    private var vboId: Int = 0
    private var index: Int = 0

    fun setTextureId(texd: Int, index: Int) {
        textureId = texd
        this.index = index;
    }

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)

        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentData)
        fragmentBuffer.position(0)

    }

    override fun onSurfaceCreated() {
        try {
            val vertexSource = WlShaderUtil.readRawTExt(context, R.raw.vertex_shader2)
            val fragmentSource: String
            when (index) {
                0 -> {
                    fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader1)
                }
                1 -> {
                    fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader2)

                }
                else -> {
                    fragmentSource = WlShaderUtil.readRawTExt(context, R.raw.fragment_shader3)

                }
            }


            program = WlShaderUtil.createProgram(vertexSource, fragmentSource)

            vPosition = GLES20.glGetAttribLocation(program, "av_Position")
            fPosition = GLES20.glGetAttribLocation(program, "af_Position")
            // uMatrixLocation = GLES20.glGetUniformLocation(program, "u_Matrix")
            //    sampler = GLES20.glGetUniformLocation(program, "sTexture");

            val vbos = IntArray(1)
            GLES20.glGenBuffers(1, vbos, 0)
            vboId = vbos[0]

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                vertexData.size * 4 + fragmentData.size * 4,
                null,
                GLES20.GL_STATIC_DRAW
            )
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, fragmentData.size * 4, fragmentBuffer)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        //正交投影，将显示区域的偏小的边设为1，偏大的边看作大边和小边的比例
        val aspectRatio = if (width > height)
            width.toFloat() / height.toFloat()
        else
            height.toFloat() / width.toFloat()

//        if (width > height) {
//            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
//        } else {
//            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
//        }
//
//        //   rotateM(projectionMatrix, 0, 180, 0, 0, 1);
//        rotateM(projectionMatrix, 0, 180f, 1f, 0f, 0f)
    }

    override fun onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f, 0f, 0f, 1f)

        GLES20.glUseProgram(program)

        //  GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(
            vPosition, 2, GLES20.GL_FLOAT, false, 8,
            0
        )

        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(
            fPosition, 2, GLES20.GL_FLOAT, false, 8,
            vertexData.size * 4
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}