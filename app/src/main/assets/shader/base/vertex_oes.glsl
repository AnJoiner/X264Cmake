attribute vec4 a_position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;
uniform mat4 u_matrix;
void main()
{
    textureCoordinate = (u_matrix * inputTextureCoordinate).xy;
    gl_Position = a_position;
}