package Core;


import Utilities.BiMap;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static Core.InputHandler.Action.*;

public class InputHandler implements KeyListener, MouseListener {

    private Vector2f mouseClickOrigin;

    private static InputHandler handler;
    //TODO: make code robust: Hashmap<Context, Hashmap<Action, Short>>. Currently assuming only one context. Could do this for world and debug view
    private static BiMap<Action, Short> keyMap;
    //organizing the possible actions in the game
    private static ArrayList<Action> actions;  //for single press activity
    private static HashMap<Action, Boolean> states;   //for press and hold activity, boolean: isDown=true, or isn't=false.
    //private ArrayList<Action> ranges; //for things like joysticks.
    private static HashMap<Action, Runnable> events;

    //private static Context context;
    private static InputNotifiee context;


    public static InputHandler getInstance(){
        if (handler == null) handler = new InputHandler();
        return handler;
    }

    private InputHandler(){
        keyMap = new BiMap<Action, Short>();
        actions = new ArrayList<>();
        states = new HashMap<>();
        events = new HashMap<>();
        //context = Context.overworld;    //default to Overworld (not necessarily a good idea).
        
        //Default keyMapping:
        keyMap.put(move_forward, KeyEvent.VK_W);
        keyMap.put(move_backward, KeyEvent.VK_S);
        keyMap.put(move_up, KeyEvent.VK_Q);
        keyMap.put(move_down, KeyEvent.VK_E);
        keyMap.put(turn_left, KeyEvent.VK_A);
        keyMap.put(turn_right, KeyEvent.VK_D);
        keyMap.put(jump, KeyEvent.VK_SPACE);
        keyMap.put(switch_mode_normal, KeyEvent.VK_1);
        keyMap.put(switch_mode_debug, KeyEvent.VK_2);
        keyMap.put(switch_mode_sun, KeyEvent.VK_3);
        keyMap.put(pause, KeyEvent.VK_ESCAPE);
        keyMap.put(shoot, KeyEvent.VK_P);

        //Classify what's an action, a state, and a range
        actions.add(jump);
        actions.add(switch_mode_normal);
        actions.add(switch_mode_debug);
        actions.add(switch_mode_sun);
        actions.add(pause);
        //default all to false
        states.put(move_forward, false);
        states.put(move_backward, false);
        states.put(move_up, false);
        states.put(move_down, false);
        states.put(move_left, false);
        states.put(move_right, false);
        states.put(turn_left, false);
        states.put(turn_right, false);
        states.put(shoot, false);
        //define the Interface function mapping.
        events.put(move_forward, ()->context.move_forward());
        events.put(move_backward, ()->context.move_backward());
        events.put(move_left, ()->context.move_left());
        events.put(move_right,()->context.move_right());
        events.put(move_up,()->context.move_up());
        events.put(move_down,()->context.move_down());
        events.put(turn_left,()->context.turn_left());
        events.put(turn_right,()->context.turn_right());
        events.put(jump,()->context.jump());
        events.put(switch_mode_normal,()->context.switch_mode_normal());
        events.put(switch_mode_debug,()->context.switch_mode_debug());
        events.put(switch_mode_sun, ()->context.switch_mode_sun());
        events.put(pause,()->context.pause());
        events.put(shoot, ()->context.shoot());
    }

    public static void setContext(InputNotifiee c){ //TODO: change parameter to Context later on down the line
        context = c;
    }

    public enum Context{
        overworld, menu
    }
    public enum Action{
        move_forward, move_backward, move_left, move_right, move_up, move_down,
        turn_left, turn_right,
        jump,
        switch_mode_normal, switch_mode_debug, switch_mode_sun,
        pause,
        shoot
    }

    //Method for state inputs (gets called from World.render() ).
    public static void pollInput(){
        //for updates to states
        for (Action action: states.keySet()){
            if (states.containsKey(action) && states.get(action) ){
                Runnable runnable = events.get(action);
                runnable.run();
            }
        }

    }

    //Method for action inputs
    private void actionInput(Action action){
        //check for valid action has already occurred.
        Runnable runnable = events.get(action);
        runnable.run();
    }

    public static String lookUpKeyBind(String buttonName){
        Short button = null;
        button = keyMap.getValueFromKey(stringToEnum(buttonName));
        if (button == null)
            return "UNBOUND";
        else
            return Character.toString((char)(int)button);
    }

    public static void changeKeyBinding(String keyAction, Short newKey){
        Action action = stringToEnum(keyAction);
        keyMap.removeKey(action);
        keyMap.put(action, newKey);
    }

    private static Action stringToEnum(String s){
        Action action = null;
        switch (s){
            case "Move Forward": action = move_forward; break;
            case "Move Backward": action = move_backward; break;
            case "Move Left": action = move_left; break;
            case "Move Right": action = move_right; break;
            case "Turn Left": action = turn_left; break;
            case "Turn Right": action = turn_right; break;
            case "Jump": action = jump; break;
            case "Switch Mode": action = switch_mode_normal; break;
            case "Pause" : action = pause; break;
            case "Shoot" : action = shoot; break;
            default : System.err.println("Unrecognized Action.");
        }
        return action;
    }

    //KeyListener Interface

    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isAutoRepeat()) {
            Action action = keyMap.getKeyFromValue(e.getKeyCode());
            //check if action is an action
            if (actions.contains(action)){
                ;   //not important unless key is released.
            }
            //check if action is a state
            else if (states.containsKey(action)){
                states.replace(action, true);
            }
            //default check
            else {
                System.err.println("The action \'" + action + "\' doesn't map to anything.");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(!e.isAutoRepeat()){
            Action action = keyMap.getKeyFromValue(e.getKeyCode());
            if (actions.contains(action)){
                actionInput(action);
            }
            //check if action is a state
            else if (states.containsKey(action)){
                states.replace(action, false);
            }
            //default check
            else {
                System.err.println("The action \'" + action + "\' doesn't map to anything.");
            }
        }
    }
    //End Keylistener Interface

    //MouseListener Interface
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
    public void mousePressed(MouseEvent e) {
        mouseClickOrigin = new Vector2f(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getButton() == 3){    //right click
            Vector2f difference = new Vector2f();
            mouseClickOrigin.sub(new Vector2f(e.getX(), e.getY()), difference);
            context.move_camera(difference);
            mouseClickOrigin = new Vector2f(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        context.zoom(e.getRotation()[1]);
    }

    //End MouseListener Interface
}
