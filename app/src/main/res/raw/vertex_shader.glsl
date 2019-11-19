attribute vec4 av_Position;//顶点坐标
attribute vec2 af_Position;//纹理坐标
varying vec2 v_texPo;//用于把纹理坐标传到fragment
attribute float red;
attribute float green;
attribute float blue;
varying float r;
varying float g;
varying float b;
uniform mat4 u_Matrix;
void main(){
    gl_Position = u_Matrix*av_Position;
    v_texPo = af_Position;
    r = red;
    g = green;
    b = blue;
}