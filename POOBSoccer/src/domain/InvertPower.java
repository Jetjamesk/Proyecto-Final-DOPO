package domain;

public class InvertPower implements Power{
    private double invertTimer = 0.0;   //Tiempo de inversion de controles
    private static final double DURATION = 6.0;

    @Override
    public void use(Character rival){
        rival.applyInvertControls(DURATION);
    }
    
    @Override
    public void use(Character character, Character rival, Ball ball){
        return;
    }
}

