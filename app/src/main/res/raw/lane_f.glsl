#version 300 es
precision mediump float;

#define NUM_LIGHTS 3;

uniform vec3 camPosition;
uniform vec3 lightPosition[3];
uniform vec3 lightColor[3];

uniform float shininess;
uniform vec3 fresnelR0;

layout (location = 0) in vec3 f_Color;
layout (location = 1) in vec3 f_Normal;
layout (location = 2) in vec2 f_TexCoords;
layout (location = 3) in vec4 f_VPosition;

out vec4 fragColor;

vec3 SchlickFresnel(vec3 R0, vec3 normal, vec3 lightVec)
{
    float cosIncidentAngle = clamp(dot(normal, lightVec), 0.0, 1.0);

    float f0 = 1.0f - cosIncidentAngle;
    vec3 reflectPercent = R0 + (1.0f - R0)*(f0*f0*f0*f0*f0);

    return reflectPercent;
}

vec3 BlinnPhong(vec3 lightStrength, vec3 lightVec, vec3 normal, vec3 toEye, float shininess, vec3 fresnelR0, vec3 diffuseAlbedo)
{
    float m = shininess * 256.0f;
    vec3 halfVec = normalize(toEye + lightVec);

    float roughnessFactor = (m + 8.0f)*pow(max(dot(halfVec, normal), 0.0f), m) / 8.0f;
    vec3 fresnelFactor = SchlickFresnel(fresnelR0, halfVec, lightVec);
    vec3 specAlbedo = fresnelFactor*roughnessFactor;

    return (diffuseAlbedo + specAlbedo) * lightStrength;
}

void main() {
    vec3 finalColor = f_Color;

    vec3 normal = normalize(f_Normal);
    //float dampling = 1.0 - clamp((length(distF) - 1.0) / 500.0, 0.0, 0.99);
    vec3 toEye = normalize(camPosition - f_VPosition.xyz);

    vec3 result = vec3(0.0, 0.0, 0.0);
    for (int i = 0; i < 3; ++i) {
        float distF = length(f_VPosition.xyz - lightPosition[i]);
        vec3 dist = normalize(f_VPosition.xyz - lightPosition[i]);
        float dampling = 1.0 / pow(distF, 2.0);
        vec3 strength = max(dot(-dist, normal), 0.0) * (dampling * lightColor[i]);
        result += BlinnPhong(strength, -dist, normal, toEye, shininess, fresnelR0, finalColor);
    }
    fragColor = vec4(result + 0.2 * finalColor, 1.0);
}