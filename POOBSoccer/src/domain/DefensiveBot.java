package domain;

public class DefensiveBot implements Bot {
    private static final double GUARD_DISTANCE = 150; 
    private static final double KICK_SPOT = 70;
    private static final double KICK_REACH = 100;
    private static final double HEAD_REACH = 60;

    private final double reactionTime;
    private double timer = 0;

    public DefensiveBot(double reactionTime) {
        this.reactionTime = reactionTime;
    }

    @Override
    public void decide(Match match, int me, double dt) {
        timer -= dt;
        if (timer > 0) {return;}
        timer = reactionTime;

        Character self = match.getPlayer(me);
        Ball ball = match.getBall();
        int attack = self.getFacing();
        double width = match.getField().getWidth();
        double ownGoalX;
        if (attack == 1) {
            ownGoalX = 0;
        } else {
            ownGoalX = width;
        }
        boolean ballInMyHalf = Math.abs(ball.getX() - ownGoalX) < width / 2;
        double target = ballInMyHalf
                ? ball.getX() - attack * KICK_SPOT        
                : ownGoalX + attack * GUARD_DISTANCE;     

        double dx = target - self.getX();
        match.move(me, Math.abs(dx) > 15 ? (dx > 0 ? 1 : -1) : 0);

        double ahead = (ball.getX() - self.getX()) * attack;
        double dist = Math.hypot(ball.getX() - self.getX(), ball.getY() - self.getY());
        if (ahead > 0 && dist <= KICK_REACH) {match.kick(me);}
        if (ball.getY() < self.getY() - 20 && Math.abs(ball.getX() - self.getX()) < HEAD_REACH) {
            match.jump(me);
        }

        if (self.isSuperReady()) {match.usePower(me);}
    }
}
