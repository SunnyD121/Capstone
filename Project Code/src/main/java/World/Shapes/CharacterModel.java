package World.Shapes;

import World.TriangleMesh;


/**
 * This class is to be the parent of all classes that try to create a character.
 * For example, AbstractHuman should extend this.
 * If other character models are created, then they should extend this class.
 */
public abstract class CharacterModel extends TriangleMesh{
    protected static float angle;
    protected static boolean increment = true;

    public static void animate(){
        if (angle >= 45 || angle <= -45) increment = !increment;
        if (increment) angle += 2;
        else angle -= 2;
    }
}
