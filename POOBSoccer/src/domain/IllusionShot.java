package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estados Unidos - Ilusión: el balón se divide en 8 (1 real y 7 falsos).
 * Los falsos desaparecen cuando termina el efecto. Si el real toca al rival, lo confunde.
 */
public class IllusionShot extends Shot {
    public static final double SPEED = 900;
    public static final double DAMAGE = 6;
    public static final double DURATION = 2.5;

    private final List<Decoy> decoys = new ArrayList<>();
    private double effectTimer = DURATION;

    public IllusionShot(Character owner) { super(owner); }

    @Override public Type getType()     { return Type.ILLUSION; }
    @Override public double getDamage() { return DAMAGE; }

    @Override
    protected void start(Ball ball) {
        int dir = owner.getFacing();
        ball.placeAt(owner.getX() + dir * (owner.getRadius() + ball.getRadius() + 2), owner.getY());
        ball.vx = dir * SPEED;
        for (int k = -4; k <= 3; k++) {                 // 7 falsos: todos menos k = 0 (el real)
            if (k != 0) decoys.add(new Decoy(ball.getX(), ball.getY(), ball.vx, k * 110.0, ball.getRadius()));
        }
    }

    @Override
    protected boolean move(Ball ball, double dt, Field field) {
        ball.vy = 0;
        ball.x += ball.vx * dt;
        return true;
    }

    @Override
    protected void tick(double dt, Field field) {
        super.tick(dt, field);
        if (effectTimer > 0) {
            effectTimer = Math.max(0, effectTimer - dt);
            for (Decoy d : decoys) d.update(dt, field);
        }
    }

    @Override
    protected void applyEffect(Character rival) {
        rival.applyConfusion(DURATION);
        effectTimer = DURATION;                          // los falsos duran 2.5 s más desde el golpe
    }

    @Override
    public boolean isFinished() {
        return !isFlying() && effectTimer <= 0;
    }

    @Override
    public List<Decoy> getDecoys() {
        return effectTimer > 0 ? decoys : Collections.<Decoy>emptyList();
    }
}