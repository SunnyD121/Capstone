package World;

import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import Core.Shader;
import World.WorldObjects.Particles.Laser;
import World.WorldObjects.Particles.Particle;
import World.WorldObjects.Particles.Snowflake;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static World.Emitter.ParticleType.LASER;
import static World.Emitter.ParticleType.SNOWFLAKE;

public class Emitter {
    ArrayList<Particle> particles;
    Vector3f location;
    Vector3f direction;
    ParticleType type;
    int density;

    public enum ParticleType{
        SNOWFLAKE, LASER
    }

    public Emitter(ParticleType type, Vector3f loc, Vector3f dir, int particleDensity){
        this.type = type;
        location = loc;
        direction = dir;
        density = particleDensity;
        particles = new ArrayList<>();
    }

    public void addParticle(int iD){
        Particle p = null;
        if (type == SNOWFLAKE) p = new Snowflake(location);
        else if (type == LASER) p = new Laser(location, direction, iD);
        else System.err.println("This Particle has not been implemented in Emitter.addParticle(): "+type);

        particles.add(p);
        World.addWorldObject(p);

        //add a collision box
        p.generateCollisionBox();
    }

    public void run(Shader shader, Vector3f location, Vector3f cameraPosition){

        for (int i = 0; i < density; i++) addParticle(-1);

        update(shader, location, -1);
    }

    public void update(Shader shader, Vector3f location, int iD){
        setMaterial(shader, type);
        setLocation(location);

        if (particles.isEmpty()) for(int i=0;i<10;i++) shader.setUniform("laserPositions["+iD+"]["+i+"]", new Vector3f());

        int counter = 0;
        shader.setUniform("numLiveLasers["+iD+"]", particles.size());
        for (int i = particles.size()-1; i >= 0; i--){
            Particle p = particles.get(i);
            //TODO: These if statements are yuck. There should be a more elegant solution?
            if (p instanceof Laser) {
                shader.setUniform("laserPositions["+iD+"][" + counter + "]", p.getPosition());
                counter++;
            }

            if (p instanceof Snowflake) ((Snowflake) p).run(shader, new Matrix4f(), null);
            else p.run(shader);

            if (p.isDead()) {
                CollisionDetectionSystem.getInstance().removeCollisionBox(p);
                World.removeWorldObject(p);
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

    private void setMaterial(Shader shader, ParticleType type){
        Material m = new Material();
        switch (type){
            case SNOWFLAKE: m.Kd = new Vector3f(2,2,2); break;
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
