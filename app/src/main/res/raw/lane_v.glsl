#version 300 es

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 color;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec2 texCoords;

uniform mat4 world;
uniform mat4 view;
uniform mat4 proj;

layout (location = 0) out vec3 f_Color;
layout (location = 1) out vec3 f_Normal;
layout (location = 2) out vec2 f_TexCoords;
layout (location = 3) out vec4 f_VPosition;

void main() {
    gl_Position = proj * view * world * vec4(position, 1.0);
    f_Color = color;
    f_Normal = normal;
    f_TexCoords = texCoords;
    f_VPosition = world * vec4(position, 1.0);
}