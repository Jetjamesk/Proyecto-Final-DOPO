package domain;

public class Scoreboard implements java.io.Serializable{
    private final int[] goals = new int[2];


    /**
     * Registra un gol en el arco del jugador 'conceded' (0 = izquierdo, 1 = derecho).
     * El punto es para el otro jugador: así los autogoles salen solos.
     */
    public void goalConcededBy(int conceded) {
        goals[1 - conceded]++;
    }

    public int getGoals(int player) {
        return goals[player];
    }

    public boolean isTied() {
        return goals[0] == goals[1];
    }

    /** Jugador que va ganando (0 o 1), o -1 si hay empate. */
    public int leader() {
        if (goals[0] > goals[1]) {return 0;}
        if (goals[1] > goals[0]) {return 1;}
        return -1;
    }
}
