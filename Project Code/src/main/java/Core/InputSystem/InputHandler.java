package Core.InputSystem;


import Utilities.BiMap;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;

import static Core.InputSystem.InputHandler.Action.*;
import static Core.InputSystem.InputHandler.Context.*;

public class InputHandler implements KeyListener, MouseListener {

    private Vector2f mouseClickOrigin;

    private static InputHandler handler;
    //TODO: make code robust: Hashmap<Context, Hashmap<Action, Short>>. Currently assuming only one context. Could do this for world and debug view
    private static BiMap<Action, Short> keyMap;
    //organizing the possible actions in the game
    private static ArrayList<Action> actions;  //for single press activity
    private static HashMap<Action, Boolean> states;   //for press and hold activity, boolean: isDown=true, or isn't=false.
    //private ArrayList<Action> ranges; //for things like joysticks. (mousewheels?)
    private static HashMap<Action, Runnable> events;

    private static boolean locked = false;

    private Context context;
    private SuperInputs notifiee;
    

    public static InputHandler getInstance(){
        if (handler == null) handler = new InputHandler();
        return handler;
    }

    private InputHandler(){
        keyMap = new BiMap<Action, Short>();
        actions = new ArrayList<>();
        states = new HashMap<>();
        events = new HashMap<>();

        //Default keyMapping:
        //Changeable:
        keyMap.put(move_forward, KeyEvent.VK_W);
        keyMap.put(move_backward, KeyEvent.VK_S);
        keyMap.put(move_up, KeyEvent.VK_Q);
        keyMap.put(move_down, KeyEvent.VK_E);
        keyMap.put(turn_left, KeyEvent.VK_A);
        keyMap.put(turn_right, KeyEvent.VK_D);
        keyMap.put(jump, KeyEvent.VK_SPACE);
        keyMap.put(shoot, KeyEvent.VK_P);
        //Internal (Shouldn't allow user to change)
        keyMap.put(switch_mode_normal, KeyEvent.VK_1);
        keyMap.put(switch_mode_debug, KeyEvent.VK_2);
        keyMap.put(switch_mode_sun, KeyEvent.VK_3);
        keyMap.put(select1, KeyEvent.VK_1);
        keyMap.put(select2, KeyEvent.VK_2);
        keyMap.put(select3, KeyEvent.VK_3);
        keyMap.put(pause, KeyEvent.VK_ESCAPE);
        keyMap.put(select, KeyEvent.VK_ENTER);
        keyMap.put(temp, KeyEvent.VK_M);
        keyMap.put(move_up, KeyEvent.VK_UP);
        keyMap.put(move_down, KeyEvent.VK_DOWN);

        //Classify what's an action, a state, and a range
        actions.add(jump);
        actions.add(switch_mode_normal);
        actions.add(switch_mode_debug);
        actions.add(switch_mode_sun);
        actions.add(pause);
        actions.add(select);
        actions.add(select1);
        actions.add(select2);
        actions.add(select3);
        actions.add(temp);
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
    }

    public void setContext(SuperInputs classReceiver, Context context){
        notifiee = classReceiver;
        this.context = context;
        events.clear();

        //have to remove these because they are states in one context and actions in another.   //TODO: redesign this, make it elegant.
        actions.remove(move_up);
        actions.remove(move_down);
        //define the Interface function mapping.
        if (this.context == OVERWORLD && notifiee instanceof GameInputs){
            events.put(move_forward, ()->((GameInputs)notifiee).move_forward());
            events.put(move_backward, ()->((GameInputs)notifiee).move_backward());
            events.put(move_left, ()->((GameInputs)notifiee).move_left());
            events.put(move_right,()->((GameInputs)notifiee).move_right());
            //events.put(move_up,()->((GameInputs)notifiee).move_up());
            //events.put(move_down,()->((GameInputs)notifiee).move_down());
            events.put(turn_left,()->((GameInputs)notifiee).turn_left());
            events.put(turn_right,()->((GameInputs)notifiee).turn_right());
            events.put(jump,()->((GameInputs)notifiee).jump());
            events.put(switch_mode_normal,()->((GameInputs)notifiee).switch_mode_normal());
            events.put(switch_mode_debug,()->((GameInputs)notifiee).switch_mode_debug());
            //events.put(switch_mode_sun, ()->((GameInputs)notifiee).switch_mode_sun());
            events.put(pause,()->((GameInputs)notifiee).pause());
            events.put(shoot, ()->((GameInputs)notifiee).shoot());
        }
        else if (this.context == SPLASH && notifiee instanceof SplashInputs){
            events.put(select, ()->((SplashInputs)notifiee).select());
        }
        else if (this.context == LEVELSELECT && notifiee instanceof LevelSelectInputs){
            events.put(select1, ()->((LevelSelectInputs)notifiee).select(1));
            events.put(select2, ()->((LevelSelectInputs)notifiee).select(2));
            events.put(select3, ()->((LevelSelectInputs)notifiee).select(3));
        }
        else if (this.context == PAUSEMENU && notifiee instanceof PauseInputs){
            actions.add(move_up);
            actions.add(move_down);
            events.put(pause, ()->((PauseInputs)notifiee).unPause());
            events.put(move_up, ()->((PauseInputs)notifiee).moveUp());
            events.put(move_down, ()->((PauseInputs)notifiee).moveDown());
            events.put(select, ()->((PauseInputs)notifiee).selectPanel());
        }
        else if (this.context == GAMEOVER && notifiee instanceof GameOverInputs){
            //no input needed (or wanted)
        }
        else {
            System.err.println("Oops! Something unexpected happened. Here's some info: \nNotifiee: " + notifiee.getClass()+"\nContext: "+context);
            System.exit(-1);
        }
    }

    public enum Context{
        OVERWORLD, PAUSEMENU, SPLASH, LEVELSELECT, GAMEOVER
    }
    public enum Action{
        move_forward, move_backward, move_left, move_right, move_up, move_down,
        turn_left, turn_right,
        jump,
        switch_mode_normal, switch_mode_debug, switch_mode_sun,
        pause,
        shoot,
        select,
        select1, select2, select3,
        temp
    }

    public void lockInput(boolean lock){
        locked = lock;
    }

    //Method for state inputs (gets called from AbstractWorld.render() ).
    public static void pollInput(){
        if (!locked) {
            //for updates to states
            for (Action action : states.keySet()) {
                if (states.containsKey(action) && events.containsKey(action) && states.get(action)) {
                    Runnable runnable = events.get(action);
                    runnable.run();
                }
            }
        }
    }

    //Method for action inputs
    private void actionInput(Action action){
        if (!locked) {
            //check for valid action has already occurred.
            Runnable runnable = events.get(action);
            if (events.get(action) != null) runnable.run();
            else System.err.println(action + " is not defined in this context: " + context);
        }
    }

    public static String lookUpKeyBind(String buttonName){
        ArrayList<Short> buttonList = null;
        buttonList = keyMap.getValueFromKey(stringToEnum(buttonName));
        if (buttonList == null)
            return "UNBOUND";
        else if (buttonList.size() == 1) {
            //TODO: check to see if the Short is not mapped to a Character, like 'Spacebar'.
            if (buttonList.get(0) == KeyEvent.VK_SPACE) return "Spacebar";
            else return Character.toString((char)(int)buttonList.get(0));
        }
        else {
            String string = "";
            for (Short s : buttonList) string += (Character.toString((char)(int)s) + ", ");
            string = string.substring(0, string.length()-2);
            return string;
        }
    }

    public static void changeKeyBinding(String keyAction, Short newKey){
        Action action = stringToEnum(keyAction);
        ArrayList<Short> currentKeysMapped = keyMap.getValueFromKey(action);

        if (currentKeysMapped == null) ; //no action is necessary
        else if (currentKeysMapped.size() == 1) keyMap.removePair(action, currentKeysMapped.get(0));
        else keyMap.removeKey(action);   //removes all keys mapped to that action.

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
            ArrayList<Action> actionList = keyMap.getKeyFromValue(e.getKeyCode());

            if (actionList == null) System.err.println(e.getKeyChar() + " isn't bound.");
            else if (actionList.size() == 1) handleKeyPress(actionList.get(0));
            else {
                //System.err.println("keyPressed(): multiple mappings found");
                //check the Context, and loop through the appropriate events keyset to find the action.
                for (Action action : actionList){
                    for (Action definedAction : events.keySet()){
                        if (action == definedAction){
                            handleKeyPress(action);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(!e.isAutoRepeat()) {
            ArrayList<Action> actionList = keyMap.getKeyFromValue(e.getKeyCode());

            if (actionList == null) ;//System.err.println(e.getKeyChar() + " isn't bound."); //we only need this to happen once (in pressed)
            else if (actionList.size() == 1) handleKeyRelease(actionList.get(0));
            else {
                //System.err.println("keyReleased(): multiple mappings found.");
                //check the Context, and loop through the appropriate events keyset to find the action.
                for (Action action : actionList){
                    for (Action definedAction : events.keySet()) {
                        if (action == definedAction) {
                            handleKeyRelease(action);
                        }
                    }
                }
            }
        }
    }

    private void handleKeyPress(Action action){
        //check if action is an action
        if (actions.contains(action)) {
            ;   //not important unless key is released.
        }
        //check if action is a state
        else if (states.containsKey(action)) {
            states.replace(action, true);
        }
        //default check
        else {
            System.err.println("keyPressed(): The action \'" + action + "\' doesn't map to anything.");
        }
    }

    private void handleKeyRelease(Action action){
        if (actions.contains(action)) {
            actionInput(action);
        }
        //check if action is a state
        else if (states.containsKey(action)) {
            states.replace(action, false);
        }
        //default check
        else {
            System.err.println("keyReleased(): The action \'" + action + "\' doesn't map to anything.");
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
            if (notifiee instanceof GameInputs) ((GameInputs)notifiee).move_camera(difference.negate());
            mouseClickOrigin = new Vector2f(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if (notifiee instanceof GameInputs) ((GameInputs)notifiee).zoom(-e.getRotation()[1]);
    }

    //End MouseListener Interface
}
