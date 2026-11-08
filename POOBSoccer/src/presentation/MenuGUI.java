

import javax.swing.*;
import java.awt.*;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuGUI extends JPanel {
    private Image backgroundImage;
    private final Timer blinkTimer;
    private boolean showText = true;

    public MenuGUI(JPanel mainCardContainer, CardLayout cardLayout) {
        java.net.URL url = getClass().getResource("/images/Menu.jpg");
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }

        this.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Al hacer clic en cualquier parte, avanza al siguiente menú
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cardLayout.show(mainCardContainer, "OptionGames");
            }
        });

        // Timer que alterna la visibilidad del texto cada 500 ms (efecto arcade)
        blinkTimer = new Timer(500, e -> {
            showText = !showText;
            repaint();
        });

        // El timer solo corre cuando el menú está en pantalla (ahorra CPU)
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                showText = true;
                blinkTimer.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                blinkTimer.stop();
            }
        });
    }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1) Fondo
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        // 2) Texto parpadeante
        if (showText) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String text = "¡ Toca para jugar ! ";
            Font font = new Font("Wide Latin", Font.ROMAN_BASELINE, 30);
            g2d.setFont(font);

            FontMetrics fm = g2d.getFontMetrics();
            int textW = fm.stringWidth(text);
            int x = (getWidth() - textW) / 2;             // Centrado horizontal
            int y = (int) (getHeight() * 0.85);            // 85% de la altura de la pantalla

            // Sombra negra para que siempre sea legible sin importar los colores del fondo
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(text, x + 2, y + 2);

            // Texto frontal blanco/dorado
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, x, y);

            g2d.dispose();
        }
    }
}