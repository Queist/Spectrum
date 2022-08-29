#version 300 es

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 texCoords;

uniform mat4 world;
uniform mat4 view;
uniform mat4 proj;

out vec3 f_Normal;
out vec2 f_TexCoords;
out vec4 f_VPosition;

void main() {
    gl_Position = vec4(position, 1.0);
    f_Normal = normal;
    f_TexCoords = texCoords;
    f_VPosition = world * vec4(position, 1.0);
}