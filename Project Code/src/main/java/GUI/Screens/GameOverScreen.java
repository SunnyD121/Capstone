package GUI.Screens;

import Core.GLListener;
import Core.InputSystem.InputHandler;
import Core.Shader;
import World.AbstractShapes.Rectangle;
import org.joml.Vector3f;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

public class GameOverScreen extends MenuScreen{
    private Rectangle background, blackGround, gameOverText;
    private static float counter = 1;
    private boolean decrease = true;
    private final float FADE_SPEED = 0.005f;

    public GameOverScreen(){
        shader = Shader.getInstance();
        textureIDs = new int[3];

        background = new Rectangle(
                new Vector3f(-1,-1,0),
                new Vector3f(1,-1,0),
                new Vector3f(1,1,0),
                new Vector3f(-1,1,0),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        blackGround = new Rectangle(
                new Vector3f(-1,-1,-1),
                new Vector3f(1,-1,-1),
                new Vector3f(1,1,-1),
                new Vector3f(-1,1,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        gameOverText = new Rectangle(
                new Vector3f(0.1f,0.5f,-0.5f),
                new Vector3f(0.8f,0.5f,-0.5f),
                new Vector3f(0.8f,0.9f,-0.5f),
                new Vector3f(0.1f,0.9f,-0.5f),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
    }

    @Override
    public void init() {
        renderPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "menuScreen");
        background.init();
        blackGround.init();
        gameOverText.init();
        initTextures();
    }

    @Override
    public void render(){
        if (counter < -0.5f) decrease = false;
        else if (counter > 1.0f){
            counter = 1.0f;
            decrease = true;
            GLListener.relinquishInputControl(InputHandler.Context.SPLASH);
        }
        if (decrease) counter -= FADE_SPEED;
        else counter += FADE_SPEED;

        super.render();
        gl.glActiveTexture(GL_TEXTURE1);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[0]);
        background.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[2]);
        gameOverText.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);
        shader.setUniform("textureAlpha", counter);
        blackGround.render();
        shader.setUniform("textureAlpha", 1);




        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    void initTextures() {
        //Texture 0
        initTexture("/Gui Elements/PlayerDeath.png", 0);
        //Texture 1
        initTexture("/Gui Elements/background_solidBlack.png", 1);
        //Texture 2
        initTexture("/Gui Elements/text_gameover.png", 2);
    }
}
