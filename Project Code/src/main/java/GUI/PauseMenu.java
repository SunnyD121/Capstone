package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PauseMenu extends JFrame{

    private static PauseMenu pauseMenu;
    public static PauseMenu getInstance(int x, int y, int w, int h){
        if (pauseMenu == null) pauseMenu = new PauseMenu(x,y,w,h);
        return pauseMenu;
    }
    public static PauseMenu getInstance(){
        if (pauseMenu == null) pauseMenu = new PauseMenu(100,100,300,300);
        return pauseMenu;
    }

    private JButton settings, quit;



    private PauseMenu(int x, int y, int w, int h){
        this.setTitle("Menu");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(w,h);
        this.setAlwaysOnTop(true);
        this.setLocation(x + w/2, y + h/2);     //TODO: A work in progress
        this.setResizable(false);
        //this.setAutoRequestFocus(true);

        JPanel panel = new JPanel();
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        settings = new JButton("Settings");
        settings.setAlignmentX(Component.CENTER_ALIGNMENT);
        settings.addActionListener(new Settings());
        quit = new JButton("Exit Game");
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel1.add(settings);
        panel2.add(quit);
        panel.add(panel1);
        panel.add(panel2);
        this.add(panel);


        this.setVisible(false);
    }

    public static void setVisibility(boolean b){
        pauseMenu.setVisible(b);
        pauseMenu.toFront();    //must call this before requestFocus
        pauseMenu.requestFocus();
    }

    private class Settings implements ActionListener{
        public void actionPerformed(ActionEvent e){
            pauseMenu.setAlwaysOnTop(false);
            ChangeKeyBindings c = new ChangeKeyBindings();
        }
    }

}
