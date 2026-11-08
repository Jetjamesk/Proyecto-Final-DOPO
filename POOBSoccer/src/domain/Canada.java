package domain;

public class Canada extends Character {
    public Canada(){
        super("Canada", 3,3,4);

    }

    @Override
    public void usarPoder(Character rival){
        rival.applyShrink(0.5,6.0);
    }
}
