package domain;

public class Canada extends Character {
    public Canada(){
        super("Canada");

    }

    @Override
    public void usarPoder(Character rival){
        rival.applyShrink(0.5,6.0);
    }
}
