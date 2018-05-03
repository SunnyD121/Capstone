package Core.CollisionDetectionSystem;

import Core.Shader;
import Utilities.Utilities;
import Utilities.BiMap;
import Utilities.BinaryTree;
import World.AbstractShapes.Triangle;

import World.Enemy;
import World.Player;
import World.WorldObjects.Particles.Laser;
import World.WorldObjects.Particles.Particle;
import World.AbstractShapes.Building;
import World.Character;
import World.SceneEntity;

import World.WorldObjects.Ground;
import World.Worlds.AbstractWorld;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;



public class CollisionDetectionSystem {

    //need a datastructure to hold all object's collision boxes.
    //need a map to map the box to the object, or some way to link the two.
    //efficiency: categorizing into subsets the objects
    static BiMap<BoundingBox, SceneEntity> map;
    static BinaryTree<FixedBoundingBox> bTree;
    ArrayList<BoundingBox> boxesToHighlight;
    FixedBoundingBox groundBox;
    private Shader shader;

    private static CollisionDetectionSystem singleton;

    private CollisionDetectionSystem(){
        map = new BiMap<BoundingBox, SceneEntity>();
        boxesToHighlight = new ArrayList<>();
        shader = Shader.getInstance();
    }

    public static CollisionDetectionSystem getInstance(){
        if (singleton == null) singleton = new CollisionDetectionSystem();
        return singleton;
    }

    public static void reset(){
        singleton = null;
        getInstance();
    }

    public void addCollisionBox(SceneEntity object, Vector3f minPoint, Vector3f maxPoint, Vector3f transform){
        BoundingBox b;
        if (object instanceof Character || object instanceof Particle) b = new MovableBoundingBox(minPoint, maxPoint, transform);
        else b = new FixedBoundingBox(minPoint, maxPoint, transform);
        map.put(b, object);
    }

    public void addCollisionBox(SceneEntity object, Vector3f center, float length, float height, float width, Vector3f transform){
        Vector3f minPoint = new Vector3f(
                center.x - length / 2.0f,
                center.y - height / 2.0f,
                center.z - width /2.0f);
        Vector3f maxPoint = new Vector3f(
                center.x + length / 2.0f,
                center.y + height / 2.0f,
                center.z + width /2.0f);
        addCollisionBox(object, minPoint, maxPoint, transform);
    }

    public void removeCollisionBox(SceneEntity entity){
        map.removeValue(entity);    //note this may remove out of order
    }

    public static BoundingBox mergeMany(BoundingBox...group){
        if (group.length == 1) return group[0]; //reuse Box, since it's around a SceneEntity (doesn't screw up mapping)
        float[] mins_x = new float[group.length];
        float[] maxes_x = new float[group.length];
        float[] mins_y = new float[group.length];
        float[] maxes_y = new float[group.length];
        float[] mins_z = new float[group.length];
        float[] maxes_z = new float[group.length];

        for (int i = 0; i < group.length; i++){
            mins_x[i] = group[i].minPoint.x;
            maxes_x[i] = group[i].maxPoint.x;
            mins_y[i] = group[i].minPoint.y;
            maxes_y[i] = group[i].maxPoint.y;
            mins_z[i] = group[i].minPoint.z;
            maxes_z[i] = group[i].maxPoint.z;
        }
        return new FixedBoundingBox(
                new Vector3f(Utilities.min(mins_x), Utilities.min(mins_y), Utilities.min(mins_z)),
                new Vector3f(Utilities.max(maxes_x), Utilities.max(maxes_y), Utilities.max(maxes_z)),
                null
        );
    }

    public <T extends SceneEntity> void formConglomerates(Class<T> classtype){
        ArrayList<SceneEntity> group = new ArrayList<>();
        for (SceneEntity e : map.valueSet()) {
            if (e.getClass() == classtype) group.add(e);
        }
        BoundingBox[] array = new BoundingBox[group.size()];
        for (int i = 0; i < array.length; i++) array[i] = map.getFirstKeyFromValue(group.get(i));
        FixedBoundingBox box = (FixedBoundingBox)mergeMany(array);
        map.put(box, null);
    }

    public void postInit(){
        ArrayList<FixedBoundingBox> fixedBoxHierarchy = new ArrayList<>();
        ArrayList<ArrayList<FixedBoundingBox>> parentArray = new ArrayList<>();
        parentArray.add(fixedBoxHierarchy);
        for (BoundingBox box : map.keySet()){
            if (box instanceof FixedBoundingBox) fixedBoxHierarchy.add((FixedBoundingBox)box);
        }
        FixedBoundingBox[] tempArray = new FixedBoundingBox[fixedBoxHierarchy.size()];
        fixedBoxHierarchy.toArray(tempArray);
        bTree = new BinaryTree<FixedBoundingBox>((FixedBoundingBox)mergeMany(tempArray));
        groundBox = findGroundInHierarchy(fixedBoxHierarchy);
        fixedBoxHierarchy.remove(groundBox);
        recursiveBuildTree(bTree, fixedBoxHierarchy);
    }

    private FixedBoundingBox findGroundInHierarchy(ArrayList<FixedBoundingBox> hierarchy){
        for (FixedBoundingBox box : hierarchy) {
            if (map.getFirstValueFromKey(box).getClass() == Ground.class)    //there should only be one Ground object. This code smells bad.
                return box;
        }
        return null;
    }

    private void recursiveBuildTree(BinaryTree tree, ArrayList<FixedBoundingBox> boxes){
        //BUG: if only one FixedBoundingBox (not counting ground) in scene, this will not add it properly
        if (boxes.size() <= 1) return;
        else {
            Vector3f axisSplit = findSplitAxis(boxes);
            boxes = Utilities.sortByLocation(axisSplit, boxes);
            ArrayList<FixedBoundingBox> leftHalf = Utilities.splitByAxis(boxes, axisSplit, true);
            ArrayList<FixedBoundingBox> rightHalf = Utilities.splitByAxis(boxes, axisSplit, false);
            FixedBoundingBox[] temp1 = new FixedBoundingBox[leftHalf.size()];
            FixedBoundingBox[] temp2 = new FixedBoundingBox[rightHalf.size()];
            leftHalf.toArray(temp1);
            rightHalf.toArray(temp2);
            if (temp1.length != 0) {tree.leftChild = new BinaryTree<FixedBoundingBox>((FixedBoundingBox) mergeMany(temp1));
            recursiveBuildTree(tree.leftChild, leftHalf);}
            if (temp2.length != 0) {tree.rightChild = new BinaryTree<FixedBoundingBox>((FixedBoundingBox) mergeMany(temp2));
            recursiveBuildTree(tree.rightChild, rightHalf);}
        }
    }

    private <T extends BoundingBox> Vector3f findSplitAxis(ArrayList<T> things){
        float x_value = 0;
        float z_value = 0;
        float max_x = things.get(0).getCenterPoint().x;
        float max_z = things.get(0).getCenterPoint().z;
        //y_value purposefully omitted since most objects lie on the xz plane

        for (T thing : things){
            x_value += thing.getCenterPoint().x;
            z_value += thing.getCenterPoint().z;
            if (max_x < Math.abs(thing.getCenterPoint().x)) max_x = Math.abs(thing.getCenterPoint().x);
            if (max_z < Math.abs(thing.getCenterPoint().z)) max_z = Math.abs(thing.getCenterPoint().z);
        }
        x_value /= things.size();
        z_value /= things.size();
        Vector3f returnee = (max_x > max_z) ? new Vector3f(x_value, 0 , 0) : new Vector3f(0,0, z_value);
        if (x_value == 0.0f) returnee = new Vector3f(0,0,z_value);
        if (z_value == 0.0f) returnee = new Vector3f(x_value,0,0);
        return returnee;
    }

    public void testCollisions(){
        updateMovingHitBoxes();
        ArrayList<MovableBoundingBox> movableBoxes = new ArrayList<>();
        for (BoundingBox b : map.keySet()) if (b instanceof MovableBoundingBox) movableBoxes.add((MovableBoundingBox)b);
        boxesToHighlight.clear();



        //Moving vs Fixed collision test (contains a continue call)
        for (MovableBoundingBox mBox : movableBoxes) {
            FixedBoundingBox box2 = recursiveTreeCollide(bTree, mBox);
            if (box2 == null) continue; //not inside SceneEntity's personal BoundingBox

            SceneEntity entity1 = map.getFirstValueFromKey(mBox);
            SceneEntity entity2 = map.getFirstValueFromKey(box2);

            if (entity1 instanceof Character) {
                //Collide Triangles!
                int count = 0;
                long startTime = System.currentTimeMillis();

                boolean collision = false;
                //labelled break? cool!
                intersectionFound:
                for (Triangle t : entity1.getTriangles()) {
                    for (Triangle t2 : entity2.getTriangles()) {
                        count++;
                        if (isCollidingMeshes(t, t2)) {
                            collision = true;
                            break intersectionFound;
                        }
                    }
                }

                long endTime = System.currentTimeMillis();
                //System.out.println("Triangle-Triangle Intersection tests performed: " + count);
                long time = (endTime - startTime);
                //System.out.println("Time to do all that calculation: " + time + "ms");

                if (collision){
                    entity1.moveDistance(shortestDirectionOut(entity1.getPosition(), entity2.getPosition(), false).normalize());    //normalized to move the offender out a little bit
                    if (entity1 instanceof Enemy) ((Enemy) entity1).collisionResponse();
                }
            }
            else if (entity1 instanceof Laser) {  //Laser hit something fixed
                ((Particle) entity1).kill();
            }
            else {        //unknown movable object
                System.err.println("Unhandled Collision Object Type (Moving).");
                System.exit(-1);
            }
        }

        //Moving vs Moving collision test
        for (MovableBoundingBox mBox : movableBoxes) {
            for (MovableBoundingBox mBox2 : movableBoxes) {
                if (isColliding(mBox, mBox2)) {

                    SceneEntity entity1 = map.getFirstValueFromKey(mBox);
                    SceneEntity entity2 = map.getFirstValueFromKey(mBox2);

                    if (entity1 instanceof Laser ) {
                        if (entity2 instanceof Player) {
                            if (((Laser) entity1).getID() == ((Player) entity2).getPlayerNum())
                                continue;    //no collision should register since the player spawned the laser
                            ((Player) entity2).kill();
                            ((Laser) entity1).kill();
                            if (((Laser) entity1).getID() == 0) AbstractWorld.increasePlayerScore();
                            //System.out.println("Player "+((Player)entity2).getPlayerNum()+" died!");
                        }
                    }

                    map.getFirstValueFromKey(mBox).moveDistance(shortestDistanceOut(mBox.minPoint, mBox.maxPoint, mBox2.minPoint, mBox2.maxPoint));

                }
            }
        }

        for (MovableBoundingBox mBox : movableBoxes) {
            //special case: ground collision    (special because I didn't want the ground to be considered for building the Bounding Box Hierarchy
            //highlightIndivualBox(shader, mBox);
            //highlightIndivualBox(shader, groundBox);
            if (isColliding(mBox, groundBox)) {

                SceneEntity entity1 = map.getFirstValueFromKey(mBox);
                SceneEntity entity2 = map.getFirstValueFromKey(groundBox);
                //TODO: might want to restrict below move call to +y direction
                entity1.moveDistance(shortestDistanceOut(mBox.minPoint, mBox.maxPoint, groundBox.minPoint, groundBox.maxPoint));
                entity1.isOnGround = true;
            }
        }
    }

    private FixedBoundingBox recursiveTreeCollide(BinaryTree<FixedBoundingBox> tree, MovableBoundingBox collidee){
        //base case
        if (tree.isLeaf()) {
            if (isColliding(collidee, tree.getRoot())){
                boxesToHighlight.add(tree.getRoot());
                return tree.getRoot();
            }
            else return null;
        }

        FixedBoundingBox returnee = null;
        if (isColliding(collidee, tree.getRoot())) {
            returnee = recursiveTreeCollide(tree.leftChild, collidee);
            boxesToHighlight.add(tree.getRoot());
            if (returnee == null) returnee = recursiveTreeCollide(tree.rightChild, collidee);
        }
        return returnee;
    }

    /**
     *
     * @param mBox one Movable Axis Aligned Bounding Box
     * @param box one Generic Axis Aligned Bounding Box
     * @return true if colliding
     */
    private boolean isColliding(MovableBoundingBox mBox, BoundingBox box){
        if (checkEquivalence(mBox, box)) return false;
        //special check because Lasers are too speedy.
        if (map.getFirstValueFromKey(mBox) instanceof Laser) {return speedyObjectCollisionTest(mBox, box);
            /*
            Laser l = (Laser)map.getValueFromKey(mBox);
            return (
                    (l.getPosition().x <= box.maxPoint.x && l.getprevPosition().x >= box.minPoint.x) &&
                            (l.getPosition().y <= box.maxPoint.y && l.getprevPosition().y >= box.minPoint.y) &&
                            (l.getPosition().z <= box.maxPoint.z && l.getprevPosition().z >= box.minPoint.z)
            );
        */
        }
        return (
                (mBox.minPoint.x <= box.maxPoint.x && mBox.maxPoint.x >= box.minPoint.x) &&
                (mBox.minPoint.y <= box.maxPoint.y && mBox.maxPoint.y >= box.minPoint.y) &&
                (mBox.minPoint.z <= box.maxPoint.z && mBox.maxPoint.z >= box.minPoint.z)
        );
    }

    private boolean speedyObjectCollisionTest(MovableBoundingBox mBox, BoundingBox box){
        //guarenteed to be a laser by a previous check //NOTE: this may change in the future
        Laser laser = ((Laser) map.getFirstValueFromKey(mBox));
        boolean output = Utilities.boxLineIntersection(box, laser.getPosition(), laser.getPrevPosition());
        return output;
    }

    /**
     *
     * @param A one triangle in 3D space
     * @param B a different triangle in 3D space
     * @returns a Vector3f detailing the point of collision, or null if none exists
     */
    public boolean isCollidingMeshes(Triangle A, Triangle B){
        //check if any points are equal for easy check
        if (A.p1.equals(B.p1) || (A.p1.equals(B.p2)) || (A.p1.equals(B.p3)) ||
        (A.p2.equals(B.p1)) || (A.p2.equals(B.p2)) || (A.p2.equals(B.p3)) ||
        (A.p3.equals(B.p1)) || (A.p3.equals(B.p2)) || A.p3.equals(B.p3)) return true;

        //instantiation
        Vector3f P = B.p1;
        float alpha1;
        Vector3f p1 = new Vector3f(); B.p2.sub(B.p1, p1);
        float alpha2;
        Vector3f p2 = new Vector3f(); B.p3.sub(B.p1, p2);

        Vector3f Q1 = A.p1;
        Vector3f Q2 = A.p1;
        Vector3f Q3 = A.p3;
        float beta1;
        float beta2;
        float beta3;
        Vector3f q1 = new Vector3f(); A.p2.sub(A.p1, q1);
        Vector3f q2 = new Vector3f(); A.p3.sub(A.p1, q2);
        Vector3f q3 = new Vector3f(); A.p2.sub(A.p3, q3);

        //Checking for intersection
        Vector3f r1 = new Vector3f(); Q1.sub(P, r1);
        Vector3f r2 = new Vector3f(); Q2.sub(P, r2);
        Vector3f r3 = new Vector3f(); Q3.sub(P, r3);
        float minor1 = p1.x * p2.y - p1.y * p2.x;
        float minor2 = p1.y * p2.z - p1.z * p2.y;
        float minor3 = p1.z * p2.x - p1.x * p2.z;

        float detq1 = (minor1 * q1.z) + (minor2 * q1.x) - (minor3 * q1.y);
        float detr1 = (minor1 * r1.z) + (minor2 * r1.x) - (minor3 * r1.y);

        float detq2 = (minor1 * q2.z) + (minor2 * q2.x) - (minor3 * q2.y);
        float detr2 = (minor1 * r2.z) + (minor2 * r2.x) - (minor3 * r2.y);

        float detq3 = (minor1 * q3.z) + (minor2 * q3.x) - (minor3 * q3.y);
        float detr3 = (minor1 * r3.z) + (minor2 * r3.x) - (minor3 * r3.y);

        if (detq1 == 0 && detq2 == 0 && detq3 == 0) return coplanarIntersection(A,B);    //Triangles are coplanar //BUG: coplanarIntersection algorithm incorrectly checks only x-y plane.

        //Note these are inverted from the article. Professor Stuart seems to think this is correct and minimal testing confirms this.
        beta1 = -(detr1/detq1);
        beta2 = -(detr2/detq2);
        beta3 = -(detr3/detq3);

        if (!((beta1 >= 0 && beta1 <= 1) ||
                (beta2 >= 0 && beta2 <= 1) ||
                (beta3 >= 0 && beta3 <= 1))) return false;

        //constraints are met, now check if the line of intersection is valid
        Vector3f T, t_vec = new Vector3f();
        //one of the Beta's above will be infitite or NaN. 3 cases:
        if (!Float.isFinite(beta1)){    //then 2 and 3 are valid
            Vector3f temp = new Vector3f(); q2.mul(beta2, temp);
            T = new Vector3f(); Q2.add(temp, T);
            Vector3f temp2 = new Vector3f(); q3.mul(beta3, temp2);
            t_vec = new Vector3f(); temp2.sub(temp, t_vec);
        }
        else if (!Float.isFinite(beta2)){   //then 1 and 3 are valid
            Vector3f temp = new Vector3f(); q1.mul(beta1, temp);
            T = new Vector3f(); Q1.add(temp, T);
            Vector3f temp2 = new Vector3f(); q3.mul(beta3, temp2);
            t_vec = new Vector3f(); temp2.sub(temp, t_vec);
        }
        else{   //!Float.isFinite(beta3)    //then 1 and 2 and valid
            Vector3f temp = new Vector3f(); q1.mul(beta1, temp);
            T = new Vector3f(); Q1.add(temp, T);
            Vector3f temp2 = new Vector3f(); q2.mul(beta2, temp2);
            t_vec = new Vector3f(); temp2.sub(temp, t_vec);
        }
        Vector3f T2 = new Vector3f(); T.add(t_vec, T2);
        Vector3f P2 = new Vector3f();
        Vector3f temp = new Vector3f();

        P.add(p1, P2);
        Vector3f intersect1 = Utilities.lineIntersect(P,P2,T, T2);
        P.add(p2, P2);
        Vector3f intersect2 = Utilities.lineIntersect(P,P2,T, T2);
        P.add(p1, temp);
        p2.sub(p1, P2);
        temp.add(P2, P2);
        Vector3f intersect3 = Utilities.lineIntersect(temp,P2,T, T2);

        if (intersect1 == null && intersect2 == null && intersect3 == null) return false;
        //else return the one that isnt null; that's where the vector that cuts triangle A into two pieces intersects with one or two of Triangle B's edges.
        return true;


    }

    private boolean coplanarIntersection(Triangle A, Triangle B){
        //test 1: check if any of the lines intersect
        boolean intersects = false;
        if ( lineIntersect(A.p1,A.p2,B.p1,B.p2) ) intersects = true;
        if ( lineIntersect(A.p1,A.p2,B.p2,B.p3) ) intersects = true;
        if ( lineIntersect(A.p1,A.p2,B.p3,B.p1) ) intersects = true;

        if ( lineIntersect(A.p2,A.p3,B.p1,B.p2) ) intersects = true;
        if ( lineIntersect(A.p2,A.p3,B.p2,B.p3) ) intersects = true;
        if ( lineIntersect(A.p2,A.p3,B.p3,B.p1) ) intersects = true;

        if ( lineIntersect(A.p3,A.p1,B.p1,B.p2) ) intersects = true;
        if ( lineIntersect(A.p3,A.p1,B.p2,B.p3) ) intersects = true;
        if ( lineIntersect(A.p3,A.p1,B.p3,B.p1) ) intersects = true;
        return intersects;
    }

    private boolean lineIntersect(Vector3f pA1, Vector3f pA2, Vector3f pB1, Vector3f pB2){
        //Algorithm assumes that triangles that collide edge to edge are not intersecting. Which is fine, they'll intersect if they overlap
        float A1 = pA2.y - pA1.y;
        float B1 = pA1.x - pA2.x;
        float C1 = A1 * pA1.x + B1 * pA1.y;

        float A2 = pB2.y - pB1.y;
        float B2 = pB1.x - pB2.x;
        float C2 = A2 * pB1.x + B2 * pB1.y;

        float det = A1 * B2 - A2 * B1;
        if (det == 0) return false;     //lines are parallel
        //else
        float x = (B2 * C1 - B1 * C2) / det;
        float y = (A1 * C2 - A2 * C1) / det;
        if ( (Math.min(pA1.x, pA2.x) <= x && Math.max(pA1.x, pA2.x) >= x) &&
                (Math.min(pA1.y, pA2.y) <= y && Math.max(pA1.y, pA2.y) >= y) &&
                (Math.min(pB1.x, pB2.x) <= x && Math.max(pB1.x, pB2.x) >= x) &&
                (Math.min(pB1.y, pB2.y) <= y && Math.max(pB1.y, pB2.y) >= y)
                ) return true;
        return false;

    }

    private Vector3f shortestDistanceOut(Vector3f min1, Vector3f max1, Vector3f min2, Vector3f max2){
        float x1 = Utilities.dist(min1.x, max2.x);
        float x2 = Utilities.dist(max1.x, min2.x);
        boolean negX = (x1 < x2 ? min1.x > max2.x : max1.x > min2.x);

        float y1 = Utilities.dist(min1.y, max2.y);
        float y2 = Utilities.dist(max1.y, min2.y);
        boolean negY = (y1 < y2 ? min1.y > max2.y : max1.y > min2.y);

        float z1 = Utilities.dist(min1.z, max2.z);
        float z2 = Utilities.dist(max1.z, min2.z);
        boolean negZ = (z1 < z2 ? min1.z > max2.z : max1.z > min2.z);

        float shortX = (x1 < x2 ? x1 : x2);
        float shortY = (y1 < y2 ? y1 : y2);
        float shortZ = (z1 < z2 ? z1 : z2);

        float shortest = Utilities.min(shortX, shortY, shortZ);

        if (shortest == shortX){
            if (negX) shortX *= -1;
            return new Vector3f(shortX, 0,0);
        }

        else if (shortest == shortY) {
            if (negY) shortY *= -1;
            return new Vector3f(0, shortY, 0);
        }
        else if (shortest == shortZ) {
            if (negZ) shortZ *= -1;
            return new Vector3f(0,0,shortZ);
        }
        else {System.err.println("What the hell?!"); System.exit(-1); return null;}   //Ha-Ha...code after a exit call.
    }
    private Vector3f shortestDirectionOut(Vector3f pos1, Vector3f pos2, boolean useYdist){
        float x_dist = pos1.x - pos2.x;
        float y_dist = pos1.y - pos2.y;
        float z_dist = pos1.z - pos2.z;
        float shortest;
        if (useYdist) shortest = Utilities.min(Math.abs(x_dist), Math.abs(y_dist), Math.abs(z_dist));
        else shortest = Utilities.min(Math.abs(x_dist), Math.abs(z_dist));

        if (shortest == Math.abs(x_dist)) return new Vector3f(0,0,z_dist);
        else if (shortest == Math.abs(y_dist) && useYdist) return new Vector3f(0, y_dist,0);
        else if (shortest == Math.abs(z_dist)) return new Vector3f(x_dist, 0,0);
        else { System.err.println("Error...Mathematical impossibility."); System.exit(-1); return null; }
    }

    private boolean checkEquivalence(MovableBoundingBox mBox, BoundingBox box){
        return map.getFirstKeyIndex(mBox) == map.getFirstKeyIndex(box);
    }

    private void updateMovingHitBoxes(){
        for (BoundingBox box : map.keySet()){
            if (box instanceof MovableBoundingBox)  {
                ((MovableBoundingBox) box).setPosition(map.getFirstValueFromKey(box).getPosition());
            }
        }
    }

    boolean needsInit = true;
    ArrayList<Building> FixedHitBoxVisuals;
    ArrayList<Building> MovableHitBoxVisuals;
    ArrayList<Building> highlights;
    public void drawBoundingBoxes(boolean boxHighlighting){
        if (needsInit && boxHighlighting) highlights = new ArrayList<>();
        if (boxHighlighting) {
            highlights.clear();
            for (BoundingBox box : boxesToHighlight) {
                highlights.add(createBoxObject(box));
            }
        }
        MovableHitBoxVisuals = new ArrayList<>();
        for (BoundingBox box : map.keySet()) {
            Building shape = null;
            if (box instanceof MovableBoundingBox){
                shape = createBoxObject(box);
                MovableHitBoxVisuals.add(shape);
            }
        }
        if (needsInit) {
            FixedHitBoxVisuals = new ArrayList<>();
            for (BoundingBox box : bTree.getTreeAsSetUnordered()) {
                Building shape = createBoxObject(box);
                FixedHitBoxVisuals.add(shape);
            }
        }
        needsInit = false;
        //order matters. Lines that are drawn first will be shown.
        shader.setUniform("ObjectToWorld", new Matrix4f());
        if (boxHighlighting) for (Building b : highlights) b.renderOnlyLines(new Vector3f(1,0,0));
        for (Building b : MovableHitBoxVisuals) b.renderOnlyLines(new Vector3f(1,0,1));
        for (Building b : FixedHitBoxVisuals) b.renderOnlyLines(new Vector3f(1,1,0));

    }

    private void highlightIndivualBox(BoundingBox b){
        Building shape = createBoxObject(b);
        shape.renderWithLines(new Vector3f(0), new Vector3f(1));
    }

    private static Building createBoxObject(BoundingBox box){
        Building shape = new Building();
        Vector3f[] data = box.getVertexData();
        Vector3f[] temp = new Vector3f[]{data[0], data[1], data[2], data[3]};
        float[] temp2 = new float[]{data[4].y, data[5].y, data[6].y, data[7].y};
        shape.setData(temp, temp2);
        shape.init();
        return shape;
    }


}
