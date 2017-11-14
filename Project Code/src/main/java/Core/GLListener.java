package Core;

import GUI.PauseMenu;
import World.World;

//import com.jogamp.opengl.*;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.util.GLBuffers;


//import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_POLYGON_OFFSET_FILL;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_VERSION;

import static com.jogamp.opengl.GL2ES2.GL_SHADING_LANGUAGE_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;


import java.nio.IntBuffer;

/**
 * Created by (User name) on 7/25/2017.
 */
public class GLListener implements GLEventListener {

    private World world;
    private Shader shader;
    private PauseMenu pauseMenu;

    public static GL4 gl;

    long currentTime;

    public GLListener(){
        world = new World("Project Code/src/main/resources/race.json");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL4();

        printVersionInfo(drawable);

        //paints background.
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        // Load and compile the shaders
        try {
            shader = new Shader();
            shader.compileStageFile("Project Code/src/main/resources/game.vert.glsl");
            shader.compileStageFile("Project Code/src/main/resources/game.frag.glsl");
            shader.link();
            shader.use();
        }
        catch(Shader.ShaderException e) {
            System.err.println("Error compiling shaders:");
            System.err.println(e.what());
            System.err.println(e.getOpenGLLog());
        }


        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1.0f, 1.0f);

        world.init();
        pauseMenu = PauseMenu.getInstance();

        //initial framerate won't be completely accurate, but subsequent updates should be.
        currentTime = System.currentTimeMillis();

    }

    int count = 0;
    @Override
    public void display(GLAutoDrawable drawable) {
        long temp = System.currentTimeMillis();
        long displayLatency = temp - currentTime;
        currentTime = temp;
        float framerate = 1 / ((float)displayLatency/1000.0f);
        if (count % 100 == 5)
            System.out.printf("Display calls/Second: %.1f\n", framerate);
        count++;

        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //render calls
        world.render(shader);

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        gl.glViewport(0,0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

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



}
