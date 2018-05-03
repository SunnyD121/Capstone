#version 430    //used to be 410 but i wanted array of arrays

///Key:
// = normal comment, keep it.
/// = useful stuff, but not wanted currently.
///// = for temporary testing. Remove when no longer needed.

//uniform vec4 color = vec4(1,0,0,1);
const int maxLightCount = 100;
uniform int lightCount;
uniform vec3 lights[maxLightCount];    //the light of the lamps' positions.
uniform vec3 lampIntensity;
const int maxAIPlayers = 10;       //number of AI players in the game
uniform int numAIPlayers;
uniform vec3 laserPositions[maxAIPlayers+1][10];  //position of laser's light, max 10 lasers before auto-death
uniform int numLiveLasers[maxAIPlayers+1];      //the amount of lasers that are actually alive
uniform vec3 laserIntensity[maxAIPlayers+1];
uniform vec3 flashLocation;     //where flash occurs
uniform vec3 flashColor;    //color of flash
uniform float flashIntensity;
uniform vec3 Kd;  // Diffuse reflectivity
uniform vec3 Ks;
uniform vec3 Ka;
uniform float shine;
uniform vec3 emission;          //how much light is emitted.
uniform vec3 sunDirection;      //direction the sun beams down
uniform vec3 sunIntensity;      //how strong the sun is
uniform float textureAlpha;
uniform sampler2D tex;
//uniform sampler2D testTexture[2];
uniform sampler2DShadow shadowMap;

vec3 ambientIntensity = vec3(0.05f);    //how strong the ambient light of the scene is. (alternative to sun?)

in vec3 normal;
in vec3 eyePos;
in vec2 texCoords;
in vec4 shadowCoord;
in mat4 WorldToEyeMatrix;

out vec4 fragColor;

//NOTE: functions must be declared first before they can be used. Order matters.

void error(){
    fragColor = vec4(1,0,1,1);
    return;
}

//Find max of r g b of a single light, and set all three values to {r,g,b}/max, so that the object in light retains color information.
vec3 lightWashOut(vec3 color){
    float max = 1;
    if (color.x > 1 || color.y > 1 || color.z > 1){     //this line feels like a hack, but the algorithm doesn't work without it.
        if (color.x > color.y) max = color.x;
        else max = color.y;
        if (max < color.z) max = color.z;
    }
    return vec3(color.x/max, color.y/max, color.z/max);
}

vec3 convertLightLocation(vec3 loc){        //takes the AbstractWorld Location and converts it to the Eye Location
    vec4 temp = vec4(loc, 1.0f);
    temp = WorldToEyeMatrix * temp;
    return temp.xyz;
}

vec3 lightEquation1(vec3 intensity, vec3 diffuseComponent, float d, vec3 n, vec3 l, vec3 h){
    vec3 light = (1.0/(d*d)) * intensity * (diffuseComponent * max(dot(n,l), 0.0) + Ks * pow(max(dot(h,n),0.0), shine));

    //High Dynamic Range adjustment
        light = lightWashOut(light);    //retains integrity of the original color (no whitewashing)

    return light;
}

vec3 lightEquation2(vec3 intensity, float d){
    vec3 light = (1.0/(d*d)) * intensity;
    return light;
}

subroutine void renderPassType();
subroutine uniform renderPassType renderPass;

subroutine (renderPassType) void renderScene(){
    bool debug = false;
    bool debugR = false;
    bool debugG = false;
    bool debugB = false;
    if (numAIPlayers > maxAIPlayers) error();
    if (lightCount > maxLightCount) error();
    //if (!gl_FrontFacing)
        //discard;    //culls fragments that are being viewed from places players aren't meant to be.
        //EDIT: the above is handled in GLListener.java
    vec3 color = vec3(0.0, 0.0, 0.0);
    vec3 texColor = texture(tex, texCoords).xyz; //NOTE: assuming the alpha is 1.0, will tack it back on later.
    vec3 diffuseComponent = Kd + texColor;
    vec3 ambientComponent = Ka + texColor;
    //vec3 color = diffuseComponent;
    vec3 v = normalize(-eyePos);
    vec3 n = normalize(normal);

    float shadow = 1.0f;

    if( shadowCoord.z >= 0 ) {
        shadow = textureProj(shadowMap, shadowCoord);
    }

    //sun
    vec3 l = normalize(sunDirection);
    vec3 h = normalize(l + v);
    //light equation, sun
    color += sunIntensity * (diffuseComponent * max(dot(n,l),0.0) + Ks * pow(max(dot(h,n),0.0), shine));
    //if fragment is in shadow, only use ambient light (shadow = 0)

    color *= shadow;

    if (debug) color= vec3(1,0,0);

    color += ambientIntensity * ambientComponent;
    color += emission;

    //lamps
    for(int i = 0; i < 12; i++){    //for each of the 12 (lamp) light sources:
        vec3 lightPos = convertLightLocation(lights[i]);
        float d = length(lightPos - eyePos);   //distance of light source from fragment in Camera/Eye space
        vec3 l = normalize(lightPos - eyePos); //direction of that distance
        vec3 h = normalize(l + v);      //TODO: figure out what this is. Guess: direction of light source to Camera location

        //light equation, lamps
        color += lightEquation1(lampIntensity, diffuseComponent, d, n, l, h);

    }

    //lasers
    for (int a=0; a < numAIPlayers+1; a++){     //+1 because player is [0]
        for (int i=0; i < numLiveLasers[a]; i++){
            vec3 lightPos = convertLightLocation(laserPositions[a][i]);

            float d = length(lightPos - eyePos);
            //vec3 l = normalize(lightPos - eyePos);
            //vec3 h = normalize(l + v);

            color += lightEquation2(laserIntensity[a], d);
        }
    }

    //deathflash
    vec3 lightPos = convertLightLocation(flashLocation);
    float d = length(lightPos - eyePos);
    color += lightEquation2(flashColor*flashIntensity, d);


    if (debugR || debugG || debugB) color = vec3(0,0,0);
    if (debugR) color += vec3(1,0,0);
    if (debugG) color += vec3(0,1,0);
    if (debugB) color += vec3(0,0,1);

    fragColor = vec4(color, 1.0);
}

subroutine (renderPassType) void createShadows(){
    //do nothing?
}
subroutine (renderPassType) void menuScreen(){

    //fragColor = vec4((texture(tex, texCoords).xyz), textureAlpha);
    vec4 color = texture(tex, texCoords);
    if (color.w == 1) color.w = textureAlpha;
    fragColor = color;

}

void main() {
    //calls either createShadows or renderScene or menuScreen, based on which renderPass is given, from the java code.
    renderPass();
}