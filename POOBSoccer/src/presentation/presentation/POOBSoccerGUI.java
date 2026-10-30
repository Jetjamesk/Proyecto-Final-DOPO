package presentation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class POOBSoccerGUI extends JFrame {
    private POOBSoccerGUI() {
       super("POOBSoccer");
       CardLayout cardLayout = new CardLayout();
       JPanel mainCardContainer = new JPanel(cardLayout);
       //De esta manera se guardaran los panels, imaginalo como un mazo de cartas. Los instanciamos y los vamos colocando
       //De manera que los botones son redirigan al siguiente Panel y adaptamos los botones y tal
       MenuGUI panelMenu = new MenuGUI(mainCardContainer, cardLayout);
       BallDemoGUI panelGame = new BallDemoGUI(mainCardContainer, cardLayout);
       //Aqui se añaden
       mainCardContainer.add(panelMenu, "Menu");
       mainCardContainer.add(panelGame, "Juego");
       //Aqui mostramos el primero y como mostrarlos en general
       cardLayout.show(mainCardContainer, "Menu");
       
       this.add(mainCardContainer);
    }
    private void exit(){
        int opcion= JOptionPane.showConfirmDialog(this,"¿Desea cerrar POOBSoccer?","Salir",JOptionPane.YES_NO_OPTION);
        if (opcion==JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }
    

    private void prepareController(){
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override 
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
            
    }
    public static void main(String[] var0) {
        POOBSoccerGUI var1 = new POOBSoccerGUI();
        var1.prepareView();
        var1.prepareController();
    }
    private void prepareView() {
        Dimension var1 = Toolkit.getDefaultToolkit().getScreenSize();
        int var2 = var1.width / 2;
        int var3 = var1.height / 2;
        this.setSize(var2, var3);
        Dimension var4 = this.getSize();
        int var5 = var4.width / 2;
        int var6 = var4.height / 2;
        int var7 = var1.width / 2;
        int var8 = var1.height / 2;
        this.setLocation(var7 - var5, var8 - var6);
        
        this.setVisible(true);
     }

}
