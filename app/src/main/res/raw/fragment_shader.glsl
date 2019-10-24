precision mediump float;
varying vec2 v_texPo;
uniform sampler2D sTexture;
varying float r;
varying float g;
varying float b;
void main(){
   // gl_FragColor = texture2D(sTexture, v_texPo);

     vec4 textureColor = texture2D(sTexture, v_texPo);
     float gray = (textureColor.r * r + textureColor.g * g + textureColor.b * b)/3.0;
     gl_FragColor = vec4(textureColor.r * r, textureColor.g * g, textureColor.b * b, textureColor.w);
}