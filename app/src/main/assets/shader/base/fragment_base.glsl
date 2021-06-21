precision mediump float;
uniform sampler2D inputTexture;
varying vec2 textureCoordinate;
void main() {
    gl_FragColor = texture2D( inputTexture, textureCoordinate );
}