#version 300 es
precision mediump float;

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform vec3 color;

layout (location = 0) in vec3 f_Normal;
layout (location = 1) in vec2 f_TexCoords;
layout (location = 2) in vec4 f_VPosition;

out vec4 fragColor;

void main() {
    fragColor = vec4(color, 1.0);
}
