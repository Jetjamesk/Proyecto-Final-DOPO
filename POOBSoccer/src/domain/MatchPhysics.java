package domain;

import java.util.List;

public class MatchPhysics {
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
        ball.update(h, field);
        for (Character c : characters) {
            ball.collideWithCircle(c.getX(), c.getY(), c.getRadius(), c.getVx(), c.getVy());
            ball.collideWithCircle(c.getFootX(), c.getFootY(), c.getFootRadius(),
                                   c.getFootVx(), c.getFootVy());
        }
        ball.collideWithField(field);
    }
}