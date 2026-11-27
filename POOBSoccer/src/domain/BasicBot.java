package domain;

public class BasicBot implements Bot {
    private static final double KICK_SPOT = 70; // El balon debe estar a 70px delante de su cabeza
    private static final double KICK_REACH = 100; //Distacia para patear
    private static final double HEAD_REACH = 60; //Distacia para saltar a cabecear

    private final double reactionTime; //Cuantos segundos peinsa
    private double timer = 0;       // Cuenta regresiva para la proxima desicion

    /**
     * Crear el bot basico
     * @param reactionTime
     */
    public BasicBot (double reactionTime) {
        this.reactionTime = reactionTime;
    }

    /**
     * Decide que personaje en este instante
     * @param match El partido
     * @param me   Del juagdor 0 si es izquierda y 1 derecha
     * @param dt   segundos desde la ultima vez que se llamo
     */
    @Override
    public void Decide (Match match, int me, double dt){
        timer -= dt;
        if (timer > 0) return;                      // todavía no reacciona
        timer = reactionTime;

        Character self = match.getPlayer(me);
        Ball ball = match.getBall();
        int attack = self.getFacing();              // 1 = ataca a la derecha, -1 = a la izquierda

        // 1) Moverse: quedar detrás del balón para poder empujarlo hacia el arco rival
        double target = ball.getX() - attack * KICK_SPOT;
        double dx = target - self.getX();
        if (Math.abs(dx) > 15) {
            match.move(me, dx > 0 ? 1 : -1);
        } else {
            match.move(me, 0);
        }

        // 2) Patear si el balón está delante y a su alcance
        double ahead = (ball.getX() - self.getX()) * attack;     // > 0: el balón está delante
        double dist = Math.hypot(ball.getX() - self.getX(), ball.getY() - self.getY());
        if (ahead > 0 && dist <= KICK_REACH) {
            match.kick(me);
        }

        // 3) Saltar si el balón viene por encima de su cabeza
        if (ball.getY() < self.getY() - 20 && Math.abs(ball.getX() - self.getX()) < HEAD_REACH) {
            match.jump(me);
        }

        // 4) Usar el poder apenas esté listo
        if (self.isSuperReady()) {
            match.usePower(me);
        }

    }
}
