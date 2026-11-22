package domain;

import java.util.List;

public class MatchPhysics implements java.io.Serializable {
    private final Field field;
    private final Ball ball;
    private final List<Character> characters;

    public MatchPhysics(Field field, Ball ball, List<Character> characters) {
        this.field = field;
        this.ball = ball;
        this.characters = characters;
    }


    public void step(double h) {
        for (Character c : characters) c.update(h, field);
        collideCharacters();
        ball.update(h, field);
        for (Character c : characters) {
            boolean bodyTouched = ball.collideWithCircle(c.getX(), c.getY(), c.getRadius(), c.getVx(), c.getVy());
            boolean footTouched = ball.collideWithCircle(c.getFootX(), c.getFootY(), c.getFootRadius(),
                    c.getFootVx(), c.getFootVy());
            if (footTouched && c.canLandKick()) c.kickTouchedBall();
            Shot current = ball.getShot();
            if (bodyTouched || footTouched) ball.hitBy(c);      // tiro especial contra el rival
            if (bodyTouched || footTouched && c.hasArmedShot() && !c.isStunned() && !c.isUnconscious() && (current == null || !current.isFlying())) {
                c.armShot(ball);
            }
        }
        resolveKicks();
    }

    private void collideCharacters() {
        /** Los personajes no se atraviesan: si se tocan, se separan y quedan frente a frente. */
            Character a = characters.get(0);
            Character b = characters.get(1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double min = a.getRadius() + b.getRadius();   // distancia mínima permitida
            double dist = Math.hypot(dx, dy);
            if (dist >= min) return;                      // no se tocan, no hay nada que hacer

            // Dirección de a hacia b
            double nx = (a.x <= b.x) ? 1 : -1;
            double ny = 0;
            if (dist > 0) { nx = dx / dist; ny = dy / dist; }

            // Cada uno retrocede la mitad de lo que se metió en el otro
            double push = (min - dist) / 2;
            a.x -= nx * push;   a.y -= ny * push;
            b.x += nx * push;   b.y += ny * push;
        }

        private void resolveKicks() {
            for (int i = 0; i < characters.size(); i++) {
                Character attacker = characters.get(i);
                Character rival = characters.get(1 - i);
                if (!attacker.canLandKick()) continue;
                double dist = Math.hypot(attacker.getFootX() - rival.getX(),
                                         attacker.getFootY() - rival.getY());
                if (dist < attacker.getFootRadius() + rival.getRadius()) {
                    attacker.landKick(rival);
                }
            }
        }

}