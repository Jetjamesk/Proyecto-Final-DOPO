package domain;

public class TornadoPower extends ShotPower{
    @Override
    protected Shot createShot(Character user) {
        return new TornadoShot(user);
    }
}
