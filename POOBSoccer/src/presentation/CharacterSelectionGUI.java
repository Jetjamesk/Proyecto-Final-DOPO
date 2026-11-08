import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.Normalizer;
import domain.Brasil;
import domain.Canada;
import domain.Character;

public class CharacterSelectionGUI extends JPanel {
    private static final String[] AVAILABLE_CHARACTERS = {"Brasil", "Canada"};

    private int p1Index = 0;
    private int p2Index = 1;

    private Image backgroundImage;
    private GameGUI gameGUI;
    private JPanel mainContainer;
    private CardLayout cardLayout;

    private JLabel p1Preview;
    private JLabel p2Preview;
    private JLabel p1NameLabel;
    private JLabel p2NameLabel;

    public CharacterSelectionGUI(JPanel mainContainer, CardLayout cardLayout, GameGUI gameGUI) {
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
        btnBack.setBounds(20, 20, 120, 35);
        btnBack.setFocusable(false);
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "OptionGames"));
        this.add(btnBack);

        // Player 1 Selector Components
        JButton p1Prev = new JButton("<");
        JButton p1Next = new JButton(">");
        p1NameLabel = new JLabel("", SwingConstants.CENTER);
        p1NameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        p1NameLabel.setForeground(Color.WHITE);
        p1Preview = new JLabel("", SwingConstants.CENTER);

        p1Prev.addActionListener(e -> {
            p1Index = (p1Index - 1 + AVAILABLE_CHARACTERS.length) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });
        p1Next.addActionListener(e -> {
            p1Index = (p1Index + 1) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });

        // Player 2 Selector Components
        JButton p2Prev = new JButton("<");
        JButton p2Next = new JButton(">");
        p2NameLabel = new JLabel("", SwingConstants.CENTER);
        p2NameLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        p2NameLabel.setForeground(Color.WHITE);
        p2Preview = new JLabel("", SwingConstants.CENTER);

        p2Prev.addActionListener(e -> {
            p2Index = (p2Index - 1 + AVAILABLE_CHARACTERS.length) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });
        p2Next.addActionListener(e -> {
            p2Index = (p2Index + 1) % AVAILABLE_CHARACTERS.length;
            updateSelection();
        });

        // Play Button
        JButton btnStart = new JButton("¡JUGAR!");
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnStart.setBackground(new Color(46, 204, 113));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusable(false);
        btnStart.addActionListener(e -> {
            Character player1 = instantiateCharacter(AVAILABLE_CHARACTERS[p1Index]);
            Character player2 = instantiateCharacter(AVAILABLE_CHARACTERS[p2Index]);
            gameGUI.setPlayers(player1, player2);
            cardLayout.show(mainContainer, "Juego");
        });

        // Add to panel
        this.add(p1Prev);
        this.add(p1Next);
        this.add(p1NameLabel);
        this.add(p1Preview);

        this.add(p2Prev);
        this.add(p2Next);
        this.add(p2NameLabel);
        this.add(p2Preview);

        this.add(btnStart);

        updateSelection();
    }
    

    /** Factory method to create new instances of chosen characters */
    private Character instantiateCharacter(String name) {
        switch (name) {
            case "Canada": return new Canada();
            case "Brasil":
            default:       return new Brasil();
        }
    }

    private void updateSelection() {
        String c1 = AVAILABLE_CHARACTERS[p1Index];
        String c2 = AVAILABLE_CHARACTERS[p2Index];

        p1NameLabel.setText("Jugador 1: " + c1);
        p2NameLabel.setText("Jugador 2: " + c2);

        p1Preview.setIcon(getFaceIcon(c1));
        p2Preview.setIcon(getFaceIcon(c2));
        repaint();
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

        // Player 2 controls layout
        p2Preview.setBounds(rightColX + (colWidth - 120) / 2, centerY - 60, 120, 120);
        p2NameLabel.setBounds(rightColX, centerY + 70, colWidth, 30);
        getComponent(5).setBounds(rightColX, centerY, 45, 45);               // p2Prev
        getComponent(6).setBounds(rightColX + colWidth - 45, centerY, 45, 45); // p2Next

        // Play Button centered at the bottom
        int btnW = 220;
        int btnH = 50;
        getComponent(9).setBounds((w - btnW) / 2, (int) (h * 0.75), btnW, btnH); // btnStart
    }
}