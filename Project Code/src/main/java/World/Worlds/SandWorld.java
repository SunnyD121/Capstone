package World.Worlds;

import Core.Camera;
import World.*;
import World.WorldObjects.Cactus;
import World.WorldObjects.Ground;
import World.WorldObjects.Obelisk;
import World.WorldObjects.StepPyramid;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;

public class SandWorld extends AbstractWorld {

    private ArrayList<StepPyramid> stepPyramids;
    private ArrayList<Obelisk> obelisks;
    private ArrayList<Cactus> cacti;
    private Emitter sandEmitter;

    public SandWorld(final String filename){
        super(filename);
        stepPyramids = new ArrayList<>();
        obelisks = new ArrayList<>();
        cacti = new ArrayList<>();
        sandEmitter = new Emitter(Emitter.ParticleType.SAND, new Vector3f(0,5,125), new Vector3f(0,0,-1), 10);
    }


    @Override
    public void init(){
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

        //Steppyramids
        JsonArray stepramids = jsonObject.get("steppyramids").getAsJsonArray();
        for (int i=0;i<stepramids.size();i++) {
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

        //Obelisks
        JsonArray array = jsonObject.get("obelisks").getAsJsonArray();
        for (int i=0; i<array.size();i++) {
            JsonObject jsonObelisk = array.get(i).getAsJsonObject();
            JsonArray positionData = jsonObelisk.get("position").getAsJsonArray();
            obelisks.add(new Obelisk(
                    new Vector3f(positionData.get(0).getAsFloat(), positionData.get(1).getAsFloat(), positionData.get(2).getAsFloat()),
                    jsonObelisk.get("baselength").getAsFloat(),
                    jsonObelisk.get("height").getAsFloat()
            ));
            addWorldObject(obelisks.get(i));
        }

        //Cacti
        array = jsonObject.get("cacti").getAsJsonArray();
        for (int i = 0; i < array.size(); i++){
            JsonObject jsonCactus = array.get(i).getAsJsonObject();
            JsonArray positionData = jsonCactus.get("position").getAsJsonArray();
            cacti.add(new Cactus(
                    new Vector3f(positionData.get(0).getAsFloat(), positionData.get(1).getAsFloat(), positionData.get(2).getAsFloat()),
                    jsonCactus.get("size").getAsInt()
            ));
            addWorldObject(cacti.get(i));
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
        //TODO

        //init calls
        for (StepPyramid pyr : stepPyramids) pyr.init();
        ground.init();
        player.init();
        for (Enemy enemy : enemies) enemy.init();
        for (Obelisk obe : obelisks) obe.init();
        for (Cactus c : cacti) c.init();

        //other
        player.setPosition(initialPlayerPosition);
        Vector3f dir = new Vector3f(player.getDirection());
        dir.rotateAxis((float)Math.toRadians(70.0f), 0,1,0);
        initialPlayerDirection = new Vector3f(dir.normalize());
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
        for (StepPyramid pyr : stepPyramids) CDS.addCollisionBox(pyr, pyr.getPosition(), pyr.getLength(), pyr.getHeight(), pyr.getWidth(), pyr.getPositionalOffset());
        for (Obelisk obe : obelisks) CDS.addCollisionBox(obe, obe.getPosition(), obe.getLength(), obe.getHeight(), obe.getWidth(), obe.getPositionalOffset());
        for (Cactus c : cacti) CDS.addCollisionBox(c, c.getPosition(), c.getLength(), c.getHeight(), c.getWidth(), c.getPositionalOffset());

        CDS.postInit();
    }


    @Override
    public void drawShadowedObjects(boolean pureDraw) {
        player.render(pureDraw);
        for (Enemy enemy : enemies) enemy.render(pureDraw);
        drawStepPyramids();
        drawObelisks();
        drawCacti();

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
        m.Kd = new Vector3f(1,1,0.37f);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        shader.setUniform("ObjectToWorld", new Matrix4f());
        ground.render();
    }

    private void drawStepPyramids(){
        for (StepPyramid pyr : stepPyramids){
            Material m = new Material();
            m.Kd = new Vector3f(0.8f, 0.4f, 0.0f);
            m.Ka = m.Kd;
            m.setUniforms(shader);

            Matrix4f mat = new Matrix4f();
            mat.translate(pyr.getPosition().x, pyr.getPosition().y + pyr.getBaseRectHeight()/2.0f, pyr.getPosition().z);
            shader.setUniform("ObjectToWorld", mat);
            pyr.render(mat);
        }
    }

    private void drawObelisks(){
        for (Obelisk obelisk : obelisks){
            Material m = new Material();
            m.Kd = new Vector3f(0.15f);
            m.Ka = m.Kd;
            m.setUniforms(shader);
            obelisk.render();
        }
    }

    private void drawCacti(){
        for (Cactus c : cacti){
            Material m = new Material();
            m.Kd = new Vector3f(0,1,0);
            m.Ka = m.Kd;
            m.setUniforms(shader);
            c.render();
        }
    }

    private void drawParticles(boolean pureDraw){
        player.drawLasers(0, pureDraw);
        for (int i=0;i<enemies.size();i++) enemies.get(i).drawLasers(i+1, pureDraw);

        Vector3f camPos;
        if (mode == CameraMode.CHASE) camPos = cam1.getPosition();
        else if (mode == CameraMode.DEBUG_VIEW) camPos = cam2.getPosition();
        else camPos = cam3.getPosition();
        sandEmitter.update(camPos, pureDraw);

    }
}
