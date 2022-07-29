#version 300 es
precision mediump float;

uniform vec3 lightPosition;

layout (location = 0) in vec3 gl_Color;
layout (location = 1) in vec3 gl_Normal;
layout (location = 2) in vec2 gl_TexCoords;
layout (location = 3) in vec4 gl_VPosition;

out vec4 gl_FragColor;

void main() {
    gl_FragColor = vec4(gl_Color, 1.0);
}
