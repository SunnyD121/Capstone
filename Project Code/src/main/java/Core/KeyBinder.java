package Core;

import com.jogamp.newt.event.KeyEvent;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Core.KeyBinder.Action.*;

public class KeyBinder implements KeyListener, MouseListener{

    //Singleton Pattern
    private static KeyBinder keyBinder;
    public static KeyBinder getInstance(){
        if (keyBinder == null) keyBinder = new KeyBinder();
        return keyBinder;
    }

    private static ArrayList<KeyPressNotifiee> subscribers = new ArrayList<>();

    private static ConcurrentLinkedQueue<Short> keysDown;
    private static HashMap<Short, Action> keyConfigMap;
    private Point premouse, postmouse;
    private boolean mouse3down = false;
    private final float MAX_ZOOM_OUT = 2.5f;
    private final float MAX_ZOOM_IN = 0.3f;

    public enum Action{
        move_forward, move_backward, move_left, move_right,
        turn_left, turn_right,
        jump,
        switch_mode
    }

    private KeyBinder(){
        keysDown = new ConcurrentLinkedQueue<>();
        keyConfigMap = new HashMap<>();

        //default configuration
        keyConfigMap.put(KeyEvent.VK_W, move_forward);
        keyConfigMap.put(KeyEvent.VK_S, move_backward);
        keyConfigMap.put(KeyEvent.VK_A, turn_left);
        keyConfigMap.put(KeyEvent.VK_D, turn_right);
        keyConfigMap.put(KeyEvent.VK_SPACE, jump);
        keyConfigMap.put(KeyEvent.VK_1, switch_mode);
        keyConfigMap.put(KeyEvent.VK_2, switch_mode);
        keyConfigMap.put(KeyEvent.VK_3, switch_mode);

    }

    public static void checkTheInput(){
        //keyInput
        for (short key : keysDown){
            Action action = keyConfigMap.get(key);
            if (action != null){
                //notify the subscribers
                for (KeyPressNotifiee k : subscribers){
                    switch(action){
                        case move_forward: k.move_forward(); break;
                        case move_backward: k.move_backward(); break;
                        case move_left: k.move_left(); break;
                        case move_right: k.move_right(); break;
                        case turn_left: k.turn_left(); break;
                        case turn_right: k.turn_right(); break;
                        case jump: k.jump(); break;
                        case switch_mode: k.switch_mode(key-48); break;
                    }
                }
            }//else action is null and not a key that has an action.
            else {
                System.err.println("Key " + (char)key + " is not mapped to an action.");
            }

        }

        //mouseInput
        //TODO.
    }

    void checkForMouseInput(){

    }

    public static void addKeyPressListener(KeyPressNotifiee subscriber){subscribers.add(subscriber);}
    public static void clearListeners(){subscribers.clear();}


    //KeyListener interface
    //NOTE: when a key is pressed and held down, keyPressed() and keyReleased are called in succession continuously.
    //      My solution here is to check isAutoRepeat(), which lets me know when the key is held down, or simply pressed
    //      and then I can update my List as appropriate.
    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isAutoRepeat())
            keysDown.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!e.isAutoRepeat())
            keysDown.remove(e.getKeyCode());
    }
    //End KeyListener Interface

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
/*        postmouse = MouseInfo.getPointerInfo().getLocation();
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
*/    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
/*        //e.getRotation() stores values as: {scroll up} [0.0, 1.0, 0.0], {scroll down} [0.0, -1.0, 0.0]
        if (e.getRotation()[1] < 0){   //if scrolling down
            zoom_factor += 0.1f;
            if (zoom_factor > MAX_ZOOM_OUT) zoom_factor = MAX_ZOOM_OUT;
        }
        else {          //else you're scrolling up.
            zoom_factor -= 0.1f;
            if (zoom_factor < MAX_ZOOM_IN) zoom_factor = MAX_ZOOM_IN;
        }
*/    }

    @Override
    public void mousePressed(MouseEvent e) {
/*        premouse = MouseInfo.getPointerInfo().getLocation();
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
*/    }

    @Override
    public void mouseReleased(MouseEvent e) {
/*        switch (e.getButton()) {
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
*/    }
    //End MouseListener interface

}
