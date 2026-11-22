import javax.swing.ImageIcon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import domain.Ball;
import domain.Character;
import domain.Decoy;
import domain.Field;
import domain.Match;
import domain.PlayerStats;
import domain.Shot;

/**
 * Se encarga de dibujar todo el partido: cancha, arcos, jugadores, balón,
 * animaciones de los tiros especiales, HUD y mensajes.
 * GameGUI solo le pide que dibuje; aquí no hay lógica del juego.
 */
public class draw {

    private static final Color[] TEAM_COLORS = { new Color(30, 90, 200), new Color(200, 40, 40) };
    private static final double HEAD_SCALE = 1.15;         // tamaño de la cara respecto a la hitbox
    private static final boolean FACE_LOOKS_RIGHT = false; // Para la cara del muñeco de la izquierda
    private static final double BOOT_SCALE = 1.0;          // tamaño de la bota
    private static final boolean SHOW_HITBOX = false;

    private final Map<String, Image> faces = new HashMap<>();   // país -> imagen de la cara
    private Image background;
    private Image ballImage;

    // Animaciones de los tiros especiales
    private int animTick;               // contador de cuadros; mueve las animaciones
    private Shot lastShot;              // para detectar cuándo se lanza un tiro nuevo
    private int burstFrames;            // cuadros que le quedan a la onda del lanzamiento
    private double burstX, burstY;
    private Shot.Type burstType;

    /** Carga el fondo y el balón. */
    public draw() {
        java.net.URL url = getClass().getResource("/images/Background.jpg");
        if (url != null) {
            background = new ImageIcon(url).getImage();
        } else {
            System.err.println("No se encontró la imagen");
        }
        java.net.URL ballUrl = getClass().getResource("/images/ball.png");
        if (ballUrl != null) {
            ballImage = trimTransparent(new ImageIcon(ballUrl).getImage());
        } else {
            System.err.println("No se encontro la imagen");
        }
    }

    // ===================== Preparación y animaciones =====================

    /** Carga las caras de los dos jugadores del partido. */
    public void loadFaces(Match match) {
        faces.clear();
        for (int i = 0; i < 2; i++) {
            String country = match.getPlayer(i).getCountry();
            faces.put(country, loadFace(country));
        }
    }

    /** Reinicia las animaciones (al empezar o abrir un partido). */
    public void resetAnimations(Match match) {
        lastShot = match.getBall().getShot();
        burstFrames = 0;
    }

    /** Avanza las animaciones un cuadro. Se llama en cada tick del juego. */
    public void update(Match match) {
        if (!match.isPaused()) animTick++;
        detectLaunch(match);
        if (burstFrames > 0) burstFrames--;
    }

    /** Si el balón acaba de salir en tiro especial, arranca la onda de lanzamiento. */
    private void detectLaunch(Match match) {
        Shot shot = match.getBall().getShot();
        if (shot != null && shot != lastShot) {
            burstFrames = 24;
            burstX = match.getBall().getX();
            burstY = match.getBall().getY();
            burstType = shot.getType();
        }
        lastShot = shot;
    }

    // Dibujo principal 

    /**
     * Dibuja el partido completo. La cancha se dibuja en coordenadas lógicas
     * (1000x600) y se escala al tamaño del panel.
     */
    public void render(Graphics2D g2d, Match match, int width, int height) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Field field = match.getField();
        double scale = Math.min(width / (double) field.getWidth(), height / (double) field.getHeight());
        double offsetX = (width - field.getWidth() * scale) / 2;
        double offsetY = (height - field.getHeight() * scale) / 2;
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        g2d.setClip(0, 0, field.getWidth(), field.getHeight());

        drawField(g2d, field);
        drawGoals(g2d, field);
        for (int i = 0; i < 2; i++) drawCharacter(g2d, match.getPlayer(i), i, field.getFloorY());

        drawBall(g2d, match);
        drawLaunchBurst(g2d);
        drawHud(g2d, match, field);
        drawMessages(g2d, match, field);
    }

    /**
     * Dibuja el campo de juego con el backgroud 
     * @param g2d
     * @param field
     */

    private void drawField(Graphics2D g2d, Field field) {
        int floorY = field.getFloorY();
        if (background != null) {
            g2d.drawImage(background, 0, 0, field.getWidth(), floorY, null);
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

    // ===================== Jugadores =====================

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

        // 3) Cabeza: se dibuja mirando al lado donde se requiera
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

        // El HitBox para que cuando le pegue funcione
        if (SHOW_HITBOX) {
            g2d.setColor(Color.YELLOW);
            g2d.drawOval(cx - r, cy - r, 2 * r, 2 * r);
            int fr = (int) c.getFootRadius();
            g2d.drawOval((int) c.getFootX() - fr, (int) c.getFootY() - fr, 2 * fr, 2 * fr);
        }

        // país sobre la cabeza
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        drawCentered(g2d, c.getCountry(), cx, cy - (int) (r * HEAD_SCALE) - 6, Color.WHITE);
        drawStatusEffects(g2d, c);
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

    /** Efectos que deja un tiro especial sobre el personaje golpeado. */
    private void drawStatusEffects(Graphics2D g2d, Character c) {
        int cx = (int) c.getX(), cy = (int) c.getY(), r = (int) c.getRadius();
        double phase = animTick * 0.25;
        int labelY = cy - (int) (r * HEAD_SCALE) - 42;
        Stroke old = g2d.getStroke();
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));

        if (c.isPushed()) {                                             // Aura azul: empujado
            glow(g2d, cx, cy, r * 1.7, new Color(60, 150, 255));
            int dir = c.getPushDirection();
            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(170, 215, 255, 200));
            for (int i = 0; i < 5; i++) {                               // rayas de viento
                int ly = cy - r + i * (2 * r / 4);
                int lx = cx - dir * (r + 8 + (animTick * 9 + i * 17) % 45);
                g2d.drawLine(lx, ly, lx - dir * 30, ly);
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            drawTag(g2d, "EMPUJADO", cx, labelY, new Color(120, 190, 255));
        }
        if (c.isElectrocuted()) {                                       // Rayo: chispas
            Random rnd = new Random(animTick / 2 * 104729L);
            g2d.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int b = 0; b < 6; b++) {
                double ang = rnd.nextDouble() * 2 * Math.PI;
                int px = cx + (int) (Math.cos(ang) * r), py = cy + (int) (Math.sin(ang) * r);
                for (int k = 0; k < 3; k++) {
                    int nx = px + rnd.nextInt(21) - 10 + (int) (Math.cos(ang) * 8);
                    int ny = py + rnd.nextInt(21) - 10 + (int) (Math.sin(ang) * 8);
                    g2d.setColor(k % 2 == 0 ? Color.WHITE : new Color(255, 235, 70));
                    g2d.drawLine(px, py, nx, ny);
                    px = nx;
                    py = ny;
                }
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            drawTag(g2d, "ELECTROCUTADO", cx, labelY, new Color(255, 235, 70));
        }
        if (c.isUnconscious()) {                                        // Tornado: estrellas girando
            for (int i = 0; i < 3; i++) {
                double ang = phase + i * 2 * Math.PI / 3;
                drawStar(g2d, cx + Math.cos(ang) * r * 0.9, cy - r - 18 + Math.sin(ang) * 7, 8, new Color(255, 225, 60));
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            drawTag(g2d, "INCONSCIENTE", cx, labelY, new Color(255, 170, 60));
        }
        if (c.isConfused()) {                                           // Ilusión: signos de pregunta
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            for (int i = 0; i < 3; i++) {
                int qy = cy - r - 14 - (int) (6 * Math.sin(phase * 1.5 + i * 1.7));
                drawTag(g2d, "?", cx - 26 + i * 26, qy, new Color(210, 130, 255));
            }
            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            drawTag(g2d, "CONFUNDIDO", cx, labelY, new Color(215, 150, 255));
        }
        g2d.setStroke(old);
    }

    /** Estrella de 5 puntas (para el efecto de inconsciente). */
    private void drawStar(Graphics2D g2d, double cx, double cy, double rad, Color color) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double rr = (i % 2 == 0) ? rad : rad * 0.45;
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + Math.cos(ang) * rr, py = cy + Math.sin(ang) * rr;
            if (i == 0) star.moveTo(px, py); else star.lineTo(px, py);
        }
        star.closePath();
        g2d.setColor(color);
        g2d.fill(star);
    }

    /**
     * Dibuja el balon del partido
     * @param g2d
     */
    private void drawBall(Graphics2D g2d, Match match) {
        Ball ball = match.getBall();
        Shot shot = ball.getShot();
        if (shot != null) {
            for (Decoy d : shot.getDecoys()) {                 // balones falsos de la Ilusión
                drawShotAura(g2d, Shot.Type.ILLUSION, d.getX(), d.getY(), ball.getRadius(), 0);
                drawBallAt(g2d, d.getX(), d.getY(), ball.getRadius());
            }
            if (shot.isFlying() || !shot.getDecoys().isEmpty()) {
                drawShotAura(g2d, shot.getType(), ball.getX(), ball.getY(), ball.getRadius(), ball.getVx());
            }
        }
        drawBallAt(g2d, ball.getX(), ball.getY(), ball.getRadius());
    }

    /** Dibuja una pelota (la real y las falsas se ven exactamente igual). */
    private void drawBallAt(Graphics2D g2d, double bx, double by, double br) {
        int d = (int) (br * 2);
        int drawX = (int) (bx - br);
        int drawY = (int) (by - br);

        if (ballImage != null) {
            double angle = bx / br;                          // gira al moverse en X
            Graphics2D b = (Graphics2D) g2d.create();
            b.rotate(angle, bx, by);                         // rota alrededor de su centro
            b.drawImage(ballImage, drawX, drawY, d, d, null);
            b.dispose();
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(drawX, drawY, d, d);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(drawX, drawY, d, d);
        }
    }

    private static Color shotColor(Shot.Type type) {
        switch (type) {
            case AURA:      return new Color(60, 150, 255);
            case LIGHTNING: return new Color(255, 235, 70);
            case TORNADO:   return new Color(170, 150, 120);
            default:        return new Color(190, 90, 255);   // ILLUSION
        }
    }

    /** Resplandor: círculos concéntricos translúcidos, más opacos hacia el centro. */
    private void glow(Graphics2D g2d, double x, double y, double radius, Color c) {
        for (int i = 4; i >= 1; i--) {
            double rr = radius * i / 4.0;
            int alpha = 28 + (4 - i) * 22;
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, alpha)));
            g2d.fill(new Ellipse2D.Double(x - rr, y - rr, 2 * rr, 2 * rr));
        }
    }

    /** Animación que rodea al balón según el tipo de tiro. */
    private void drawShotAura(Graphics2D g2d, Shot.Type type, double x, double y, double r, double vx) {
        double phase = animTick * 0.35;
        int dir = vx >= 0 ? 1 : -1;
        Stroke old = g2d.getStroke();
        Color c = shotColor(type);
        switch (type) {
            case AURA:
            case LIGHTNING:
                for (int i = 1; i <= 8; i++) {                         // estela detrás del balón
                    double s = r * (1.05 - i * 0.09);
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(10, 150 - i * 17)));
                    g2d.fill(new Ellipse2D.Double(x - dir * i * 15 - s, y - s, 2 * s, 2 * s));
                }
                glow(g2d, x, y, r * (1.9 + 0.2 * Math.sin(phase)), c);
                if (type == Shot.Type.LIGHTNING) {                     // rayos que cambian cada 2 cuadros
                    Random rnd = new Random(animTick / 2 * 7919L);
                    g2d.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    for (int b = 0; b < 4; b++) {
                        double ang = rnd.nextDouble() * 2 * Math.PI;
                        double px = x + Math.cos(ang) * r * 0.8, py = y + Math.sin(ang) * r * 0.8;
                        for (int k = 0; k < 4; k++) {
                            double a2 = ang + (rnd.nextDouble() - 0.5) * 1.2;
                            double nx = px + Math.cos(a2) * r * 0.55, ny = py + Math.sin(a2) * r * 0.55;
                            g2d.setColor(k % 2 == 0 ? Color.WHITE : new Color(255, 240, 90));
                            g2d.drawLine((int) px, (int) py, (int) nx, (int) ny);
                            px = nx;
                            py = ny;
                        }
                    }
                }
                break;
            case TORNADO:
                for (int i = 0; i < 6; i++) {                          // embudo que gira
                    double w = r * (1.3 + i * 0.65);
                    double yy = y + r * 0.9 - i * r * 0.75;
                    double off = Math.sin(phase * 1.5 + i) * 5;
                    double start = (phase * 70 + i * 60) % 360;
                    g2d.setStroke(new BasicStroke(6f - i * 0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.setColor(new Color(70, 55, 40, 170));          // contorno oscuro para dar contraste
                    g2d.draw(new Arc2D.Double(x - w + off, yy - 7, 2 * w, 14, start, 230, Arc2D.OPEN));
                    g2d.setStroke(new BasicStroke(3.4f - i * 0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.setColor(new Color(240, 230, 205, 235 - i * 15));
                    g2d.draw(new Arc2D.Double(x - w + off, yy - 7, 2 * w, 14, start, 230, Arc2D.OPEN));
                }
                for (int i = 1; i <= 5; i++) {                         // polvo que deja en el suelo
                    double s = 6 + (animTick + i * 3) % 7;
                    g2d.setColor(new Color(120, 100, 75, 170 - i * 25));
                    g2d.fill(new Ellipse2D.Double(x - dir * i * 18 - s, y + r - s, 2 * s, 2 * s));
                }
                break;
            default:                                                    // ILLUSION: halo violeta igual en los 8
                glow(g2d, x, y, r * (1.6 + 0.15 * Math.sin(phase)), c);
                break;
        }
        g2d.setStroke(old);
    }

    /** Onda que se expande en el punto donde sale el tiro. */
    private void drawLaunchBurst(Graphics2D g2d) {
        if (burstFrames <= 0 || burstType == null) return;
        double progress = 1 - burstFrames / 24.0;
        Color c = shotColor(burstType);
        Stroke old = g2d.getStroke();
        for (int i = 0; i < 2; i++) {
            double rr = 26 + (progress - i * 0.15) * 110;
            if (rr <= 0) continue;
            int alpha = (int) (Math.max(0, 1 - progress) * 220);
            g2d.setStroke(new BasicStroke(6f - i * 2));
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            g2d.draw(new Ellipse2D.Double(burstX - rr, burstY - rr, 2 * rr, 2 * rr));
        }
        g2d.setStroke(old);
    }

// HUD

    private void drawHud(Graphics2D g2d, Match match, Field field) {
        int w = field.getWidth();
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(w / 2 - 190, 8, 380, 66, 20, 20);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
        String marcador = match.getPlayer(0).getCountry() + "  " + match.getScoreboard().getGoals(0)
                + " - " + match.getScoreboard().getGoals(1) + "  " + match.getPlayer(1).getCountry();
        drawCentered(g2d, marcador, w / 2, 40, Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        String tiempo = match.isGoldenGoal() ? "GOL DE ORO" : String.valueOf((int) Math.ceil(match.getTimeLeft())) + " s";
        drawCentered(g2d, tiempo, w / 2, 64, new Color(255, 215, 0));

        // superbars
        drawSuperBar(g2d, match.getPlayer(0), 40, 20, "SUPER [Q]");
        drawSuperBar(g2d, match.getPlayer(1), w - 180, 20, "SUPER [CTRL-DER]");
        drawHealthBar(g2d, match.getPlayer(0), 40, 20, "Vida");
        drawHealthBar(g2d, match.getPlayer(1), w - 180, 20, "vida");
        // Super affections being drawn
        for (int i = 0; i < 2; i++) {
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

    // BARRA DE PODERES
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
        String text = c.isSuperReady() ? label + " (¡LISTO!)" : label + " (" + (int) (progress * 100) + "%)";
        g2d.drawString(text, x, y + 12);
    }

    // BARRA DE VIDA
    private void drawHealthBar(Graphics2D g2d, Character c, int x, int y, String label) {
        int barW = 140;
        int barH = 14;
        double progress = Math.min(1.0, c.getHealth() / c.getMaxHealth());

        // Fondo gris
        g2d.setColor(new Color(40, 40, 40, 200));
        g2d.fillRoundRect(x, y + 32, barW, barH, 6, 6);

        // Rojo oscuro si está aturdido, verde si no
        if (c.isStunned()) {
            g2d.setColor(new Color(77, 0, 0));
        } else {
            g2d.setColor(new Color(47, 186, 88));
        }
        g2d.fillRoundRect(x, y + 32, (int) (barW * progress), barH, 6, 6);

        // Borde
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y + 32, barW, barH, 6, 6);
    }

        /**
     * Mensajes del partido ("Gano el partido ...., Gol de oro")
     * @param g2d
     * @param field
     */
    private void drawMessages(Graphics2D g2d, Match match, Field field) {
        String msg = null;
        String pointsMsg = null;   // mensaje pequeño debajo: puntos ganados
        if (!match.isRunning()) {
            Character winner = match.getWinner();
            if (winner != null) {
                int winnerNumber = (winner == match.getPlayer(0)) ? 1 : 2;
                int loserNumber = 3 - winnerNumber;
                msg = "¡Ganó Jugador " + winnerNumber + "!";
                pointsMsg = "Jugador " + winnerNumber + " ganó " + PlayerStats.WIN_POINTS + " puntos"
                          + "   ·   Jugador " + loserNumber + " ganó " + PlayerStats.LOSS_POINTS + " puntos";
            } else {
                msg = "EMPATE";
                pointsMsg = "Cada jugador ganó " + PlayerStats.LOSS_POINTS + " puntos";
            }
        } else if (match.isPaused()) {
            msg = "PAUSA";
        }
        if (msg == null) {
            return;
        }
        int centerX = field.getWidth() / 2;
        int centerY = field.getHeight() / 2;
        int bandHeight = (pointsMsg != null) ? 135 : 100;   // franja más alta si hay mensaje de puntos

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, centerY - 60, field.getWidth(), bandHeight);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 44));
        drawCentered(g2d, msg, centerX, centerY + 8, Color.WHITE);

        if (pointsMsg != null) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
            drawCentered(g2d, pointsMsg, centerX, centerY + 50, new Color(255, 215, 0));
        }
    }

    // ===================== Utilidades =====================

    private void drawCentered(Graphics2D g2d, String text, int cx, int baselineY, Color color) {
        int w = g2d.getFontMetrics().stringWidth(text);
        g2d.setColor(color);
        g2d.drawString(text, cx - w / 2, baselineY);
    }

    /** Texto centrado con sombra negra, para que se lea sobre cualquier fondo. */
    private void drawTag(Graphics2D g2d, String text, int cx, int baselineY, Color color) {
        drawCentered(g2d, text, cx + 1, baselineY + 1, Color.BLACK);
        drawCentered(g2d, text, cx, baselineY, color);
    }

    /** Convierte el nombre de un país en el nombre de su archivo de imagen. */
    private static String key(String country) {
        return Normalizer.normalize(country, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    /** Carga la imagen de la cara del país (o la genérica si no existe). */
    private Image loadFace(String country) {
        java.net.URL url = getClass().getResource("/images/" + key(country) + ".png");
        if (url == null) {
            url = getClass().getResource("/images/face.png");
        }
        if (url == null) {
            return null;                              // no hay archivo, no hay imagen
        } else {
            return new ImageIcon(url).getImage();     // hay archivo, carga la imagen
        }
    }

    /** Recorta los bordes transparentes para que la imagen coincida con el círculo real. */
    private static Image trimTransparent(Image img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return img;

        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (bi.getRGB(x, y) >>> 24) & 0xFF;
                if (alpha > 20) {                       // píxel visible
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) return img;                       // imagen totalmente transparente
        return bi.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}