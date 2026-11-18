package domain;
import java.util.ArrayList;

public abstract class Match {
    public static final double STEP = 1.0 / 120.0;   // paso máximo de la física (segundos)

    protected boolean isRunning;
    protected boolean isPaused;

    protected final Scoreboard scoreboard = new Scoreboard();
    protected Character winner;
    protected final ArrayList<Character> jugadores = new ArrayList<>();
    protected final Field field;
    protected final Ball ball;
    protected final MatchPhysics world;

    public Match(Character p1,Character p2, Field field, Ball ball){
        this.field = field;
        this.ball = ball;
        jugadores.add(p1);
        jugadores.add(p2);
        this.world = new MatchPhysics(field, ball, jugadores);
        p1.setStartX(field.getWidth() * 0.25);
        p2.setStartX(field.getWidth() * 0.75);
        p1.setFacing(1);
        p2.setFacing(-1);
        isRunning = true;
        isPaused = false;
        resetRound();
        for(Character p: jugadores){
            p.superReset();
        }
    }
    /** Avanza el partido dt segundos. Lo llama la GUI en cada tick de su Timer. */
    public abstract void updateMatch(double dt);


    /**
     * Verifica los goles hayan sido hechos
     */
    public abstract void checkGoal();

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

    
    public abstract void endMatch();
    

    /**
     * Reinicia a lso juagdores y el balon a la posicion inicial.
     */
    public void resetRound(){
        for (Character p : jugadores){
            p.reset(field);
        } 
        ball.reset(field.getWidth() / 2.0, field.getHeight() * 0.25);
    }

    /**
     * Ordenes que los jugadores pueden tener
     * 
     */
    public  void move(int player, int direction){
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

    public boolean isRunning()   { return isRunning; }
    public boolean isPaused()    { return isPaused; }
    public Character getWinner() { return winner; }
    public Character getPlayer(int i){ return jugadores.get(i); }
    public Ball getBall()        { return ball; }
    public Field getField()      { return field; }
    public Scoreboard getScoreboard() { return scoreboard; }
}