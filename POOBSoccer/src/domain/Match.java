package domain;
import java.util.ArrayList;

public class Match {
    private static final double STEP = 1.0 / 120.0;   // paso máximo de la física (segundos)
    private static final double MATCH_TIME = 60;      // duración del partido (segundos)

    private double timeLeft;
    private boolean isRunning;
    private boolean isPaused;
    private boolean goldenGoal;                       // true cuando hay empate y se juega gol de oro
    private Character winner;
    private ArrayList<Character> jugadores;
    private Field field;
    private Ball ball;
    private MatchPhysics world;


    public Match(Character p1,Character p2, Field field, Ball ball){
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

    /** Avanza el partido dt segundos. Lo llama la GUI en cada tick de su Timer. */
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


    /**
     * Verifica los goles hayan sido hechos
     */
    public void checkGoal(){
        int conceded = field.goalConcededBy(ball);   // 0 = arco izquierdo, 1 = derecho
        if (conceded < 0) return;
        // El punto es para el jugador del otro lado: así los autogoles salen solos.
        jugadores.get(1 - conceded).addScore();
        if (goldenGoal) {
            endMatch();
        } else {
            resetRound();
        }
    }

    /**
     * usar el poder sobre el rival
     * @param player que jugador usara el poder. 
     */
    public void usePower(int player){
        if (isRunning && !isPaused) {
            Character user = jugadores.get(player);
            Character rival = jugadores.get(1 - player);
            user.activateSuper(rival);
        }
    }

    private void timeUp(){
        timeLeft = 0;
        if (jugadores.get(0).getScore() == jugadores.get(1).getScore()) {
            goldenGoal = true;     // empate: gana el primero que anote
        } else {
            endMatch();
        }
    }

    /**
     * Reinicia a lso juagdores y el balon a la posicion inicial.
     */
    private void resetRound(){
        for (Character p : jugadores){
            p.reset(field);
        } 
        ball.reset(field.getWidth() / 2.0, field.getHeight() * 0.25);
    }

    /**
     * Ordenes que los jugadores pueden tener
     * 
     */
    public void move(int player, int direction){
        if (isRunning && !isPaused) jugadores.get(player).movement(direction);
    }

    public void jump(int player){
        if (isRunning && !isPaused) jugadores.get(player).jump();
    }

    public void kick(int player){
        if (isRunning && !isPaused) {
            jugadores.get(player).kickBall();
        }
    }

    public void pause(){
        if (isRunning) {
            isPaused = true;
        }    
    }

    public void resume(){
        isPaused = false;
    }

    public void endMatch(){
        isRunning = false;
        int s0 = jugadores.get(0).getScore();
        int s1 = jugadores.get(1).getScore();
        winner = s0 > s1 ? jugadores.get(0) : (s1 > s0 ? jugadores.get(1) : null);
    }

    public boolean isRunning()   { return isRunning; }
    public boolean isPaused()    { return isPaused; }
    public boolean isGoldenGoal(){ return goldenGoal; }
    public double getTimeLeft()  { return timeLeft; }
    public Character getWinner() { return winner; }
    public Character getPlayer(int i){ return jugadores.get(i); }
    public Ball getBall()        { return ball; }
    public Field getField()      { return field; }
}