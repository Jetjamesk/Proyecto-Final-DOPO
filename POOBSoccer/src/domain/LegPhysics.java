package domain;

public class LegPhysics implements java.io.Serializable{
    private static final double REST_ANGLE = 0.6;   // pie en reposo
    private static final double KICK_ANGLE = 2.0;   // pie en lo más alto de la patada
    private static final double BASE_LEG = 55;      // largo de la pierna
    private static final double BASE_FOOT_RADIUS = 18;

    private static final double KICK_DURATION = 0.25;  // más fuerza = patada más rápida
    private double kickTimer;           // > 0 mientras está pateando
    private boolean hit;
    private double footX, 
                   footY;
    private double footVx, 
                   footVy;
    private double footTilt;


    /** Empieza la patada si no está pateando ya. */
    public void kick() {
        if (kickTimer <= 0) {
            kickTimer = KICK_DURATION;
            hit= false;
        }
    }

    /** Detiene la patada (al reiniciar la ronda). */
    public void reset() {
        kickTimer = 0;
        hit = false;
    }

    /** Avanza la patada y coloca el pie según dónde está el cuerpo. */
    public void update(double dt, double hipX, double hipY, int facing, double scale, double floorY) {
        if (kickTimer > 0) kickTimer = Math.max(0, kickTimer - dt);

        double progress = kickTimer > 0 ? 1 - kickTimer / KICK_DURATION : 0;
        double angle = REST_ANGLE + (KICK_ANGLE - REST_ANGLE) * Math.sin(Math.PI * progress);
        footTilt = angle - REST_ANGLE;

        double leg = BASE_LEG * scale;
        double newX = hipX + facing * leg * Math.sin(angle);
        double newY = Math.min(hipY + leg * Math.cos(angle), floorY);

        if (dt > 0) {
            footVx = (newX - footX) / dt;
            footVy = (newY - footY) / dt;
        } else {
            footVx = 0;
            footVy = 0;
        }
        footX = newX;
        footY = newY;
    }

    public double getFootX(){
        return footX; 
    }
    public double getFootY(){
        return footY; 

    }
    public double getFootVx(){
        return footVx; 
    }
    public double getFootVy(){
        return footVy;
    }
    public double getFootTilt(){
        return footTilt; 
    }
    public double getFootRadius(double scale){
        return BASE_FOOT_RADIUS * scale; 
    }

    public boolean isKicking() { return kickTimer > 0; }
    public boolean hasHit()    { return hit; }
    public void markHit()      { hit = true; }
}
