#version 300 es
precision mediump float;

uniform vec3 camPosition;
uniform vec3 lightPosition;
uniform vec3 lightColor;

uniform float shininess;
uniform vec3 fresnelR0;

layout (location = 0) in vec3 f_Color;
layout (location = 1) in vec3 f_Normal;
layout (location = 2) in vec2 f_TexCoords;
layout (location = 3) in vec4 f_VPosition;

out vec4 fragColor;

float Cut1(float var)
{
    return 0.33 * floor(pow(var, 0.7) * 3.0);
}

float Cut2(float var)
{
    return 0.25 * floor(pow(var, 0.3) * 4.0);
}

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

    /*// Our spec formula goes outside [0,1] range, but we are
    // doing LDR rendering.  So scale it down a bit.
    specAlbedo = specAlbedo / (specAlbedo + 1.0f);
    specAlbedo.x = Cut2(specAlbedo.x);
    specAlbedo.y = Cut2(specAlbedo.y);
    specAlbedo.z = Cut2(specAlbedo.z);
    vec3 newDiffuseAlbedo = diffuseAlbedo;
    newDiffuseAlbedo.r = Cut1(newDiffuseAlbedo.r);
    newDiffuseAlbedo.g = Cut1(newDiffuseAlbedo.g);
    newDiffuseAlbedo.b = Cut1(newDiffuseAlbedo.b);
    vec3 newLightStrength = lightStrength;
    newLightStrength.r = Cut1(newLightStrength.r);
    newLightStrength.g = Cut1(newLightStrength.g);
    newLightStrength.b = Cut1(newLightStrength.b);*/

    return (diffuseAlbedo + specAlbedo) * lightStrength;
}

void main() {
    vec3 finalColor = f_Color;
    vec3 dist = f_VPosition.xyz - lightPosition;
    vec3 normal = normalize(f_Normal);
    vec3 strength = max(dot(-dist, normal), 0.0) * (lightColor / pow(length(dist),1.5));

    vec3 toEye = normalize(camPosition - f_VPosition.xyz);

    fragColor = vec4(BlinnPhong(strength, -dist, normal, toEye, shininess, fresnelR0, finalColor), 1.0);
}