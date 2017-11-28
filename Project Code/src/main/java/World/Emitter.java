package World;

import Core.Shader;
import World.Particles.Particle;
import World.Particles.Snowflake;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static World.Emitter.ParticleType.SNOWFLAKE;

public class Emitter {
    ArrayList<Particle> particles;
    Vector3f location;
    ParticleType type;
    int density;

    public enum ParticleType{
        SNOWFLAKE
    }

    public Emitter(ParticleType type, Vector3f loc, int particleDensity){
        this.type = type;
        location = loc;
        density = particleDensity;
        particles = new ArrayList<>();
    }

    public void addParticle(){
        if (type == SNOWFLAKE) particles.add(new Snowflake(location));
        else System.err.println("This Particle has not been implemented in Emitter.addParticle(): "+type);
    }

    public void update(Shader shader, Matrix4f O2W, Vector3f cameraPosition){
        setMaterial(shader, type);

        for (int i = 0; i < density; i++) addParticle();

        for (int i = particles.size()-1; i >= 0; i--){
            Particle p = particles.get(i);
            //TODO: This if statement is yuck. There should be a more elegant solution?
            if (p instanceof Snowflake) ((Snowflake) p).run(shader, new Matrix4f(), cameraPosition);
            else p.run(shader, new Matrix4f());

            if (p.isDead()) particles.remove(i);
        }
    }

    public void setLocation(Vector3f newLocation){
        location = newLocation;
    }

    private void setMaterial(Shader shader, ParticleType type){
        Material m = new Material();
        switch (type){
            case SNOWFLAKE: m.Kd = new Vector3f(2,2,2); break;
            default: System.err.println("Unregistered Emitter ParticleType: " + type);
        }
        m.setUniforms(shader);
    }

}
