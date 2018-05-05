package GUI.Screens;

import Core.Shader;
import GUI.ChangeKeyBindings;
import World.AbstractShapes.Rectangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.GL_TEXTURE1;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

public class PauseScreen extends MenuScreen{
    private Rectangle background, controlsPanel, mainMenuPanel, quitPanel, border;
    private final float PANEL_HEIGHT = 0.2f;
    private int buttonIndex = 1;
    private Matrix4f moveBox;

    public PauseScreen(){
        shader = Shader.getInstance();
        textureIDs = new int[5];
        moveBox = new Matrix4f();

        background = new Rectangle(
                new Vector3f(-1,-1,0),
                new Vector3f(1,-1,0),
                new Vector3f(1,1,0),
                new Vector3f(-1,1,0),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        controlsPanel = new Rectangle(
                new Vector3f(-0.5f,0.3f,-0.5f),
                new Vector3f(0.5f,0.3f,-0.5f),
                new Vector3f(0.5f,0.3f+PANEL_HEIGHT,-0.5f),
                new Vector3f(-0.5f,0.3f+PANEL_HEIGHT,-0.5f),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        mainMenuPanel = new Rectangle(
                new Vector3f(-0.5f,-0.1f,-0.5f),
                new Vector3f(0.5f,-0.1f,-0.5f),
                new Vector3f(0.5f,-0.1f+PANEL_HEIGHT,-0.5f),
                new Vector3f(-0.5f,-0.1f+PANEL_HEIGHT,-0.5f),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        quitPanel = new Rectangle(
                new Vector3f(-0.5f,-0.5f,-0.5f),
                new Vector3f(0.5f,-0.5f,-0.5f),
                new Vector3f(0.5f,-0.5f+PANEL_HEIGHT,-0.5f),
                new Vector3f(-0.5f,-0.5f+PANEL_HEIGHT,-0.5f),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        border = new Rectangle(
                new Vector3f(-0.5f,0.3f,-1),
                new Vector3f(0.5f,0.3f,-1),
                new Vector3f(0.5f,0.3f+PANEL_HEIGHT,-1),
                new Vector3f(-0.5f,0.3f+PANEL_HEIGHT,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
    }

    @Override
    public void init() {
        renderPassID = gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "menuScreen");
        background.init();
        controlsPanel.init();
        mainMenuPanel.init();
        quitPanel.init();
        border.init();
        initTextures();
    }

    @Override
    public void render(){
        super.render();
        gl.glActiveTexture(GL_TEXTURE1);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[0]);
        shader.setUniform("textureAlpha", 0.3f);
        background.render();
        shader.setUniform("textureAlpha", 1.0f);

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);
        controlsPanel.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[2]);
        mainMenuPanel.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[3]);
        quitPanel.render();


        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[4]);
        shader.setUniform("ObjectToWorld", moveBox);
        border.render();
        shader.setUniform("ObjectToWorld", new Matrix4f());

        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    void initTextures() {
        //Texture 0
        initTexture("/Gui Elements/background_solidBlack.png", 0);
        //Texture 1
        initTexture("/Gui Elements/text_controls.png", 1);
        //Texture 2
        initTexture("/Gui Elements/text_mainmenu.png", 2);
        //Texture 3
        initTexture("/Gui Elements/text_quitgame.png", 3);
        //Texture 4
        initTexture("/Gui Elements/highlight.png", 4);

    }

    public void moveSelectorUp(){
        if (buttonIndex > 1) {
            moveBox.translate(0,PANEL_HEIGHT * 2,0);
            buttonIndex--;
        }
    }

    public void moveSelectorDown(){
        if (buttonIndex < 3) {
            moveBox.translate(0,-PANEL_HEIGHT * 2,0);
            buttonIndex++;
        }
    }

    public void selectButton(int index) throws IndexOutOfBoundsException{
        switch (index){
            case 1:
                ChangeKeyBindings c = new ChangeKeyBindings();
                break;
            case 2:
                //handled in GLListener
                break;
            case 3:
                System.exit(0);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    public int getButtonIndex(){
        return buttonIndex;
    }
}
