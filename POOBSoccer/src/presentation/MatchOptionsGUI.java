

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Pantalla para elegir el tipo de partido.
 * Los botones crecen al pasar el mouse y muestran su explicación debajo.
 */
public class MatchOptionsGUI extends JPanel {
    private static final double HOVER_SCALE = 1.12;   // el botón crece un 12 % al pasar el mouse

    // Valores con los que se crea cada tipo de partido
    private static final int DEFAULT_MINUTES = 1;
    private static final int DEFAULT_GOALS = 5;

    private static final String TIME_TEXT =
        "Partido por tiempo: gana quien tenga más goles al acabarse el reloj. Si hay empate, ¡gol de oro!";
    private static final String GOAL_TEXT =
        "Partido por goles: gana el primero que llegue al número de goles acordado.";




    private Image backgroundImage;
    private Image imgMatchTime;
    private Image imgMatchGoal;

    private int descCenterY = 0;        // altura donde va el texto explicativo (la calcula doLayout)

    private HoverImageButton button1;   // Time Match
    private HoverImageButton button2;   // Gol Match

    private String hoverText = null;    // texto explicativo que se muestra (o null)


    public MatchOptionsGUI(JPanel mainCardContainer, CardLayout cardLayout, GameGUI gameGUI){

        java.net.URL url = getClass().getResource("/images/menu2.jpg");
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }
        this.setLayout(null);

        java.net.URL url1 = getClass().getResource("/images/TimedMatch.png");
        java.net.URL url2 = getClass().getResource("/images/GoalMatch.png");

        imgMatchTime = (url1 != null) ? new ImageIcon(url1).getImage() : null;
        imgMatchGoal = (url2 != null) ? new ImageIcon(url2).getImage() : null;


        // Los botones se agrandan solos; aquí solo se decide qué hacer con su texto
        button1 = new HoverImageButton(imgMatchTime, TIME_TEXT, HOVER_SCALE);
        button2 = new HoverImageButton(imgMatchGoal, GOAL_TEXT, HOVER_SCALE);
        button1.setOnHoverChange(this::showDescription);
        button2.setOnHoverChange(this::showDescription);

        //Jsliders

        JSlider timeForMatch = new JSlider(1, 10, 1);
        timeForMatch.setMajorTickSpacing(1);   
        timeForMatch.setSnapToTicks(true);     
        timeForMatch.setPaintTicks(true);      
        timeForMatch.setPaintLabels(true);


        JSlider goalsForMatch = new JSlider(1, 15, 3);
        goalsForMatch.setMajorTickSpacing(1);   
        goalsForMatch.setSnapToTicks(true);     
        goalsForMatch.setPaintTicks(true);      
        goalsForMatch.setPaintLabels(true);

        // Elegir el tipo de partido y pasar a la selección de personajes
        button1.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,timeForMatch,"¿Cuantos minutos durara el partido?",JOptionPane.DEFAULT_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                return;  
            }

            int minutes = timeForMatch.getValue();
            gameGUI.setMatchConfig(GameGUI.MatchType.TIME, minutes);
            if (!OptionGamesGUI.getIsBot()){
                cardLayout.show(mainCardContainer, "SelectCharacter");
            }else{
                cardLayout.show(mainCardContainer, "SelectBot");
            }
        });
        button2.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,goalsForMatch,"¿Cuantos goles decidiran el partido?",JOptionPane.DEFAULT_OPTION);

            if (result != JOptionPane.OK_OPTION) {
                return;   
            }

            int goalsMatch = goalsForMatch.getValue();
            gameGUI.setMatchConfig(GameGUI.MatchType.GOAL, goalsMatch);
            
            if (!OptionGamesGUI.getIsBot()){
            cardLayout.show(mainCardContainer, "SelectCharacter");
            }else{
                cardLayout.show(mainCardContainer, "SelectBot");
                
            }
        });

        JButton button4 = new JButton("Volver");
        button4.setBounds(20, 20, 140, 35);
        button4.setFocusable(false);
        button4.addActionListener(e -> cardLayout.show(mainCardContainer, "OptionGames"));

        //Estilo del bton de volver
        button4.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE, 15));
        button4.setBackground(new Color(20,20,20));
        button4.setForeground(Color.white);
        button4.setBorderPainted(false);

        this.add(button1);
        this.add(button2);
        this.add(button4);
    }

    /** Lo llaman los botones: texto al entrar el mouse, null al salir. */
    private void showDescription(String text) {
        hoverText = text;
        repaint();
    }

    /**
    * Fondo y texto explicativo
    */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        if (hoverText != null) {
            drawDescription((Graphics2D) g, hoverText);
        }
    }

    /** Dibuja el texto explicativo en una franja oscura debajo de los botones. */
    private void drawDescription(Graphics2D g, String text) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Tamaño de letra según la ventana; se achica si el texto no cabe
        int size = Math.max(12, h / 28);
        g2.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g2.getFontMetrics();
        while (fm.stringWidth(text) > w * 0.92 && size > 10) {
            size--;
            g2.setFont(new Font("SansSerif", Font.BOLD, size));
            fm = g2.getFontMetrics();
        }

        int bandH = fm.getHeight() * 2;
        int bandY = descCenterY - bandH / 2;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, bandY, w, bandH);

        int textX = (w - fm.stringWidth(text)) / 2;
        int textY = bandY + (bandH - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
        g2.dispose();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        // Botones lado a lado, centrados en la pantalla
        int gap = (int) (w * 0.04);          // espacio entre los dos botones
        int bw  = (int) (w * 0.40);          // ancho de cada botón
        int bh  = heightFor(imgMatchTime, bw, (int) (h * 0.45));   // alto según la proporción de la imagen

        int totalW = 2 * bw + gap;
        int startX = (w - totalW) / 2;
        int y = (h - bh) / 2;

        button1.setBaseBounds(startX, y, bw, bh);              // izquierda
        button2.setBaseBounds(startX + bw + gap, y, bw, bh);   // derecha

        // Texto explicativo debajo de los botones
        descCenterY = y + (int) (bh * HOVER_SCALE) + (int) (h * 0.10);
    }

    /** Alto que respeta la proporción de la imagen, sin pasarse de maxH. */
    private int heightFor(Image img, int width, int maxH) {
        if (img == null || img.getWidth(null) <= 0) return maxH;
        int hByRatio = (int) (width * img.getHeight(null) / (double) img.getWidth(null));
        return Math.min(hByRatio, maxH);
    }
}