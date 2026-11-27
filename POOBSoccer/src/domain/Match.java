package domain;
import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public abstract class Match implements Serializable {
    public static final String EXTENSION = ".poob";
    private static final String HEADER = "POOBSoccer-";
    public static final double STEP = 1.0 / 120.0;   // paso máximo de la física (segundos)

    protected boolean isRunning;
    protected boolean isPaused;

    protected final Scoreboard scoreboard = new Scoreboard();
    protected Character winner;
    protected final ArrayList<Character> jugadores = new ArrayList<>();
    protected final Field field;
    protected final Ball ball;
    protected final MatchPhysics world;
    protected final Bot[] bots = new Bot[2];

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
        p1.restoreHealth();
        p2.restoreHealth();
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
            user.activateSuper(rival,ball);
        }
    }

    
    public void endMatch(){
        if (!isRunning) {return;}
        isRunning = false;
        int leader = scoreboard.leader();
        winner = leader < 0 ? null : jugadores.get(leader);
        awardPoints();
        
    }
    
    /** Informa el resultado a cada jugador; PlayerStats decide cuántos puntos da. */
    private void awardPoints(){
        for (Character p : jugadores) {
            if (winner.equals(p)){
                p.getPlayerStats().rewardMatch(true);
            }
            
        }
    }

    /**
     * Reinicia a lso juagdores y el balon a la posicion inicial.
     */
    public void resetRound(){
        for (Character p : jugadores){
            p.reset(field);
            p.restoreHealth();
            
        } 
        ball.reset(field.getWidth() / 2.0, field.getHeight() * 0.25);
    }

    /**
     * Ordenes que los jugadores pueden tener
     * 
     */
    public  void move(int player, int direction){
        if (isRunning && !isPaused) {jugadores.get(player).movement(direction);}
    }

    public void jump(int player){
        if (isRunning && !isPaused) {jugadores.get(player).jump();}
    }

    public void kick(int player){
        if (isRunning && !isPaused) {
            jugadores.get(player).kickBall();
        }
    }

    public void setBot(int player, Bot bot){
        bots[player] = bot;
    }

    public boolean isBot(int player){
        return bots[player] != null;
    }

    public void pause(){
        if (isRunning) {
            isPaused = true;
        }    
    }

    public void resume(){
        isPaused = false;
    }

    /**
     * Se encarga de guardar el archivo .poob
     * @param file
     * @throws POOBSoccerException
     */
    public void save(File file) throws POOBSoccerException {
        if (file == null) {
            throw new POOBSoccerException(POOBSoccerException.FILE_NULL);
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(EXTENSION)) {
            throw new POOBSoccerException(POOBSoccerException.WRONG_EXTENSION);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeUTF(HEADER);
            out.writeObject(this);
        } catch (IOException e) {
            throw new POOBSoccerException("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    /**
     * El encargado de abir el archivo .poob
     * @param file
     * @return
     * @throws POOBSoccerException
     */
    public static Match open(File file) throws POOBSoccerException {
        if (file == null) {
            throw new POOBSoccerException(POOBSoccerException.FILE_NULL);
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(EXTENSION)) {
            throw new POOBSoccerException(POOBSoccerException.WRONG_EXTENSION);
        }
        if (!file.isFile()) {
            throw new POOBSoccerException(POOBSoccerException.FILE_NOT_FOUND);
        }
        if (file.length() == 0) {
            throw new POOBSoccerException(POOBSoccerException.FILE_EMPTY);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            if (!HEADER.equals(in.readUTF())) {
                throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
            }
            Object obj = in.readObject();
            if (!(obj instanceof Match)) {
                throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
            }
            return (Match) obj;
        } catch (StreamCorruptedException e) {
            throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
        } catch (FileNotFoundException e) {
            throw new POOBSoccerException(POOBSoccerException.FILE_NOT_FOUND);
        } catch (IOException | ClassNotFoundException e) {
            throw new POOBSoccerException(POOBSoccerException.CORRUPT_DATA);
        }
    }

    protected void updateBots(double dt) {
        if (!isRunning || isPaused) {return;}
        for (int i = 0; i < bots.length; i++) {
            if (bots[i] != null) {bots[i].decide(this, i, dt);}
        }
    }

    public boolean isRunning()   { return isRunning; }
    public boolean isPaused()    { return isPaused; }
    public Character getWinner() { return winner; }
    public Character getPlayer(int i){ return jugadores.get(i); }
    public Ball getBall()        { return ball; }
    public Field getField()      { return field; }
    public Scoreboard getScoreboard() { return scoreboard; }
    public abstract boolean isGoldenGoal();
    public abstract double getTimeLeft();
}