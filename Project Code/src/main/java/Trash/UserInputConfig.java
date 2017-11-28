package Trash;

import Core.InputNotifiee;
import com.jogamp.newt.event.KeyEvent;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static Trash.UserInputConfig.Action.*;

public class UserInputConfig implements KeyListener, MouseListener{

    //Singleton Pattern
    private static UserInputConfig userInputConfig;
    public static UserInputConfig getInstance(){
        if (userInputConfig == null) userInputConfig = new UserInputConfig();
        return userInputConfig;
    }

    private static ArrayList<InputNotifiee> subscribers = new ArrayList<>();

    private static ConcurrentLinkedQueue<Short> keysDown;
    private static HashMap<Short, Action> keyConfigMap;
    private static HashMap<Action, Short> reverseKeyConfigMap;
    private static HashMap<Action, Boolean> isASinglePress;
    private Point premouse, postmouse;
    private boolean mouse3down = false;
    private static float zoom_factor = 1;
    private final float MAX_ZOOM_OUT = 2.5f;
    private final float MAX_ZOOM_IN = 0.3f;

    public enum Action{
        move_forward, move_backward, move_left, move_right,
        turn_left, turn_right,
        jump,
        switch_mode,
        pause
    }

    private UserInputConfig(){
        keysDown = new ConcurrentLinkedQueue<>();
        keyConfigMap = new HashMap<>();
        reverseKeyConfigMap = new HashMap<>();
        isASinglePress = new HashMap<>();

        //default configuration
        keyConfigMap.put(KeyEvent.VK_W, move_forward);
        keyConfigMap.put(KeyEvent.VK_S, move_backward);
        keyConfigMap.put(KeyEvent.VK_A, turn_left);
        keyConfigMap.put(KeyEvent.VK_D, turn_right);
        keyConfigMap.put(KeyEvent.VK_SPACE, jump);
        keyConfigMap.put(KeyEvent.VK_1, switch_mode);
        keyConfigMap.put(KeyEvent.VK_2, switch_mode);
        keyConfigMap.put(KeyEvent.VK_3, switch_mode);
        keyConfigMap.put(KeyEvent.VK_ESCAPE, pause);

        //and the reverse mapping.
        reverseKeyConfigMap.put(move_forward, KeyEvent.VK_W);
        reverseKeyConfigMap.put(move_backward, KeyEvent.VK_S);
        reverseKeyConfigMap.put(turn_left, KeyEvent.VK_A);
        reverseKeyConfigMap.put(turn_right, KeyEvent.VK_D);
        reverseKeyConfigMap.put(jump, KeyEvent.VK_SPACE);
        reverseKeyConfigMap.put(switch_mode, KeyEvent.VK_1);
        reverseKeyConfigMap.put(switch_mode, KeyEvent.VK_2);
        reverseKeyConfigMap.put(switch_mode, KeyEvent.VK_3);
        reverseKeyConfigMap.put(pause, KeyEvent.VK_ESCAPE);

        //mapping of actions to whether or not they're from a toggle key
        isASinglePress.put(move_forward, false);
        isASinglePress.put(move_backward, false);
        isASinglePress.put(turn_left, false);
        isASinglePress.put(turn_right, false);
        isASinglePress.put(jump, false);
        isASinglePress.put(switch_mode, true);
        isASinglePress.put(pause, true);
        
    }

    public static void checkTheInput(){
        //TODO -refactoring: instead of having world.java call this function, have this class notify it's subscribers straight
        //      from the event when the input is read from the user.

        //keyInput
        for (short key : keysDown){
            Action action = keyConfigMap.get(key);
            if (action != null){
                //notify the subscribers
                for (InputNotifiee s : subscribers){
                    switch(action){
                        case move_forward: s.move_forward(); break;
                        case move_backward: s.move_backward(); break;
                        case move_left: s.move_left(); break;
                        case move_right: s.move_right(); break;
                        case turn_left: s.turn_left(); break;
                        case turn_right: s.turn_right(); break;
                        case jump: s.jump(); break;
                        case switch_mode: s.switch_mode_normal(); break;
                        case pause: s.pause(); break;
                    }
                }
            }//else action is null and not a key that has an action.
            else {
                System.err.println("Key " + (char)key + " is not mapped to an action.");
            }

        }

        //mouseInput
        for (InputNotifiee s : subscribers){
            s.zoom(zoom_factor);    //TODO -efficiency: only notify if zoom_factor has changed.
        }
    }

    private void notifyOfSinglePress(short key){
        Action action = keyConfigMap.get(key);
        if (action == null) System.err.println("Key " + (char)key + " is not mapped to an action.");
        else {
            for (InputNotifiee s : subscribers) {
                switch (action) {
                    //case switch_mode: s.switch_mode(key - 48); break;
                    case pause: s.pause(); break;
                }
            }
        }
    }


    public static void addKeyPressListener(InputNotifiee subscriber){subscribers.add(subscriber);}
    public static void clearListeners(){subscribers.clear();}

    public static Short lookUpKeyBind(String s){
        Action action = null;
        switch(s){
            case "Move Forward": action = move_forward; break;
            case "Move Backward": action = move_backward; break;
            case "Move Left": action = move_left; break;
            case "Move Right": action = move_right; break;
            case "Turn Left": action = turn_left; break;
            case "Turn Right": action = turn_right; break;
            case "Jump": action = jump; break;
            case "Switch Mode": action = switch_mode; break;
            case "Pause": action = pause; break;
            default: System.err.println("Action " + s + " doesn't appear to have an action.");
        }

        return reverseKeyConfigMap.get(action);
    }


    //KeyListener interface
    //NOTE: when a key is pressed and held down, keyPressed() and keyReleased are called in succession continuously.
    //      My solution here is to check isAutoRepeat(), which lets me know when the key is held down, or simply pressed
    //      and then I can update my List as appropriate.
    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isAutoRepeat() && !isASinglePress.get(keyConfigMap.get(e.getKeyCode())))  //is not a key meant for press and hold
            keysDown.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!e.isAutoRepeat())
            keysDown.remove(e.getKeyCode());    //will return null, but not crash if below check is true, so that's ok.
        if (!e.isAutoRepeat() && isASinglePress.get(keyConfigMap.get(e.getKeyCode())))
            notifyOfSinglePress(e.getKeyCode());
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

        if (mode == DEBUG_VIEW) {
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
