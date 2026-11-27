package domain;

public class InvertPower implements Power{
    private static final double DURATION = 6.0; //Tiempo de inversion de controles

    @Override
    public void use(Character rival){
        rival.applyInvertControls(DURATION);
    }
    
    @Override
    public void use(Character character, Character rival, Ball ball){
        use(rival);
    }
}

