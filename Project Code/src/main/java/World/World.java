package World;

import Core.*;
import Shapes.*;
import Shapes.Rectangle;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static Core.GLListener.gl;
import static World.Emitter.ParticleType.SNOWFLAKE;
import static World.World.CameraMode.CHASE;
import static World.World.CameraMode.DEBUG_VIEW;
import static com.jogamp.opengl.GL.*;

/**
 * Created by (User name) on 8/14/2017.
 */
public class World implements InputNotifiee {

    private Vector3f initialPlayerPosition;
    private Player player;
    private Camera cam1, cam2, cam3;
    private float zoom_factor = 1;
    protected CameraMode mode = CHASE;   //default is CHASE
    private Vector3f sunlightDirection;
    private ArrayList<Vector3f> treeLocation;
    private ArrayList<Building> city;
    private Rectangle ground;
    private JsonObject jsonObject;
    private Vector3f fixedPos, DEBUG_VIEWPos;
    private ArrayList<Cylinder> forestTrunks;
    private ArrayList<Cone> forestTops;
    private ArrayList<Vector4f> lampData;  //(x,y,z,height)
    private ArrayList<Cylinder> lampPosts;
    private ArrayList<IcoSphere> lampBulbs;
    private ArrayList<Vector3f> stepPyramidPosition;
    private ArrayList<StepPyramid> stepPyramids;
    private Texture theTexture;
    private int textureID; //TODO: remove this?
    private Emitter emitter;
    private BillBoardTriangle bill;

    private Cube c;

    protected enum CameraMode{
        CHASE, DEBUG_VIEW
    }

    public World(final String filename){
        //subscribe to KeyPresses events!
        UserInputConfig.addKeyPressListener(this);

        //open the json file.
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filename);
        }
        catch (FileNotFoundException e){
            System.err.println("File cannot be read or does not exist.");
            System.exit(-1);
        }

        c = new Cube(3.0f);

        Gson gson = new Gson();
        JsonParser parse = new JsonParser();
        JsonReader reader = gson.newJsonReader(fileReader);
        jsonObject = (JsonObject) parse.parse(reader);

        player = new Player();
        
    }

    public void init(Shader shader){
        //bounding box coordinates, forms two opposite vertices.
        JsonArray bbox = jsonObject.get("bbox").getAsJsonArray();
        Vector3f min = new Vector3f(bbox.get(0).getAsFloat(), bbox.get(1).getAsFloat(), bbox.get(2).getAsFloat());
        Vector3f max = new Vector3f(bbox.get(3).getAsFloat(), bbox.get(4).getAsFloat(), bbox.get(5).getAsFloat());

        //fixed camera position
        JsonArray fixed = jsonObject.get("photoPosition").getAsJsonArray();
        fixedPos = new Vector3f(fixed.get(0).getAsFloat(), fixed.get(1).getAsFloat(), fixed.get(2).getAsFloat());

        //DEBUG_VIEW camera position
        JsonArray observe = jsonObject.get("observerPosition").getAsJsonArray();
        DEBUG_VIEWPos = new Vector3f(observe.get(0).getAsFloat(), observe.get(1).getAsFloat(), observe.get(2).getAsFloat());

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
        }

        //Trees
        JsonArray woods = jsonObject.get("trees").getAsJsonArray();
        treeLocation = new ArrayList<>();
        forestTrunks = new ArrayList<>();
        forestTops = new ArrayList<>();
        for(int i = 0; i < woods.size(); i++){
            JsonObject tree = woods.get(i).getAsJsonObject();
            JsonArray treePosition = tree.get("position").getAsJsonArray();
            treeLocation.add(new Vector3f(treePosition.get(0).getAsFloat(), treePosition.get(1).getAsFloat(), treePosition.get(2).getAsFloat()));
            float treeHeight = tree.get("height").getAsFloat();
            forestTrunks.add(new Cylinder(0.4f, treeHeight * 0.3f));
            forestTops.add(new Cone(1,treeHeight * 0.7f,20, false));
        }

        //Ground
        Vector3f p1 = new Vector3f(min.x, min.y, max.z);
        Vector3f p2 = new Vector3f(max.x, min.y, max.z);
        Vector3f p3 = new Vector3f(max.x, min.y, min.z);
        Vector3f p4 = min;
        ground = new Rectangle(p1, p2, p3, p4);

        //Player
        JsonArray startPosition = jsonObject.get("startPosition").getAsJsonArray();
        initialPlayerPosition = new Vector3f(startPosition.get(0).getAsFloat(), startPosition.get(1).getAsFloat(), startPosition.get(2).getAsFloat());

        //Lamps
        JsonArray lights = jsonObject.get("lamps").getAsJsonArray();
        lampData = new ArrayList<>();
        lampPosts = new ArrayList<>();
        lampBulbs = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {        //for each lamp
            JsonObject lampInfo = lights.get(i).getAsJsonObject();
            JsonArray lampPos = lampInfo.get("position").getAsJsonArray();  //get the position of a lamp
            float height = lampInfo.get("height").getAsFloat();
            lampData.add(new Vector4f(lampPos.get(0).getAsFloat(), lampPos.get(1).getAsFloat(), lampPos.get(2).getAsFloat(), height));
            lampPosts.add(new Cylinder(0.5f, height));
            lampBulbs.add(new IcoSphere(1.0f, 3));
        }

        //Weather
        JsonObject weatherObject = jsonObject.get("weather").getAsJsonObject();
        JsonArray sunDirection = weatherObject.get("sunLightDirection").getAsJsonArray();
        sunlightDirection = new Vector3f(sunDirection.get(0).getAsFloat(), sunDirection.get(1).getAsFloat(), sunDirection.get(2).getAsFloat());

        //Step Pyramids
        stepPyramidPosition = new ArrayList<>();
        stepPyramids = new ArrayList<>();
        JsonArray stepramids = jsonObject.get("steppyramids").getAsJsonArray();
        for (int i=0;i<stepramids.size();i++){
            JsonObject pyramidData = stepramids.get(i).getAsJsonObject();
            JsonArray positionData = pyramidData.get("position").getAsJsonArray();
            stepPyramidPosition.add(new Vector3f(positionData.get(0).getAsFloat(), positionData.get(1).getAsFloat(), positionData.get(2).getAsFloat()));
            stepPyramids.add(new StepPyramid(
                    pyramidData.get("baselength").getAsFloat(),
                    pyramidData.get("stepheight").getAsFloat(),
                    pyramidData.get("stepshrink").getAsFloat(),
                    pyramidData.get("mirrored").getAsBoolean()
            ));
        }

        //Textures
        //initTextures(shader);
        //loadTextures(shader, new int[1]);
        //initializeTexture();
        initTex();

        //init calls
        for (Building b : city) b.init();
        for (Cylinder cyl : forestTrunks) cyl.init();
        for (Cone cone : forestTops) cone.init();
        for (Cylinder cyl : lampPosts) cyl.init();
        for (IcoSphere sphere : lampBulbs) sphere.init();
        for (StepPyramid step : stepPyramids) step.init();
        ground.init();

        c.init();
        emitter = new Emitter(SNOWFLAKE, new Vector3f(0,10,0));
        bill = new BillBoardTriangle(3.0f);
        bill.init();

        //world has been initialized, now add dynamic things like players
        player.init();
        player.setPosition(initialPlayerPosition);
        Vector3f dir = new Vector3f(player.getDirection());
        dir.rotateAxis((float)Math.toRadians(70.0f), 0,1,0);
        player.setDirection(dir.normalize(dir));

        //Cameras
        cam1 = new Camera();
        cam2 = new Camera();
        cam3 = new Camera();
        cam3.orient(DEBUG_VIEWPos, new Vector3f(0,0,0), new Vector3f(0,1,0));

    } //end init()

    public void render(Shader shader){
        Matrix4f worldToEye = updateCamera(shader);
        updateLight(shader, worldToEye);

        shader.setUniform("ObjectToWorld", new Matrix4f());

        //draw the objects in the World:
        drawGround(shader);
        drawBuildings(shader);
        drawTrees(shader);
        drawLamps(shader);
        drawStepPyramids(shader);


        Material m = new Material();
        m.Kd = new Vector3f(0); //Textures need black.
        m.setUniforms(shader);
        gl.glActiveTexture(GL_TEXTURE0);
        gl.glBindTexture(GL_TEXTURE_2D, textureID);
        //theTexture.enable(gl);
        //theTexture.bind(gl);
        shader.setUniform("tex", 0);


        Matrix4f move = new Matrix4f().translate(0,5,20);
        shader.setUniform("ObjectToWorld",move);
        c.render();

        //theTexture.disable(gl);
        gl.glBindTexture(GL_TEXTURE_2D, 0); //unbind


        //TODO: change emitter Material. (maybe in the Emitter or particle class themself?
        emitter.update(shader, new Matrix4f());
        //Matrix4f location = new Matrix4f().translate(20,10,0);
        //bill.render(shader, location, player.getDirection());

        //finished rendering the world, now render the player
        player.render(shader);

        //check for user input
        UserInputConfig.checkTheInput();  //there is value in doing this, because this gets called once per frame

    }//end render

    private void drawTrees(Shader shader){
        for(int i=0; i < treeLocation.size(); i++)
            drawTree(shader, treeLocation.get(i), forestTrunks.get(i), forestTops.get(i));
    }

    private void drawTree(Shader shader, final Vector3f treeLocation, Cylinder trunk, Cone top){

        Matrix4f rotation = new Matrix4f();
        rotation.rotateX((float)Math.toRadians(-90), rotation);

        Matrix4f attachment = new Matrix4f();
        attachment.translate(new Vector3f(0,0,1), attachment);

        Matrix4f deploy = new Matrix4f();
        deploy.translate(treeLocation);
        Matrix4f forCylinders = new Matrix4f(deploy);
        Matrix4f forCones = new Matrix4f(deploy);

        Material m = new Material();
        //draw the tree trunk
        m.Kd = new Vector3f(0.38f,0.2f,0.07f);  //brown
        m.setUniforms(shader);

        Matrix4f temp = new Matrix4f();
        forCylinders.translate(0,trunk.getHeight()/2.0f,0);    //Cylinder origin (0, halfheight, 0)
        forCylinders.mul(rotation, temp);
        shader.setUniform("ObjectToWorld", temp);
        trunk.render();

        //draw the tree top

        m = new Material();
        m.Kd = new Vector3f(0.1f, 0.6f, 0.1f);  //green
        m.setUniforms(shader);

        forCones.translate(0, top.getHeight()/2.0f + trunk.getHeight() - 1,0);     //Cone origin is at (0,halfheight,0) //no clue the -1.
        forCones.mul(rotation.mul(attachment), temp);
        shader.setUniform("ObjectToWorld", temp);
        top.render();
    }

    private void drawLamps(Shader shader){
        for (int i = 0; i < lampData.size(); i++){
            drawLamp(shader, lampData.get(i), lampPosts.get(i), lampBulbs.get(i));
        }
    }

    private void drawLamp(Shader shader, final Vector4f lampData, Cylinder post, IcoSphere bulb){
        Matrix4f rotation = new Matrix4f();
        rotation.rotateX((float)Math.toRadians(-90), rotation);

        Matrix4f attachment = new Matrix4f();
        attachment.translate(new Vector3f(0,0,1), attachment);

        Matrix4f deploy = new Matrix4f();
        deploy.translate(lampData.x, lampData.y, lampData.z);
        Matrix4f forCylinders = new Matrix4f(deploy);
        Matrix4f forSpheres = new Matrix4f(deploy);

        Material m = new Material();
        //draw the lamp post
        m.Kd = new Vector3f(0.0f);      //black
        m.setUniforms(shader);

        Matrix4f temp = new Matrix4f();
        forCylinders.translate(0, post.getHeight()/2.0f, 0);    //Cylinder origin (0, halfheight, 0)
        forCylinders.mul(rotation, temp);
        shader.setUniform("ObjectToWorld", temp);
        post.render();

        //draw the lamp bulb
        m.Ka = new Vector3f(1.0f);
        m.Kd = new Vector3f(1.0f);
        m.Ks = new Vector3f(1.0f);
        m.Le = new Vector3f(1.0f);
        m.shine = 1.0f;
        m.setUniforms(shader);
        forSpheres.translate(0, post.getHeight(), 0, temp);   // y + bulb.radius?
        shader.setUniform("ObjectToWorld", temp);
        bulb.render();
    }

    private void drawBuildings(Shader shader){
        Material m = new Material();
        m.Kd = new Vector3f(0.5f, 0.5f, 0.2f);      //Yellow
        m.setUniforms(shader);

        for(Building b : city)
            b.render();
    }

    private void drawGround(Shader shader){
        Material m = new Material();
        m.Kd = new Vector3f(0,1,0);     //green
        m.setUniforms(shader);

        shader.setUniform("ObjectToWorld", new Matrix4f());
        ground.render();
    }

    private void drawStepPyramids(Shader shader){
        for(int i = 0; i < stepPyramids.size(); i++){
            drawStepPyramid(shader, stepPyramidPosition.get(i), stepPyramids.get(i));
        }
    }
    private void drawStepPyramid(Shader shader, Vector3f position, StepPyramid step){
        Material m = new Material();
        m.Kd = new Vector3f(1,1,0.37f); //beige
        m.setUniforms(shader);

        Matrix4f mat = new Matrix4f();
        mat.translate(position.x, position.y + step.getBaseRectHeight()/2.0f, position.z);  //center is at the center of the bottommost rectangle.
        shader.setUniform("ObjectToWorld", mat);
        step.render(shader, mat);
    }

    private Matrix4f updateCamera(Shader shader){
        //temporary allocations for use below.
        Matrix4f worldToEye = new Matrix4f();
        Vector3f g = new Vector3f();    //placeholder/garbage; prevents data overwrite. because @*$% joml. //TODO: remove this expletive.
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
            cam1.orient(arg1, arg2, new Vector3f(1,0,0));                                                                    //__,__,cam Up vector

            //set the looking at matrix.
            player.getPosition().add(player.getDirection().mul(10.0f, g), arg2);
            worldToEye.lookAt(cam1.getPosition(), arg2, new Vector3f(0,1,0));

            //set the shaders
            shader.setUniform("WorldToEye", worldToEye);
            shader.setUniform("Projection", cam1.getProjectionMatrix());
        }
//        else if(mode == PHOTO){
//            cam2.orient(fixedPos, player.getPosition(), new Vector3f(0,1,0));
//            worldToEye.lookAt(fixedPos, player.getPosition(), new Vector3f(0,1,0));
//            shader.setUniform("WorldToEye", worldToEye);
//            shader.setUniform("Projection", cam2.getProjectionMatrix());
//        }
        else if(mode == DEBUG_VIEW){
            worldToEye = cam3.getViewMatrix();
            shader.setUniform("WorldToEye", worldToEye);
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
        shader.setUniform("ambientIntensity", new Vector3f(1.0f));  //formerly 0.0f

        for (int i = 0; i < lampData.size(); i++) {
            Vector4f lampPosition = new Vector4f(lampData.get(i).x, lampData.get(i).y, lampData.get(i).z, 1.0f);
            float lampHeight = lampData.get(i).w;
            lampPosition.mul(worldToEye);    //originally worldToEye * lampPosition
            shader.setUniform("lights["+i+"]",new Vector3f(lampPosition.x, lampPosition.y + lampHeight, lampPosition.z));
        }

        //sunlight
        Vector4f sunlightDir = new Vector4f(sunlightDirection, 0.0f);    //0.0f, otherwise no sunlight.
        sunlightDir.mul(worldToEye);
        shader.setUniform("sunDirection", new Vector3f(sunlightDir.x, sunlightDir.y, sunlightDir.z));
        shader.setUniform("sunIntensity", new Vector3f(test));  //1.0f
        /////
        if (sunbool) test += 0.01f;
        else test -= 0.01f;
        if (test >= 1.0f){ sunbool = !sunbool; test = 0.99f;}
        else if (test <= 0.0f) { sunbool = !sunbool; test = 0.01f; }
        /////
    }

    private void initTextures(Shader shader){
        IntBuffer texturebuf = GLBuffers.newDirectIntBuffer(1);
        //null texture
        int[] nulltexture = new int[4];
        Vector3f color = new Vector3f(0.0f);
        nulltexture[0] = (byte)color.x;
        nulltexture[1] = (byte)color.y;
        nulltexture[2] = (byte)color.z;
        nulltexture[3] = (byte)255;

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glGenTextures(1,texturebuf);
        gl.glBindTexture(GL_TEXTURE_2D, texturebuf.get(0));

        theTexture = null;
        String filepath = "Project Code/src/main/resources/textures/facade1.jpg";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try{ImageIO.write(ImageIO.read(new File(filepath)), "png", outputStream);}
        catch(IOException e){System.err.println("there's been an exception!");}
        InputStream fis = new ByteArrayInputStream(outputStream.toByteArray());
        try{theTexture = TextureIO.newTexture(fis, true, TextureIO.JPG);}
        catch(IOException e){System.err.println("there's been an exception! A different one!");}
        //TODO: clean this mess up.
        ByteBuffer pixelData = GLBuffers.newDirectByteBuffer(16384);        //TODO: Was hardcoded.
        BufferedImage bufImage = null;
        try{bufImage = ImageIO.read(new File(filepath));}
        catch(Throwable threat){threat.printStackTrace();}
        byte[] barray = getPixels(bufImage, 512, 512);    //TODO: Hardcoded
        pixelData = ByteBuffer.wrap(barray);

/*
        System.out.println("height: "+theTexture.getHeight());
        System.out.println("width: "+theTexture.getWidth());
        System.out.println("Iheight: "+theTexture.getImageHeight());
        System.out.println("Iwidth: "+theTexture.getImageWidth());
        System.out.println("TexCoords: "+theTexture.getImageTexCoords());
        System.out.println("Texture: "+theTexture.toString());
        System.out.println("Bytes: "+theTexture.getEstimatedMemorySize());
*/
        System.err.println("before: "+Utilities.iBufToString(texturebuf));
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glGenTextures(1, texturebuf);
        System.err.println("genTexs: "+Utilities.iBufToString(texturebuf));
        gl.glBindTexture(GL_TEXTURE_2D, texturebuf.get(0));
        System.err.println("bindTex: "+Utilities.iBufToString(texturebuf));
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, theTexture.getImageWidth(), theTexture.getImageHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelData);     //8 * theTexture.getEstimatedMemorySize()
        System.err.println("teximage2d: "+Utilities.iBufToString(texturebuf));
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        System.err.println("texparam1: "+Utilities.iBufToString(texturebuf));
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        System.err.println("texparam2: "+Utilities.iBufToString(texturebuf));

        textureID = texturebuf.get(0);
        
        shader.setUniform("tex", 0);

    }
    private void loadTextures(Shader shader, int[] textures){
        textures = new int[2];
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glGenTextures(1, textures, 1);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[1]);
        try {
            BufferedImage image = ImageIO.read(new File("Project Code/src/main/resources/textures/facade1.jpg"));
            //DataBufferByte dbb = (DataBufferByte)image.getRaster().getDataBuffer();
            //byte[] data = dbb.getData();
            byte[] dataRGBA = getPixels(image, image.getWidth(), image.getHeight());
            ByteBuffer pixels = GLBuffers.newDirectByteBuffer(dataRGBA.length);
            pixels.put(dataRGBA);
            pixels.flip();
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 64, 64, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixels);
            /////
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            /////
        } catch(Throwable t) {
            t.printStackTrace();
        }
        System.out.println("building texture ID: "+textures[1]);
        //null texture

        int[] nulltexture = new int[4];
        Vector3f color = new Vector3f(0.0f);
        nulltexture[0] = (byte)color.x;
        nulltexture[1] = (byte)color.y;
        nulltexture[2] = (byte)color.z;
        nulltexture[3] = (byte)255;

        gl.glActiveTexture(GL_TEXTURE0);
        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL_TEXTURE_2D, textures[0]);
        System.out.println("null texture ID: "+textures[0]);

        shader.setUniform("tex", 0);
    }
    private void initializeTexture(){
        String filepath = "Project Code/src/main/resources/textures/facade1.jpg";
        //gl.glEnable(GL_TEXTURE_2D); //commented because use of shaders.
        gl.glActiveTexture(GL.GL_TEXTURE0);
        try{
            File im = new File(filepath);       //possible conversion causing white box?
            Texture t = TextureIO.newTexture(im, false);
            textureID = t.getTextureObject(gl);
            System.out.println(textureID);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }
        catch (Throwable trap){
            trap.printStackTrace();
        }
        
    }

    private void initTex(){
        try {
            //read texture picture file
            InputStream texStream = getClass().getResourceAsStream("/textures/facade2.jpg");
            TextureData textureData = TextureIO.newTextureData(gl.getGLProfile(), texStream, false, TextureIO.JPG);
            //create and load buffers
            int[] texNames = new int[1];
            gl.glGenTextures(1, texNames, 0);
            textureID = texNames[0];
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, textureID);
            //texturing options
            gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_BASE_LEVEL, 0);
            gl.glTexParameteri(GL_TEXTURE_2D, GL4.GL_TEXTURE_MAX_LEVEL, 0);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            gl.glTexStorage2D(GL_TEXTURE_2D, 1, textureData.getInternalFormat(), textureData.getWidth(), textureData.getHeight());
            gl.glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,textureData.getWidth(), textureData.getHeight(), textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());

        }
        catch (IOException e){System.err.println("Failed to load texture."); e.printStackTrace();}
    }

    private byte[] getPixels(BufferedImage image, int width, int height) {
        byte[] result = new byte[height * width * 4];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y), true);
                result[x + y * width + 0] = (byte)c.getRed();
                result[x + y * width + 1] = (byte)c.getGreen();
                result[x + y * width + 2] = (byte)c.getBlue();
                result[x + y * width + 3] = (byte)c.getAlpha();
            }
        }
        return result;
    }

    //InputNotifiee interface
    final float MOVE_SPEED = 0.5f;
    final float TURN_SPEED = 3.0f;

    @Override
    public void move_forward() {
        if (mode == DEBUG_VIEW)
            cam3.slide(cam3.getN().negate());
        else if(mode == CHASE)
            player.move(MOVE_SPEED);
    }

    @Override
    public void move_backward() {
        if (mode == DEBUG_VIEW)
            cam3.slide(cam3.getN());
        else if(mode == CHASE)
            player.move(-MOVE_SPEED);
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
            cam3.slide(cam3.getU().negate());
        else if(mode == CHASE)
            player.turn(TURN_SPEED);
    }

    @Override
    public void turn_right() {
        if (mode == DEBUG_VIEW)
            cam3.slide(cam3.getU());
        else if(mode == CHASE)
            player.turn(-TURN_SPEED);
    }

    @Override
    public void jump() {
        System.err.println("Jumping is not implemented yet.");
    }

    @Override
    public void switch_mode(int cameraMode){
        switch (cameraMode){
            case 1: mode = CHASE; break;
            case 2: mode = DEBUG_VIEW; break;
            default: System.err.println("Unrecognized camera mode: " + cameraMode);
        }
    }

    @Override
    public void zoom(float amount){
        zoom_factor = amount;
    }
    //End of InputNotifiee Interface
}
