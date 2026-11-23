package domain;

public class AuraPower extends ShotPower {
    @Override
    protected Shot createShot(Character user) {
        return new AuraShot(user);
    }

    @Override
    protected boolean firesOnTouch() { return true; }
}