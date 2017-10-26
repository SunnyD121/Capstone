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
    public enum ParticleType{
        SNOWFLAKE
    }

    public Emitter(ParticleType type, Vector3f loc){
        this.type = type;
        location = loc;
        particles = new ArrayList<>();
    }

    public void addParticle(){
        if (type == SNOWFLAKE)
            particles.add(new Snowflake(location));
        else {
            System.err.println("This Particle has not been implemented in Emitter.addParticle(): "+type);
        }
    }

    public void update(Shader shader, Matrix4f O2W){
        addParticle();
        for (int i = particles.size()-1; i >= 0; i--){
            Particle p = particles.get(i);
            p.run(shader, new Matrix4f());
            //System.out.println(i + ", Location: " + p.location);
            //System.out.println(i + ", Velocity: " + p.velocity);
            //System.out.println(i + ", Acceleration:" + p.acceleration);
            if (p.isDead()) {
                particles.remove(i);
            }
        }
    }

    public void setLocation(Vector3f newLocation){
        location = newLocation;
    }
}
