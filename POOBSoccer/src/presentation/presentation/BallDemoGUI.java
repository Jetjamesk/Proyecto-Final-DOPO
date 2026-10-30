package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import domain.Ball;

public class BallDemoGUI extends JPanel implements Runnable {
    private Ball ball;
    
    // Recibimos el mazo y el layout para poder volver al menú
    public BallDemoGUI(JPanel mainCardContainer, CardLayout cardLayout) {
        setBackground(new Color(135, 206, 235)); //Azul cielo
        this.setLayout(null); 
        
       
        ball = new Ball(400, 100);

        // Botón para regresar al menú
        JButton btnVolver = new JButton("Volver al Menú");
        btnVolver.setBounds(10, 10, 150, 30);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> {
            cardLayout.show(mainCardContainer, "Menu"); 
        });
        this.add(btnVolver);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Reiniciar la pelota a su posición original cada vez que entras
                ball = new Ball(400, 100); 
            }
        });
        // Inicia el bucle de juego en un hilo separado al instanciar el panel[cite: 4]
        new Thread(this).start();
    }
    //metodo probable para patear la pelota
    private void kickBall(int targetX, int targetY) {
        double angle = Math.atan2(targetY - ball.getY(), targetX - ball.getX());
        double KICK_POWER = 1200;
        
        ball.setVX(Math.cos(angle) * KICK_POWER);
        ball.setVY(Math.sin(angle) * KICK_POWER - 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujar el suelo adaptándose al alto actual del panel
        int groundHeight = 50;
        int floorY = this.getHeight() - groundHeight; 
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, floorY, this.getWidth(), groundHeight);

        
        int d = (int)(ball.radius * 2);
        int drawX = (int)(ball.getX() - ball.radius);
        int drawY = (int)(ball.getY() - ball.radius);
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(drawX, drawY, d, d);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(drawX, drawY, d, d);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double targetFPS = 60.0;
        double nsPerTick = 1000000000.0 / targetFPS;
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            while (delta >= 1) {
                // Calculamos los bordes dinámicamente para que la pelota no se salga de la ventana
                int currentWidth = this.getWidth() > 0 ? this.getWidth() : 800;
                int currentHeight = this.getHeight() > 0 ? this.getHeight() : 600;
                int floorY = currentHeight - 50;
                
                // Actualiza las físicas
                ball.update(1.0 / 60.0, floorY, 0, 0, currentWidth);
                delta--;
            
            repaint();
            
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            }
        }
    }
}