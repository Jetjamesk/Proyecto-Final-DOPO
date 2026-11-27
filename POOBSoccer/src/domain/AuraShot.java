package domain;

/**
 * Corea del Sur - Aura Azul: tiro recto y veloz que empuja al rival hacia su propio arco.
 */
public class AuraShot extends StraightShot {
    public static final double SPEED = 1500;  //Velocidad
    public static final double DAMAGE = 8;    //Daño
    public static final double PUSH_DURATION = 1.5;   //Segundos en los que el usuario es empujado hacia atras

    /**
     * Crea un tiro de AuraShot
     * @param owner El perosnaje que lanzo el tiro
     */
    public AuraShot(Character owner) {
        super(owner);
    }

    /**
     * Velocidad en la que sale el tiro
     * @return
     */
    @Override
    protected double speed(){
        return SPEED;
    }

    /**
     * Identifica el tiro para que el GUI lo usa para dibujar el efecto visual
     * @return
     */
    @Override
    public Type getType(){
        return Type.AURA;
    }

    /**
     * Vida que se le va a quitar al rival al golpearlo
     * @return
     */
    @Override
    public double getDamage(){
        return DAMAGE;
    }

    /**
     * Efecto extra del poder sobre el rival (Empujar hacia atras)
     * @param rival
     */
    @Override
    protected void applyEffect(Character rival) {
        rival.applyPush(-rival.getFacing(), PUSH_DURATION);   // su arco está a su espalda
    }
}