package GUI;

import Core.UserInputConfig;

import javax.swing.*;
import java.awt.*;

import java.awt.event.*;


public class ChangeKeyBindings extends JFrame implements KeyListener {
//TODO: JFrame.addKeyListener
    private JFrame frame;
    private JPanel topPanel, centerPanel;
    private JLabel newLabel;
    private JComboBox<String> comboBox;

    private boolean isListeningForKeyEvents = false;

    public ChangeKeyBindings(){
        this.setTitle("Keybindings");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(500,400);
        this.setLocation(200,200);

        frame = this;
        topPanel = new JPanel();
        centerPanel = new JPanel();

        String[] comboBoxItems = {
                "Move Forward", "Move Backward", "Move Left" , "Move Right",
                "Turn Left", "Turn Right",
                "Jump",
                "Switch Mode",      //TODO: this is for debugging and should be removed
                "Pause"
        };
        comboBox = new JComboBox<>(comboBoxItems);
        comboBox.addItemListener(new BoxItemSelect());

        topPanel.add(comboBox);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    private void updateGui(String actionName){
        frame.remove(centerPanel);
        frame.revalidate();
        centerPanel = new JPanel();
        char oldKey = (char)(int)UserInputConfig.lookUpKeyBind(actionName);
        JLabel oldLabel = new JLabel("Current KeyBinding: "+oldKey);
        newLabel = new JLabel("New KeyBinding");
        JButton listenForChange = new JButton("Click here to change the binding!");
        listenForChange.addActionListener(new ChangeKey());
        JButton apply = new JButton("Apply");
        apply.addActionListener(new Apply());
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

    public void keyPressed(KeyEvent e){
        //if (isListeningForKeyEvents) {
        System.out.println("A: "+e.getKeyChar());
        //}
    }
    public void keyReleased(KeyEvent e){
        //if (isListeningForKeyEvents) {
            System.out.println("B: "+e.getKeyChar());
        //}
    }
        public void keyTyped(KeyEvent e){
        //if (isListeningForKeyEvents) {
            System.out.println("C: "+e.getKeyChar());
        //}
    }

    private class BoxItemSelect implements ItemListener {
        public void itemStateChanged(ItemEvent e){
            //NOTE: calls this for the deselection and the selection.
            updateGui((String)e.getItem());
        }
    }

    private class ChangeKey implements ActionListener{
        public void actionPerformed(ActionEvent e){
            isListeningForKeyEvents = true;
        }
    }

    private class Apply implements ActionListener {
        public void actionPerformed(ActionEvent e){
            isListeningForKeyEvents = false;
            //TODO: update UserInputConfig
        }
    }
}
