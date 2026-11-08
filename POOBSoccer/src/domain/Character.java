package domain;

public abstract class Character {

    //Valores base para el encogimiento
    private static final double BASE_RADIUS = 45;       // Cabeza
    private static final double BASE_GAP = 30;          // Separación del suelo
    private static final double BASE_FOOT_RADIUS = 18;  // Pie (hitbox)
    private static final double BASE_LEG = 55;          // Pierna

    
    private static final double GRAVITY = 2500.0;      
    private static final double RADIUS = 45;          // la cabeza
    private static final double GAP = 30;             // la cabeza flota sobre el suelo para poner el pie 
    private static final double LEG = 55;             // largo de la pierna (La conexion entre la cabeza y el pie)
    private static final double REST_ANGLE = 0.6;     // pie en reposo (Derecho no tiene ningun movimiento)
    private static final double KICK_ANGLE = 2.0;   // pie en lo más alto de la patada (adelante y arriba)
    private double headScale = 1.0;


    //medidas para el super y efectos
    public static final double SUPER_COOLDOWN = 10.0; // Tiempo de cooldown del super
    private double superTimer = 20.0;   //Tiempo de activacion del super
    private double invertTimer = 0.0;   //Tiempo de inversion de controles
    private double shrinkTimer = 0.0;   //Tiempo de encogimiento del personaje
    private double scale = 1.0;         // Escala del personaje(actual)

    //Movimiento de pies y cabeza
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
        int dir = Integer.signum(direction);
        if (invertTimer > 0){
            dir = -dir;
        }
        this.direction = dir;
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

    public void activateSuper(Character rival) {
        if (isSuperReady()) {
            superTimer = 0.0;
            usarPoder(rival);
        }
    }
    /**
     * Analiza si el personaje puede aplicar o no el super
     * @return si el tiempo del super ya paso o no
     */
    public boolean isSuperReady() {
        return superTimer >= SUPER_COOLDOWN;
    }

    /**
     * Invierte los controles del rival por una duraccion dada  
     * 
     */ 
    public void applyInvertControls(double duration) {
        this.invertTimer = duration;
    }

    /**
     * Aplica el efecto de encogimiento
     * @param factor Por cuanto se disminuira o aumentara el tamaño del personaje
     * @param duration Cuanto durara este efecto
     */
    public void applyShrink(double factor, double duration) {
        this.scale = factor;        // Por ejemplo 0.5 para reducir a la mitad
        this.shrinkTimer = duration;
    }
    


    /**
     *  Física del personaje: avanza dt segundos.
     */
    public void update(double dt, Field field) {
        // 1. Temporizador del encogimiento
        if (shrinkTimer > 0) {
            shrinkTimer = Math.max(0, shrinkTimer - dt);
            if (shrinkTimer == 0) {
                scale = 1.0; // Vuelve a su tamaño normal
            }
        }
        if (invertTimer > 0) {
            invertTimer = Math.max(0, invertTimer - dt);
        }
        
    
        // 2. Física adaptada al tamaño actual
        vx = direction * (180 + 60 * speed);
        vy += GRAVITY * dt;
        x += vx * dt;
        y += vy * dt;
    
        double r = getRadius();
        x = Math.max(r, Math.min(x, field.getWidth() - r));
    
        // Altura del suelo ajustada al tamaño
        double floorCenter = field.getFloorY() - r - (BASE_GAP * scale);
        if (y >= floorCenter) {
            y = floorCenter;
            vy = 0;
            onGround = true;
        }
    
        if (kickTimer > 0) kickTimer = Math.max(0, kickTimer - dt);
        updateFoot(dt, field);
        if (superTimer < SUPER_COOLDOWN) {
            superTimer = Math.min(SUPER_COOLDOWN, superTimer + dt);
        }
    }

    /** 
     * Coloca el pie según el avance de la patada y calcula su velocidad. 
    */
    private void updateFoot(double dt, Field field) {
        double progress = kickTimer > 0 ? 1 - kickTimer / kickDuration : 0;
        double angle = REST_ANGLE + (KICK_ANGLE - REST_ANGLE) * Math.sin(Math.PI * progress);
        footTilt = angle - REST_ANGLE;
    
        double currentLeg = BASE_LEG * scale;
        double newX = x + facing * currentLeg * Math.sin(angle);
        double newY = Math.min(getHipY() + currentLeg * Math.cos(angle), field.getFloorY());
    
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
        invertTimer = 0.0;
        scale = 1.0;
        shrinkTimer = 0.0;
        x = startX;
        y = field.getFloorY() - RADIUS - GAP;
        vx = 0;
        vy = 0;
        direction = 0;
        onGround = true;
        kickTimer = 0;
        headScale = 1.0;
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
    public abstract void usarPoder(Character rival);

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

    public int getFacing(){
        return facing;
    }

    /** Altura de la cadera: donde la pierna se une a la cabeza. */
    public double getHipY() {
        return y + getRadius() - (5 * scale);
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

    public double getFootRadius() {
        return BASE_FOOT_RADIUS * scale;
    }
    


    public double getFootTilt(){
        return footTilt;
    }
    public double getScale() {
        return scale;
    }
    

    public double getRadius() {
        return BASE_RADIUS * scale;
    }
    

    public double getSuperTimer(){
        return superTimer;
    }

    public boolean isInverted() { 
        return invertTimer > 0; 
    }

    public boolean isShrink(){
        return shrinkTimer > 0;
    }
}