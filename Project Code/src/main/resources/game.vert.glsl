#version 410

layout(location=1) in vec4 vPosition;   //coordinates in shape coordinate space
layout(location=2) in vec4 vNormal;
//layout(location=3) in vec4 vColor;    //Color given from TriangleMesh's.
layout(location=4) in vec2 vTexCoord;

uniform mat4 ObjectToWorld; //for converting from shape space to world space
uniform mat4 Projection;    //TODO: remember what this is for. Something about necessary further down graphics pipeline.
uniform mat4 WorldToEye;    //for converting from world space to camera space (where we render the scene from)
uniform mat4 ShadowMatrix;  //for computing the location of shadow...?  //TODO: Investigate this.

//return variables; must match in vars @.frag.glsl
out vec3 normal;
out vec3 eyePos;    //the position of the surface in eye coordinates
out vec2 texCoords;
out vec4 shadowCoord;

void main() {
    mat4 toEye = WorldToEye * ObjectToWorld;
    eyePos = (toEye * vPosition).xyz;   //The position in eye coords.

    //The top left 3x3 of: view * model
    mat3 normMatrix = mat3(toEye[0].xyz, toEye[1].xyz, toEye[2].xyz);

    //The normal vector in Eye coords.
    normal = normalize(normMatrix * vNormal.xyz);

    //sending texture data to other shader
    texCoords = vTexCoord;

    //shadow Coordinates
    shadowCoord = (ShadowMatrix * ObjectToWorld) * vPosition;

    gl_Position = Projection * toEye * vPosition;
}