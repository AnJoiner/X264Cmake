attribute vec4 a_position; 
attribute vec2 inputTextureCoordinate; 
varying vec2 textureCoordinate; 
void main() 
{ 
    textureCoordinate = inputTextureCoordinate; 
    gl_Position = a_position; 
}