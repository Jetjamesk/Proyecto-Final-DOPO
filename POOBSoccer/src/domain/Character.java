package domain;

public abstract class Character extends Physics {
    //Valores base para el encogimiento
    private static final double BASE_GAP = 30;          // Separación del suelo
    private static final double RADIUS = 35;          // la cabeza CAMBIAR
    private static final double GAP = 30;             // la cabeza flota sobre el suelo para poner el pie
    
    public static final double STUN_DURATION= 3.0;
    private double stunTimer = 0.0;

    //medidas para el super y efectos
    public static final double SUPER_COOLDOWN = 10.0; // Tiempo de cooldown del super
    private double superTimer = 0.0;   //Tiempo de activacion del super
    private double invertTimer = 0.0;   //Tiempo de inversion de controles
    private double shrinkTimer = 0.0;   //Tiempo de encogimiento del personaje
    private double scale = 1.0;         // Escala del personaje(actual)

    //Movimiento de pies y cabeza

    private LegPhysics leg;
    private double startX;
    private int direction;              // -1 izquierda, 0 quieto, 1 derecha
    private int facing = 1;             // hacia dónde mira: 1 derecha, -1 izquierda
    private boolean onGround;

    //Efectos de los poderes
    public static final double PUSH_SPEED = 800.0;            // velocidad al ser empujado
    public static final double ELECTRO_SPEED_FACTOR = 0.5;    // electrocutado: 50 % de velocidad
    public static final double KNOCKOUT_LAUNCH = 900.0;       // impulso hacia arriba al quedar inconsciente
    private double pushTimer = 0.0;
    private int pushDirection = 0;
    private double electroTimer = 0.0;
    private double unconsciousTimer = 0.0;
    private double confusedTimer = 0.0;

    
    private String country;             //Pais
    private final CharacterStats stats = new CharacterStats();
    private double health;
    private final Power power;
    public Shot ArmedShot= null;

    public Character(String country, Power power){
        this.country = country;
        this.leg = new LegPhysics();
        this.health = stats.maxHealth();
        this.power = power;
    
    }

    /** Orden de movimiento: -1 izquierda, 0 quieto, 1 derecha. */
    public void movement(int direction){
        int dir = Integer.signum(direction);
        if (invertTimer > 0){
            dir = -dir;
        }
        this.direction = dir;
    }

    public void jump(){
        if (onGround && !isStunned() && !isUnconscious() && !isElectrocuted()) {
            vy = -stats.jumpVelocity();
            onGround = false;
        }
    }

    public void kickBall(){
        if (!isStunned() && !isUnconscious()){
            leg.kick();
        }
    }

    public void kickTouchedBall(){
        leg.markHit();
    }
    

    public void landKick(Character rival){
        rival.takeDamage(stats.kickDamage());
        leg.markHit();
    }

        /**
     * Recibe daño; la vida nunca baja de 0. Al llegar a 0 queda aturdido.
     * Mientras está aturdido no recibe más daño.
     */
    public void takeDamage(double amount){
        if (amount <= 0 || isStunned()) {return;}
        health = Math.max(0, health - amount);
        if (health == 0) {stunTimer = STUN_DURATION;}
    }

    
    public void restoreHealth(){
        health = stats.maxHealth();
        stunTimer = 0.0;
    }

    /** Versión que también entrega el balón, para los poderes que lo lanzan. */
    public void activateSuper(Character rival, Ball ball) {
        if (isSuperReady() && !isUnconscious()) {
            superTimer = 0.0;
            power.use(this, rival, ball);
        }
    }

    /** Empujado hacia una dirección (-1 izquierda, 1 derecha) durante duration segundos. */
    public void applyPush(int dir, double duration) {
        pushDirection = Integer.signum(dir);
        pushTimer = duration;
    }

    /** Electrocutado: no salta y se mueve más lento. */
    public void applyElectrocution(double duration) {
        electroTimer = duration;
    }

    /** Inconsciente: sale disparado al aire y no puede moverse, saltar ni patear. */
    public void applyUnconscious(double duration) {
        unconsciousTimer = duration;
        vy = -KNOCKOUT_LAUNCH;
        onGround = false;
    }

    /** Confundido: no sabe cuál es el balón real (efecto de la Ilusión). */
    public void applyConfusion(double duration) {
        confusedTimer = duration;
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
        pushTimer = Math.max(0, pushTimer - dt);
        electroTimer = Math.max(0, electroTimer - dt);
        unconsciousTimer = Math.max(0, unconsciousTimer - dt);
        confusedTimer = Math.max(0, confusedTimer - dt);
        if (stunTimer > 0){
            stunTimer = Math.max(0,stunTimer - dt);
            if(stunTimer == 0) {
                restoreHealth();
            }
        }
        // 2. Física: gravedad y movimiento heredados de Physics
        if (isStunned() || isUnconscious()) {
            vx = 0;
        } else if (isPushed()) {
            vx = pushDirection * PUSH_SPEED;
        } else {
            vx = direction * stats.moveSpeed() * (isElectrocuted() ? ELECTRO_SPEED_FACTOR : 1.0);
        }
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
        pushTimer = 0.0;
        electroTimer = 0.0;
        unconsciousTimer = 0.0;
        confusedTimer = 0.0;
        x = startX;
        y = field.getFloorY() - RADIUS - GAP;
        vx = 0;
        vy = 0;
        direction = 0;
        onGround = true;
        leg.reset();
        leg.update(0, x, getHipY(), facing, scale, field.getFloorY());
        ArmedShot = null;
    }
    public void newShot(Shot newShot){
        ArmedShot = newShot;
    }
    public void armShot(Ball ball){
        if (ArmedShot == null){return;}
        ball.launch(ArmedShot);
        ArmedShot = null;
    }

    public boolean hasArmedShot(){
        return ArmedShot!=null;
    }

    public void superReset(){
        superTimer = 0.0;
    }

    public void setStartX(double startX){
        this.startX = startX;
    }

    /** 1 = mira a la derecha, -1 = mira a la izquierda. */
    public void setFacing(int facing){
        this.facing = Integer.signum(facing) == 0 ? 1 : Integer.signum(facing);
    }



    public String getCountry(){
        return country;
    }

    public boolean canLandKick(){
        return leg.isKicking() && !leg.hasHit();
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

    public boolean isPushed()       { return pushTimer > 0; }

    public boolean isElectrocuted() { return electroTimer > 0; }

    public boolean isUnconscious()  { return unconsciousTimer > 0; }

    public boolean isConfused()     { return confusedTimer > 0; }

    public int getPushDirection()   { return pushDirection; }

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

    public boolean isStunned(){
        return stunTimer > 0;
    }

    /** Atributos efectivos del personaje (para mostrarlos en la GUI, por ejemplo). */
    public CharacterStats getStats(){
        return stats;
    }

    public void setPlayerStats(PlayerStats playerStats){
        stats.setProgress(playerStats);
    }

    public PlayerStats getPlayerStats(){
        return stats.getProgress();
    }

    public double getHealth()    { return health; }
    public double getMaxHealth() { return stats.maxHealth(); }
    public double getStunTimer() { return stunTimer; }


}