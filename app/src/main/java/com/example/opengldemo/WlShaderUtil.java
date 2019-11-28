package com.example.opengldemo;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WlShaderUtil {

    public static String readRawTExt(Context context,int rawId) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null){
            stringBuilder.append(line).append("\n");
        }

        reader.close();
        return stringBuilder.toString();
    }


    public static int loadShader(int shaderType,String source){
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compile = new int[1];
            GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compile,0);
            if (compile[0] != GLES20.GL_TRUE){
                String info = GLES20.glGetShaderInfoLog(shader);
                Log.d("WlShaderUtil","shader compile error:" + info);
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader;
    }

    public static int createProgram(String vertexSource,String fragmentSource){
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if (vertexShader == 0){
            return 0;
        }

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if (fragmentShader == 0){
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0){
            GLES20.glAttachShader(program,vertexShader);
            GLES20.glAttachShader(program,fragmentShader);
            GLES20.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linkStatus,0);
            if (linkStatus[0] != GLES20.GL_TRUE){
                Log.d("WlShaderUtil","link shader error");
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }
}
