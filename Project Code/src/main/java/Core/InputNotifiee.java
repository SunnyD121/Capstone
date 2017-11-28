package Core;

import org.joml.Vector2f;

public interface InputNotifiee {

    //Keys
    void move_forward();
    void move_backward();
    void move_up();
    void move_down();
    void move_left();
    void move_right();
    void turn_left();
    void turn_right();
    void jump();
    void switch_mode_normal();
    void switch_mode_debug();
    void switch_mode_sun();
    void pause();

    //Mouse
    void zoom(float amount);
    void move_camera(Vector2f dif);

}
