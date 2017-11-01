#version 410

///Key:
// = normal comment, keep it.
/// = useful stuff, but not wanted currently.
///// = for temporary testing. Remove when no longer needed.

//uniform vec4 color = vec4(1,0,0,1);
uniform vec3 lights[12];    //the light of the lamps' positions.
uniform vec3 lampIntensity;
uniform vec3 Kd;  // Diffuse reflectivity
uniform vec3 Ks;
uniform vec3 Ka;
uniform float shine;
uniform vec3 emission;          //how much light is emitted.
uniform vec3 sunDirection;      //direction the sun beams down
uniform vec3 sunIntensity;      //how strong the sun is
uniform vec3 ambientIntensity;  //how strong the ambient light of the scene is. (alternative to sun?)
uniform sampler2D tex;

in vec3 normal;
in vec3 eyePos;
in vec2 texCoords;

out vec4 fragColor;

void main() {
    if (!gl_FrontFacing)
        discard;    //culls fragments that are being viewed from places players aren't meant to be.
    vec3 color = vec3(0.0, 0.0, 0.0);
    vec3 v = normalize(-eyePos);
    vec3 n = normalize(normal);
    for(int i = 0; i < 12; i++){    //for each of the 12 (lamp) light sources:
        float d = length(lights[i] - eyePos);
        vec3 l = normalize(lights[i] - eyePos);
        vec3 h = normalize(l + v);

        //light equation, lamps
        color += (1.0/(d*d)) * lampIntensity * (Kd * max(dot(n,l), 0.0) + Ks * pow(max(dot(h,n),0.0), shine));  //find brightest lamp and use it to 1/color the others
    }
    //for the sun:
    vec3 l = normalize(sunDirection);
    vec3 h = normalize(l + v);

    //light equation, sun
    color += sunIntensity * (Kd * max(dot(n,l),0.0) + Ks * pow(max(dot(h,n),0.0), shine));

    color += ambientIntensity * Ka;
    color += emission;


    vec4 texColor = texture(tex, texCoords);

    fragColor = vec4(color, 1.0);
    fragColor += texColor;  //replace diffuse light with the texture color and continue light calculations from there?

    fragColor = vec4(texCoords, 0.0, 0.0);  //all my textcoordinates are 0
}