package presentation;

import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Barra que muestra el nivel de una estadística (el nivel real lo guarda PlayerStats).
 * No fija su propio tamaño: lo decide el layout del panel que la contiene.
 */
public class UpgradesBar extends JProgressBar {

    public UpgradesBar(int maxLevel) {
        super(SwingConstants.HORIZONTAL, 0, maxLevel);
        setStringPainted(true);
        setLevel(0);
    }

    /** Muestra el nivel dado, por ejemplo "Nivel 3 / 10". */
    public void setLevel(int level) {
        setValue(level);
        setString("Nivel " + level + " / " + getMaximum());
    }

    public int getLevel() {
        return getValue();
    }
}
