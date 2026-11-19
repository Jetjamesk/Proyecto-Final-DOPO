package domain;

public class GoalMatch extends Match {
 
    private final int goalsToWin;


    public GoalMatch(Character p1,Character p2, Field field, Ball ball, int goalsToWin){
        super(p1, p2, field, ball);
        this.goalsToWin = goalsToWin;
    }


    public void updateMatch(double dt){
        if (!isRunning || isPaused || dt <= 0) return;
        int steps = (int) Math.ceil(dt / STEP);
        double h = dt / steps;
        for (int i = 0; i < steps && isRunning; i++) {
            world.step(h);      // la física: mover y resolver choques
            checkGoal();        // las reglas: ¿hubo gol?
        }
    }
    public void checkGoal(){
        int conceded = field.goalConcededBy(ball);
        if (conceded < 0) return;
        scoreboard.goalConcededBy(conceded);
        int scorer = 1 - conceded;
        if (scoreboard.getGoals(scorer) >= goalsToWin) endMatch();
        else resetRound();
    }
    public double getTimeLeft(){
        return 0.0;
    };
    public boolean isGoldenGoal(){
        return false;
    };
}
