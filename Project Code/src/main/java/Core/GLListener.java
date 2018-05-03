package Core;

import Core.InputSystem.*;
import Core.Screens.GameOverScreen;
import Core.Screens.LevelSelectScreen;
import Core.Screens.PauseScreen;
import Core.Screens.SplashScreen;
import GUI.ChangeKeyBindings;
import GUI.PauseMenu;

//import com.jogamp.opengl.*;
import World.AbstractShapes.Cube;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.util.GLBuffers;


//import static com.jogamp.opengl.GL.*;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_SHADING_LANGUAGE_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;


import java.nio.IntBuffer;

/**
 * Created by (User name) on 7/25/2017.
 */
public class GLListener implements GLEventListener, SplashInputs, LevelSelectInputs, PauseInputs, GameOverInputs {

    private int screenWidth = 800;
    private int screenHeight = 600;
    private final float originalAspectRatio = (float)screenWidth/screenHeight;
    private Shader shader;
    //private PauseMenu pauseMenu;
    private PauseScreen pauseScreen;
    private SplashScreen splashScreen;
    private LevelSelectScreen levelSelectScreen;
    private GameOverScreen gameOverScreen;
    public static GL4 gl;
    private long currentTime;
    private WorldController worldController;

    private static boolean onSplashScreen = true;
    private boolean onLevelSelectScreen = false;
    private static boolean onGameOverScreen = false;
    private static boolean isPaused = false;

    private static GLListener thisClass;


    private GLListener(){
        InputHandler.getInstance().setContext(this, InputHandler.Context.SPLASH);    //TODO Perhaps write a class to separate GLListener out
        worldController = new WorldController();
        splashScreen = new SplashScreen();
        levelSelectScreen = new LevelSelectScreen();
        pauseScreen = new PauseScreen();
        gameOverScreen = new GameOverScreen();
    }

    public static GLListener getInstance(){
        if (thisClass == null) thisClass = new GLListener();
        return thisClass;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL4();

        printVersionInfo(drawable);

        //paints background.
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //gl.glClearColor(0,0,0,1);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1.0f, 1.0f);

        // Load and compile the shaders
        shader = Shader.getInstance();
        compileAndLinkShaders();

        splashScreen.init();
        levelSelectScreen.init();
        //world.init();
        worldController.init();
        //pauseMenu = PauseMenu.getInstance();
        pauseScreen.init();
        gameOverScreen.init();


        //initial framerate won't be completely accurate, but subsequent updates should be.
        currentTime = System.currentTimeMillis();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        computeCodeExecutionSpeed();
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //TODO instead of all these booleans for which screen to render, perhaps a more elegant solution?
        if (onSplashScreen) {
            gl.glViewport(0,0, screenWidth,screenHeight);
            splashScreen.render();
        }
        else if (onLevelSelectScreen) {
            gl.glViewport(0, 0, screenWidth, screenHeight);
            levelSelectScreen.render();
        }
        else if (onGameOverScreen){
            gl.glViewport(0, 0, screenWidth, screenHeight);
            gameOverScreen.render();
        }
        else {
            if (!isPaused) {
                if (!worldController.getWorldIsDefined()) worldController.createWorld();
                worldController.drawWorld(false);
            }
            else {
                worldController.drawWorld(true);
                pauseScreen.render();
            }
        }
    }

    @Override
    public void select() {
        InputHandler.getInstance().setContext(this, InputHandler.Context.LEVELSELECT);
        onLevelSelectScreen = true;
        worldController.destroyWorld();
        onSplashScreen = false;


    }

    @Override
    public void select(int value) {
        worldController.setWorldIsDefined(false);
        switch (value){
            case 1:
                worldController.setWorldType(WorldController.WorldType.DEMO);
                break;
            case 2:
                worldController.setWorldType(WorldController.WorldType.SAND);
                break;
            case 3:
                worldController.setWorldType(WorldController.WorldType.SNOW);
                break;
            default:
                System.err.println("Something unexpected happened!");
                System.exit(-1);
        }
        onLevelSelectScreen = false;
    }
    @Override
    public void unPause() {
        InputHandler.getInstance().setContext(worldController.getWorld(), InputHandler.Context.OVERWORLD);
        isPaused = false;
    }
    @Override
    public void selectPanel() {
        int index = pauseScreen.getButtonIndex();
        pauseScreen.selectButton(index);
        if (index == 2){
            worldController.destroyWorld();
            select();
            isPaused = false;
        }


    }
    @Override
    public void moveUp(){
        pauseScreen.moveSelectorUp();
    }
    @Override
    public void moveDown(){
        pauseScreen.moveSelectorDown();
    }

    public static void relinquishInputControl(InputHandler.Context c){
        switch (c){
            case PAUSEMENU:
                InputHandler.getInstance().setContext(thisClass, c);
                isPaused = true;
            break;
            case GAMEOVER:
                InputHandler.getInstance().setContext(thisClass, c);
                onGameOverScreen = true;
            break;
            case SPLASH:
                InputHandler.getInstance().setContext(thisClass, c);
                onGameOverScreen = false;   //GameOverScreen is the only place this context is passed as an arg
                onSplashScreen = true;
            break;

            default:
                System.err.println("relinquishInputControl(): Something unexpected just happened.");
                System.exit(-1);
        }

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        //x,y don't appear to change, at all? constantly 0.
        //gl.glViewport(0,0, width, height);
        screenHeight = height;
        screenWidth = width;
        worldController.updateScreenSize(screenWidth, screenHeight, originalAspectRatio);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("Yay I got called from...somewhere!");
    }

    private void compileAndLinkShaders(){
        try {
            shader.compileStageFile("Project Code/src/main/resources/game.vert.glsl");
            shader.compileStageFile("Project Code/src/main/resources/game.frag.glsl");
            shader.link();
            shader.use();
            System.out.println("Shaders successfully compiled and linked.");
        }
        catch(Shader.ShaderException e) {
            System.err.println("Error compiling shaders:");
            System.err.println(e.what());
            System.err.println(e.getOpenGLLog());
        }
    }

    private void printVersionInfo(GLAutoDrawable drawable) {
        IntBuffer value = GLBuffers.newDirectIntBuffer(1);

        String glVendor = gl.glGetString(GL_VENDOR);
        String glRenderer = gl.glGetString(GL_RENDERER);
        String glVersion = gl.glGetString(GL_VERSION);
        String glslVersion = gl.glGetString(GL_SHADING_LANGUAGE_VERSION);
        gl.glGetIntegerv(GL_MAJOR_VERSION, value);
        int major = value.get(0);
        gl.glGetIntegerv(GL_MINOR_VERSION, value);
        int minor = value.get(0);
        String profile = drawable.getGLProfile().getName();
        GLCapabilitiesImmutable caps = drawable.getChosenGLCapabilities();

        System.out.printf("Version Information\n");
        System.out.printf("==========================================================\n");
        System.out.printf("OpenGL version:              %d.%d\n", major, minor);
        System.out.printf("OpenGL profile:              %s\n", profile);
        System.out.printf("Depth buffer size:           %d\n", caps.getDepthBits());
        System.out.printf("Color buffer:                (%d, %d, %d, %d)\n",
                caps.getRedBits(), caps.getGreenBits(), caps.getBlueBits(), caps.getAlphaBits());
        System.out.printf("GL_VENDOR:                   %s\n", glVendor);
        System.out.printf("GL_RENDERER:                 %s\n", glRenderer);
        System.out.printf("GL_VERSION:                  %s\n", glVersion);
        System.out.printf("GL_SHADING_LANGUAGE_VERSION: %s\n", glslVersion);
        System.out.printf("JOGL Version:                %s\n",
                JoglVersion.getInstance().getImplementationVersion());
        System.out.printf("==========================================================\n");
    }

    int count = 0;
    long[] times = new long[10];
    private void computeCodeExecutionSpeed(){
        long temp = System.currentTimeMillis();
        long displayLatency = temp - currentTime;
        currentTime = temp;
        times[count % times.length] = displayLatency;
        double average = 0;
        for (long num : times) average += (num/(double)times.length);
        float framerate = 1 / ((float)average/1000.0f);
        if (count % 100 == 5)
            System.out.printf("Display calls/Second: %.1f\n", framerate);
        count++;

    }


}