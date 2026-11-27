package domain;

/**
 * Balon del partido
 */
public class Ball extends Physics{
    private static final double RADIUS = 20; // Tamaño del balon
    private static final double RESTITUTION = 0.78;  //Fracción de velocidad que conserva al rebotar
    private static final double GROUND_FRICTION = 0.95;  //Factor por el que se frena su velocidad horizontal cada vez que toca el piso
    private Shot shot; //Tiro especial

    /**
     * Crear el balon en reposo en una posicion
     * @param startX Posicion inicial
     * @param startY Posicion inical
     */
    public Ball(double startX, double startY) {
        drag = 0.999;
        placeAt(startX, startY);
    }

    /**
     * Avanza el balon en segundos
     * @param dt  segudos que avanza el balon
     * @param field  cancha en donde se mueve el balon
     */
    public void update(double dt, Field field) {
        if (shot != null) shot.tick(dt, field);
        boolean ownMotion = shot != null && shot.isFlying() && shot.move(this, dt, field);
        if (!ownMotion) integrate(dt);
        collideWithField(field);
        if (shot != null && shot.isFlying() && touchesSideWall(field)) shot.endFlight();
        if (shot != null && shot.isFinished()) shot = null;
    }


    /**
     * Indica si el balon esta pegado a alguna de las paredes laterales
     * @param field cancha donde se mueve el balon
     * @return  Si toca la pared a al derecha o a la izquierda
     */
    private boolean touchesSideWall(Field field) {
        return x - RADIUS <= 0 || x + RADIUS >= field.getWidth();
    }

    /**
     * Hacer rebotar el balon contra los limites de la cancha y con los travesaños de los arcos
     * @param field cancha donde se mueve el balon
     */
    public void collideWithField(Field field) {
        Collisions.bounceOffBounds(this, field, RESTITUTION, GROUND_FRICTION);
        Collisions.bounceOffCrossbars(this, field, RESTITUTION);
    }

    /**
     * Rebote contra un circulo (Cabeza del personaje)
     * @param cx  posición horizontal del centro del círculo
     * @param cy  posición vertical del centro del círculo
     * @param cr  radio del círculo
     * @param cvx velocidad horizontal del círculo
     * @param cvy velocidad vertical del círculo
     * @return Si hubo contacto o no
     */
        public boolean collideWithCircle(double cx, double cy, double cr, double cvx, double cvy) {
        return Collisions.bounceOffCircle(this, RESTITUTION, cx, cy, cr, cvx, cvy);
    }

    /**
     * Devuelve a la poscion inical el balon (Se usa cuando hacen gol o inicia el partido)
     * @param newX
     * @param newY
     */
    public void reset(double newX, double newY) {
        placeAt(newX, newY);
        shot = null;
    }

    /**
     * Lanza el balón con un tiro especial.
     * @param newShot tiro que va a controlar el balon
     */
    public void launch(Shot newShot) {
        shot = newShot;
        shot.start(this);
    }

    /** Un personaje tocó el balón: si va en tiro especial, el tiro le pega.
     * @param who personaje que toco el balon
     */
    public void hitBy(Character who) {
        if (shot != null && shot.isFlying()) shot.hit(who);
    }

    /**
     *
     * @return el tiro especial en curso
     */
    public Shot getShot() {
        return shot;
    }

    /**
     *
     * @return el radio del balon
     */
    public double getRadius(){
        return RADIUS;
    }
}