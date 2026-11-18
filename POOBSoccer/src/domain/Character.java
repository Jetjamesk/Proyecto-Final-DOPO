package domain;

public abstract class Character extends Physics {

    //Valores base para el encogimiento
    private static final double BASE_GAP = 30;          // Separación del suelo 
    private static final double RADIUS = 35;          // la cabeza CAMBIAR 
    private static final double GAP = 30;             // la cabeza flota sobre el suelo para poner el pie 
    private double headScale = 1.0;

    //medidas para el super y efectos
    public static final double SUPER_COOLDOWN = 10.0; // Tiempo de cooldown del super
    private double superTimer = 20.0;   //Tiempo de activacion del super
    private double invertTimer = 0.0;   //Tiempo de inversion de controles
    private double shrinkTimer = 0.0;   //Tiempo de encogimiento del personaje
    private double scale = 1.0;         // Escala del personaje(actual)

    //Movimiento de pies y cabeza

    private LegPhysics leg;
    private double startX;
    private int direction;              // -1 izquierda, 0 quieto, 1 derecha
    private int facing = 1;             // hacia dónde mira: 1 derecha, -1 izquierda
    private boolean onGround;

    
    private String country;             //Pais
    private int speed, jumpPower, strength;        //Valores del personaje            

    public Character(String country, int speed, int jumpPower, int strength){
        this.country = country;
        this.speed = speed;
        this.jumpPower = jumpPower;
        this.strength = strength;
        this.leg = new LegPhysics(0.40 - 0.04 * strength);
    
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
        leg.kick();
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
        // 1. Temporizadores de los efectos
        if (shrinkTimer > 0) {
            shrinkTimer = Math.max(0, shrinkTimer - dt);
            if (shrinkTimer == 0) {
                scale = 1.0; // Vuelve a su tamaño normal
            }
        }
        if (invertTimer > 0) {
            invertTimer = Math.max(0, invertTimer - dt);
        }

        // 2. Física: gravedad y movimiento heredados de Physics
        vx = direction * (180 + 60 * speed);
        integrate(dt);

        double r = getRadius();
        x = Math.max(r, Math.min(x, field.getWidth() - r));
        double floorCenter = field.getFloorY() - r - (BASE_GAP * scale);
        if (y >= floorCenter) { y = floorCenter; vy = 0; onGround = true; }

        // 3. Patada, pie y recarga del super
        leg.update(dt, x, getHipY(), facing, scale, field.getFloorY());
        if (superTimer < SUPER_COOLDOWN) {
            superTimer = Math.min(SUPER_COOLDOWN, superTimer + dt);
        }
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
        headScale = 1.0;
        leg.reset();
        leg.update(0, x, getHipY(), facing, scale, field.getFloorY());
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


    public String getCountry(){
        return country;
    }


    public int getFacing(){
        return facing;
    }

    /** Altura de la cadera: donde la pierna se une a la cabeza. */
    public double getHipY() {
        return y + getRadius() - (5 * scale);
    }

    public double getScale() {
        return scale;
    }
    

    public double getRadius() {
        return RADIUS * scale;
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
    public double getFootX(){
         return leg.getFootX(); 
    }

    public double getFootY(){
         return leg.getFootY(); 
    }

    public double getFootVx(){
         return leg.getFootVx(); 
    }

    public double getFootVy(){
         return leg.getFootVy(); 
    }

    public double getFootTilt(){
         return leg.getFootTilt(); 
    }

    public double getFootRadius(){
         return leg.getFootRadius(scale); 
    }
}