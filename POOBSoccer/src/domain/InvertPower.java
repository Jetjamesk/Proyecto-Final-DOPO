package domain;

public class InvertPower implements Power{
    private static final double DURATION = 6.0;

    @Override
    public void use(Character rival){
        rival.applyInvertControls(DURATION);
    }
}
