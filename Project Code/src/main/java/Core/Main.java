package Core;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;

public class Main {

    /*TODO List:
    improve mouse-camera and key-camera interaction to the feel of modern games.
    clean up camera alterations in GLListener. Maybe make more methods in Camera? Want to have very little Camera code in GLListener.
    Look into other weather effects.
    Possible refactoring: drawLamp()/drawTree() code is almost identical.
    See if possible to move lamp rendering to World from GLListener
    Add collisions
    Look into the sun
    Fiddle with the light equations in the shaders, dont make color so additive. (lamp vs sun isnt additive, it has a diminished return irl)
    Check shapes for incorrect normals.
    Remove Cube c from World (init, render)
    Remove theTexture from World
    Player.java: commented code (color)
    Write Tests!!
    Pyramid Normals (same for 3 vertices of a face, or true to the corners?)
    Cone Normals not wrapping properly.
    World.render() currently passes a Player object. Remove this. terribly smelly.

    Remove Commented Code /////Testing Normals (World.java)
    Cone Normal seems to not wrap around correctly.


    //TODO side projects of interest:
    glWindow.setDestroyNotification...something might be useful later on?

    //TODO before capstone presentation:
    remove extraneous imports, methods and class variables
    rename GLListener to OmniListener
    Remove Q and E keybindings for CHASE, PHOTO cameras.
     */

    /*
    //TODO: before next meeting with Wolff
    Refine the User Stories to be more specific and helpful as a User to the project.
    Push code up for viewing.
    Write commits to solve issues.
     */

    public static void main(String[] args) {

        // Set the profile to 4.0 core
        GLProfile glp = GLProfile.get("GL4");
        GLCapabilities caps = new GLCapabilities(glp);

        //get current screen's dimensions
        int screenwidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screenheight = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        // Create the GLWindow
        GLWindow glWindow = GLWindow.create(caps);
        glWindow.setSize(800,600);     //originally 800,600
        glWindow.setTitle("Capstone Project");

        //add Listeners
        GLListener gl = new GLListener();
        KeyBinder keyListener = KeyBinder.getInstance();
        // Connect to main listener
        glWindow.addGLEventListener(gl);
        // add keyListener
        glWindow.addKeyListener(keyListener);
        //add mouseListener
        glWindow.addMouseListener(keyListener);


        // Handle window closing
        glWindow.addWindowListener(new WindowAdapter() {
            public void windowDestroyNotify(WindowEvent e) {
                System.exit(0);
            }
        });

        // Set up animator 
        FPSAnimator animator = new FPSAnimator(glWindow, 60);
        animator.start();

        // Show the window
        glWindow.setVisible(true);
    }
}
