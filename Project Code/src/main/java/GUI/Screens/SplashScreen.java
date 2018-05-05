package GUI.Screens;

import Core.Shader;
import World.AbstractShapes.Rectangle;
import org.joml.Vector3f;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

public class SplashScreen extends MenuScreen{

    private Rectangle splashScreen;
    private Rectangle banner;

    public SplashScreen(){
        shader = Shader.getInstance();
        textureIDs = new int[2];
        splashScreen = new Rectangle(
                new Vector3f(-1,-1,0),
                new Vector3f(1,-1,0),
                new Vector3f(1,1,0),
                new Vector3f(-1,1,0),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        banner = new Rectangle(
                new Vector3f(-0.6f,-0.6f,-1),
                new Vector3f(0.6f,-0.6f,-1),
                new Vector3f(0.6f,-0.4f,-1),
                new Vector3f(-0.6f,-0.4f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );

    }

    @Override
    public void init() {
        renderPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "menuScreen");
        splashScreen.init();
        banner.init();
        initTextures();
    }

    @Override
    public void render(){
        super.render();
        gl.glActiveTexture(GL_TEXTURE1);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[0]);
        splashScreen.render();

        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);
        shader.setUniform("textureAlpha", alpha);
        banner.render();
        gl.glDisable(GL_BLEND);

        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    void initTextures(){
        //Texture 1
        initTexture("/Gui Elements/splash.png", 0);

        //Texture 2
        initTexture("/Gui Elements/banner.png", 1);

    }
}
