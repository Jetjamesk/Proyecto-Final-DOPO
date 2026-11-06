package domain;


public class Field {
    private static final int GROUND_HEIGHT = 50;
    private static final int GOAL_DEPTH = 80;
    private int width;
    private int height;
    private int goalHeight;

    public Field(int width,int height,int goalHeight){
        this.height = height;
        this.width = width;
        this.goalHeight = goalHeight;
    }


    public int getWidth(){
        return width;
    }
    
    public int getHeight(){
        return height;
    }

    public int getGoalHeight(){
        return goalHeight;
    }

    public int getGoalDepth(){
        return GOAL_DEPTH;
    }

    public int getFloorY(){
        return height - GROUND_HEIGHT;
    }

    public int getGoalTopY(){
        return getFloorY() - goalHeight;
    }

    public int goalConcededBy(Ball ball){
        double r = ball.getRadius();
        boolean underBar = ball.getY() - r >= getGoalTopY();
        if (underBar && ball.getX() + r <= GOAL_DEPTH){
            return 0;
        }
        if(underBar && ball.getX() - r >= width - GOAL_DEPTH){
            return 1;
        }
        return -1;
    }
}
