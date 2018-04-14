package CollisionDetectionSystem;

import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollisionDetectionSystemTest {
    private CollisionDetectionSystem CDS;
    @Before
    public void setup(){
        CDS = CDS.getInstance();
    }
    @Test
    public void isCollidingMeshes1() throws Exception {
        //1 Edge to Edge nonCoplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, 2, 0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, -2, 2)
        );
        assertTrue("1: Edge to Edge nonCoplanar", CDS.isCollidingMeshes(A, B));
    }
    @Test
    public void isCollidingMeshes2() throws Exception {
        //2 Edge to Edge coplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, 2, 0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, -2, 0)
        );
        assertTrue("2: Edge to Edge coplanar", CDS.isCollidingMeshes(A, B));
    }
    @Test
    public void isCollidingMeshes3() throws Exception {
        //3 Point to Point nonCoplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 0, 0),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, 2, 0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0, 1, 2),
                new org.joml.Vector3f(2, 0, 0),
                new org.joml.Vector3f(1, 2, 4)
        );
        assertTrue("3: Point to Point nonCoplanar", CDS.isCollidingMeshes(A, B));
    }
    @Test
    public void isCollidingMeshes4() throws Exception {
        //4 Point to Point coplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(-1,-1,0),
                new org.joml.Vector3f(1,-1,0)
        );
        assertTrue("4: Point to Point coplanar", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes5() throws Exception {
        //5 in the hole
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(-1,1,-1),
                new org.joml.Vector3f(3,1,-1),
                new org.joml.Vector3f(1,1,1)
        );
        assertTrue("5: In the hole", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes6a() throws Exception {
        //6 Jewish star
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,1,0),
                new org.joml.Vector3f(2,1,0),
                new org.joml.Vector3f(1,-1,0)
        );
        assertTrue("6: Jewish Star", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes6b() throws Exception {
        //6 Jewish star
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(0,2,0),
                new org.joml.Vector3f(0,1,2)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,1),
                new org.joml.Vector3f(0,2,1),
                new org.joml.Vector3f(0,1,-1)
        );
        assertTrue("6: Jewish Star", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes7() throws Exception {
        //7 Point to Face
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,1,-1),
                new org.joml.Vector3f(2,1,-1),
                new org.joml.Vector3f(1,1,0)
        );
        assertTrue("7: Point to Face", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes8() throws Exception {
        //8 not intersecting noncoplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,1),
                new org.joml.Vector3f(2,1,2),
                new org.joml.Vector3f(1,2,3)
        );
        assertFalse("8: Not Intersecting nonCoplanar", CDS.isCollidingMeshes(A,B));
    }
    @Test
    public void isCollidingMeshes9() throws Exception {
        //9 Not Intersecting coplanar
        World.AbstractShapes.Triangle A = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(0,0,0),
                new org.joml.Vector3f(2,0,0),
                new org.joml.Vector3f(1,2,0)
        );
        World.AbstractShapes.Triangle B = new World.AbstractShapes.Triangle(
                new org.joml.Vector3f(1,-1,0),
                new org.joml.Vector3f(3,-1,0),
                new org.joml.Vector3f(2,-3,0)
        );
        assertFalse("9: Not Intersecting coplanar", CDS.isCollidingMeshes(A,B));
    }

}