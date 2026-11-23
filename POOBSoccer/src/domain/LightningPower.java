package domain;

public class LightningPower extends ShotPower{
    
    @Override
    protected Shot createShot(Character user) {
        return new LightningShot(user);
    }
    @Override
    protected boolean firesOnTouch() { return true; }

}
