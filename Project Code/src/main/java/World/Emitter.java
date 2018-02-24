package World;

import Core.Shader;
import World.Particles.Laser;
import World.Particles.Particle;
import World.Particles.Snowflake;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import Utilities.Utilities;

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

    public void addParticle(){
        if (particles.size() > 1342) return;
        if (type == SNOWFLAKE) particles.add(new Snowflake(location));
        else if (type == LASER) particles.add(new Laser(location, direction));
        else System.err.println("This Particle has not been implemented in Emitter.addParticle(): "+type);
    }

    public void run(Shader shader, Vector3f location, Vector3f cameraPosition){

        for (int i = 0; i < density; i++) addParticle();

        update(shader, location, cameraPosition);
    }

    public void update(Shader shader, Vector3f location, Vector3f cameraPosition){
        setMaterial(shader, type);
        setLocation(location);

        if (particles.isEmpty()) for(int i=0;i<10;i++) shader.setUniform("laserPositions["+i+"]", new Vector3f());

        int counter = 0;
        shader.setUniform("numLiveLasers", particles.size());
        for (int i = particles.size()-1; i >= 0; i--){
            Particle p = particles.get(i);
            //TODO: These if statements are yuck. There should be a more elegant solution?
            if (p instanceof Laser) {
                shader.setUniform("laserPositions[" + counter + "]", p.getLocation());
                counter++;
            }

            if (p instanceof Snowflake) ((Snowflake) p).run(shader, new Matrix4f(), cameraPosition);
            else p.run(shader);

            if (p.isDead()) particles.remove(i);
        }

    }

    public void setLocation(Vector3f newLocation){
        location = newLocation;
    }

    public void setDirection(Vector3f newDirection){
        direction = newDirection;
    }

    private void setMaterial(Shader shader, ParticleType type){
        Material m = new Material();
        switch (type){
            case SNOWFLAKE: m.Kd = new Vector3f(2,2,2); break;
            case LASER:
                m.Kd = new Vector3f(0,0,2);
                m.Ks = new Vector3f(0,0,2);
                m.Le = new Vector3f(0,0,1);
                break;
            default: System.err.println("Unregistered Emitter ParticleType: " + type);
        }
        m.setUniforms(shader);
    }

}
