package domain;

import java.io.*;

/**
 * Progreso de un jugador: sus puntos y el nivel de cada estadística.
 * Aplica a todos los personajes que use el jugador y se conserva de un partido a otro.
 */
public class PlayerStats implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final String EXTENSION = ".stats";
    private static final String HEADER = "POOBSoccer-Stats-";

    public enum Stat { SPEED, JUMP, KICK, SURVIVAL }

    public static final int MIN_LEVEL = 1;              // todas las estadísticas empiezan en 1
    public static final int MAX_LEVEL = 10;             // y pueden subir hasta 10
    public static final int BASE_UPGRADE_COST = 500;    // precio de la primera mejora; luego se duplica
    public static final int WIN_POINTS = 100;           // puntos para el ganador de un partido
    public static final int LOSS_POINTS = 25;           // puntos para el perdedor

    private int points;
    private final int[] levels = new int[Stat.values().length];

    public PlayerStats() {
        java.util.Arrays.fill(levels, MIN_LEVEL);
    }

    public void addPoints(int amount) {
        if (amount > 0) points += amount;
    }

    /** Recompensa por un partido terminado. */
    public void rewardMatch(boolean won) {
        addPoints(won ? WIN_POINTS : LOSS_POINTS);
    }

    /** Precio del siguiente punto de mejora: 500, 1000, 2000, ... (se duplica en cada nivel). */
    public int upgradeCost(Stat stat) {
        return BASE_UPGRADE_COST << (getLevel(stat) - MIN_LEVEL);
    }

    public boolean canUpgrade(Stat stat) {
        return getLevel(stat) < MAX_LEVEL && points >= upgradeCost(stat);
    }

    /** Gasta puntos para subir un nivel. Devuelve false si no alcanza o ya está al máximo. */
    public boolean upgrade(Stat stat) {
        if (!canUpgrade(stat)) return false;
        points -= upgradeCost(stat);
        levels[stat.ordinal()]++;
        return true;
    }
    public static void save(File file, PlayerStats[] players) throws POOBSoccerException {
        if (file == null) throw new POOBSoccerException(POOBSoccerException.FILE_NULL);
        if (!file.getName().toLowerCase().endsWith(EXTENSION))
            throw new POOBSoccerException(POOBSoccerException.WRONG_EXTENSION);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeUTF(HEADER);
            out.writeObject(players);
        } catch (IOException e) {
            throw new POOBSoccerException("No se pudo guardar el progreso: " + e.getMessage());
        }
    }

    public static PlayerStats[] open(File file) throws POOBSoccerException {
        if (file == null) throw new POOBSoccerException(POOBSoccerException.FILE_NULL);
        if (!file.isFile()) throw new POOBSoccerException(POOBSoccerException.FILE_NOT_FOUND);
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            if (!HEADER.equals(in.readUTF()))
                throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
            Object obj = in.readObject();
            if (!(obj instanceof PlayerStats[]))
                throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
            return (PlayerStats[]) obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new POOBSoccerException(POOBSoccerException.CORRUPT_DATA);
        }
    }

    public int getLevel(Stat stat) { return levels[stat.ordinal()]; }
    public int getPoints()         { return points; }
}