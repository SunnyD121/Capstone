package Core;

import Core.CollisionDetectionSystem.BoundingBox;
import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import Core.CollisionDetectionSystem.FixedBoundingBox;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import org.joml.Vector3f;

public class Main {

    /*
    Fiddle with the light equations in the shaders, dont make color so additive. (lamp vs sun isnt additive, it has a diminished return irl)
    Look into SSAO Screen space Ambient Occlusion for realistic ambient lighting.
    */

    public static void main(String[] args) {
        function(args);
        //testFunction();

    }
    private static void testFunction(){

    }

    public static void function(String [] args){
        // Set the profile to 4.0 core
        GLProfile glp = GLProfile.get("GL4");
        GLCapabilities caps = new GLCapabilities(glp);

        //get current screen's dimensions
        //int screenwidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        //int screenheight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        // Create the GLWindow
        GLWindow glWindow = GLWindow.create(caps);
        glWindow.setSize(800,600);     //originally 800,600
        glWindow.setTitle("Capstone Project");

        //add Listeners
        GLListener gl = new GLListener();
        // Connect to main listener
        glWindow.addGLEventListener(gl);
        // add keyListener
        glWindow.addKeyListener(InputHandler.getInstance());
        //add mouseListener
        glWindow.addMouseListener(InputHandler.getInstance());

        // Handle window closing
        glWindow.addWindowListener(new WindowAdapter() {
            public void windowDestroyNotify(WindowEvent e) {
                System.exit(0);
            }
        });

        glWindow.setResizable(true);

        // Set up animator 
        FPSAnimator animator = new FPSAnimator(glWindow, 60);
        animator.start();

        // Show the window
        glWindow.setVisible(true);
    }

}
