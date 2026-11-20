package domain;

public interface Bot {
    /**
     * Decide que va a hacer el personaje.
     * @param match El partido
     * @param me   Del juagdor 0 si es izquierda y 1 derecha
     * @param dt   segundos desde la ultima vez que se llamo
     */
    public void Decide(Match match,int me, double dt);
}
