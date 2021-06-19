attribute vec4 a_position;
attribute vec2 inputTextureCoordinate;
varying vec2 textureCoordinate;
uniform mat4 u_matrix;
void main()
{
    textureCoordinate = (u_matrix * inputTextureCoordinate);
    gl_Position = a_position;
}