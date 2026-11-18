package domain;
import java.util.ArrayList;

public interface Match {
    public static final double STEP = 1.0 / 120.0;   // paso máximo de la física (segundos)

    public boolean isRunning;
    public boolean isPaused;

    public final Scoreboard scoreboard = new Scoreboard();
    public Character winner;
    public ArrayList<Character> jugadores;
    public Field field;
    public Ball ball;
    public MatchPhysics world;

    /** Avanza el partido dt segundos. Lo llama la GUI en cada tick de su Timer. */
    public void updateMatch(double dt);


    /**
     * Verifica los goles hayan sido hechos
     */
    public void checkGoal();

    /**
     * usar el poder sobre el rival
     * @param player que jugador usara el poder. 
     */
    public default void usePower(int player){
        if (isRunning && !isPaused) {
            Character user = jugadores.get(player);
            Character rival = jugadores.get(1 - player);
            user.activateSuper(rival);
        }
    }

    
    public void endMatch();
    

    /**
     * Reinicia a lso juagdores y el balon a la posicion inicial.
     */
    public default void resetRound(){
        for (Character p : jugadores){
            p.reset(field);
        } 
        ball.reset(field.getWidth() / 2.0, field.getHeight() * 0.25);
    }

    /**
     * Ordenes que los jugadores pueden tener
     * 
     */
    public default void move(int player, int direction){
        if (isRunning && !isPaused) jugadores.get(player).movement(direction);
    }

    public default void jump(int player){
        if (isRunning && !isPaused) jugadores.get(player).jump();
    }

    public default void kick(int player){
        if (isRunning && !isPaused) {
            jugadores.get(player).kickBall();
        }
    }

    public default void pause(){
        if (isRunning) {
            isPaused = true;
        }    
    }

    public default void resume(){
        isPaused = false;
    }

    public default boolean isRunning()   { return isRunning; }
    public default boolean isPaused()    { return isPaused; }
    public default Character getWinner() { return winner; }
    public default Character getPlayer(int i){ return jugadores.get(i); }
    public default Ball getBall()        { return ball; }
    public default Field getField()      { return field; }
    public default Scoreboard getScoreboard() { return scoreboard; }
}