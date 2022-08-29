#version 300 es
precision mediump float;

#define NUM_LIGHTS 3;

uniform vec3 camPosition;
uniform vec3 lightPosition[3];
uniform vec3 lightColor[3];

uniform float shininess;
uniform vec3 fresnelR0;

uniform sampler2D texture1;
uniform mat4 texTransform;

in vec3 f_Normal;
in vec2 f_TexCoords;
in vec4 f_VPosition;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(texture1, (texTransform * vec4(f_TexCoords, 1.0, 1.0)).xy);
    fragColor = textureColor;
}
