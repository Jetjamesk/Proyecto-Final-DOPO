package domain;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public abstract class Shot implements Serializable {
    public enum Type {AURA, LIGHTNING, TORNADO, ILLUSION}

    protected static final double MAX_FLIGHT = 3.0;  //Segundo del vuelo
    protected final Character owner;
    protected double time;
    private boolean flying = true;

    protected Shot (Character owner){
        this.owner = owner;
    }

    public abstract Type getType();

    /**
     * Vida que le quita el tiro
     */
    public abstract double getDamage();

    /**
     * Efecto que recibe el jugador
     */
    protected abstract void applyEffect(Character rival);

    /**
     *
     */
    protected abstract void start (Ball ball);

    /**
     *
     */
    protected boolean move (Ball ball, double dt, Field field){
        return false;
    }

    protected void tick(double dt, Field field) {
        time += dt;
        if (flying && time >= MAX_FLIGHT) flying = false;
    }

    /** El balón tocó a un personaje. El dueño del tiro no cuenta. */
    public void hit(Character who) {
        if (!flying || who == owner) return;
        who.takeDamage(getDamage());
        applyEffect(who);
        flying = false;
    }

    public void endFlight()        { flying = false; }
    public boolean isFlying()      { return flying; }
    public boolean isFinished()    { return !flying; }
    public Character getOwner()    { return owner; }

    /** Balones falsos (solo la Ilusión tiene). */
    public List<Decoy> getDecoys() {
        return Collections.emptyList();
    }
}
