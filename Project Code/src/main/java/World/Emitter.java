package World;

import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import Core.Shader;
import World.WorldObjects.Particles.Laser;
import World.WorldObjects.Particles.Particle;
import World.WorldObjects.Particles.Sand;
import World.WorldObjects.Particles.Snowflake;
import World.Worlds.AbstractWorld;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static World.Emitter.ParticleType.LASER;
import static World.Emitter.ParticleType.SAND;
import static World.Emitter.ParticleType.SNOWFLAKE;

public class Emitter {
    ArrayList<Particle> particles;
    Vector3f location;
    Vector3f direction;
    ParticleType type;
    int iD;
    int density;
    int maxCount;
    private Shader shader;

    public enum ParticleType{
        SNOWFLAKE, SAND, LASER
    }

    public Emitter(ParticleType type, Vector3f loc, Vector3f dir, int particleDensity){
        this.type = type;
        location = loc;
        direction = dir;
        density = particleDensity;
        particles = new ArrayList<>();
        shader = Shader.getInstance();

        //TODO: Figure out a way to compute these values. No guessing and checking.
        if (this.type == LASER) maxCount = 10;
        else if (this.type == SNOWFLAKE) maxCount = 1200;
        else if (this.type == SAND) maxCount = 1600;
    }

    //Laser
    public void addLaserParticle(int iD){
        Particle p  = new Laser(location, direction, iD);

        particles.add(p);
        AbstractWorld.addWorldObject(p);

        //add a collision box
        p.generateCollisionBox();
    }

    //Weather
    public void addWeatherParticle(){
        Particle p = null;
        if (type == ParticleType.SNOWFLAKE) p = new Snowflake(location);
        else if (type == ParticleType.SAND) p = new Sand(location);
        else System.err.println("Unsupported Weather Particle Type");

        if (particles.size() < maxCount) particles.add(p);
    }

    //for weather effects
    public void update(Vector3f cameraPosition, boolean pureDraw){
        setMaterial(type);
        if (!pureDraw)for (int i = 0; i < density; i++) addWeatherParticle();

        for (int i = particles.size()-1; i >=0; i--){
            Particle p = particles.get(i);
            if (p instanceof Snowflake) ((Snowflake) p).run(new Matrix4f(), cameraPosition, pureDraw);
            else if (p instanceof Sand) ((Sand) p).run(new Matrix4f(), cameraPosition, pureDraw);
            else System.err.println("Unsupported ParticleType.");

            //if (p.isDead()) particles.remove(i);  //EDIT: instead of tons of object recreation, now repurposes the dead ones.
            if (p.isDead()){
                p.renew(this.location);
            }
        }
    }

    //for lasers
    public void update(Vector3f location, int iD, boolean pureDraw){
        setMaterial(type);
        setLocation(location);

        if (particles.isEmpty()) for(int i=0;i<10;i++) shader.setUniform("laserPositions["+iD+"]["+i+"]", new Vector3f());

        int counter = 0;
        shader.setUniform("numLiveLasers["+iD+"]", particles.size());
        for (int i = particles.size()-1; i >= 0; i--){
            Particle p = particles.get(i);
            shader.setUniform("laserPositions["+iD+"][" + counter + "]", p.getPosition());
            counter++;

            p.run(pureDraw);

            if (p.isDead()) {
                CollisionDetectionSystem.getInstance().removeCollisionBox(p);
                AbstractWorld.removeWorldObject(p);
                particles.remove(i);
            }
        }

    }

    public void setLocation(Vector3f newLocation){
        location = new Vector3f(newLocation);
    }

    public void setDirection(Vector3f newDirection){
        direction = new Vector3f(newDirection);
    }

    private void setMaterial(ParticleType type){
        Material m = new Material();
        switch (type){
            case SNOWFLAKE: m.Kd = new Vector3f(2,2,2); break;
            case SAND: m.Kd = new Vector3f(0.8f,0.4f,0.0f); break;
            case LASER:
                m.Kd = new Vector3f(1,1,1);
                m.Ks = new Vector3f(0,0,2);
                m.Le = new Vector3f(0,0,1);
                break;
            default: System.err.println("Unregistered Emitter ParticleType: " + type);
        }
        m.setUniforms(shader);
    }

}
