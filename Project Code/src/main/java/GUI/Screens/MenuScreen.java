package GUI.Screens;

import Core.Shader;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

public abstract class MenuScreen {

    Shader shader;
    int[] textureIDs;
    int renderPassID;
    private boolean increment = true;
    float alpha = 1.0f;

    public abstract void init();
    abstract void initTextures();

    public void render(){
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
        int[] subroutinePass = new int[] {renderPassID};
        gl.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, IntBuffer.wrap(subroutinePass));  //for the shader

        shader.setUniform("ObjectToWorld", new Matrix4f());
        shader.setUniform("WorldToEye", new Matrix4f());
        shader.setUniform("Projection", new Matrix4f());
        shader.setUniform("textureAlpha", 1.0f);

        if (alpha > 0.9f) increment = false;
        if (alpha < 0.1f) increment = true;

        final float FADE = 0.02f;
        if (increment) alpha += FADE;
        else alpha -= FADE;

    }

    void initTexture(String filename, int iD){
        TextureData textureData;
        shader.setUniform("tex", 1);
        textureData = helperTextureIO(filename, iD);
        createTexture(textureData);

        //unbind
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    private TextureData helperTextureIO(String filename, int iD){
        InputStream texStream = null;
        TextureData textureData = null;
        try {
            //read texture picture file
            texStream = getClass().getResourceAsStream(filename);
            String fileSuffix = filename.substring(filename.length()-3, filename.length());
            textureData = TextureIO.newTextureData(gl.getGLProfile(), texStream, false, fileSuffix);    //TextureIO.JPG or TextureIO.PNG
            //create and load buffers
            int[] texNames = new int[1];
            gl.glGenTextures(1, texNames, 0);
            textureIDs[iD] = texNames[0];   //if this gives arrayOutOfBoundsException, go up to the top and equal the array size with number of textures.
            gl.glActiveTexture(GL_TEXTURE1);
            gl.glBindTexture(GL_TEXTURE_2D, textureIDs[iD]);
            //parameters


        }
        catch (IOException e){System.err.println("Failed to load texture: " + filename); e.printStackTrace();}

        return textureData;
    }

    private void createTexture(TextureData textureData){
        gl.glTexStorage2D(GL_TEXTURE_2D, 1, textureData.getInternalFormat(), textureData.getWidth(), textureData.getHeight());
        gl.glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,textureData.getWidth(), textureData.getHeight(), textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());

    }
}
