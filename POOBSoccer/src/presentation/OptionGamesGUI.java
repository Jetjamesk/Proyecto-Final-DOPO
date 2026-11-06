package presentation;

import javax.swing.*;
import java.awt.*;


public class OptionGamesGUI {
    //Crear tres botones llamados modo 1, modo 2 y modo 3. 
    private JButton button1;
    private JButton button2;
    private JButton button3;
    public OptionGamesGUI(JPanel mainCardContainer, JPanel cardLayout){
        button1 = new JButton("Modo 1");
        button1.addActionListener(null);
        button2 = new JButton("Modo 2");
        button2.addActionListener(null);
        button3 = new JButton("Modo 3");
        button3.addActionListener(null);
        
    }
}
