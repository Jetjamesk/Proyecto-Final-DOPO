package domain;

public interface Power extends java.io.Serializable {
    public void use (Character rival);
    public void use(Character user, Character rival, Ball ball);
}
