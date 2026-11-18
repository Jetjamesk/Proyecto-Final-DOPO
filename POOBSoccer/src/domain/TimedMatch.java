package domain;

import java.util.ArrayList;

public class TimedMatch implements Match{

    public static final double MATCH_TIME = 60;      // duración del partido (segundos)
    private double timeLeft;
    private boolean goldenGoal;                       // true cuando hay empate y se juega gol de oro


    public TimedMatch(Character p1,Character p2, Field field, Ball ball){
        this.jugadores = new ArrayList<>();
        this.jugadores.add(p1);
        this.jugadores.add(p2);
        this.field = field;
        this.ball = ball;
        this.world = new MatchPhysics(field, ball, jugadores);
        this.timeLeft = MATCH_TIME;
        this.isRunning = true;
        this.isPaused = false;
        this.goldenGoal = false;
        p1.setStartX(field.getWidth() * 0.25);
        p2.setStartX(field.getWidth() * 0.75);
        p1.setFacing(1);      
        p2.setFacing(-1);     
        resetRound();
    }

    public void updateMatch(double dt){
        if (!isRunning || isPaused || dt <= 0) return;
        int steps = (int) Math.ceil(dt / STEP);
        double h = dt / steps;
        for (int i = 0; i < steps && isRunning; i++) {
            world.step(h);      // la física: mover y resolver choques
            checkGoal();        // las reglas: ¿hubo gol?
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

    public void endMatch(){
        isRunning = false;
        int leader = scoreboard.leader();
        winner = leader < 0 ? null : jugadores.get(leader);
    }
    public boolean isGoldenGoal(){ return goldenGoal; }
    public double getTimeLeft()  { return timeLeft; }
}
