package domain;

public class ShrinkPower implements Power{
    private static final double FACTOR = 0.5;
    private static final double DURATION = 6.0;

    @Override
    public void use(Character rival){
        rival.applyShrink(FACTOR,DURATION);
    }
}
