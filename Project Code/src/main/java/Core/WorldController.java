package Core;

import Core.InputSystem.InputHandler;
import World.Worlds.AbstractWorld;
import World.Worlds.DemoWorld;
import World.Worlds.SandWorld;
import World.Worlds.SnowWorld;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static Core.GLListener.gl;
import static Core.WorldController.WorldType.DEMO;
import static Core.WorldController.WorldType.SAND;
import static Core.WorldController.WorldType.SNOW;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL2ES2.GL_DEPTH_COMPONENT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

public class WorldController {

    private int screenWidth = 800;
    private int screenHeight = 600;
    private float oAspect = 1;
    private AbstractWorld world;
    private Shader shader;
    private Camera shadowCamera;
    private final int SHADOWFRAME = 1;
    private int shadowTex;
    private final int SHADOWMAP_WIDTH = 2048;
    private final int SHADOWMAP_HEIGHT = 2048;
    private int shadowPassID, renderPassID;
    //public static GL4 gl;
    private boolean isWorldDefined;
    private WorldType worldType;

    public WorldController(){
        isWorldDefined = false;
    }

    public enum WorldType{
        SNOW, SAND, DEMO
    }

    public void setWorldType(WorldType wType){
        this.worldType = wType;
    }

    public void createWorld(){
        isWorldDefined = true;

        if (worldType == DEMO) {
            gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
            world = new DemoWorld("Project Code/src/main/resources/JSON files/demo.json");
        }
        else if (worldType == SAND) {
            gl.glClearColor(1.0f, 1.0f, 0.37f, 1.0f);
            world = new SandWorld("Project Code/src/main/resources/JSON files/sand.json");
        }
        else if (worldType == SNOW) {
            gl.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
            world = new SnowWorld("Project Code/src/main/resources/JSON files/snow.json");
        }
        else {
            System.err.println("Unregistered World Type: " + worldType);
            System.exit(-1);
        }

        world.init();
        updateScreenSize(screenWidth, screenHeight, oAspect);
        InputHandler.getInstance().setContext(world, InputHandler.Context.OVERWORLD);


        //More shadow stuff
        shadowCamera = new Camera(Camera.ProjectionType.ORTHO);
        shadowCamera.setOrtho(-100, 100, -100, 100, -100, 100);
        //shadowCamera.setViewVolume(50.0f,1.0f,1.0f,25.0f);        //EDIT: Do NOT use this line of code, breaks shadow map visual
        Matrix4f shadowBias = new Matrix4f(
                0.5f,0.0f,0.0f,0.0f,
                0.0f,0.5f,0.0f,0.0f,
                0.0f,0.0f,0.5f,0.0f,
                0.5f,0.5f,0.5f,1.0f);
        Matrix4f lightCoord = new Matrix4f();
        shadowBias.mul(shadowCamera.getProjectionMatrix(), lightCoord);  //Projection
        lightCoord.mul(new Matrix4f().lookAt(world.getSunlightDirection(), new Vector3f(0), new Vector3f(0,1,0)));  //View

        //ShadowMatrix = (Bias * Projection) * WorldToEye
        shader.setUniform("ShadowMatrix", lightCoord);
        shader.setUniform("shadowMap", 0);  //sampler2D

    }

    public boolean getWorldIsDefined(){ return isWorldDefined;}
    public void setWorldIsDefined(boolean b){
        this.isWorldDefined = b;
    }

    public void init(){
        shader = Shader.getInstance();
        /* Shadow Map */
        //Setup a texture to store the Shadow Map
        setupShadowTexture();

        //Use that texture, in channel 0
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex);

        //FrameBuffer stuff
        int frameBufferCount = 1;   //total number of framebuffers, not counting the default one the scene renders to.
        IntBuffer frameBufferObject = GLBuffers.newDirectIntBuffer(new int[frameBufferCount]);  //Intbuffer storing the framebuffer id's
        gl.glGenFramebuffers(frameBufferCount, frameBufferObject);  //arg1: number of FrameBuffers to create, arg2: data structure storing pointers
        //NOTE: 0 is the default framebuffer, the one the scene gets rendered to.
        gl.glBindFramebuffer(GL_FRAMEBUFFER, SHADOWFRAME);  //SHADOWFRAME = 1
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowTex, 0);

        //NOTE: instead of SHADOWFRAME = 1, use framebufferobject.get(X) (if multiple framebuffers)

        int successfulSetup = gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (successfulSetup == GL_FRAMEBUFFER_COMPLETE)
            System.out.println("Shadow map framebuffer successfully loaded.");
        else
            System.err.println("Shadow map framebuffer encountered errors.");

        //unbind shadow framebuffer, so we see the scene.
        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //Subroutine ID's
        shadowPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "createShadows");
        renderPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "renderScene");

    }

    public void drawWorld(boolean pureDraw){
        generateShadows();
        renderScene(pureDraw);
        if (!pureDraw) drawScore();

    }

    private void drawScore(){
        world.drawScore();
    }

    private void renderScene(boolean pureDraw){
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0,0, screenWidth,screenHeight);    //same as glWindow in Main.java
        int[] subroutinePass = new int[] {renderPassID};
        gl.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, IntBuffer.wrap(subroutinePass));  //for the shader
        world.render(pureDraw);
    }

    private void generateShadows(){
        shader.setUniform("WorldToEye", new Matrix4f().lookAt(world.getSunlightDirection(), new Vector3f(0), new Vector3f(0,1,0)));
        shader.setUniform("Projection", shadowCamera.getProjectionMatrix());

        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, SHADOWFRAME);
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        gl.glViewport(0,0,SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);
        int[] subroutinePass = new int[] {shadowPassID};
        gl.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, IntBuffer.wrap(subroutinePass));
        gl.glEnable(GL_CULL_FACE);
        gl.glCullFace(GL_FRONT);    //gets rid of Shadow Acne, but shadows now have holes in them from the culling.
        world.drawShadowedObjects(true);    //separation of shadowed Objects and nonShadowed.
        gl.glCullFace(GL_BACK); //for normal rendering. disables frontface culling.
        gl.glFlush();
        //seeShadowMapImage(); System.exit(0);  //uncomment this only if you want to see the ShadowMap.

    }

    private void setupShadowTexture(){
        float[] border = {1.0f, 0.0f, 0.0f, 0.0f};

        gl.glActiveTexture(GL_TEXTURE0);
        IntBuffer textureBuffer = GLBuffers.newDirectIntBuffer(new int[1]); //buffer holding pointers to textures
        gl.glGenTextures(1, textureBuffer);     //associates list of textures with this buffer
        shadowTex = textureBuffer.get(0);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex);
        gl.glTexStorage2D(GL_TEXTURE_2D,1, gl.GL_DEPTH_COMPONENT24, SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT);    //parameters for the depth texture
        //Texture modifications, //TODO: learn about the ones you don't know about.
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_BORDER);
        gl.glTexParameterfv(GL_TEXTURE_2D, gl.GL_TEXTURE_BORDER_COLOR, FloatBuffer.wrap(border));
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_COMPARE_MODE, gl.GL_COMPARE_REF_TO_TEXTURE);
        gl.glTexParameteri(GL_TEXTURE_2D, gl.GL_TEXTURE_COMPARE_FUNC, gl.GL_LESS);

        //unbind
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void seeShadowMapImage(){
        int size = SHADOWMAP_WIDTH * SHADOWMAP_HEIGHT;
        FloatBuffer buffer = GLBuffers.newDirectFloatBuffer(new float[size]);
        gl.glBindTexture(GL_TEXTURE_2D, shadowTex);
        gl.glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);
        float[] depthValues = new float[size];
        float min = 1.0f;
        for (int i = 0; i < buffer.capacity(); i++){
            depthValues[i] = buffer.get(i);
            if (depthValues[i] < min) min = depthValues[i];
        }
        //System.out.println(Utilities.fBufToString(buffer));
        //float[] depthValues = buffer.array(); //sad this doesn't work.
        float[] pixels = new float[size * 4];

        for(int i = 0; i < SHADOWMAP_HEIGHT; i++){
            for(int j = 0; j < SHADOWMAP_WIDTH; j++){
                int imageIndex = 4 * (i * SHADOWMAP_WIDTH + j);
                int depthBufferIndex = (SHADOWMAP_HEIGHT - i - 1) * SHADOWMAP_WIDTH + j;

                float minLightVal = min;
                float scaleBy = (depthValues[depthBufferIndex] - minLightVal) / (1.0f - minLightVal);
                float pixelValue = (scaleBy) * 255; //NOTE: to see reverse, (1 - scaleBy)

                pixels[imageIndex] = pixelValue;  //R
                pixels[imageIndex+1] = pixelValue;  //G
                pixels[imageIndex+2] = pixelValue;  //B
                pixels[imageIndex+3] = 255f;  //A
            }
        }
        createPicture(pixels);

    }

    private void createPicture(float[] data){
        BufferedImage b = new BufferedImage(SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT, BufferedImage.TYPE_INT_ARGB);    //Despite this, the pixels are still read in RGBA.
        WritableRaster r = b.getRaster();
        r.setPixels(0,0,SHADOWMAP_WIDTH, SHADOWMAP_HEIGHT, data);
        File f = new File("ShadowMapVisualization.png");
        try{
            ImageIO.write(b, "png", f);}
        catch (IOException e){e.printStackTrace();}
        System.out.println("Picture Creation Successful.");

    }

    public void updateScreenSize(int width, int height, float aspect){
        this.screenWidth = width;
        this.screenHeight = height;
        this.oAspect = aspect;
        if (world != null) world.updateAspectRatio(((float)width/height)/aspect);

    }

    public AbstractWorld getWorld(){
        return world;
    }

    public void destroyWorld(){
        world = null;
        AbstractWorld.destroyStaticData();
        shader.nullifyUniforms();
    }
}
