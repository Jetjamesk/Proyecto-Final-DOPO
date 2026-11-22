package domain;

public class IllusionPower extends ShotPower{
    @Override
    protected Shot createShot(Character user) {
        return new IllusionShot(user);
    }

    @Override
    protected boolean firesOnTouch() { return true; }

}
