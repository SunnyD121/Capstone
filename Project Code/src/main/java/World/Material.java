package World;

import Core.Shader;
import org.joml.Vector3f;

/**
 * Created by (User name) on 8/26/2017.
 */
public class Material {
    public Vector3f Le;     //Emission
    public Vector3f Ka;     //Ambient Reflectivity
    public Vector3f Kd;     //Diffuse Reflectivity
    public Vector3f Ks;     //Specular Reflectivity
    public float shine;     //Specular Exponent

    public Material(){
        Le = new Vector3f(0);
        Ka = new Vector3f(0);
        Kd = new Vector3f(0);
        Ks = new Vector3f(0);
        shine = 1.0f;
    }

    public void setUniforms(Shader s){
        s.setUniform("Ka",Ka);
        s.setUniform("Kd",Kd);
        s.setUniform("Ks",Ks);
        s.setUniform("Le",Le);
        s.setUniform("shine",shine);
    }

    public String toString(){
        String string = "";
        string += ("Light Emitted: " + Le.toString());
        string += ("\nAmbient Color: " + Ka.toString());
        string += ("\nDiffuse Color: " + Kd.toString());
        string += ("\nSpecular Color: " + Ks.toString());
        string += ("\nShine: " + shine);
        return string;
    }

}
