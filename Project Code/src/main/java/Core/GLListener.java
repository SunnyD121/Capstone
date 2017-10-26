package Core;

import World.World;
import World.Player;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
//import com.jogamp.opengl.*;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.JoglVersion;
import com.jogamp.opengl.util.GLBuffers;

import static Core.GLListener.CameraMode.CHASE;
import static Core.GLListener.CameraMode.OBSERVER;
import static Core.GLListener.CameraMode.PHOTO;
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

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.KeyEvent;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

//import java.awt.*;
import java.awt.Point;
import java.awt.MouseInfo;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by (User name) on 7/25/2017.
 */
public class GLListener implements GLEventListener, KeyListener, MouseListener{

    private World world;
    private Player player;
    private Shader shader;
    private Camera cam1, cam2, cam3;
    private final float MAX_ZOOM_OUT = 2.5f;
    private final float MAX_ZOOM_IN = 0.3f;
    private float zoom_factor = 1;
    private Point premouse, postmouse;
    private boolean mouse3down = false;
    private ConcurrentLinkedQueue<Integer> keysDown;
    public static GL4 gl;

    protected enum CameraMode{
        CHASE, PHOTO, OBSERVER
    };
    protected CameraMode mode = CHASE;   //default is CHASE


    public GLListener(){
        world = new World("Project Code/src/main/resources/race.json");
        player = new Player();
        keysDown = new ConcurrentLinkedQueue<>();
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

        shader.setUniform("lightIntensity", new Vector3f(1.0f));

        world.init(shader);
        player.init();
        player.setPosition(world.getInitialPlayerPosition());
        Vector3f dir = new Vector3f(player.getDirection());
        dir.rotateAxis((float)Math.toRadians(70.0f), 0 , 1 , 0);
        player.setDirection(dir.normalize(dir));

        cam1 = new Camera();
        cam2 = new Camera();
        cam3 = new Camera();
        cam3.orient(world.getFlyingPosition(), new Vector3f(0,0,0), new Vector3f(0,1,0));
    }

    float test = 1.0f; boolean sunbool = false;
    @Override
    public void display(GLAutoDrawable drawable) {

        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //temporary allocations for use below.
        Matrix4f worldToEye = new Matrix4f();
        Vector3f g = new Vector3f();    //placeholder/garbage; prevents data overwrite. because @*$% joml. //TODO: remove this expletive.
        Vector3f arg1 = new Vector3f(); //placeholder for argument 1 to other functions.
        Vector3f arg2 = new Vector3f(); //placeholder for argument 2 to other functions.

        if( mode == CHASE){
            //orient the camera
            //player.getPosition().add(new Vector3f(0,5,0).add(player.getDirection().mul(-7.0f, player.getDirection()),g), arg1);   //cam location
            player.getDirection().mul(-7.0f * zoom_factor, arg1);
            new Vector3f(0,5,0).add(arg1, arg1);
            player.getPosition().add(arg1, arg1);
            //arg1.z *= zoom_factor;

            player.getPosition().add(player.getDirection().mul(4.0f, g), arg2);                                                     //cam lookAt point
            cam1.orient(arg1, arg2, new Vector3f(1,0,0));                                                                    //__,__,cam Up vector

            //set the looking at matrix.
            player.getPosition().add(player.getDirection().mul(10.0f, g), arg2);
            worldToEye.lookAt(cam1.getPosition(), arg2, new Vector3f(0,1,0));

            //set the shaders
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam1.getProjectionMatrix());
        }
        else if(mode == PHOTO){
            cam2.orient(world.getFixedPosition(), player.getPosition(), new Vector3f(0,1,0));
            worldToEye.lookAt(world.getFixedPosition(), player.getPosition(), new Vector3f(0,1,0));
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam2.getProjectionMatrix());
        }
        else if(mode == OBSERVER){
            worldToEye = cam3.getViewMatrix();
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam3.getProjectionMatrix());
        }
        else {    //Unsupported Camera mode
            System.err.println("Unsupported Camera Mode: "+ mode.toString());
            System.exit(-1);
        }

        //draw the lamp's light
        shader.setUniform("lampIntensity", new Vector3f(18.0f));
        shader.setUniform("ambientIntensity", new Vector3f(1.0f));  //formerly 0.0f

        ArrayList<Vector4f> t = world.getLampData();
        for (int i = 0; i < t.size(); i++) {
            Vector4f lampPosition = new Vector4f(t.get(i).x, t.get(i).y, t.get(i).z, 1.0f);
            float lampHeight = t.get(i).w;
            lampPosition.mul(worldToEye);    //originally worldToEye * lampPosition
            shader.setUniform("lights["+i+"]",new Vector3f(lampPosition.x, lampPosition.y + lampHeight, lampPosition.z));
        }

        //sunlight
        Vector4f sunlightDir = new Vector4f(world.getSunlightDirection(), 0.0f);    //0.0f, otherwise no sunlight.
        sunlightDir.mul(worldToEye);
        shader.setUniform("sunDirection", new Vector3f(sunlightDir.x, sunlightDir.y, sunlightDir.z));
        shader.setUniform("sunIntensity", new Vector3f(test));  //1.0f
        /////
        if (sunbool) test += 0.01f;
        else test -= 0.01f;
        if (test >= 1.0f){ sunbool = !sunbool; test = 0.99f;}
        else if (test <= 0.0f) { sunbool = !sunbool; test = 0.01f; }
        /////

        //render calls
        world.render(shader, player);
        player.render(shader);

        //check for input
        checkForKeyInput();
        checkForMouseInput();
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

    //KeyListener interface methods
    //NOTE: when a key is pressed and held down, keyPressed() and keyReleased are called in succession continuously.
    //      My solution here is to check isAutoRepeat(), which lets me know when the key is held down, or simply pressed
    //      and then I can update my List as appropriate.
    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isAutoRepeat())
            keysDown.add((int)e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!e.isAutoRepeat())
            keysDown.remove(Integer.valueOf((int)e.getKeyCode()));
    }
    //End KeyListener Interface

    //KeyBindings
    private void checkForKeyInput() {
        for (int key : keysDown){
            if (mode == OBSERVER) {
                switch (key) {
                    case KeyEvent.VK_W:
                        cam3.slide(cam3.getN().negate());
                        break;
                    case KeyEvent.VK_S:
                        cam3.slide(cam3.getN());
                        break;
                    case KeyEvent.VK_A:
                        cam3.slide(cam3.getU().negate());
                        break;
                    case KeyEvent.VK_D:
                        cam3.slide(cam3.getU());
                        break;
                    case KeyEvent.VK_Q:
                        cam3.slide(cam3.getV().negate());
                        break;
                    case KeyEvent.VK_E:
                        cam3.slide(cam3.getV());
                        break;
                    default:
                        System.err.println("Unmapped key for Observer: " + (char) key);
                    }   //end switch
            }   //end if
            else if (mode == PHOTO){
                final float MOVE_SPEED = 0.5f;
                final float TURN_SPEED = 3.0f;
                switch(key){
                    case KeyEvent.VK_W:
                        player.move(MOVE_SPEED);
                        break;
                    case KeyEvent.VK_S:
                        player.move(-MOVE_SPEED);
                        break;
                    case KeyEvent.VK_A:
                        player.turn(TURN_SPEED);
                        break;
                    case KeyEvent.VK_D:
                        player.turn(-TURN_SPEED);
                        break;
                    case KeyEvent.VK_Q:
                        player.moveY(MOVE_SPEED);
                        break;
                    case KeyEvent.VK_E:
                        player.moveY(-MOVE_SPEED);
                        break;
                    default:
                        System.err.println("Unmapped key for Photo}: " + (char) key);
                } //end switch
            }   //end else
            else if (mode == CHASE){
                final float MOVE_SPEED = 1.0f;
                final float TURN_SPEED = 5.0f;
                switch(key){
                    case KeyEvent.VK_W:
                        player.move(MOVE_SPEED);
                        break;
                    case KeyEvent.VK_S:
                        player.move(-MOVE_SPEED);
                        break;
                    case KeyEvent.VK_A:
                        player.turn(TURN_SPEED);
                        break;
                    case KeyEvent.VK_D:
                        player.turn(-TURN_SPEED);
                        break;
                    case KeyEvent.VK_Q:
                        player.moveY(MOVE_SPEED);
                        break;
                    case KeyEvent.VK_E:
                        player.moveY(-MOVE_SPEED);
                        break;
                    default:
                        System.err.println("Unmapped key for Chase: " + (char) key);
                } //end switch
            }   //end else
            else{
                System.err.println("checkForInput: New Camera?");
            }
            //for all modes:
            if (key == KeyEvent.VK_1)
                mode = CHASE;
            if (key == KeyEvent.VK_2)
                mode = PHOTO;
            if (key == KeyEvent.VK_3)
                mode = OBSERVER;
        }   //end for
    }

    //MouseListener interface
    //NOTE: When you pull this out, think about whether you want to pass a Camera by
    //reference, or whether to do something else.

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        postmouse = MouseInfo.getPointerInfo().getLocation();
        Point displacement = new Point(postmouse.x - premouse.x, postmouse.y - premouse.y);
        premouse.x = postmouse.x;
        premouse.y = postmouse.y;

        if (mode == OBSERVER) {
            cam3.rotate(displacement.x * 1.0f, new Vector3f(0, 1, 0));
            cam3.pitch(-displacement.y * 0.025f);
        }
        else if (mode == CHASE){
            if (e.getButton() == 3){    //if was a right click
                player.turn(1.5f * -displacement.x);
            }
        }
        else if (mode == PHOTO){

        }
        else{
            System.err.println("MouseDragged: New Camera?");
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        //e.getRotation() stores values as: {scroll up} [0.0, 1.0, 0.0], {scroll down} [0.0, -1.0, 0.0]
        if (e.getRotation()[1] < 0){   //if scrolling down
            zoom_factor += 0.1f;
            if (zoom_factor > MAX_ZOOM_OUT) zoom_factor = MAX_ZOOM_OUT;
        }
        else {          //else you're scrolling up.
            zoom_factor -= 0.1f;
            if (zoom_factor < MAX_ZOOM_IN) zoom_factor = MAX_ZOOM_IN;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        premouse = MouseInfo.getPointerInfo().getLocation();
        switch (e.getButton()){
            case 1:
                break;
            case 2:
                break;
            case 3:
                mouse3down = true;
                break;
            default:
                System.err.println("mousePressed(): Unregistered mouse button.");
        }
        if (mode == OBSERVER){

        }
        else if (mode == PHOTO){

        }
        else if (mode == CHASE){

        }
        else{
            System.err.println("mousePressed(): New Camera?");
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                mouse3down = false;
                break;
            default:
                System.err.println("mouseReleased(): Unregistered mouse button.");
        }
    }
    //End MouseListener interface

    private void checkForMouseInput(){

    }

}
