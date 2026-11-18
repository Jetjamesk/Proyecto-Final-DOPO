package domain;



public class TimedMatch extends Match{


    private double timeLeft;
    private boolean goldenGoal;                       // true cuando hay empate y se juega gol de oro


    public TimedMatch(Character p1,Character p2, Field field, Ball ball, int MATCH_TIME){
        super(p1, p2, field, ball);
        this.timeLeft = MATCH_TIME;
        this.goldenGoal = false;
    }

    public void updateMatch(double dt){
        if (!isRunning || isPaused || dt <= 0) return;
        int steps = (int) Math.ceil(dt / STEP);
        double h = dt / steps;
        for (int i = 0; i < steps && isRunning; i++) {
            world.step(h);      
            checkGoal();        
        }
        if (isRunning && !goldenGoal) {
            timeLeft -= dt;
            if (timeLeft <= 0) timeUp();
        }
    }
    public void checkGoal(){
        int conceded = field.goalConcededBy(ball);
        if (conceded < 0) return;
        scoreboard.goalConcededBy(conceded);
        if (goldenGoal) endMatch();
        else resetRound();
    }

    private void timeUp(){
        timeLeft = 0;
        if (scoreboard.isTied()) goldenGoal = true;
        else endMatch();
    }
    
    public boolean isGoldenGoal(){ return goldenGoal; }
    public double getTimeLeft()  { return timeLeft; }
}
