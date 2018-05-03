package Core.Screens;

import Core.Shader;
import World.AbstractShapes.Rectangle;
import World.AbstractShapes.Triangle;
import World.TriangleMesh;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_BLEND;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static Core.GLListener.gl;

public class LevelSelectScreen extends MenuScreen{
    private Rectangle background, snowPicture, sandPicture, demoPicture, one, two, three, info, title;


    public LevelSelectScreen(){
        shader = Shader.getInstance();
        textureIDs = new int[9];
        background = new Rectangle(
                new Vector3f(-1,-1,0),
                new Vector3f(1,-1,0),
                new Vector3f(1,1,0),
                new Vector3f(-1,1,0),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        title = new Rectangle(
                new Vector3f(-0.6f,0.7f,-1),
                new Vector3f(0.6f,0.7f,-1),
                new Vector3f(0.6f,0.9f,-1),
                new Vector3f(-0.6f,0.9f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        demoPicture = new Rectangle(
                new Vector3f(-0.8f,0.1f,-1),
                new Vector3f(-0.4f,0.1f,-1),
                new Vector3f(-0.4f,0.5f,-1),
                new Vector3f(-0.8f,0.5f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        sandPicture = new Rectangle(
                new Vector3f(-0.2f,0.1f,-1),
                new Vector3f(0.2f,0.1f,-1),
                new Vector3f(0.2f,0.5f,-1),
                new Vector3f(-0.2f,0.5f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        snowPicture = new Rectangle(
                new Vector3f(0.4f,0.1f,-1),
                new Vector3f(0.8f,0.1f,-1),
                new Vector3f(0.8f,0.5f,-1),
                new Vector3f(0.4f,0.5f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        one = new Rectangle(
                new Vector3f(-0.8f,-0.2f,-1),
                new Vector3f(-0.4f,-0.2f,-1),
                new Vector3f(-0.4f,-0.0f,-1),
                new Vector3f(-0.8f,-0.0f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        two = new Rectangle(
                new Vector3f(-0.2f,-0.2f,-1),
                new Vector3f(0.2f,-0.2f,-1),
                new Vector3f(0.2f,-0.0f,-1),
                new Vector3f(-0.2f,-0.0f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        three = new Rectangle(
                new Vector3f(0.4f,-0.2f,-1),
                new Vector3f(0.8f,-0.2f,-1),
                new Vector3f(0.8f,-0.0f,-1),
                new Vector3f(0.4f,-0.0f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        info = new Rectangle(
                new Vector3f(-0.8f,-0.8f,-1),
                new Vector3f(0.8f,-0.8f,-1),
                new Vector3f(0.8f,-0.6f,-1),
                new Vector3f(-0.8f,-0.6f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
    }

    @Override
    public void init() {
        renderPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "menuScreen");
        background.init();
        title.init();
        demoPicture.init();
        sandPicture.init();
        snowPicture.init();
        one.init();
        two.init();
        three.init();
        info.init();
        initTextures();

    }

    @Override
    public void render(){
        super.render();
        gl.glActiveTexture(GL_TEXTURE1);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[0]);
        background.render();

        //enables the use of Alpha in coloring.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);
        title.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[2]);
        demoPicture.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[3]);
        sandPicture.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[4]);
        snowPicture.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[5]);
        one.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[6]);
        two.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[7]);
        three.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[8]);
        shader.setUniform("textureAlpha", alpha);
        info.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    void initTextures(){
        //Texture 0
        initTexture("/Gui Elements/background3.png", 0);
        //Texture 1
        initTexture("/Gui Elements/text_levelselect.png", 1);
        //Texture 2
        initTexture("/Gui Elements/demo.png", 2);
        //Texture 3
        initTexture("/Gui Elements/sun.png", 3);
        //Texture 4
        initTexture("/Gui Elements/snowflake.png", 4);
        //Texture 5
        initTexture("/Gui Elements/text_worldone.png", 5);
        //Texture 6
        initTexture("/Gui Elements/text_worldtwo.png", 6);
        //Texture 7
        initTexture("/Gui Elements/text_worldthree.png", 7);
        //Texture 8
        initTexture("/Gui Elements/text_info.png", 8);
    }


}
