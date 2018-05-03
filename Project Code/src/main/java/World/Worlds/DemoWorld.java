package World.Worlds;


import Core.Camera;
import World.*;
import World.AbstractShapes.Building;
import World.AbstractShapes.Cone;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.IcoSphere;
import World.WorldObjects.Ground;
import World.WorldObjects.Lamp;
import World.WorldObjects.StepPyramid;
import World.WorldObjects.Tree;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.TextureData;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.Color;
import java.util.ArrayList;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_NEAREST;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;

public class DemoWorld extends AbstractWorld{

    private ArrayList<Building> city;
    private ArrayList<Tree> trees;
    private ArrayList<Lamp> lamps;
    private ArrayList<StepPyramid> stepPyramids;


    public DemoWorld(final String filename){
        super(filename);

        trees = new ArrayList<>();
        lamps = new ArrayList<>();
        city = new ArrayList<>();
        stepPyramids = new ArrayList<>();

    }

    @Override
    public void init(){
        //bounding box coordinates, forms two opposite vertices.
        JsonArray bbox = jsonObject.get("bbox").getAsJsonArray();
        Vector3f minbbox = new Vector3f(bbox.get(0).getAsFloat(), bbox.get(1).getAsFloat(), bbox.get(2).getAsFloat());
        Vector3f maxbbox = new Vector3f(bbox.get(3).getAsFloat(), bbox.get(4).getAsFloat(), bbox.get(5).getAsFloat());

        //DEBUG_VIEW camera position
        JsonArray observe = jsonObject.get("observerPosition").getAsJsonArray();
        debuggerStartPos = new Vector3f(observe.get(0).getAsFloat(), observe.get(1).getAsFloat(), observe.get(2).getAsFloat());

        //Buildings
        JsonArray buildings = jsonObject.get("buildings").getAsJsonArray();
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
        JsonArray startPosition = jsonObject.get("playerStartPosition").getAsJsonArray();
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
        enemies.add(new Enemy(Color.CYAN));
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
        for (Enemy enemy : enemies){
            //enemies.get(i).setPosition(findOpposingPosition(initialPlayerPosition));
            enemy.setPosition(findRandomPosition());
            enemy.getPosition().sub(new Vector3f(0,0,0), dir); //vector pointing towards middle
            dir.negate().normalize();
            enemy.setDirection(dir);
            //set the spawnData in stone.
            enemy.setSpawnLocation(enemy.getPosition());
            enemy.setSpawnDirection(enemy.getDirection());
            enemy.becomeAutonomous();

        }


        //Cameras
        cam1 = new Camera(Camera.ProjectionType.PERSPECTIVE);
        cam2 = new Camera(Camera.ProjectionType.PERSPECTIVE);
        cam2.orient(debuggerStartPos, new Vector3f(0,0,0), new Vector3f(0,1,0));
        cam3 = new Camera(Camera.ProjectionType.ORTHO);
        //Vector3f camera3Pos = new Vector3f(sunlightDirection).mul(160);
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



        gl.glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void drawShadowedObjects(boolean pureDraw){
        drawBuildings();
        drawTrees();
        drawLamps();
        drawStepPyramids();
        player.render(pureDraw);
        for (Enemy enemy : enemies) enemy.render(pureDraw);
    }

    @Override
    public void drawNonShadowedObjects(boolean pureDraw) {
        drawGround();
        drawParticles(pureDraw);
    }

    @Override
    public void updateLampLight() {
        //draw the lamp's light
        shader.setUniform("lightCount", lamps.size());
        if (lamps.size() > 0) shader.setUniform("lampIntensity", new Vector3f(18.0f));
        else shader.setUniform("lampIntensity", new Vector3f(0.0f));

        for (int i = 0; i < lamps.size(); i++){
            Vector3f place = lamps.get(i).getPosition();
            shader.setUniform("lights["+i+"]", new Vector3f(place.x, place.y + lamps.get(i).getLightHeight(), place.z));
        }

    }

    private void drawTrees(){
        for (Tree t : trees) t.render();
    }

    private void drawLamps(){
        for (Lamp l : lamps) l.render();
    }


    private void drawBuildings(){

        Material m = new Material();
        //m.Kd = new Vector3f(0.5f, 0.5f, 0.2f);      //Yellow
        m.Kd = new Vector3f(0);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        gl.glActiveTexture(GL_TEXTURE1);
        gl.glBindTexture(GL_TEXTURE_2D, textureIDs[1]);

        shader.setUniform("ObjectToWorld", new Matrix4f());
        for(Building b : city)
            b.render();

        gl.glBindTexture(GL_TEXTURE_2D, 0); //unbind
    }

    private void drawGround(){
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

    private void drawStepPyramids(){
        for (StepPyramid pyr : stepPyramids){
            drawStepPyramid(pyr.getPosition(), pyr);
        }
    }

    private void drawStepPyramid(Vector3f position, StepPyramid step){
        Material m = new Material();
        m.Kd = new Vector3f(0.15f);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        Matrix4f mat = new Matrix4f();
        mat.translate(position.x, position.y + step.getBaseRectHeight()/2.0f, position.z);  //center is at the center of the bottommost rectangle.
        mat.rotateX((float)Math.toRadians(45));
        shader.setUniform("ObjectToWorld", mat);
        step.render(mat);
    }
    private void drawParticles(boolean pureDraw){
        player.drawLasers(0, pureDraw);
        for (int i = 0; i < enemies.size(); i++) enemies.get(i).drawLasers(i+1, pureDraw);

    }






}
