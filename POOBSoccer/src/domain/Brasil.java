package domain;

public class Brasil extends Character {

    public Brasil(){
        super("Brasil", 4, 4, 3);   // velocidad, salto, fuerza
    }

    @Override
    public void usarPoder(Character rival){
        rival.applyInvertControls(6);
    }
}