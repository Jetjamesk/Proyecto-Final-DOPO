package domain;

public abstract class Character {
    private static final double GRAVITY = 2500.0;      
    private static final double RADIUS = 45;          // la cabeza
    private static final double GAP = 30;             // la cabeza flota sobre el suelo para poner el pie 
    private static final double FOOT_RADIUS = 18;     // El pie  
    private static final double LEG = 55;             // largo de la pierna (La conexion entre la cabeza y el pie)
    private static final double REST_ANGLE = 0.6;     // pie en reposo (Derecho no tiene ningun movimiento)
    private static final double KICK_ANGLE = 2.0;     // pie en lo más alto de la patada (adelante y arriba)

    private double x, y;                // centro de la cabeza
    private double vx, vy;      
    private double startX;
    private int direction;              // -1 izquierda, 0 quieto, 1 derecha
    private int facing = 1;             // hacia dónde mira: 1 derecha, -1 izquierda
    private boolean onGround;
    private double footX, footY;        // centro del pie
    private double footVx, footVy;      // velocidad del pie (sirve para el golpe)
    private double kickTimer;           // > 0 mientras la pierna está pateando
    private final double kickDuration;  // más fuerza = patada más rápida
    private int score;                  // MArcador que lleva el juagador
    private String country;             //Pais
    private int speed, jumpPower, strength;        //Valores del personaje
    private double footTilt;            

    public Character(String country, int speed, int jumpPower, int strength){
        this.country = country;
        this.speed = speed;
        this.jumpPower = jumpPower;
        this.strength = strength;
        this.kickDuration = 0.40 - 0.04 * strength;
        this.score = 0;
    }

    /** Orden de movimiento: -1 izquierda, 0 quieto, 1 derecha. */
    public void movement(int direction){
        this.direction = Integer.signum(direction);
    }

    /**
     * Salto del personaje
     */
    public void jump(){
        if (onGround) {
            vy = -(450 + 70 * jumpPower);
            onGround = false;
        }
    }

    /** Lanza la patada: el pie hace un arco hacia adelante y arriba. 
     * El golpe lo resuelve la colisión pie-balón. 
     */
    public void kickBall(){
        if (kickTimer <= 0) kickTimer = kickDuration;
    }

    /**
     *  Física del personaje: avanza dt segundos.
     */
    public void update(double dt, Field field){
        vx = direction * (180 + 60 * speed);
        vy += GRAVITY * dt;
        x += vx * dt;
        y += vy * dt;
        x = Math.max(RADIUS, Math.min(x, field.getWidth() - RADIUS));
        double floorCenter = field.getFloorY() - RADIUS - GAP;
        if (y >= floorCenter) {
            y = floorCenter;
            vy = 0;
            onGround = true;
        }
        if (kickTimer > 0) kickTimer = Math.max(0, kickTimer - dt);
        updateFoot(dt, field);
    }

    /** 
     * Coloca el pie según el avance de la patada y calcula su velocidad. 
    */
    private void updateFoot(double dt, Field field){
        double progress = kickTimer > 0 ? 1 - kickTimer / kickDuration : 0;   // 0 → 1
        double angle = REST_ANGLE + (KICK_ANGLE - REST_ANGLE) * Math.sin(Math.PI * progress);  // sube y vuelve
        footTilt = angle - REST_ANGLE;
        double newX = x + facing * LEG * Math.sin(angle);
        double newY = Math.min(getHipY() + LEG * Math.cos(angle), field.getFloorY());          // no baja del suelo
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

    /** 
     * Vuelve a su posición inicial (después de un gol o al empezar). 
    */
    public void reset(Field field){
        x = startX;
        y = field.getFloorY() - RADIUS - GAP;
        vx = 0;
        vy = 0;
        direction = 0;
        onGround = true;
        kickTimer = 0;
        updateFoot(0, field);
    }

    public void setStartX(double startX){
        this.startX = startX;
    }

    /** 1 = mira a la derecha, -1 = mira a la izquierda. */
    public void setFacing(int facing){
        this.facing = Integer.signum(facing) == 0 ? 1 : Integer.signum(facing);
    }

    /**
     * Es una clase abstracta o una interfaz ??
     */
    public abstract void usarPoder();

    public int getScore(){
        return score;
    }

    public String getCountry(){
        return country;
    }

    public int addScore(){
        return ++score;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getVx(){
        return vx;
    }

    public double getVy(){
        return vy;
    }

    public double getRadius(){
        return RADIUS;
    }

    public int getFacing(){
        return facing;
    }

    /** Altura de la cadera: donde la pierna se une a la cabeza. */
    public double getHipY(){
        return y + RADIUS - 5;
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

    public double getFootRadius(){
        return FOOT_RADIUS;
    }

    public double getFootTilt(){
        return footTilt;
    }

}