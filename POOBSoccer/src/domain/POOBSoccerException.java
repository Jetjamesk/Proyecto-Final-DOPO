package domain;

public class POOBSoccerException extends Exception {
    public final static String IN_PROGRESS = "En proceso";
    public final static String FILE_NULL = "El archivo esta vacio";
    public final static String WRONG_EXTENSION = "La extension es incorrecta verificar";
    public final static String FILE_NOT_FOUND = "El archivo no fue encontrado";
    public final static String FILE_EMPTY = "El archivo esta vacio";
    public final static String NOT_POOBSccoer_FILE = "El archvio no es un arhcivo sobre el juego";
    public final static String CORRUPT_DATA = "El archvio esta corrupto";
    public POOBSoccerException(String message) {
        super(message);
    }
}
