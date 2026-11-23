package presentation;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import domain.Ball;
import domain.BotDifficulty;
import domain.Canada;
import domain.Brasil;
import domain.Cameroon;
import domain.SouthKorea;
import domain.Nigeria;
import domain.UnitedStates;
import domain.Character;
import domain.Field;
import domain.Match;
import domain.TimedMatch;
import domain.GoalMatch;

import domain.POOBSoccerException;
import domain.PlayerStats;

import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GameGUI extends JPanel {
    private static final File PROGRESS_FILE =
    new File(System.getProperty("user.home"), "poobsoccer" + PlayerStats.EXTENSION);
    private PlayerStats[] playerStats;
    private boolean progressSaved = false; 
    private final draw drawer = new draw();
    private static final int KEY_RIGHT_CTRL = -9999;
    public enum MatchType {TIME,GOAL};


    
    private BotDifficulty botDifficulty = BotDifficulty.TORPE; 
    private MatchType matchType = MatchType.TIME;
    private int matchValue = 1;
    private final Timer timer;                      
    private final Set<Integer> pressed = new HashSet<>();
    private Match match;
    private JButton btnResume;
    private JButton btnMenu;
    private JButton btnSave;
    private JButton btnOpen;

    private long lastNanos; //Con él se calcula cuánto tiempo pasó entre un tick y el siguiente, para que el juego se mueva a la misma velocidad
    private Character player1 = new Canada();
    private Character player2 = new Brasil();


    /**
     * Guarda el tipo de partido y su valor elegidos en el menú.
     * @param type  TIME o GOAL
     * @param value minutos si es TIME, goles para ganar si es GOAL
     */
        public void setMatchConfig(MatchType type, int value) {
            this.matchType = type;
            this.matchValue = Math.max(1, value);
        }

    public void setPlayers(Character p1, Character p2) {
        this.player1 = p1;
        this.player2 = p2;
        startMatch();
    }

    // Recibimos el mazo y el layout para poder volver al menú
    public GameGUI(JPanel mainCardContainer, CardLayout cardLayout) {
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);                    // para recibir las teclas

        loadProgress();   

        startMatch();

        // Botón de pausa del juego.
        RoundButton btnPausa = new RoundButton("II");
        btnPausa.setBounds(10, 40, 40, 40);
        btnPausa.addActionListener(e -> {
            if (match.isRunning()){
                togglePause();
            }else {
                cardLayout.show(mainCardContainer, "Menu");
            }
        });
        this.add(btnPausa);
        // Botones del menú de pausa (empiezan ocultos)
        btnResume = createMenuButton("Seguir jugando");
        btnResume.addActionListener(e -> {
            togglePause();
            requestFocusInWindow();
        });

        btnMenu = createMenuButton("Salir al menú principal");
        btnMenu.addActionListener(e -> cardLayout.show(mainCardContainer, "Menu"));
        btnSave = createMenuButton("Guardar Partida");
        btnSave.addActionListener(e -> saveGame());
        btnOpen = createMenuButton("Abrir partida");
        btnOpen.addActionListener(e-> openGame());
        this.add(btnResume);
        this.add(btnMenu);
        this.add(btnOpen);
        this.add(btnSave);



        // Cada ciclo de ~16 ms: leer teclas, avanzar el dominio y redibujar
        timer = new Timer(16, e -> tick());

        this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    startMatch();
                    timer.start();
                    requestFocusInWindow();
                }
            
                @Override
                public void componentHidden(ComponentEvent e) {
                    timer.stop();
                    pressed.clear();
                }
            });

            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int key = getProcessedKey(e);
                    if (pressed.add(key) && key == KeyEvent.VK_P) {
                        togglePause();
                    }
                }
            
                @Override
                public void keyReleased(KeyEvent e) {
                    int key = getProcessedKey(e);
                    pressed.remove(key);
                    pressed.remove(e.getKeyCode()); // Previene que la tecla quede trabada
                }
            
                // Identifica si es específicamente el Control Derecho
                private int getProcessedKey(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_CONTROL && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
                        return KEY_RIGHT_CTRL;
                    }
                    return e.getKeyCode();
                }
            });
    }

    private void startMatch() {
        if (btnResume != null){
            setPauseMenuVisible(false);
        }
        Field field =new Field(1000, 600, 180);
        Ball ball =new Ball(0, 0);
        
        player1.setPlayerStats(playerStats[0]);
        player2.setPlayerStats(playerStats[1]);

        progressSaved = false;

        if (matchType == MatchType.GOAL) {
            match = new GoalMatch(player1, player2, field, ball, matchValue);
        } else {
            match = new TimedMatch(player1, player2, field, ball, matchValue * 60);   // minutos -> segundos
        }
        if (OptionGamesGUI.getIsBot()) {
            match.setBot(1, botDifficulty.createBot());
        }
        drawer.loadFaces(match);        // ← reemplaza el for de faces
        drawer.resetAnimations(match);  // ← reemplaza lastShot = null; burstFrames = 0;
        lastNanos = System.nanoTime();
    }



    /**
     * Si el partido está en pausa lo reanuda, y si está corriendo lo pausa.
     */
    private void togglePause() {
        if (match.isPaused()) {
            match.resume();
        } else {
            match.pause();
        }
        lastNanos = System.nanoTime();
        setPauseMenuVisible(match.isPaused());
    }

    /**
     * es un ciclo del juego, y se repite unas 60 veces por segundo.
     *  En cada ciclo lee las teclas, hace avanzar el partido el tiempo que pasó 
     * y vuelve a dibujar la pantalla.
     */
    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.05);   // tope para evitar saltos
        lastNanos = now;
        readKeys();
        match.updateMatch(dt);
        drawer.update(match);
        repaint();
        if (!match.isRunning() && !progressSaved) {
            saveProgress();
            progressSaved = true;
        }
    }

    /** Jugador 1: A D W S.
     *  Jugador 2: flechas. 
     */
    private void readKeys() {
        if(!match.isBot(0)){
            control(0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_Q);
        }
        if(!match.isBot(1)){
            control(1, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KEY_RIGHT_CTRL);
        }
    }

    /**
     * Traduce las teclas presionadas de un jugador en órdenes para su personaje.
     */

    private void control(int player, int left, int right, int up, int kick, int superKey) {
        int dir = 0;
        if (pressed.contains(left)) dir--;
        if (pressed.contains(right)) dir++;
        match.move(player, dir);
        if (pressed.contains(up)) match.jump(player);
        if (pressed.contains(kick)) match.kick(player);
        if (pressed.contains(superKey)) match.usePower(player);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        drawer.render(g2d, match, getWidth(), getHeight());   // ← aquí
        g2d.dispose();
    
    }

    private JButton createMenuButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 18));
        b.setBackground(new Color(30, 30, 30));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setPauseMenuVisible(boolean visible) {
        btnResume.setVisible(visible);
        btnMenu.setVisible(visible);
        btnSave.setVisible(visible);
        btnOpen.setVisible(visible);
    }

    private JFileChooser newChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Partida POOBSoccer (*.poob)", "poob"));
        return chooser;
    }

    /** Guarda la partida actual (solo se llega aquí desde el menú de pausa). */
    private void saveGame() {
        JFileChooser chooser = newChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(Match.EXTENSION)) {
            file = new File(file.getParentFile(), file.getName() + Match.EXTENSION);
        }
        try {
            match.save(file);
            JOptionPane.showMessageDialog(this, "Partida guardada correctamente.", "Guardar",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (POOBSoccerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void setBotDifficulty(BotDifficulty difficulty) {
        if (difficulty != null) {
            this.botDifficulty = difficulty;
        }
    }

    /** Abre una partida guardada; queda en pausa para que el jugador la reanude. */
    private void openGame() {
        JFileChooser chooser = newChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Match loaded = Match.open(chooser.getSelectedFile());
            if (!(loaded instanceof TimedMatch)) {
                throw new POOBSoccerException(POOBSoccerException.NOT_POOBSccoer_FILE);
            }
            match = (TimedMatch) loaded;
            player1 = match.getPlayer(0);
            player2 = match.getPlayer(1);
            drawer.loadFaces(match);        // ← reemplaza el for de faces
            drawer.resetAnimations(match);  // ← reemplaza lastShot = ...; burstFrames = 0;
            pressed.clear();
            JOptionPane.showMessageDialog(this, "Partida abierta correctamente.", "Abrir",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (POOBSoccerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (btnResume == null || btnMenu == null || btnSave == null || btnOpen == null) return;
        int bw = 220, bh = 50, gap = 15;
        int x = (getWidth() - (2 * bw + gap)) / 2;
        int y = (int) (getHeight() * 0.62);
        btnResume.setBounds(x, y, bw, bh);
        btnMenu.setBounds(x + bw + gap, y, bw, bh);
        btnSave.setBounds(x, y + bh + gap, bw, bh);
        btnOpen.setBounds(x + bw + gap, y + bh + gap, bw, bh);
    }

    public PlayerStats getPlayerStats(int player) {
        return playerStats[player];
    }

        /** Carga el progreso guardado; si no existe (primera vez) o está dañado, empieza de cero. */
        private void loadProgress() {
            if (PROGRESS_FILE.isFile()) {
                try {
                    PlayerStats[] loaded = PlayerStats.open(PROGRESS_FILE);
                    if (loaded.length >= 2) {
                        playerStats = new PlayerStats[]{ loaded[0], loaded[1] };
                        return;
                    }
                } catch (POOBSoccerException ex) {
                    JOptionPane.showMessageDialog(null, "No se pudo cargar el progreso: " + ex.getMessage(),
                            "Progreso", JOptionPane.WARNING_MESSAGE);
                }
            }
            playerStats = new PlayerStats[]{ new PlayerStats(), new PlayerStats() };
        }
    
        /** Guarda los puntos y niveles de los dos jugadores. Lo usa también el menú de mejoras. */
        public void saveProgress() {
            try {
                PlayerStats.save(PROGRESS_FILE, playerStats);
            } catch (POOBSoccerException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar el progreso",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    

}

