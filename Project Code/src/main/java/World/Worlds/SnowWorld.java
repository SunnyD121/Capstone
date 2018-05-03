package World.Worlds;

import Core.Camera;
import World.*;
import World.AbstractShapes.Building;
import World.AbstractShapes.Cone;
import World.AbstractShapes.Cylinder;
import World.WorldObjects.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.TextureData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;

public class SnowWorld extends AbstractWorld{

    private ArrayList<Building> buildings;
    private ArrayList<Tree> trees;
    private Emitter snowEmitter;

    public SnowWorld(final String filename){
        super(filename);
        buildings = new ArrayList<>();
        trees = new ArrayList<>();
        snowEmitter = new Emitter(Emitter.ParticleType.SNOWFLAKE, new Vector3f(0,20,0), new Vector3f(0,-1,0), 10);
    }

    @Override
    public void init() {
        //bbox
        JsonArray bbox = jsonObject.get("bbox").getAsJsonArray();
        Vector3f minbbox = new Vector3f(bbox.get(0).getAsFloat(), bbox.get(1).getAsFloat(), bbox.get(2).getAsFloat());
        Vector3f maxbbox = new Vector3f(bbox.get(3).getAsFloat(), bbox.get(4).getAsFloat(), bbox.get(5).getAsFloat());

        //debugCamera view location
        JsonArray observe = jsonObject.get("debugPosition").getAsJsonArray();
        debuggerStartPos = new Vector3f(observe.get(0).getAsFloat(), observe.get(1).getAsFloat(), observe.get(2).getAsFloat());

        //Ground
        Vector3f p1 = new Vector3f(minbbox.x, minbbox.y, maxbbox.z);    //using the bounding box to derive the ground of the world.
        Vector3f p2 = new Vector3f(maxbbox.x, minbbox.y, maxbbox.z);
        Vector3f p3 = new Vector3f(maxbbox.x, minbbox.y, minbbox.z);
        Vector3f p4 = minbbox;
        ground = new Ground(p1, p2, p3, p4);
        addWorldObject(ground);

        //Trees
        JsonArray array = jsonObject.get("trees").getAsJsonArray();
        for (int i = 0; i < array.size(); i++){
            JsonObject tree = array.get(i).getAsJsonObject();
            JsonArray positionData = tree.get("position").getAsJsonArray();
            float height = tree.get("height").getAsFloat();
            trees.add(new Tree(
                    new Cylinder(0.4f, height * 0.3f),
                    new Cone(1, height * 0.7f, 20, false),
                    new Vector3f(positionData.get(0).getAsFloat(),positionData.get(1).getAsFloat(),positionData.get(2).getAsFloat() )
            ));
            addWorldObject(trees.get(i));
        }

        //Buildings
        array = jsonObject.get("buildings").getAsJsonArray();
        for (int i = 0; i < array.size(); i++){
            //pull data from Json
            JsonObject building = array.get(i).getAsJsonObject();
            JsonArray positionData = building.get("vertexData").getAsJsonArray();
            buildings.add(new Building());

            //organize the data. json numbers are clockwise, hence the descending fashion.  //TODO they are?
            Vector3f[] points = new Vector3f[4];
            points[0] = new Vector3f(positionData.get(12).getAsFloat(), positionData.get(13).getAsFloat(), positionData.get(14).getAsFloat());   //point 1
            points[1] = new Vector3f(positionData.get(8).getAsFloat(), positionData.get(9).getAsFloat(), positionData.get(10).getAsFloat());     //point 2
            points[2] = new Vector3f(positionData.get(4).getAsFloat(), positionData.get(5).getAsFloat(), positionData.get(6).getAsFloat());      //point 3
            points[3] = new Vector3f(positionData.get(0).getAsFloat(), positionData.get(1).getAsFloat(), positionData.get(2).getAsFloat());      //point 4

            float[] height = new float[4];
            height[0] = positionData.get(15).getAsFloat();
            height[1] = positionData.get(11).getAsFloat();
            height[2] = positionData.get(7).getAsFloat();
            height[3] = positionData.get(3).getAsFloat();

            //create the building in the world
            buildings.get(i).setData(points, height);
            addWorldObject(buildings.get(i));
        }

        //Player
        JsonArray startPosition = jsonObject.get("playerStartPosition").getAsJsonArray();
        initialPlayerPosition = new Vector3f(startPosition.get(0).getAsFloat(), startPosition.get(1).getAsFloat() + 1, startPosition.get(2).getAsFloat());
        player = new Player(Color.WHITE);
        addWorldObject(player);
        player.assignPlayerNum(0);

        //Enemies
        enemies.add(new Enemy(Color.RED));
        addWorldObject(enemies.get(0));
        enemies.add(new Enemy(Color.YELLOW));
        addWorldObject(enemies.get(1));
        enemies.add(new Enemy(Color.CYAN));
        addWorldObject(enemies.get(2));
        enemies.add(new Enemy(Color.MAGENTA));
        addWorldObject(enemies.get(3));
        enemies.add(new Enemy(Color.GREEN));
        addWorldObject(enemies.get(4));
        for (int i = 0; i < enemies.size();i++) enemies.get(i).assignPlayerNum(i+1);

        //Weather
        JsonObject weatherObject = jsonObject.get("weather").getAsJsonObject();
        JsonArray sunDirection = weatherObject.get("sunLightDirection").getAsJsonArray();
        sunlightDirection = new Vector3f(sunDirection.get(0).getAsFloat(), sunDirection.get(1).getAsFloat(), sunDirection.get(2).getAsFloat());

        //Textures
        initTextures();

        //init calls
        ground.init();
        player.init();
        for (Enemy enemy : enemies) enemy.init();
        for (Tree tree : trees) tree.init();
        for (Building building : buildings) building.init();

        //other
        player.setPosition(initialPlayerPosition);
        Vector3f dir = new Vector3f(player.getDirection());
        dir.rotateAxis((float)Math.toRadians(70.0f), 0,1,0);
        initialPlayerDirection = player.getPosition().sub(new Vector3f()).normalize().negate();
        player.setDirection(initialPlayerDirection);

        array = jsonObject.getAsJsonArray("spawnLocations");
        for (int i = 0; i < enemies.size(); i++){
            JsonArray pos = array.get(i).getAsJsonArray();
            enemies.get(i).setPosition(new Vector3f(pos.get(0).getAsFloat(), pos.get(1).getAsFloat(), pos.get(2).getAsFloat()));
            enemies.get(i).getPosition().sub(new Vector3f(0,0,0), dir); //vector pointing towards middle
            dir.negate().normalize();
            enemies.get(i).setDirection(dir);
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


        //Collision Boxes
        CDS.addCollisionBox(player, player.getPosition(), player.getLength(), player.getHeight(), player.getWidth(), player.getGroundAdjustment());
        for (Enemy p : enemies) CDS.addCollisionBox(p, p.getPosition(), p.getLength(), p.getHeight(), p.getWidth(), p.getGroundAdjustment());
        CDS.addCollisionBox(ground, ground.getPosition(), ground.getLength(), ground.getWidth(), ground.getHeight(), null);
        for (Tree tree : trees) CDS.addCollisionBox(tree, tree.getPosition(), tree.getLength(), tree.getHeight(), tree.getWidth(), tree.getPositionalOffset());

        for (Building b : buildings) {
            Vector3f minP = b.getMin();
            Vector3f maxP = b.getMax();
            CDS.addCollisionBox(b, minP, maxP, null);
        }


        CDS.postInit();
    }

    @Override
    public void drawShadowedObjects(boolean pureDraw) {
        player.render(pureDraw);
        for (Enemy enemy : enemies) enemy.render(pureDraw);
        drawTrees();
        drawBuildings();
    }

    @Override
    public void drawNonShadowedObjects(boolean pureDraw) {
        drawGround();
        drawParticles(pureDraw);
    }

    @Override
    public void updateLampLight() {
        shader.setUniform("lampIntensity", new Vector3f());
        shader.setUniform("flashIntensity", 0);
    }

    private void drawGround(){
        Material m = new Material();
        m.Kd = new Vector3f(1,1,1f);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        shader.setUniform("ObjectToWorld", new Matrix4f());
        ground.render();
    }

    private void drawParticles(boolean pureDraw){
        player.drawLasers(0, pureDraw);
        for (int i=0;i<enemies.size();i++) enemies.get(i).drawLasers(i+1, pureDraw);
        Vector3f camPos;
        if (mode == CameraMode.CHASE) camPos = cam1.getPosition();
        else if (mode == CameraMode.DEBUG_VIEW) camPos = cam2.getPosition();
        else camPos = cam3.getPosition();
        snowEmitter.update(camPos, pureDraw);
    }

    private void drawTrees(){
        for (Tree t : trees) t.render();
    }

    private void drawBuildings(){
        Material m = new Material();
        m.Kd = new Vector3f(0);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);

        shader.setUniform("ObjectToWorld", new Matrix4f());
        for(Building b : buildings)
            b.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0); //unbind
    }

    private void initTextures(){
        TextureData textureData;
        //Texture 1
        textureData = helperTextureIO("/textures/facade1.jpg", 1);
        gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_MAX_LEVEL, 0);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        createTexture(textureData);

        //texture 2


        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }
}
