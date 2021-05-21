package com.coder.x264cmake.module.filter;

public class GLTestFilter {

    public static final String VERTEX_TEST_SHADER =
            "attribute vec4 a_Position;\n" +
                    "void main(){\n" +
                    "\tgl_Position = a_Position;\n" +
                    "}";

    public static final String FRAGMENT_TEST_SHADER =
            "precision mediump float;\n" +
                    "vec4 u_Color;\n" +
                    "void main(){\n" +
                    "\tgl_FragColor = u_Color;\n" +
                    "}";
}
