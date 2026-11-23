package presentation;

import javax.swing.*;
import java.awt.*;
import java.text.Normalizer;
import java.util.Random;
import domain.Brasil;
import domain.Cameroon;
import domain.Canada;
import domain.Character;
import domain.Nigeria;
import domain.SouthKorea;
import domain.UnitedStates;
import domain.BotDifficulty;

public class BotSelectionGUI extends JPanel {
    private int difficultyIndex = 1;
    private final Random random = new Random();

    private static final String[] AVAILABLE_CHARACTERS = {"Brasil", "Canada","SouthKorea","Cameroon","Nigeria","UnitedStates"};
    private static final BotDifficulty[] AVAILABLE_DIFFICULTIES = BotDifficulty.values();

    private int p1Index = 0;

    private Image backgroundImage;
    private GameGUI gameGUI;
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private Image imgStart;

    private JLabel p1Preview;
    private JLabel p1NameLabel;
    private JLabel p2NameLabel;

    private JButton diffPrev;
    private JButton diffNext;
    private JLabel diffStars;
    private JLabel diffNameLabel;

    private JButton btnUpgrade;

    public BotSelectionGUI(JPanel mainContainer, CardLayout cardLayout, GameGUI gameGUI) {
        this.mainContainer = mainContainer;
        this.cardLayout = cardLayout;
        this.gameGUI = gameGUI;
        this.setLayout(null);

        java.net.URL bgUrl = getClass().getResource("/images/Menu.jpg");
        if (bgUrl != null) {
            backgroundImage = new ImageIcon(bgUrl).getImage();
        }

        buildUI();
    }

    private void buildUI() {
        // Back Button
        JButton btnBack = new JButton("Volver");
        btnBack.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE, 15));
        btnBack.setForeground(Color.white);
        btnBack.setBackground(new Color(20,20,20));
        btnBack.setBounds(20, 20, 140, 35);
        btnBack.setFocusable(false);
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "OptionGames"));
        this.add(btnBack);

        // Back Button
        btnUpgrade = new JButton("Mejoras");
        btnUpgrade.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE, 15));
        btnUpgrade.setForeground(Color.white);
        btnUpgrade.setBackground(new Color(20,20,20));
        btnUpgrade.setFocusable(false);
        btnUpgrade.setBorderPainted(false);
        btnUpgrade.addActionListener(e -> cardLayout.show(mainContainer, "UpgradeMenu"));

        // Player 1 Selector Components
        JButton p1Prev = new JButton("<");
        //Estilo 
        p1Prev.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE,15));
        p1Prev.setForeground(Color.white);
        p1Prev.setBackground(new Color(20,20,20));
        p1Prev.setBorderPainted(false);

        JButton p1Next = new JButton(">");
        //Estilo 
        p1Next.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE,15));
        p1Next.setForeground(Color.white);
        p1Next.setBackground(new Color(20,20,20));
        p1Next.setBorderPainted(false);
    
        
        p1NameLabel = new JLabel("", SwingConstants.CENTER);
        p1NameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        p1NameLabel.setForeground(Color.WHITE);
        p1Preview = new JLabel("", SwingConstants.CENTER);

        diffStars = new JLabel("", SwingConstants.CENTER);
        diffStars.setFont(new Font("SansSerif", Font.BOLD, 54));

        p1Prev.addActionListener(e -> {
            p1Index = (p1Index - 1 + AVAILABLE_CHARACTERS.length) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });
        p1Next.addActionListener(e -> {
            p1Index = (p1Index + 1) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });

        // Bot Selector Components
        
        //Estilo
        diffPrev = new JButton("<");
        diffPrev.setFont(new Font("Wide Latin", Font.ROMAN_BASELINE,15));
        diffPrev.setForeground(Color.white);
        diffPrev.setBackground(new Color(20,20,20));
        diffPrev.setBorderPainted(false);

        
        //Estilo
        diffNext = new JButton(">");
        diffNext.setFont(new Font("Wide Latin",Font.ROMAN_BASELINE,15));
        diffNext.setForeground(Color.white);
        diffNext.setBackground(new Color(20,20,20));
        diffNext.setBorderPainted(false);
        diffNameLabel = new JLabel("", SwingConstants.CENTER);
        diffNameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        diffNameLabel.setForeground(Color.WHITE);
        

        diffPrev.addActionListener(e -> {
            difficultyIndex = (difficultyIndex - 1 + AVAILABLE_DIFFICULTIES.length) % AVAILABLE_DIFFICULTIES.length;
            updateSelection();
        });
        diffNext.addActionListener(e -> {
            difficultyIndex = (difficultyIndex + 1) % AVAILABLE_DIFFICULTIES.length;
            updateSelection();
        });

        // Play Button
        java.net.URL url1 = getClass().getResource("/images/startGame.png");
        ImageIcon iconStart = (url1 != null) ? new ImageIcon(url1) : new ImageIcon();
        imgStart = iconStart.getImage();
        JButton btnStart = new JButton(iconStart);
        btnStart.setBorderPainted(false);       
        btnStart.setContentAreaFilled(false);   
        btnStart.setFocusPainted(false);        
        btnStart.setOpaque(false);              
        btnStart.addActionListener(e -> {
            Character player1 = instantiateCharacter(AVAILABLE_CHARACTERS[p1Index]);
            Character cpu = instantiateCharacter(AVAILABLE_CHARACTERS[randomCpuIndex()]);
            gameGUI.setBotDifficulty(AVAILABLE_DIFFICULTIES[difficultyIndex]);
            gameGUI.setPlayers(player1, cpu);
            cardLayout.show(mainContainer, "Juego");
        
        });

        // Add to panel
        this.add(p1Prev);
        this.add(p1Next);
        this.add(p1NameLabel);
        this.add(p1Preview);

        this.add(diffPrev);
        this.add(diffNext);
        this.add(diffNameLabel);
        this.add(diffStars);
        

        this.add(btnStart);
        this.add(btnUpgrade);

        updateSelection();
    }
    
    /** Un personaje al azar para el bot, distinto al del jugador si hay más de uno. */
    private int randomCpuIndex() {
        if (AVAILABLE_CHARACTERS.length == 1) return 0;
        int i = random.nextInt(AVAILABLE_CHARACTERS.length - 1);
        return (i >= p1Index) ? i + 1 : i;
    }
    /** Factory method to create new instances of chosen characters */
    private Character instantiateCharacter(String name) {
        switch (name) {
            case "Canada": return new Canada();
            case "SouthKorea": return new SouthKorea();
            case "Cameroon": return new Cameroon();
            case "UnitedStates":return new UnitedStates();
            case "Nigeria":return new Nigeria();
            case "Brasil":
            default:       return new Brasil();
        }
    }

    private void updateSelection() {
        String c1 = AVAILABLE_CHARACTERS[p1Index];


        p1NameLabel.setText("Jugador 1: " + c1);

        p1Preview.setIcon(getFaceIcon(c1));
        BotDifficulty d = AVAILABLE_DIFFICULTIES[difficultyIndex];
        diffNameLabel.setText("CPU: " + d.getDisplayName());
        diffStars.setText(stars(difficultyIndex + 1, AVAILABLE_DIFFICULTIES.length));
        diffStars.setForeground(Color.BLACK);
        diffNameLabel.setText("CPU: " + d);
        repaint();
    }

    private static String stars(int filled, int total) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) sb.append(i < filled ? '★' : '☆');
        return sb.toString();
    }

    private ImageIcon getFaceIcon(String country) {
        String clean = Normalizer.normalize(country, Normalizer.Form.NFD)
                                 .replaceAll("\\p{M}", "")
                                 .toLowerCase();
        java.net.URL url = getClass().getResource("/images/" + clean + ".png");
        if (url == null) url = getClass().getResource("/images/face.png");

        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        // Dark translucent overlay
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        int colWidth = 240;
        int leftColX = (int) (w * 0.25 - colWidth / 2);
        int rightColX = (int) (w * 0.75 - colWidth / 2);
        int centerY = (int) (h * 0.35);

        // Player 1 controls layout
        p1Preview.setBounds(leftColX + (colWidth - 120) / 2, centerY - 60, 120, 120);
        p1NameLabel.setBounds(leftColX, centerY + 70, colWidth, 30);
        getComponent(1).setBounds(leftColX, centerY, 45, 45);               // p1Prev
        getComponent(2).setBounds(leftColX + colWidth - 45, centerY, 45, 45); // p1Next

        // Bot controls layout
        diffStars.setBounds(rightColX + 45, centerY - 60, colWidth - 90, 120);
        diffNameLabel.setBounds(rightColX, centerY + 70, colWidth, 30);
        diffPrev.setBounds(rightColX, centerY, 45, 45);
        diffNext.setBounds(rightColX + colWidth - 45, centerY, 45, 45);

        // Play Button centered at the bottom
        int btnW = 220;
        int btnH = 50;
        getComponent(9).setBounds((w - btnW) / 2, (int) (h * 0.75), btnW, btnH); // btnStart

        btnUpgrade.setBounds((w-btnW)/8,(int)(h*0.75),160,35);
    }
}