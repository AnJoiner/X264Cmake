package com.coder.x264cmake.module.filter;


public class GLImageFilter {
    public static final String VERTEX_SHADER = "" +
            "attribute vec4 aPosition; //顶点坐标\n" +
            "attribute vec2 aTexCoord; //材质顶点坐标\n" +
            "varying vec2 vTexCoord;   //输出的材质坐标\n" +
            "void main(){\n" +
            "    vTexCoord = vec2(aTexCoord.x,1.0-aTexCoord.y);\n" +
            "    gl_Position = aPosition;\n" +
            "}";
    public static final String FRAGMENT_SHADER = "" +
            "precision mediump float;    //精度\n" +
            "varying vec2 vTexCoord;     //顶点着色器传递的坐标\n" +
            "uniform sampler2D yTexture; //输入的材质（不透明灰度，单像素）\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform sampler2D vTexture;\n" +
            "void main(){\n" +
            "    vec3 yuv;\n" +
            "    vec3 rgb;\n" +
            "    yuv.r = texture2D(yTexture,vTexCoord).r;\n" +
            "    yuv.g = texture2D(uTexture,vTexCoord).r - 0.5;\n" +
            "    yuv.b = texture2D(vTexture,vTexCoord).r - 0.5;\n" +
            "    rgb = mat3(1.0,     1.0,    1.0,\n" +
            "               0.0,-0.39465,2.03211,\n" +
            "               1.13983,-0.58060,0.0)*yuv;\n" +
            "    //输出像素颜色\n" +
            "    gl_FragColor = vec4(rgb,1.0);\n" +
            "}";

}
