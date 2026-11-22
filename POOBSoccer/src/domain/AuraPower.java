package domain;

public class AuraPower extends ShotPower {
    @Override
    protected Shot createShot(Character user) {
        return new AuraShot(user);
    }
}