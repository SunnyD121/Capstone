import World.AbstractShapes.Cube;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by (User name) on 8/3/2017.
 */
public class CubeTest {
    @Test
    public void init() throws Exception {
        Cube c = new Cube(2.0f);
        // ..?
        assertSame(c,c);
    }

}