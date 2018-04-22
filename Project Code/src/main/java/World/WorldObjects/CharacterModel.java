package World.WorldObjects;



/**
 * This class is to be the parent of all classes that try to create a character.
 * For example, HumanModel should extend this.
 * If other character models are created, then they should extend this class.
 */
public abstract class CharacterModel extends CompositeShape{
    protected float angle;
    protected boolean increment = true;

    public void animate(){
        if (angle >= 45 || angle <= -45) increment = !increment;
        if (increment) angle += 2;
        else angle -= 2;
    }
}
