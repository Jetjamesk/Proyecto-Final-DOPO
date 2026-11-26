package domain;

public interface Power extends java.io.Serializable {
    void use (Character rival);
    void use(Character user, Character rival, Ball ball);
}
