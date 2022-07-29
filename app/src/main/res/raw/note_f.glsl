#version 300 es
precision mediump float;

uniform vec3 lightPosition;
uniform vec3 color;

layout (location = 0) in vec3 gl_Normal;
layout (location = 1) in vec2 gl_TexCoords;
layout (location = 2) in vec4 gl_VPosition;

out vec4 gl_FragColor;

void main() {
    gl_FragColor = vec4(color, 1.0);
}
