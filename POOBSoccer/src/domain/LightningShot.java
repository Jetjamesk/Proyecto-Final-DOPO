package domain;

/** Camerún - Rayo: tiro recto cubierto de electricidad; electrocuta al rival. */
public class LightningShot extends StraightShot {
    public static final double SPEED = 1700;
    public static final double DAMAGE = 12;
    public static final double ELECTRO_DURATION = 3.0;

    public LightningShot(Character owner) { super(owner); }

    @Override protected double speed()      { return SPEED; }
    @Override public Type getType()         { return Type.LIGHTNING; }
    @Override public double getDamage()     { return DAMAGE; }

    @Override
    protected void applyEffect(Character rival) {
        rival.applyElectrocution(ELECTRO_DURATION);
    }
}