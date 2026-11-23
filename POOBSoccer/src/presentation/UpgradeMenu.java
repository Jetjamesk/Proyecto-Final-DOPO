import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import domain.PlayerStats;
import domain.PlayerStats.Stat;

/**
 * Pantalla de mejoras: una columna por jugador con sus puntos y, por cada
 * estadística, su nombre, su barra de nivel y un botón "+" para comprar el siguiente nivel.
 * Los puntos y niveles viven en PlayerStats (dominio); esta pantalla solo los muestra
 * y le pide a PlayerStats que haga la mejora.
 */
public class UpgradeMenu extends JPanel {

    private static final int PLAYERS = 2;

    private final GameGUI gameGUI;   // dueño del progreso de los dos jugadores

    // [jugador][estadística]
    private final UpgradesBar[][] bars = new UpgradesBar[PLAYERS][Stat.values().length];
    private final JButton[][] plusButtons = new JButton[PLAYERS][Stat.values().length];
    private final JLabel[] titles = new JLabel[PLAYERS];
    private final JLabel[] pointsLabels = new JLabel[PLAYERS];

    private final JButton btnBack;
    private final JPanel columns;
    private Image backgroundImage;

    public UpgradeMenu(JPanel mainContainer, CardLayout cardLayout, GameGUI gameGUI) {
        this.gameGUI = gameGUI;
        this.setLayout(null);   // igual que las otras pantallas: posiciones en doLayout()

        java.net.URL bgUrl = getClass().getResource("/images/Menu.jpg");
        if (bgUrl != null) {
            backgroundImage = new ImageIcon(bgUrl).getImage();
        }

        // Volver a la pantalla de la que se vino (jugador vs jugador o vs bot)
        btnBack = new JButton("Volver");
        btnBack.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE, 15));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(new Color(20, 20, 20));
        btnBack.setFocusable(false);
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(e -> cardLayout.show(mainContainer,
                OptionGamesGUI.getIsBot() ? "SelectBot" : "SelectCharacter"));
        this.add(btnBack);

        // Dos columnas: Jugador 1 a la izquierda, Jugador 2 a la derecha
        columns = new JPanel(new GridLayout(1, PLAYERS, 60, 0));
        columns.setOpaque(false);
        for (int p = 0; p < PLAYERS; p++) {
            columns.add(createPlayerColumn(p));
        }
        this.add(columns);

        // Cada vez que se abre la pantalla se leen los puntos y niveles actuales
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });
        refresh();
    }

    /** Columna de un jugador: título y puntos arriba, y sus 4 estadísticas debajo. */
    private JPanel createPlayerColumn(int player) {
        titles[player] = new JLabel("", SwingConstants.CENTER);
        titles[player].setFont(new Font("SansSerif", Font.BOLD, 24));
        titles[player].setForeground(Color.WHITE);

        pointsLabels[player] = new JLabel("", SwingConstants.CENTER);
        pointsLabels[player].setFont(new Font("SansSerif", Font.BOLD, 18));
        pointsLabels[player].setForeground(new Color(255, 215, 0));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(titles[player]);
        header.add(pointsLabels[player]);

        JPanel rows = new JPanel(new GridLayout(Stat.values().length, 1, 0, 14));
        rows.setOpaque(false);
        for (Stat stat : Stat.values()) {
            rows.add(createStatRow(player, stat));
        }

        JPanel column = new JPanel(new BorderLayout(0, 16));
        column.setOpaque(false);
        column.add(header, BorderLayout.NORTH);
        column.add(rows, BorderLayout.CENTER);
        return column;
    }

    /** Una fila: nombre a la izquierda, barra al centro y botón "+" a la derecha. */
    private JPanel createStatRow(int player, Stat stat) {
        JLabel name = new JLabel(displayName(stat));
        name.setFont(new Font("SansSerif", Font.BOLD, 16));
        name.setForeground(Color.WHITE);
        name.setPreferredSize(new Dimension(110, 0));   // mismo ancho en todas las filas

        UpgradesBar bar = new UpgradesBar(PlayerStats.MAX_LEVEL);
        bars[player][stat.ordinal()] = bar;

        JButton plus = new JButton("+");
        plus.setFont(new Font("SansSerif", Font.BOLD, 18));
        plus.setForeground(Color.WHITE);
        plus.setBackground(new Color(20, 20, 20));
        plus.setFocusable(false);
        plus.setBorderPainted(false);
        plus.addActionListener(e -> tryUpgrade(player, stat));
        plusButtons[player][stat.ordinal()] = plus;

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        row.add(name, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(plus, BorderLayout.EAST);
        return row;
    }

    /**
     * Muestra cuánto cuesta la mejora y, si el jugador confirma y le alcanzan
     * los puntos, se la compra a PlayerStats.
     */
    private void tryUpgrade(int player, Stat stat) {
        PlayerStats ps = gameGUI.getPlayerStats(player);
        String who = playerName(player);
        String statName = displayName(stat);
        int level = ps.getLevel(stat);

        if (level >= PlayerStats.MAX_LEVEL) {
            JOptionPane.showMessageDialog(this,
                    statName + " de " + who + " ya está en el nivel máximo (" + PlayerStats.MAX_LEVEL + ").",
                    "Mejora", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int cost = ps.upgradeCost(stat);
        int points = ps.getPoints();

        if (points < cost) {
            JOptionPane.showMessageDialog(this,
                    "Mejorar " + statName + " al nivel " + (level + 1) + " cuesta " + cost + " puntos.\n"
                    + who + " tiene " + points + " puntos: le faltan " + (cost - points) + ".",
                    "Puntos insuficientes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Mejorar " + statName + " del nivel " + level + " al " + (level + 1)
                + " cuesta " + cost + " puntos.\n"
                + who + " tiene " + points + " puntos y le quedarían " + (points - cost) + ".\n\n"
                + "¿Confirmar la mejora?",
                "Coste de la mejora", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.YES_OPTION && ps.upgrade(stat)) {
            refresh();
        }
    }

    /** Vuelve a leer puntos y niveles de PlayerStats y los pinta. */
    private void refresh() {
        for (int p = 0; p < PLAYERS; p++) {
            PlayerStats ps = gameGUI.getPlayerStats(p);
            titles[p].setText(playerName(p));
            pointsLabels[p].setText("Puntos: " + ps.getPoints());

            for (Stat stat : Stat.values()) {
                int level = ps.getLevel(stat);
                bars[p][stat.ordinal()].setLevel(level);
                plusButtons[p][stat.ordinal()].setToolTipText(level >= PlayerStats.MAX_LEVEL
                        ? "Nivel máximo"
                        : "Siguiente nivel: " + ps.upgradeCost(stat) + " puntos");
            }
        }
        repaint();
    }

    private String playerName(int player) {
        if (player == 0) return "Jugador 1";
        return OptionGamesGUI.getIsBot() ? "CPU" : "Jugador 2";
    }

    private static String displayName(Stat stat) {
        switch (stat) {
            case SPEED: return "Velocidad";
            case JUMP:  return "Salto";
            case KICK:  return "Patada";
            default:    return "Resistencia";   // SURVIVAL
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        g.setColor(new Color(0, 0, 0, 120));   // capa oscura, como en las otras pantallas
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        btnBack.setBounds(20, 20, 140, 35);

        int cw = (int) (w * 0.85);
        int ch = (int) (h * 0.65);
        columns.setBounds((w - cw) / 2, (int) (h * 0.15), cw, ch);
    }
}