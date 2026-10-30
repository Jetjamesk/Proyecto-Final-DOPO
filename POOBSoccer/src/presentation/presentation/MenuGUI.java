package presentation;

import javax.swing.*;
import java.awt.*;
//Initial menu of the POOBSoccer(just a mockup as it's provisional)
public class MenuGUI extends JPanel{
    private Image backgroundImage;
    private JButton btnDemoPlay;

    public MenuGUI(JPanel mainCardContainer, CardLayout cardLayout){
        //Reemplaza por la ruta de tu computador por el momento para probar, luego lo solucionare
        backgroundImage = new ImageIcon("C:\\Users\\Usuario\\Documents\\DOPO\\Proyecto Final\\Proyecto-Final-DOPO\\POOBSoccer\\src\\images\\menu.jpg").getImage();
        this.setLayout(null);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int widthPanel = screen.width / 2;
        int heightPanel = screen.height / 2;

    
        int posX = (int) (widthPanel * 0.07);   // 7% desde la izquierda
        int posY = (int) (heightPanel * 0.225);    // 27% desde arriba
        int widthBtn = (int) (widthPanel * 0.31); // 31% de ancho
        int heightBtn = (int) (heightPanel * 0.09);

        btnDemoPlay = new JButton("play Demo");

        btnDemoPlay.setBounds(posX, posY, widthBtn, heightBtn);

        btnDemoPlay.addActionListener(e -> {
        
            cardLayout.show(mainCardContainer, "Juego");
        });
        

        this.add(btnDemoPlay);
    }

    @Override 

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }

    }
