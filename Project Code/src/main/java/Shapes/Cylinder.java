package Shapes;

import Core.TriangleMesh;

/**
 * Created by (User name) on 8/12/2017.
 */
public class Cylinder extends TriangleMesh {

    private Prism p;

    public Cylinder(float radius, float height) {
        int sides = (int)(radius * 40);
        if (sides < 10) sides = 20;
        p = new Prism(radius, height, sides);
    }

    public void setHeight(float newHeight){
        p.setHeight(newHeight);
    }

    public float getHeight(){
        return p.getHeight();
    }

    @Override
    public void init(){
        p.init();
    }

    @Override
    public void render(){
        p.render();
    }

}
