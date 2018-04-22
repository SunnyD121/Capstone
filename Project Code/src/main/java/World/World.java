package World;

import Core.*;
import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import GUI.PauseMenu;
import World.AbstractShapes.*;
import World.WorldObjects.Ground;
import World.WorldObjects.Lamp;
import World.WorldObjects.StepPyramid;
import World.WorldObjects.Tree;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import com.jogamp.opengl.GL4;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Random;

import static Core.GLListener.gl;
import static World.World.CameraMode.CHASE;
import static World.World.CameraMode.DEBUG_VIEW;
import static World.World.CameraMode.SUN_VIEW;
import static com.jogamp.opengl.GL.*;

/**
 * Created by (User name) on 8/14/2017.
 */
public class World implements InputNotifiee {

    private final Vector3f GRAVITY = new Vector3f(0, -0.2f, 0);

    private static Vector3f initialPlayerPosition;
    private static Vector3f initialPlayerDirection;
    private static Player player;
    private static ArrayList<Enemy> enemies;
    private Camera cam1, cam2, cam3;
    private float zoom_factor = 1;
    private final float MAX_ZOOM = 3;
    private final float MIN_ZOOM = 0.5f;
    protected CameraMode mode = CHASE;   //default is CHASE
    private static ArrayList<SceneEntity> worldObjects;
    private Vector3f sunlightDirection;
    private ArrayList<Building> city;
    private Ground ground;
    private JsonObject jsonObject;
    private Vector3f fixedPos, debuggerStartPos;
    private ArrayList<Tree> trees;
    private ArrayList<Lamp> lamps;
    private ArrayList<StepPyramid> stepPyramids;
    private int[] textureIDs = new int[4];  //change this value per number of textures being loaded.
    private Emitter snowEmitter;
    private CollisionDetectionSystem CDS;

    protected enum CameraMode{
        CHASE, DEBUG_VIEW, SUN_VIEW
    }

    public World(final String filename){
        //Set the Context for polling the correct keybinding
        InputHandler.setContext(this);

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
        enemies = new ArrayList<Enemy>();
        trees = new ArrayList<>();
        lamps = new ArrayList<>();

        CDS = CollisionDetectionSystem.getInstance();

    }

    public void init(){
        //bounding box coordinates, forms two opposite vertices.
        JsonArray bbox = jsonObject.get("bbox").getAsJsonArray();
        Vector3f minbbox = new Vector3f(bbox.get(0).getAsFloat(), bbox.get(1).getAsFloat(), bbox.get(2).getAsFloat());
        Vector3f maxbbox = new Vector3f(bbox.get(3).getAsFloat(), bbox.get(4).getAsFloat(), bbox.get(5).getAsFloat());

        //fixed camera position
        JsonArray fixed = jsonObject.get("photoPosition").getAsJsonArray();
        fixedPos = new Vector3f(fixed.get(0).getAsFloat(), fixed.get(1).getAsFloat(), fixed.get(2).getAsFloat());

        //DEBUG_VIEW camera position
        JsonArray observe = jsonObject.get("observerPosition").getAsJsonArray();
        debuggerStartPos = new Vector3f(observe.get(0).getAsFloat(), observe.get(1).getAsFloat(), observe.get(2).getAsFloat());

        //Buildings
        JsonArray buildings = jsonObject.get("buildings").getAsJsonArray();
        city = new ArrayList<>();
        for (int i = 0; i < buildings.size(); i++){
            //pull data from Json
            JsonObject buildingObjects = buildings.get(i).getAsJsonObject();
            JsonArray floorplan = buildingObjects.get("outline").getAsJsonArray();
            city.add(new Building());

            //organize the data. json numbers are clockwise, hence the descending fashion.
            Vector3f[] points = new Vector3f[4];
            points[0] = new Vector3f(floorplan.get(12).getAsFloat(), floorplan.get(13).getAsFloat(), floorplan.get(14).getAsFloat());   //point 1
            points[1] = new Vector3f(floorplan.get(8).getAsFloat(), floorplan.get(9).getAsFloat(), floorplan.get(10).getAsFloat());     //point 2
            points[2] = new Vector3f(floorplan.get(4).getAsFloat(), floorplan.get(5).getAsFloat(), floorplan.get(6).getAsFloat());      //point 3
            points[3] = new Vector3f(floorplan.get(0).getAsFloat(), floorplan.get(1).getAsFloat(), floorplan.get(2).getAsFloat());      //point 4

            float[] height = new float[4];
            height[0] = floorplan.get(15).getAsFloat();
            height[1] = floorplan.get(11).getAsFloat();
            height[2] = floorplan.get(7).getAsFloat();
            height[3] = floorplan.get(3).getAsFloat();

            //create the building in the world
            city.get(i).setData(points, height);

            addWorldObject(city.get(i));
        }

        //Trees
        JsonArray woods = jsonObject.get("trees").getAsJsonArray();
        for(int i = 0; i < woods.size(); i++){
            JsonObject tree = woods.get(i).getAsJsonObject();
            JsonArray treePosition = tree.get("position").getAsJsonArray();
            float treeHeight = tree.get("height").getAsFloat();
            trees.add(new Tree(
                    new Cylinder(0.4f, treeHeight * 0.3f),
                    new Cone(1, treeHeight * 0.7f, 20, false),
                    new Vector3f(treePosition.get(0).getAsFloat(),treePosition.get(1).getAsFloat(),treePosition.get(2).getAsFloat() )
            ));

            addWorldObject(trees.get(i));
        }

        //Ground
        Vector3f p1 = new Vector3f(minbbox.x, minbbox.y, maxbbox.z);    //using the bounding box to derive the ground of the world.
        Vector3f p2 = new Vector3f(maxbbox.x, minbbox.y, maxbbox.z);
        Vector3f p3 = new Vector3f(maxbbox.x, minbbox.y, minbbox.z);
        Vector3f p4 = minbbox;
        ground = new Ground(p1, p2, p3, p4);

        addWorldObject(ground);

        //Player
        JsonArray startPosition = jsonObject.get("startPosition").getAsJsonArray();
        initialPlayerPosition = new Vector3f(startPosition.get(0).getAsFloat(), startPosition.get(1).getAsFloat() + 1, startPosition.get(2).getAsFloat());
        player = new Player(Color.WHITE);
        addWorldObject(player);
        player.assignPlayerNum(0);

        //Enemies
        //enemies.add(new Enemy(Color.BLACK));  //DO NOT USE BLACK

        enemies.add(new Enemy(Color.RED));
        addWorldObject(enemies.get(0));

        enemies.add(new Enemy(Color.YELLOW));
        addWorldObject(enemies.get(1));
        enemies.add(new Enemy(Color.PINK));
        addWorldObject(enemies.get(2));
        enemies.add(new Enemy(Color.MAGENTA));
        addWorldObject(enemies.get(3));
        enemies.add(new Enemy(Color.GREEN));
        addWorldObject(enemies.get(4));

        for (int i = 0; i < enemies.size();i++) enemies.get(i).assignPlayerNum(i+1);

        //Lamps
        JsonArray lights = jsonObject.get("lamps").getAsJsonArray();
        for (int i = 0; i < lights.size(); i++) {        //for each lamp
            JsonObject lampInfo = lights.get(i).getAsJsonObject();
            JsonArray lampPos = lampInfo.get("position").getAsJsonArray();  //get the position of a lamp
            float height = lampInfo.get("height").getAsFloat();
            lamps.add(new Lamp(
                    new Cylinder(0.5f, height),
                    new IcoSphere(1.0f, 3),
                    new Vector4f(lampPos.get(0).getAsFloat(), lampPos.get(1).getAsFloat(), lampPos.get(2).getAsFloat(), height)
            ));

            addWorldObject(lamps.get(i));
        }

        //Weather
        JsonObject weatherObject = jsonObject.get("weather").getAsJsonObject();
        JsonArray sunDirection = weatherObject.get("sunLightDirection").getAsJsonArray();
        sunlightDirection = new Vector3f(sunDirection.get(0).getAsFloat(), sunDirection.get(1).getAsFloat(), sunDirection.get(2).getAsFloat());

        //Step Pyramids
        stepPyramids = new ArrayList<>();
        JsonArray stepramids = jsonObject.get("steppyramids").getAsJsonArray();
        for (int i=0;i<stepramids.size();i++){
            JsonObject pyramidData = stepramids.get(i).getAsJsonObject();
            JsonArray positionData = pyramidData.get("position").getAsJsonArray();
            stepPyramids.add(new StepPyramid(
                    new Vector3f(positionData.get(0).getAsFloat(), positionData.get(1).getAsFloat(), positionData.get(2).getAsFloat()),
                    pyramidData.get("baselength").getAsFloat(),
                    pyramidData.get("stepheight").getAsFloat(),
                    pyramidData.get("stepshrink").getAsFloat(),
                    pyramidData.get("mirrored").getAsBoolean()
            ));

            addWorldObject(stepPyramids.get(i));
        }

        //Textures
        initTextures();

        //Test Objects


        //init calls
        for (Building b : city) b.init();
        for (Tree t : trees) t.init();
        for (Lamp l : lamps) l.init();
        for (StepPyramid step : stepPyramids) step.init();
        ground.init();
        player.init();
        for (Enemy enemy : enemies) enemy.init();

        //Particles
        //NOTE: increasing the particleDensity increases severity of storm, but it also introduces lag.
        //snowEmitter = new Emitter(SNOWFLAKE, new Vector3f(0,50,0), 30);

        //world has been initialized, now add dynamic things like players
        //player
        player.setPosition(initialPlayerPosition);
        Vector3f dir = new Vector3f(player.getDirection());
        dir.rotateAxis((float)Math.toRadians(70.0f), 0,1,0);
        initialPlayerDirection = new Vector3f(dir.normalize());
        player.setDirection(initialPlayerDirection);
        //enemies
        for (int i = 0; i < enemies.size(); i++){
            //enemies.get(i).setPosition(findOpposingPosition(initialPlayerPosition));
            enemies.get(i).setPosition(findRandomPosition());
            enemies.get(i).getPosition().sub(new Vector3f(0,0,0), dir); //vector pointing towards middle
            dir.negate().normalize();
            enemies.get(i).setDirection(dir);
            //set the spawnData in stone.
            enemies.get(i).setSpawnLocation(enemies.get(i).getPosition());
            enemies.get(i).setSpawnDirection(enemies.get(i).getDirection());
            enemies.get(i).becomeAutonomous();

        }


        //Cameras
        cam1 = new Camera(Camera.ProjectionType.PERSPECTIVE);
        cam2 = new Camera(Camera.ProjectionType.PERSPECTIVE);
        cam2.orient(debuggerStartPos, new Vector3f(0,0,0), new Vector3f(0,1,0));
        cam3 = new Camera(Camera.ProjectionType.ORTHO);
        Vector3f camera3Pos = new Vector3f(sunlightDirection.x, sunlightDirection.y, sunlightDirection.z).mul(160);
        cam3.orient(camera3Pos, new Vector3f(0,0,0), new Vector3f(0,1,0));
        cam3.setOrtho(-100,100,-100,100,-100,100);
        //cam3.setViewVolume(50.0f,1.0f,1.0f,250.0f); //not needed, values are set in other statements

        /* Collision Boxes */
        //player
        CDS.addCollisionBox(player, player.getPosition(), player.getLength(), player.getHeight(), player.getWidth(), player.getGroundAdjustment());
        //enemies
        for (Enemy p : enemies) CDS.addCollisionBox(p, p.getPosition(), p.getLength(), p.getHeight(), p.getWidth(), p.getGroundAdjustment());
        //buildings
        for (Building b : city) {
            Vector3f minP = b.getMin();
            Vector3f maxP = b.getMax();
            CDS.addCollisionBox(b, minP, maxP, null);
        }
        //trees
        for (Tree tree : trees) CDS.addCollisionBox(tree, tree.getPosition(), tree.getLength(), tree.getHeight(), tree.getWidth(), tree.getPositionalOffset());
        //ground
        CDS.addCollisionBox(ground, ground.getPosition(), ground.getLength(), ground.getWidth(), ground.getHeight(), null);
                //width and height switch here because of how getHeight() and getWidth() are defined in Rectangle. 3D vs 2D.
        //lamps
        for (Lamp lamp : lamps) CDS.addCollisionBox(lamp, lamp.getPosition(), lamp.getLength(), lamp.getHeight(), lamp.getWidth(), lamp.getPositionalOffset());
        //steppyramid
        for (StepPyramid pyr : stepPyramids) CDS.addCollisionBox(pyr, pyr.getPosition(), pyr.getLength(), pyr.getHeight(), pyr.getWidth(), pyr.getPositionalOffset());
        //Particles
        //This must be done when the object is created, so, not here.
        //TODO: Particle box doesn't accurately surround laser  EDIT: it's better, but still needs a little work.

        //CDS.formConglomerates(Tree.class);  //creates a bounding box around all instances of Tree in the scene. (thus far)
        CDS.postInit();

    } //end init()

    boolean isPaused = false;
    public void render(Shader shader){
        Matrix4f worldToEye = updateCamera(shader);
        updateLight(shader, worldToEye);

        //TODO: This only needs to be called once in init, but I don't want to pass a shader argument?
        shader.setUniform("tex", 1);    // 1 Because using glActiveTexture(TEXTURE1)
        shader.setUniform("numAIPlayers", enemies.size());
        shader.setUniform("ObjectToWorld", new Matrix4f());

        //Render Test Objects:

        //draw the objects of the world:
        drawWorld(shader);
        //The following will not cast a shadow.
        drawGround(shader);
        drawParticles(shader);

        //Test for colliding objects
        CDS.testCollisions();
        //CDS.drawBoundingBoxes(shader, false);


        //add gravity
        for (SceneEntity entity : worldObjects) if (entity.isAffectedByGravity) entity.moveDistance(GRAVITY);

        //check if player is out-of-bounds
        if (player.getPosition().y < -10) killPlayer(player);
        //check if for some reason an enemy went out of bounds
        for (Enemy enemy : enemies) if (enemy.getPosition().y < -10) killPlayer(enemy);

        //Run an iteration of the Ai algorithms
        for (Enemy enemy : enemies) enemy.updateAI(player.getPosition());

        //poll for user input
        InputHandler.pollInput();   //there is value in doing this, because this gets called once per frame

    }//end render

    //For Objects that will have shadows.
    public void drawWorld(Shader shader){
        drawBuildings(shader);
        drawTrees(shader);
        drawLamps(shader);
        drawStepPyramids(shader);
        player.render(shader);
        for (Enemy enemy : enemies) enemy.render(shader);
    }

    private void drawTrees(Shader shader){
        for (Tree t : trees) t.render(shader);
    }

    private void drawLamps(Shader shader){
        for (Lamp l : lamps) l.render(shader);
    }


    private void drawBuildings(Shader shader){

        Material m = new Material();
        //m.Kd = new Vector3f(0.5f, 0.5f, 0.2f);      //Yellow
        m.Kd = new Vector3f(0);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);

        for(Building b : city)
            b.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0); //unbind
    }

    private void drawGround(Shader shader){
        shader.setUniform("ObjectToWorld", new Matrix4f());

        Material m = new Material();
        m.Kd = new Vector3f(0,0.1f,0);      //dark green, for shading the texture more accurately
        m.Ka = m.Kd;
        m.setUniforms(shader);

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[2]);

        ground.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0); //unbind
    }

    private void drawStepPyramids(Shader shader){
        for(int i = 0; i < stepPyramids.size(); i++){
            drawStepPyramid(shader, stepPyramids.get(i).getPosition(), stepPyramids.get(i));
        }
    }

    private void drawStepPyramid(Shader shader, Vector3f position, StepPyramid step){
        Material m = new Material();
        m.Kd = new Vector3f(1,1,0.37f); //beige
        m.Ka = m.Kd;
        m.setUniforms(shader);

        Matrix4f mat = new Matrix4f();
        mat.translate(position.x, position.y + step.getBaseRectHeight()/2.0f, position.z);  //center is at the center of the bottommost rectangle.
        shader.setUniform("ObjectToWorld", mat);
        step.render(shader, mat);
    }
    private void drawParticles(Shader shader){
        player.drawLasers(shader, 0);
        for (int i = 0; i < enemies.size(); i++) enemies.get(i).drawLasers(shader, i+1);

        //Material coloring is handled withing emitter.update()
        //snowEmitter.run(shader, new Matrix4f(), (mode == CHASE) ? cam1.getPosition() : cam2.getPosition());

    }

    private Matrix4f updateCamera(Shader shader){
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
            worldToEye = cam3.getViewMatrix();
            //shader.setUniform("WorldToEye", worldToEye);
            //shader.setUniform("Projection", cam3.getProjectionMatrix());
            shader.setUniform("WorldToEye", new Matrix4f().lookAt(getSunlightDirection(), new Vector3f(0), new Vector3f(0,1,0)));
            shader.setUniform("Projection", cam3.getProjectionMatrix());

        }
        else {    //Unsupported Camera mode
            System.err.println("Unsupported Camera Mode: "+ mode.toString());
            System.exit(-1);
        }
        return worldToEye;
    }

    float test = 1.0f; boolean sunbool = false;
    private void updateLight(Shader shader, Matrix4f worldToEye){

        //draw the lamp's light
        shader.setUniform("lampIntensity", new Vector3f(18.0f));

        for (int i = 0; i < lamps.size(); i++){
            Vector3f place = lamps.get(i).getPosition();
            shader.setUniform("lights["+i+"]", new Vector3f(place.x, place.y + lamps.get(i).getLightHeight(), place.z));
        }

        //sunlight
        Vector4f sunlightDir = new Vector4f(sunlightDirection, 0.0f);    //0.0f, otherwise no sunlight.
        sunlightDir.mul(worldToEye);
        shader.setUniform("sunDirection", new Vector3f(sunlightDir.x, sunlightDir.y, sunlightDir.z));
        shader.setUniform("sunIntensity", new Vector3f(1.0f));  //test
        /////
        if (sunbool) test += 0.01f;
        else test -= 0.01f;
        if (test >= 1.0f){ sunbool = !sunbool; test = 0.99f;}
        else if (test <= 0.0f) { sunbool = !sunbool; test = 0.01f; }
        /////
    }

    private void initTextures(){
        TextureData textureData;
        //Texture 1
        textureData = helperTextureIO("/textures/facade1.jpg", 1);
        //texturing options
        gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_MAX_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        createTexture(textureData);

        //Texture 2
        textureData = helperTextureIO("/textures/grass.png", 2);
        createTexture(textureData);



        //shader.setUniform("tex", 0);
        //TODO: do I need a null texture? cross reference with C++ code.
        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    private TextureData helperTextureIO(String filename, int iD) {
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

    private void createTexture(TextureData textureData){
        gl.glTexStorage2D(GL_TEXTURE_2D, 1, textureData.getInternalFormat(), textureData.getWidth(), textureData.getHeight());
        gl.glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,textureData.getWidth(), textureData.getHeight(), textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());

    }

    public Vector3f getSunlightDirection() {return new Vector3f(sunlightDirection.x, sunlightDirection.y, sunlightDirection.z);}

    public static void addWorldObject(SceneEntity e){
        worldObjects.add(e);
    }

    public static void removeWorldObject(SceneEntity e){
        worldObjects.remove(e);
    }

    private Vector3f findOpposingPosition(Vector3f pos) {
        //only use x,z and NOT y.
        float x,z;
        x = (pos.x > 0) ? ground.getMinCorner().x + 5 : ground.getMaxCorner().x - 5;
        z = (pos.z > 0) ? ground.getMinCorner().z + 5 : ground.getMaxCorner().z - 5;
        return new Vector3f(x, pos.y, z);

    }

    Random r = new Random();
    private Vector3f findRandomPosition(){
        int x = r.nextInt(90) + 5;
        int y = 1;
        int z = r.nextInt(90) + 5;
        return new Vector3f(x,y,z);
    }

    public void killPlayer(Player p){
        spawnPlayer(p);
    }
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

    //InputNotifiee interface
    final float MOVE_SPEED = 0.5f;
    final float TURN_SPEED = 3.0f;

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

    }

    @Override
    public void move_right() {

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
        //player.isOnGround
        player.moveY(MOVE_SPEED * 10);
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
        isPaused = !isPaused;
        PauseMenu.setVisibility(true);
    }

    @Override
    public void zoom(float amount){
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
    //End of InputNotifiee Interface
}
