package World;

import Core.GLListener;
import Core.InputSystem.InputHandler;
import Core.Shader;
import World.AbstractShapes.Triangle;
import World.WorldObjects.HumanModel;
import World.Worlds.AbstractWorld;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;

import static World.Emitter.ParticleType.LASER;

/**
 * Created by (User name) on 8/14/2017.
 */
public class Player extends Character {

    private float length = 1.0f;
    private float width = 1.0f;
    private float height = 3.5f;
    private Color color = null;
    private Emitter laserEmitter;
    private Shader shader;
    private int playerNum;
    private static float DEFAULT_VALUE = -5;
    private static float flashCountDown = DEFAULT_VALUE;
    private boolean isAlive = true;

    private Vector3f groundAdjustment = new Vector3f(0,height/2.0f - 0.25f,0);


    public Player(Color c){
        this.position = new Vector3f(0,0,0);
        this.direction = new Vector3f(0,0,1);
        this.model = new HumanModel(height);
        this.isAffectedByGravity = true;
        this.color = c;
        this.laserEmitter = new Emitter(LASER, new Vector3f(0,4,0), this.direction, 1);
        this.shader = Shader.getInstance();
    }

    public void assignPlayerNum(int number){
        this.playerNum = number;
    }
    public int getPlayerNum(){
        return playerNum;
    }

    @Override
    public void init(){
        model.init();
    }

    @Override
    public void render() {
        System.err.println("Need to call Player.render(boolean) instead.");
        System.exit(-1);
    }

    //@Override //bummer.
    public void render(boolean pureDraw){
        this.setDirection(this.getDirection().normalize()); //this line of code belongs elsewhere, but it MUST exist. otherwise direction -> 0,0,0.
        Matrix4f m = new Matrix4f().translate(position).mul(Utilities.Utilities.changeDirection(this, getDirection()));
        m.translate(groundAdjustment);   //second arg to move the player onto the ground, instead of through it.
        //set shaders
        shader.setUniform("ObjectToWorld", m);

        //shader.setUniform("color", new Vector4f(1,0,0,1));      //red
        //define the material's light properties
        Material material = new Material();
        material.Kd = new Vector3f(color.getRed()/255.0f,color.getGreen()/255.0f,color.getBlue()/255.0f);
        //material.Ks = new Vector3f(1,1,1);    //humans aren't shiny!
        material.setUniforms(shader);

        if (isAlive) model.render(m);


        if (flashCountDown != DEFAULT_VALUE) shader.setUniform("flashIntensity", flashCountDown);
        if (!pureDraw) {
            if (flashCountDown > -0.1f) flashCountDown -= 0.005f;
            else shader.setUniform("flashColor", new Vector3f(0, 1, 0));
        }
    }

    //Method so shadows aren't drawn, and other bugs (like no lights)
    public void drawLasers(int iD, boolean pureDraw){
        //Emitter stuff
        laserEmitter.setDirection(this.direction);
        shader.setUniform("laserIntensity["+iD+"]", new Vector3f(color.getRed(),color.getGreen(), color.getBlue()).normalize().mul(10));  //normalized because color 0-255
        laserEmitter.update(this.getPosition().add(new Vector3f(0,2.5f,0.0f), new Vector3f()),iD, pureDraw);
    }

    @Override
    public Triangle[] getTriangles() {
        return model.getTriangles();
    }


    long timeCheck = 0;
    Vector3f vel;
    public void shootLaser(){
        boolean debug = false;
        if (!debug) {
            long timeNow = System.currentTimeMillis();
            if (timeNow > timeCheck + 500) {     //if 500ms have passed since last laser was fired...
                laserEmitter.addLaserParticle(playerNum);
                timeCheck = timeNow;
            }
        }
        else {  //debugging
            if (laserEmitter.particles.size() < 1) timeCheck = 0;
            if (timeCheck == 0) {
                laserEmitter.addLaserParticle(playerNum);
                //This stuff below might want to be moved to Player, since thats where the Emitter now resides.
                vel = laserEmitter.particles.get(0).getVelocity();
                laserEmitter.particles.get(0).setVelocity(new Vector3f(0));
                timeCheck++;
            }
            laserEmitter.particles.get(0).setLifespan(200);
            laserEmitter.particles.get(0).moveDistance(vel.normalize());
        }
    }

    public void kill(){
        if (isAlive)flashCountDown = 1;
        shader.setUniform("flashLocation", this.getPosition());
        shader.setUniform("flashColor", new Vector3f(color.getRed(), color.getGreen(), color.getBlue()));
        if (playerNum != 0) AbstractWorld.spawnPlayer(this);    //infinite respawns
        else if (isAlive){
            isAlive = false;
            AbstractWorld.slowAnimation(36);
        }
    }

    public void fallDeath(){
        AbstractWorld.spawnPlayer(this);
    }

    public float getLength(){
        return model.getLength();
    }
    public float getWidth() {return model.getWidth();}
    public float getHeight(){return model.getHeight();}
    public Vector3f getGroundAdjustment() {
        return new Vector3f(groundAdjustment);
    }

}
