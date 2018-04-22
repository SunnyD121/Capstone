package World;

import org.joml.Vector3f;

import java.awt.Color;
import java.util.Random;

public class Enemy extends Player{

    private Vector3f spawnLocation;
    private Vector3f spawnDirection;

    private AIAlgorithm AI;

    public Enemy(Color c){
        super(c);
        AI = new AIAlgorithm(this);
        setSpawnLocation(new Vector3f());
        setSpawnDirection(new Vector3f());
    }

    public void becomeAutonomous(){
        AI.startAlgorithm();
    }

    public void updateAI(Vector3f playerPos){
        AI.update(playerPos);
        AI.startAlgorithm();
    }

    public void collisionResponse(){
        AI.collisionResponse();
    }


    public void setSpawnLocation(Vector3f newLoc){
        spawnLocation = new Vector3f(newLoc);
    }

    public Vector3f getSpawnLocation(){
        return new Vector3f(spawnLocation);
    }

    public void setSpawnDirection(Vector3f newDir){
        spawnDirection = new Vector3f(newDir);
    }

    public Vector3f getSpawnDirection(){
        return new Vector3f(spawnDirection);
    }


    private class AIAlgorithm {

        //TODO: tell the enemies to move, shoot and search for the player
        //move: look into how player moves, and implement similarly
        //shoot: be smart about it. spamming shoot isnt fun
        //searching: this will be tricky because do I 'hide' the player until the player shoots or crosses cone of visibility of enemy?
        private final float TURN_AMOUNT = 80.0f;
        private int iteration = 0;

        private Enemy thisEnemy;
        private Vector3f lastKnownPlayerPos = new Vector3f();

        public AIAlgorithm(Enemy thisEnemy){
            this.thisEnemy = thisEnemy;
        }


        public void startAlgorithm(){
            run();
        }

        public void update(Vector3f playerPos){
            lastKnownPlayerPos = playerPos;
        }

        public void collisionResponse(){
            thisEnemy.turn(180);
        }


        public void run(){
            iteration++;
            if (iteration > (Integer.MAX_VALUE - 100)) iteration = 0;   //prevent overflow
            move();
            shoot();
        }

        private void move(){

            //seek player
            float distance = Utilities.Utilities.dist(thisEnemy.getPosition(), lastKnownPlayerPos);
            if (distance < 25){
                Vector3f dir = new Vector3f();
                thisEnemy.getPosition().sub(lastKnownPlayerPos, dir);
                dir.normalize().negate();
                thisEnemy.setDirection(dir);

            }

            //Random MovePattern
            else if (iteration % 20 == 0) {
                Random r = new Random();
                float turn = ( r.nextFloat() * 2 - 1.0f) * TURN_AMOUNT;
                thisEnemy.turn(turn);

            }
            //only move if more than 5m from player
            if (distance > 5) thisEnemy.move(new Vector3f(thisEnemy.getDirection().x, 0.0f, thisEnemy.getDirection().z).normalize().mul(0.1f));

        }

        private void shoot(){
            float distance = Utilities.Utilities.dist(thisEnemy.getPosition(), lastKnownPlayerPos);
            if (distance < 15) thisEnemy.shootLaser();
        }

    }   //END inner class AIAlgorithm
}
