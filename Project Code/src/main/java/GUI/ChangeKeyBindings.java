package GUI;

import Core.InputSystem.InputHandler;

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;


public class ChangeKeyBindings extends JFrame implements KeyListener {

    private JFrame frame;
    private JPanel topPanel, centerPanel;
    private JLabel newLabel, southLabel;
    private JButton apply;
    private JComboBox<String> comboBox;

    private boolean isListeningForKeyEvents = false;
    private String currentItemSelected = null;
    private int newKeyCode;

    public ChangeKeyBindings(){
        this.setTitle("Keybindings");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500,400);
        this.setLocation(200,200);

        frame = this;
        topPanel = new JPanel();
        centerPanel = new JPanel();
        southLabel = new JLabel("");

        String[] comboBoxItems = {
                "Move Forward", "Move Backward", "Move Left" , "Move Right",
                "Turn Left", "Turn Right",
                "Jump",
                "Shoot"
        };

        currentItemSelected = "Move Forward";
        updateGui(currentItemSelected);

        comboBox = new JComboBox<>(comboBoxItems);
        comboBox.addItemListener(new BoxItemSelect());

        topPanel.add(comboBox);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(southLabel, BorderLayout.SOUTH);

        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }

    private void updateGui(String actionName){
        frame.remove(centerPanel);
        frame.revalidate();
        centerPanel = new JPanel();
        String oldKey = InputHandler.lookUpKeyBind(actionName);
        JLabel oldLabel = new JLabel("Current KeyBinding: "+oldKey);
        newLabel = new JLabel("New KeyBinding");
        JButton listenForChange = new JButton("Click here to change the binding!");
        listenForChange.addActionListener(new ChangeKey());
        listenForChange.addKeyListener(this);
        apply = new JButton("Apply");
        apply.addActionListener(new Apply());
        apply.setEnabled(false);
        //JButton cancel = new JButton("Cancel");

        JPanel left = new JPanel();
        JPanel right = new JPanel();

        left.add(oldLabel);
        left.add(listenForChange);
        right.add(newLabel);
        right.add(apply);

        centerPanel.add(left,BorderLayout.WEST);
        centerPanel.add(right,BorderLayout.EAST);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.setFocusable(true);
    }

    //KeyListener Interface
    public void keyPressed(KeyEvent e){
    }

    public void keyReleased(KeyEvent e){
        if (isListeningForKeyEvents) {
            newKeyCode = e.getKeyCode();
            apply.setEnabled(true);
            southLabel.setText("Keybinding changed to: " + e.getKeyChar());
        }
    }

    public void keyTyped(KeyEvent e){
    }
    //End KeyListener Interface

    private class BoxItemSelect implements ItemListener {
        public void itemStateChanged(ItemEvent e){
            //NOTE: calls this for the deselection and the selection.
            currentItemSelected = (String)e.getItem();
            updateGui((String)e.getItem());
        }
    }

    private class ChangeKey implements ActionListener{
        public void actionPerformed(ActionEvent e){
            isListeningForKeyEvents = true;
            southLabel.setText("Waiting for input...");
        }
    }

    private class Apply implements ActionListener {
        public void actionPerformed(ActionEvent e){
            isListeningForKeyEvents = false;
            InputHandler.changeKeyBinding(currentItemSelected, (short) newKeyCode);  //NOTE: beware the conversion from int to short.
            apply.setEnabled(false);
            southLabel.setText("");
            updateGui(currentItemSelected);
        }
    }
}
