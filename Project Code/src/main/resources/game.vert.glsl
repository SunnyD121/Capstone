#version 410

layout(location=1) in vec4 vPosition;
layout(location=2) in vec4 vNormal;
//layout(location=3) in vec4 vColor;    //Color given from TriangleMesh's.
layout(location=4) in vec2 vTexCoord;

uniform mat4 ObjectToWorld;
uniform mat4 Projection;
uniform mat4 WorldToEye;

//return variables; must match in vars @.frag.glsl
out vec3 normal;
out vec3 eyePos;
out vec2 texCoords;

void main() {
    mat4 toEye = WorldToEye * ObjectToWorld;
    eyePos = (toEye * vPosition).xyz;   //The position in eye coords.

    //The top left 3x3 of: view * model
    mat3 normMatrix = mat3(toEye[0].xyz, toEye[1].xyz, toEye[2].xyz);

    //The normal vector in Eye coords.
    normal = normalize(normMatrix * vNormal.xyz);

    //sending texture data to other shader
    texCoords = vTexCoord;

    gl_Position = Projection * toEye * vPosition;
}