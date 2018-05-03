package World.Worlds;

import Core.*;
import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import Core.InputSystem.GameInputs;
import Core.InputSystem.InputHandler;
import World.*;
import World.AbstractShapes.Rectangle;
import World.WorldObjects.Ground;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

import static Core.GLListener.gl;
import static World.Worlds.AbstractWorld.CameraMode.CHASE;
import static World.Worlds.AbstractWorld.CameraMode.DEBUG_VIEW;
import static World.Worlds.AbstractWorld.CameraMode.SUN_VIEW;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

/**
 * Created by (User name) on 8/14/2017.
 */
public abstract class AbstractWorld implements GameInputs {

    private final Vector3f GRAVITY = new Vector3f(0, -0.2f, 0);
    Shader shader;

    Camera cam1, cam2, cam3;
    private float zoom_factor = 1;
    CameraMode mode = CHASE;   //default is CHASE
    Vector3f debuggerStartPos;
    Vector3f sunlightDirection;

    static Player player;
    static Vector3f initialPlayerPosition;
    static Vector3f initialPlayerDirection;
    static ArrayList<Enemy> enemies;
    private static ArrayList<SceneEntity> worldObjects;
    Ground ground;
    CollisionDetectionSystem CDS;
    private Rectangle scoreText, ones, tens, hundreds;
    private static int playerScore = 0;


    private boolean isPaused = false;
    private int frameCount = 0;
    private static int slowDownCounter;
    private Random r = new Random();
    JsonObject jsonObject;
    int[] textureIDs = new int[15];  //change this value per number of textures being loaded.

    protected enum CameraMode{
        CHASE, DEBUG_VIEW, SUN_VIEW
    }

    public AbstractWorld(final String filename){
        shader = Shader.getInstance();

        //open the json file.
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filename);
        }
        catch (FileNotFoundException e){
            System.err.println("File cannot be read or does not exist.");
            System.exit(-1);
        }

        Gson gson = new Gson();
        JsonParser parse = new JsonParser();
        JsonReader reader = gson.newJsonReader(fileReader);
        jsonObject = (JsonObject) parse.parse(reader);

        worldObjects = new ArrayList<>();
        enemies = new ArrayList<>();

        CDS = CollisionDetectionSystem.getInstance();

        scoreText = new Rectangle(
                new Vector3f(0.5f,-1.0f,-1),
                new Vector3f(0.7f,-1.0f,-1),
                new Vector3f(0.7f,-0.9f,-1),
                new Vector3f(0.5f,-0.9f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        hundreds = new Rectangle(
                new Vector3f(0.7f,-1.0f,-1),
                new Vector3f(0.8f,-1.0f,-1),
                new Vector3f(0.8f,-0.9f,-1),
                new Vector3f(0.7f,-0.9f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        tens = new Rectangle(
                new Vector3f(0.8f,-1.0f,-1),
                new Vector3f(0.9f,-1.0f,-1),
                new Vector3f(0.9f,-0.9f,-1),
                new Vector3f(0.8f,-0.9f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        ones = new Rectangle(
                new Vector3f(0.9f,-1.0f,-1),
                new Vector3f(1.0f,-1.0f,-1),
                new Vector3f(1.0f,-0.9f,-1),
                new Vector3f(0.9f,-0.9f,-1),
                new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f}
        );
        scoreText.init();   //this should be in init() but...it's abstract. Clearly a refactoring design overhaul is warranted.
        ones.init();
        tens.init();
        hundreds.init();
        initRectangleTextures();
    }

    public abstract void init();

    public void render(boolean pureDraw){

        if (slowDownCounter > 1){
            InputHandler.getInstance().lockInput(true);
            try{Thread.sleep(100);}
            catch (InterruptedException e){e.printStackTrace();}
            slowDownCounter--;
        }
        else if (slowDownCounter == 1){
            slowDownCounter--;  //Just because.
            InputHandler.getInstance().lockInput(false);
            GLListener.relinquishInputControl(InputHandler.Context.GAMEOVER);
        }
        if (frameCount > Integer.MAX_VALUE - 5) frameCount = 0;
        frameCount++;
        Matrix4f worldToEye = updateCamera();
        updateSunLight(worldToEye);
        updateLampLight();
        //TODO: This only needs to be called once in init, but I don't want to pass a shader argument?
        shader.setUniform("tex", 1);    // 1 Because using glActiveTexture(TEXTURE1)
        shader.setUniform("numAIPlayers", enemies.size());
        shader.setUniform("ObjectToWorld", new Matrix4f());

        drawShadowedObjects(pureDraw);
        drawNonShadowedObjects(pureDraw);

        if (!pureDraw) {
            //Test for colliding objects
            CDS.testCollisions();
            //CDS.drawBoundingBoxes(false);

            //add gravity
            for (SceneEntity entity : worldObjects) if (entity.isAffectedByGravity) entity.moveDistance(GRAVITY);

            //check if player is out-of-bounds
            if (player.getPosition().y < -10) player.fallDeath();

            //check if for some reason an enemy went out of bounds
            for (Enemy enemy : enemies) if (enemy.getPosition().y < -10) enemy.fallDeath();

            //Run an iteration of the Ai algorithms
            for (Enemy enemy : enemies) enemy.updateAI(player.getPosition());

            //poll for user input
            InputHandler.pollInput();   //there is value in doing this, because this gets called once per frame
        }

    }   //End render()

    public void drawScore(){
        gl.glUniformSubroutinesuiv(GL_FRAGMENT_SHADER, 1, IntBuffer.wrap(new int[] {gl.glGetSubroutineIndex(shader.getProgramID(), GL_FRAGMENT_SHADER, "menuScreen")}));
        shader.setUniform("ObjectToWorld", new Matrix4f());
        shader.setUniform("WorldToEye", new Matrix4f());
        shader.setUniform("Projection", new Matrix4f());

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[4]);
        scoreText.render();

        int num1, num2, num3;
        num1 = playerScore / 100;
        num2 = (playerScore - num1 * 100)/10;
        num3 = (playerScore - (num1 * 100) - num2 * 10)/1;
        if (playerScore > 999) num1 = num2 = num3 = 9;

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[num1 + 5]);
        hundreds.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[num2 + 5]);
        tens.render();

        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[num3 + 5]);
        ones.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0);

    }

    private void initRectangleTextures(){
        //Texture 0
        initTexture("/Gui Elements/text_score.png", 4);
        //numbers
        initTexture("/Gui Elements/0.png", 5);
        initTexture("/Gui Elements/1.png", 6);
        initTexture("/Gui Elements/2.png", 7);
        initTexture("/Gui Elements/3.png", 8);
        initTexture("/Gui Elements/4.png", 9);
        initTexture("/Gui Elements/5.png", 10);
        initTexture("/Gui Elements/6.png", 11);
        initTexture("/Gui Elements/7.png", 12);
        initTexture("/Gui Elements/8.png", 13);
        initTexture("/Gui Elements/9.png", 14);







    }

    //For Objects that will have shadows.
    public abstract void drawShadowedObjects(boolean pureDraw);
    public abstract void drawNonShadowedObjects(boolean pureDraw);
    public abstract void updateLampLight();

    Matrix4f updateCamera(){
        //temporary allocations for use below.
        Matrix4f worldToEye = new Matrix4f();
        Vector3f g = new Vector3f();    //placeholder/garbage, prevents data overwrite.
        Vector3f arg1 = new Vector3f(); //placeholder for argument 1 to other functions.
        Vector3f arg2 = new Vector3f(); //placeholder for argument 2 to other functions.

        if( mode == CHASE){
            //orient the camera
            //player.getPosition().add(new Vector3f(0,5,0).add(player.getDirection().mul(-7.0f, player.getDirection()),g), arg1);   //cam location
            player.getDirection().mul(-7.0f * zoom_factor, arg1);
            new Vector3f(0,5,0).add(arg1, arg1);
            player.getPosition().add(arg1, arg1);
            //arg1.z *= zoom_factor;

            player.getPosition().add(player.getDirection().mul(4.0f, g), arg2);                                                     //cam lookAt point
            cam1.orient(arg1, arg2, new Vector3f(0,1,0));                                                                    //__,__,cam Up vector

            //set the looking at matrix.
            player.getPosition().add(player.getDirection().mul(10.0f, g), arg2);
            worldToEye.lookAt(cam1.getPosition(), arg2, new Vector3f(0,1,0));

            //set the shaders
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam1.getProjectionMatrix());
        }
        else if(mode == DEBUG_VIEW){
            worldToEye = cam2.getViewMatrix();
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam2.getProjectionMatrix());
        }
        else if (mode == SUN_VIEW){
            worldToEye = new Matrix4f().lookAt(getSunlightDirection(), new Vector3f(0), new Vector3f(0,1,0));
            cam3.setLookAtMatrix(getSunlightDirection(), new Vector3f(0), new Vector3f(0,1,0)); //this code prevents nullExceptions, but also freezes cam3
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam3.getProjectionMatrix());

        }
        else {    //Unsupported Camera mode
            System.err.println("Unsupported Camera Mode: "+ mode.toString());
            System.exit(-1);
        }
        return worldToEye;
    }

    private void updateSunLight(Matrix4f worldToEye){
        //if (frameCount % 60 == 0) sunlightDirection.x--;  //perhaps tie the decrement to a key so you can choose when it happens for testing.
        Vector4f sunlightDir = new Vector4f(sunlightDirection, 0.0f);    //0.0f, otherwise no sunlight.
        sunlightDir.mul(worldToEye);
        shader.setUniform("sunDirection", new Vector3f(sunlightDir.x, sunlightDir.y, sunlightDir.z));
        shader.setUniform("sunIntensity", new Vector3f(1.0f));
    }


    private void initTexture(String filename, int iD){
        TextureData textureData;
        shader.setUniform("tex", 1);
        textureData = helperTextureIO(filename, iD);
        createTexture(textureData);

        //unbind
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    TextureData helperTextureIO(String filename, int iD) {
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
        }
        catch (IOException e){System.err.println("Failed to load texture: " + filename); e.printStackTrace();}

        return textureData;
    }

    void createTexture(TextureData textureData){
        gl.glTexStorage2D(GL_TEXTURE_2D, 1, textureData.getInternalFormat(), textureData.getWidth(), textureData.getHeight());
        gl.glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,textureData.getWidth(), textureData.getHeight(), textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());

    }

    public Vector3f getSunlightDirection() {return new Vector3f(sunlightDirection);}
    public void setSunlightDirection(Vector3f newDir){sunlightDirection = new Vector3f(newDir);}

    public static void addWorldObject(SceneEntity e){
        worldObjects.add(e);
    }

    public static void removeWorldObject(SceneEntity e){
        worldObjects.remove(e);
    }

    public static void increasePlayerScore(){
        playerScore++;
    }

    public static void slowAnimation(int forHowManyFrames){
        slowDownCounter = forHowManyFrames;
    }

    public void updateAspectRatio(float newAspect){
        cam1.updateAspect(newAspect);
        cam2.updateAspect(newAspect);
        cam3.updateAspect(newAspect);
    }

    private Vector3f findOpposingPosition(Vector3f pos) {
        //only use x,z and NOT y.
        float x,z;
        x = (pos.x > 0) ? ground.getMinCorner().x + 5 : ground.getMaxCorner().x - 5;
        z = (pos.z > 0) ? ground.getMinCorner().z + 5 : ground.getMaxCorner().z - 5;
        return new Vector3f(x, pos.y, z);

    }

    Vector3f findRandomPosition(){
        int x;
        int y;
        int z;
        do {
            x = r.nextInt(190) - 95;
            y = 1;
            z = r.nextInt(190) - 95;
        } while (Utilities.Utilities.dist(new Vector3f(x,y,z), player.getPosition()) < 10);
        return new Vector3f(x,y,z);
    }

    //NOTE: alternative is to check an .isDead field of player per frame and respawn based on that.
    public static void spawnPlayer(Player p){
        if (p.equals(player)) {
            player.setPosition(initialPlayerPosition);
            player.setDirection(initialPlayerDirection);
        }
        else {
            for (Enemy enemy : enemies){
                if (p.equals(enemy)) {
                    enemy.setPosition(enemy.getSpawnLocation());
                    enemy.setDirection(enemy.getSpawnDirection());
                }
            }
        }
    }

    public static void destroyStaticData(){
        player = null;
        initialPlayerPosition = null;
        initialPlayerDirection = null;
        enemies = null;
        worldObjects = null;
        playerScore = 0;
        CollisionDetectionSystem.reset();
    }

    //GameInputs interface
    private final float MOVE_SPEED = 0.5f;
    private final float TURN_SPEED = 3.0f;

    @Override
    public void move_forward() {
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getN().negate());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getN().negate());
        else if(mode == CHASE)
            player.move(MOVE_SPEED);
    }

    @Override
    public void move_backward() {
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getN());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getN());
        else if(mode == CHASE)
            player.move(-MOVE_SPEED);
    }

    @Override
    public void move_up(){
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getV());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getN());
        else{
            player.moveY(0.3f);
            //System.out.println("People don't normally float up into the air.");
        }
    }

    @Override
    public void move_down(){
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getV().negate());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getV().negate());
        else {
            player.moveY(-0.3f);
            //System.out.println("People don't normally descend into the ground.");
        }
    }

    @Override
    public void move_left() {
        System.err.println("Move_Left is bugged.");
        player.move(player.getDirection().normalize().rotateY((float)Math.toRadians(90)).normalize());
    }

    @Override
    public void move_right() {
        System.err.println("Move_Right is bugged.");
        player.move(player.getDirection().normalize().rotateY((float)Math.toRadians(-90)).normalize());
    }

    @Override
    public void turn_left() {
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getU().negate());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getU().negate());
        else if(mode == CHASE)
            player.turn(TURN_SPEED);
    }

    @Override
    public void turn_right() {
        if (mode == DEBUG_VIEW)
            cam2.slide(cam2.getU());
        else if (mode == SUN_VIEW)
            cam3.slide(cam3.getU());
        else if(mode == CHASE)
            player.turn(-TURN_SPEED);
    }

    @Override
    public void jump() {
    //implement acceleration and velocity. ugh.
        //player.isAffectedByGravity
        if (player.isOnGround) {
            player.moveY(MOVE_SPEED * 10);
            player.isOnGround = false;
        }
    }

    @Override
    public void shoot(){
        player.shootLaser();
    }

    @Override
    public void switch_mode_normal(){ mode = CHASE; }

    @Override
    public void switch_mode_debug(){ mode = DEBUG_VIEW; }

    @Override
    public void switch_mode_sun(){ mode = SUN_VIEW;}

    @Override
    public void pause(){
        //TODO: pause animation?
        //isPaused = !isPaused;
        //PauseMenu.setVisibility(true);
        GLListener.relinquishInputControl(InputHandler.Context.PAUSEMENU);
    }

    @Override
    public void zoom(float amount){
        final float MAX_ZOOM = 3;
        final float MIN_ZOOM = 0.5f;
        zoom_factor += amount;
        if (zoom_factor > MAX_ZOOM) zoom_factor = MAX_ZOOM;
        if (zoom_factor < MIN_ZOOM) zoom_factor = MIN_ZOOM;
    }

    @Override
    public void move_camera(Vector2f dif){
        if (mode == DEBUG_VIEW) {
            cam2.rotate(dif.x, new Vector3f(0, 1, 0));
            cam2.pitch(dif.negate().y * 0.05f);
        }
        else if (mode == SUN_VIEW){
            cam3.rotate(dif.x, new Vector3f(0, 1, 0));
            //cam3.pitch(dif.negate().y * 0.05f);
        }
    }
    //End of GameInputs Interface
}
