package domain;

/** Corea del Sur - Aura Azul: tiro recto y veloz que empuja al rival hacia su propio arco. */
public class AuraShot extends StraightShot {
    public static final double SPEED = 1500;
    public static final double DAMAGE = 8;
    public static final double PUSH_DURATION = 1.5;

    public AuraShot(Character owner) { super(owner); }

    @Override protected double speed()      { return SPEED; }
    @Override public Type getType()         { return Type.AURA; }
    @Override public double getDamage()     { return DAMAGE; }

    @Override
    protected void applyEffect(Character rival) {
        rival.applyPush(-rival.getFacing(), PUSH_DURATION);   // su arco está a su espalda
    }
}