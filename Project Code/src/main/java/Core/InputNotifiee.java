package Core;

import java.awt.*;

public interface InputNotifiee {

    //Keys
    void move_forward();
    void move_backward();
    void move_left();
    void move_right();
    void turn_left();
    void turn_right();
    void jump();
    void switch_mode(int mode);

    //Mouse
    void zoom(float amount);

}
