

import javax.swing.*;
import java.awt.*;

public class OptionGamesGUI extends JPanel {
    //Images
    private Image backgroundImage;
    private Image imgPvsP;
    private Image imgPvsM;
    private Image imgMvsM;
    //Sizes for the buttons
    private int lastBw = 0;
    private int lastBh = 0;
    //Buttons
    private JButton button1;
    private JButton button2;
    private JButton button3;

    public OptionGamesGUI(JPanel mainCardContainer, CardLayout cardLayout){

        java.net.URL url = getClass().getResource("/images/menu2.jpg");
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }
        this.setLayout(null);

        java.net.URL url1 = getClass().getResource("images/ArcadeButton.png");
        java.net.URL url2 = getClass().getResource("images/MultiplayerButton.png");
        java.net.URL url3 = getClass().getResource("images/RobotButton.png");

        ImageIcon iconPvsP = (url1 != null) ? new ImageIcon(url1) : new ImageIcon(); 
        ImageIcon iconPvsM = (url2 != null) ? new ImageIcon(url2) : new ImageIcon();
        ImageIcon iconMvsM = (url3 != null) ? new ImageIcon(url3) : new ImageIcon();

        
        imgPvsP = iconPvsP.getImage();
        imgPvsM = iconPvsM.getImage();
        imgMvsM = iconMvsM.getImage();

        button1 = new JButton(iconPvsP);
        button1.setBorderPainted(false);       
        button1.setContentAreaFilled(false);   
        button1.setFocusPainted(false);        
        button1.setOpaque(false);              
        button1.addActionListener(null);

        button2 = new JButton(iconPvsM);
        button2.setBorderPainted(false);       
        button2.setContentAreaFilled(false);   
        button2.setFocusPainted(false);        
        button2.setOpaque(false);              
        button2.addActionListener(null);

        // Kept as text for now, but you can add a 3rd image url for this too
        button3 = new JButton(iconMvsM);
        button3.setBorderPainted(false);       
        button3.setContentAreaFilled(false);   
        button3.setFocusPainted(false);        
        button3.setOpaque(false);              
        button3.addActionListener(null);

        button1.addActionListener(e -> cardLayout.show(mainCardContainer, "Juego"));

        this.add(button1);
        this.add(button2);
        this.add(button3);
    }

    /**
    * Se usa para poner la imagen en el option games
    */
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        int bw = (int) (w * 0.31);  
        int bh = (int) (h * 0.17);  
        
        int x = (w - bw) / 2; 

        int startY = (int) (h * 0.25); 
        int gap = (int) (h * 0.05); 

        button1.setBounds(x, startY, bw, bh);
        button2.setBounds(x, startY + bh + gap, bw, bh);
        button3.setBounds(x, startY + (2 * bh) + (2 * gap), bw, bh);
        
        if (bw > 0 && bh > 0 && (bw != lastBw || bh != lastBh)) {
            
            if (imgPvsP != null) {
                Image scaled1 = imgPvsP.getScaledInstance(bw, bh, Image.SCALE_SMOOTH);
                button1.setIcon(new ImageIcon(scaled1));
            }
            
            if (imgPvsM != null) {
                Image scaled2 = imgPvsM.getScaledInstance(bw, bh, Image.SCALE_SMOOTH);
                button2.setIcon(new ImageIcon(scaled2));
            }

            if (imgMvsM != null){
                Image scaled3 = imgMvsM.getScaledInstance(bw, bh, Image.SCALE_SMOOTH);
                button3.setIcon(new ImageIcon(scaled3));
            }
            
            // Save the new dimensions so it doesn't recalculate on the next frame
            lastBw = bw;
            lastBh = bh;
        }
    }
}
