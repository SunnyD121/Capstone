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
uniform sampler2DShadow shadowMap;

in vec3 normal;
in vec3 eyePos;
in vec2 texCoords;
in vec4 shadowCoord;

out vec4 fragColor;

//NOTE: functions must be declared first before they can be used. Order matters.

//High Dynamic Range. Find max of r g b of a single light, and set all three values to {r,g,b}/max, so that the object in light retains color information.
vec3 highDynamicRange(vec3 color){
    float max = 1;
    if (color.x > 1 || color.y > 1 || color.z > 1){     //this line feels like a hack, but the algorithm doesn't work without it.
        if (color.x > color.y) max = color.x;
        else max = color.y;
        if (max < color.z) max = color.z;
    }
    return vec3(color.x/max, color.y/max, color.z/max);
}

subroutine void renderPassType();
subroutine uniform renderPassType renderPass;

subroutine (renderPassType) void renderScene(){
    bool debug = false;
    if (!gl_FrontFacing)
        discard;    //culls fragments that are being viewed from places players aren't meant to be.
    vec3 color = vec3(0.0, 0.0, 0.0);
    vec4 texColor = texture(tex, texCoords);
    vec3 diffuse = Kd + texColor.xyz;   //NOTE: assuming the alpha is 1.0, will tack it back on later.
    vec3 v = normalize(-eyePos);
    vec3 n = normalize(normal);

    float shadow = 1.0;
        if( shadowCoord.z >= 0 ) {
            shadow = textureProj(shadowMap, shadowCoord);
        }

    for(int i = 0; i < 12; i++){    //for each of the 12 (lamp) light sources:
        float d = length(lights[i] - eyePos);   //distance of light source from fragment in Camera/Eye space
        vec3 l = normalize(lights[i] - eyePos); //direction of that distance
        vec3 h = normalize(l + v);      //TODO: figure out what this is. Guess: direction of light source to Camera location

        //light equation, lamps
        color += (1.0/(d*d)) * lampIntensity * (diffuse * max(dot(n,l), 0.0) + Ks * pow(max(dot(h,n),0.0), shine));

    }
    //for the sun:
    vec3 l = normalize(sunDirection);
    vec3 h = normalize(l + v);

    //light equation, sun
    color += sunIntensity * (diffuse * max(dot(n,l),0.0) + Ks * pow(max(dot(h,n),0.0), shine));
    //color = color * shadow;   //TODO: fix this when ready
    color += ambientIntensity * Ka;
    color += emission;

    //High Dynamic Range adjustment
    color = highDynamicRange(color);

    fragColor = vec4(color, 1.0);
}

subroutine (renderPassType) void createShadows(){
    //do nothing?
}

void main() {
    //calls either createShadows or renderScene, based on which renderPass is given, from the java code.
    renderPass();
}