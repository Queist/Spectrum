#version 300 es
precision mediump float;

uniform vec3 lightPosition;

layout (location = 0) in vec3 f_Color;
layout (location = 1) in vec3 f_Normal;
layout (location = 2) in vec2 f_TexCoords;
layout (location = 3) in vec4 f_VPosition;

out vec4 fragColor;

void main() {
    fragColor = vec4(f_Color, 1.0);
}
