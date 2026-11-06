package presentation;

import javax.swing.*;
import java.awt.*;

public class MenuGUI extends JPanel{
    private Image backgroundImage;
    private JButton btnDemoPlay;

    public MenuGUI(JPanel mainCardContainer, CardLayout cardLayout){
        java.net.URL url = getClass().getResource("/images/Menu.jpg");
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }

        btnDemoPlay = new JButton("Play");

        btnDemoPlay.addActionListener(e -> cardLayout.show(mainCardContainer, "Juego"));

        this.add(btnDemoPlay);

    }

    /**
     * Se usa para poner la imagen en el menu
     */
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * Se usa para que el boton jugar quede en una posicion
     */
    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        int bw = (int) (w * 0.31);  // ancho
        int bh = (int) (h * 0.09);  // alto
        int x = (int) (w - bw) / 2;   // distacia para que quede centrado 
        int y = (int) (h * 0.85);   // distancia desde arriba (sube o baja aquí)
        

        btnDemoPlay.setBounds(x, y, bw, bh);
    }

}
