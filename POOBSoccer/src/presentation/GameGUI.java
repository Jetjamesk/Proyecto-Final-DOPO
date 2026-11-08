

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import domain.Ball;
import domain.Canada;
import domain.Brasil;
import domain.Character;
import domain.Field;
import domain.Match;
import java.awt.geom.Path2D;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class GameGUI extends JPanel {
    private static final int KEY_RIGHT_CTRL = -9999;
    private static final Color[] TEAM_COLORS = { new Color(30, 90, 200), new Color(200, 40, 40) };
    private static final double HEAD_SCALE = 1.15;         // tamaño de la cara respecto a la hitbox 
    private static final boolean FACE_LOOKS_RIGHT = false; //Para la cara del muñeco de la izquierda 
    private static final double BOOT_SCALE = 1.0;          // tamaño de la bota 
    private static final boolean SHOW_HITBOX = false;      
    private final Timer timer;                      
    private final Set<Integer> pressed = new HashSet<>();
    private Match match;
    private Image image;
    private final Map<String, Image> faces = new HashMap<>();   // país -> imagen de la cara
    private long lastNanos; //Con él se calcula cuánto tiempo pasó entre un tick y el siguiente, para que el juego se mueva a la misma velocidad
    private Character player1 = new Canada();
    private Character player2 = new Brasil();

    public void setPlayers(Character p1, Character p2) {
        this.player1 = p1;
        this.player2 = p2;
        startMatch();
    }

    // Recibimos el mazo y el layout para poder volver al menú
    public GameGUI(JPanel mainCardContainer, CardLayout cardLayout) {
        java.net.URL url = getClass().getResource("/images/Background.jpg");
        if (url != null) {
            image = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }
        this.setLayout(null);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);                    // para recibir las teclas

        startMatch();

        // Botón para regresar al menú
        JButton btnVolver = new JButton("Volver al Menú");
        btnVolver.setBounds(10, 40, 150, 30);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setFocusable(false);              // si no, el botón le quita las teclas al panel
        btnVolver.addActionListener(e -> {
            cardLayout.show(mainCardContainer, "Menu");
        });
        this.add(btnVolver);

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
        match = new Match(player1, player2, new Field(1000, 600, 180), new Ball(0, 0));
        faces.clear();
        for (int i = 0; i < 2; i++) {
            String country = match.getPlayer(i).getCountry();
            faces.put(country, loadFace(country));
        }
        lastNanos = System.nanoTime();
    }

    /**
     * Convierte el nombre de un país en el nombre de su archivo de imagen
    */
    private static String key(String country) {
        return Normalizer.normalize(country, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    /**
     * Carga la imagen respectiva 
     */
    private Image loadFace(String country) {
        java.net.URL url = getClass().getResource("/images/" + key(country) + ".png");
        if (url == null){
            url = getClass().getResource("/images/face.png");
        } 
        if (url == null) {
            return null;                              // no hay archivo no hay imagen
        } else {
            return new ImageIcon(url).getImage();     // hay archivo carga  la imagen
        }
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
        repaint();
    }

    /** Jugador 1: A D W S.
     *  Jugador 2: flechas. 
     */
    private void readKeys() {
        control(0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_Q);
        control(1, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KEY_RIGHT_CTRL);
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
        if (pressed.contains(superKey)) match.useSuper(player);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // La cancha se dibuja en coordenadas lógicas (1000x600) y se escala a la ventana
        Field field = match.getField();
        double scale = Math.min(getWidth() / (double) field.getWidth(), getHeight() / (double) field.getHeight());
        double offsetX = (getWidth() - field.getWidth() * scale) / 2;
        double offsetY = (getHeight() - field.getHeight() * scale) / 2;
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        g2d.setClip(0, 0, field.getWidth(), field.getHeight());

        drawField(g2d, field);
        drawGoals(g2d, field);
        for (int i = 0; i < 2; i++) drawCharacter(g2d, match.getPlayer(i), i, field.getFloorY());

        drawBall(g2d);
        drawHud(g2d, field);
        drawMessages(g2d, field);
        g2d.dispose();
    }

    /**
     * Dibuja el campo de juego con el backgroud 
     * @param g2d
     * @param field
     */

    private void drawField(Graphics2D g2d, Field field) {
        int floorY = field.getFloorY();
        if (image != null) {
            g2d.drawImage(image, 0, 0, field.getWidth(), floorY, this);   
        }
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, floorY, field.getWidth(), field.getHeight() - floorY);
    }

    /**
     * Dibuja el arco y todo lo relacioando con el campo 
     * @param g2d
     * @param field
     */

    private void drawGoals(Graphics2D g2d, Field field) {
        int top = field.getGoalTopY();
        int floorY = field.getFloorY();
        int depth = field.getGoalDepth();
        int rightX = field.getWidth() - depth;
        g2d.setColor(new Color(255, 255, 255, 70));                       // la red
        g2d.fillRect(0, top, depth, floorY - top);
        g2d.fillRect(rightX, top, depth, floorY - top);
        g2d.setColor(Color.WHITE);                                         // travesaño y palo frontal
        g2d.setStroke(new BasicStroke(6));
        g2d.drawLine(0, top, depth, top);
        g2d.drawLine(depth, top, depth, floorY);
        g2d.drawLine(rightX, top, field.getWidth(), top);
        g2d.drawLine(rightX, top, rightX, floorY);
        g2d.setStroke(new BasicStroke(1));
    }

    /**
     * Dibuja a los juagdores 
     * @param g2d
     * @param c
     * @param index
     * @param floorY
     */
    private void drawCharacter(Graphics2D g2d, Character c, int index, int floorY) {
        int r = (int) c.getRadius();
        int cx = (int) c.getX();
        int cy = (int) c.getY();
        int facing = c.getFacing();
        Color team = TEAM_COLORS[index];

        // 1) Pierna: de la cadera al tobillo 
        g2d.setColor(team.darker().darker());
        g2d.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(cx, (int) c.getHipY(), (int) c.getFootX(), (int) c.getFootY() - 14);
        g2d.setStroke(new BasicStroke(1));

        // 2) Bota: se dibuja mirando a la derecha, se voltea según hacia dónde mira y se inclina al patear
        drawBoot(g2d, c, team);

        // 3) Cabeza: Se dibuja mirando al lode donde se requiera 
        Image face = faces.get(c.getCountry());
        if (face != null && face.getHeight(null) > 0) {
            double scale = (2 * r * HEAD_SCALE) / face.getHeight(null);
            int dw = (int) (face.getWidth(null) * scale);
            int dh = (int) (face.getHeight(null) * scale);
            boolean flip = facing != (FACE_LOOKS_RIGHT ? 1 : -1);
            if (flip) {
                g2d.drawImage(face, cx + dw / 2, cy - dh / 2, -dw, dh, null);   
            } else {
                g2d.drawImage(face, cx - dw / 2, cy - dh / 2, dw, dh, null);
            }
        } else {
            g2d.setColor(team);
            g2d.fillOval(cx - r, cy - r, 2 * r, 2 * r);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        //El HitBox Para que cuando le pegue funcione 
        if (SHOW_HITBOX) {                       
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(cx - r, cy - r, 2 * r, 2 * r);
            int fr = (int) c.getFootRadius();
            g2d.drawOval((int) c.getFootX() - fr, (int) c.getFootY() - fr, 2 * fr, 2 * fr);
        }

        // país sobre la cabeza
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        drawCentered(g2d, c.getCountry(), cx, cy - (int) (r * HEAD_SCALE) - 6, Color.WHITE);
        g2d.setColor(team.darker().darker());
        g2d.setStroke(new BasicStroke((float) (14 * c.getScale()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setStroke(new BasicStroke(1));
    }

    /** Dibuja la bota con la suela apoyada en el suelo, al patear la punta sube. */
    private void drawBoot(Graphics2D g2d, Character c, Color team) {
        Graphics2D b = (Graphics2D) g2d.create();
        b.translate(c.getFootX(), c.getFootY());
        double finalScale = BOOT_SCALE * c.getScale();
        b.scale(c.getFacing() * finalScale, finalScale);
        b.rotate(-c.getFootTilt());
    
        Path2D boot = new Path2D.Double();
        boot.moveTo(-24, -26);
        boot.lineTo(-2, -26);
        boot.lineTo(0, -14);
        boot.quadTo(20, -14, 22, -4);
        boot.quadTo(23, 0, 16, 0);
        boot.lineTo(-24, 0);
        boot.closePath();    
        b.setColor(team.darker());
        b.fill(boot);
        b.setColor(Color.WHITE);
        b.setStroke(new BasicStroke(3));
        b.drawLine(-22, -3, 16, -3);
        b.setColor(Color.BLACK);
        b.setStroke(new BasicStroke(1.5f));
        b.draw(boot);
        b.dispose();
    }

    /**
     * Dibuja el balon del partido
     * @param g2d
     */
    private void drawBall(Graphics2D g2d) {
        Ball ball = match.getBall();
        int d = (int) (ball.getRadius() * 2);
        int drawX = (int) (ball.getX() - ball.getRadius());
        int drawY = (int) (ball.getY() - ball.getRadius());
        g2d.setColor(Color.WHITE);
        g2d.fillOval(drawX, drawY, d, d);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(drawX, drawY, d, d);
    }

    private void drawHud(Graphics2D g2d, Field field) {
        int w = field.getWidth();
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(w / 2 - 190, 8, 380, 66, 20, 20);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
        String marcador = match.getPlayer(0).getCountry() + "  " + match.getPlayer(0).getScore()
                + " - " + match.getPlayer(1).getScore() + "  " + match.getPlayer(1).getCountry();
        drawCentered(g2d, marcador, w / 2, 40, Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        String tiempo = match.isGoldenGoal() ? "GOL DE ORO" : String.valueOf((int) Math.ceil(match.getTimeLeft())) + " s";
        drawCentered(g2d, tiempo, w / 2, 64, new Color(255, 215, 0));
        
        //superbars
        drawSuperBar(g2d, match.getPlayer(0), 40, 20, "SUPER [Q]");
        drawSuperBar(g2d, match.getPlayer(1), w - 180, 20, "SUPER [CTRL-DER]");
        //Super affections being drawn
        for (int i=0; i<2; i++){
            Character p = match.getPlayer(i);
            if (p.isInverted()) {
                g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
                drawCentered(g2d, "INVERTIDO", (int) p.getX(), (int) (p.getY() - p.getRadius() * HEAD_SCALE - 24), Color.YELLOW);
            } else if (p.isShrink()) {
                g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
                drawCentered(g2d, "ENCOGIDO", (int) p.getX(), (int) (p.getY() - p.getRadius() * HEAD_SCALE - 24), Color.MAGENTA);
            }
        }
    }

    private void drawSuperBar(Graphics2D g2d, Character c, int x, int y, String label) {
        int barW = 140;
        int barH = 14;
        double progress = Math.min(1.0, c.getSuperTimer() / Character.SUPER_COOLDOWN);
    
        // Fondo gris
        g2d.setColor(new Color(40, 40, 40, 200));
        g2d.fillRoundRect(x, y + 16, barW, barH, 6, 6);
    
        // Barra de progreso (Oro si está lista, azul/cian cargando)
        if (c.isSuperReady()) {
            g2d.setColor(new Color(255, 215, 0));
        } else {
            g2d.setColor(new Color(0, 180, 255));
        }
        g2d.fillRoundRect(x, y + 16, (int) (barW * progress), barH, 6, 6);
    
        // Borde
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y + 16, barW, barH, 6, 6);
    
        // Texto de la barra
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        String text = c.isSuperReady() ? label + " (¡LISTO!)" : label + " (" + (int)(progress * 100) + "%)";
        g2d.drawString(text, x, y + 12);
    }

    /**
     * Mensajes del partido ("Gano el partido ...., Gol de oro")
     * @param g2d
     * @param field
     */
    private void drawMessages(Graphics2D g2d, Field field) {
        String msg = null;
        if (!match.isRunning()) {
            Character winner = match.getWinner();
            msg = winner == null ? "EMPATE" : "¡Ganó " + winner.getCountry() + "!";
        } else if (match.isPaused()) {
            msg = "PAUSA  (P para continuar)";
        }
        if (msg == null){
             return;
            }
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, field.getHeight() / 2 - 60, field.getWidth(), 100);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 44));
        drawCentered(g2d, msg, field.getWidth() / 2, field.getHeight() / 2 + 8, Color.WHITE);
    }

    private void drawCentered(Graphics2D g2d, String text, int cx, int baselineY, Color color) {
        int w = g2d.getFontMetrics().stringWidth(text);
        g2d.setColor(color);
        g2d.drawString(text, cx - w / 2, baselineY);
    }

    
}