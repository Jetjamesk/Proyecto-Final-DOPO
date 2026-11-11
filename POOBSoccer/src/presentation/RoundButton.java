package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class RoundButton extends JButton {
    private Color normalColor  = new Color(30, 30, 30);
    private Color hoverColor   = new Color(70, 70, 70);
    private Color pressedColor = new Color(0, 0, 0);
    private Color borderColor  = Color.WHITE;
    private boolean filled = true;   // true = círculo relleno, false = solo circunferencia

    public RoundButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setRolloverEnabled(true);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Fuerza que el botón SIEMPRE sea cuadrado (usa el lado menor)
    @Override
    public void setBounds(int x, int y, int width, int height) {
        int d = Math.min(width, height);
        super.setBounds(x, y, d, d);
    }

    @Override
    public Dimension getPreferredSize() {
        int d = Math.max(super.getPreferredSize().width, super.getPreferredSize().height);
        return new Dimension(d, d);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int d = Math.min(getWidth(), getHeight()) - 2;   // diámetro
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;

        if (filled) {
            if (getModel().isPressed())       g2.setColor(pressedColor);
            else if (getModel().isRollover()) g2.setColor(hoverColor);
            else                              g2.setColor(normalColor);
            g2.fillOval(x, y, d, d);
        }

        // Circunferencia (borde)
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, d, d);

        g2.dispose();
        super.paintComponent(g);   // texto encima
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Vacío: el borde ya se dibuja en paintComponent
    }

    @Override
    public boolean contains(int x, int y) {
        int d = Math.min(getWidth(), getHeight());
        return new Ellipse2D.Float(0, 0, d, d).contains(x, y);
    }

    // Opciones de estilo
    public void setFilled(boolean filled) { this.filled = filled; repaint(); }
    public void setBorderColor(Color c)   { this.borderColor = c; repaint(); }
}