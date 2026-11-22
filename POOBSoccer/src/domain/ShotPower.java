package domain;

/** Poder cuyo efecto es lanzar el balón con un tiro especial. */
public abstract class ShotPower implements Power {

    protected abstract Shot createShot(Character user);

    /** Un tiro no hace nada sin el balón. */
    @Override
    public void use(Character rival){}

    @Override
    public void use(Character user, Character rival, Ball ball) {
        ball.launch(createShot(user));
    }
}