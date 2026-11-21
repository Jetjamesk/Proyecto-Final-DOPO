package Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import domain.*;
import domain.Character;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Pruebas del paquete domain de POOBSoccer (JUnit 4).
 * Cada bloque corresponde a una clase del dominio.
 * No usan hilos ni dependen de la interfaz gráfica.
 */
public class POOBSoccerTest {

    private static final double EPS = 1e-6;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private Field field;
    private Character canada;
    private Character brasil;

    @Before
    public void setUp() {
        field = new Field(1000, 600, 180);
        canada = new Canada();
        brasil = new Brasil();
    }

    // ---------- utilidades ----------

    /** Pone al personaje en el suelo en la posición x dada. */
    private Character placed(Character c, double x) {
        c.setStartX(x);
        c.reset(field);
        return c;
    }

    private TimedMatch newMatch(int seconds) {
        return new TimedMatch(new Canada(), new Brasil(), new Field(1000, 600, 180), new Ball(0, 0), seconds);
    }

    private void assertMessage(String expected, Runnable r) {
        try {
            r.run();
            fail("Debía lanzar POOBSoccerException: " + expected);
        } catch (IllegalStateException e) {
            assertEquals(expected, e.getCause().getMessage());
        }
    }

    private Runnable opening(File f) {
        return () -> {
            try { Match.open(f); } catch (POOBSoccerException e) { throw new IllegalStateException(e); }
        };
    }

    private Runnable saving(Match m, File f) {
        return () -> {
            try { m.save(f); } catch (POOBSoccerException e) { throw new IllegalStateException(e); }
        };
    }

    // =====================================================================
    // Ball / Physics
    // =====================================================================

    @Test
    public void ball_resetPonePosicionYQuitaVelocidad() {
        Ball b = new Ball(0, 0);
        b.update(0.1, field);
        b.reset(500, 150);
        assertEquals(500, b.getX(), EPS);
        assertEquals(150, b.getY(), EPS);
        assertEquals(0, b.getVx(), EPS);
        assertEquals(0, b.getVy(), EPS);
    }

    @Test
    public void ball_laGravedadLaHaceCaer() {
        Ball b = new Ball(500, 100);
        b.update(0.1, field);
        assertTrue(b.getY() > 100);
        assertTrue(b.getVy() > 0);
    }

    @Test
    public void ball_reboteEnElSueloNoLaDejaAtravesar() {
        Ball b = new Ball(500, 545);
        b.update(0.016, field);
        assertTrue(b.getY() + b.getRadius() <= field.getFloorY() + EPS);
    }

    @Test
    public void ball_colisionConCirculoDevuelveTrueSoloSiSeTocan() {
        Ball b = new Ball(500, 300);
        assertFalse(b.collideWithCircle(100, 300, 35, 0, 0));
        assertTrue(b.collideWithCircle(520, 300, 35, 0, 0));
    }

    @Test
    public void ball_radioEs20() {
        assertEquals(20, new Ball(0, 0).getRadius(), EPS);
    }

    // =====================================================================
    // Collisions
    // =====================================================================

    @Test
    public void collisions_pelotaNoSaleDeLaCanchaPorLaIzquierda() {
        Ball b = new Ball(5, 100);              // medio cuerpo fuera de la cancha
        Collisions.bounceOffBounds(b, field, 0.78, 0.95);
        assertEquals(b.getRadius(), b.getX(), EPS);
    }

    @Test
    public void collisions_pelotaRebotaEnElSuelo() {
        Ball b = new Ball(500, 545);            // hundida en el suelo
        Collisions.bounceOffBounds(b, field, 0.78, 0.95);
        assertEquals(field.getFloorY() - b.getRadius(), b.getY(), EPS);
    }

    @Test
    public void collisions_bounceOffCircleSeparaLosCuerpos() {
        Ball b = new Ball(500, 300);
        assertTrue(Collisions.bounceOffCircle(b, 0.78, 520, 300, 35, 0, 0));
        double dist = Math.hypot(b.getX() - 520, b.getY() - 300);
        assertEquals(b.getRadius() + 35, dist, 1e-6);
    }

    @Test
    public void collisions_bounceOffCircleSinContactoNoHaceNada() {
        Ball b = new Ball(100, 100);
        assertFalse(Collisions.bounceOffCircle(b, 0.78, 800, 300, 35, 0, 0));
        assertEquals(100, b.getX(), EPS);
    }

    @Test
    public void collisions_travesanoDevuelveLaPelotaPorEncima() {
        double top = field.getGoalTopY();
        Ball b = new Ball(40, top - 5);   // pegada al travesaño izquierdo
        Collisions.bounceOffCrossbars(b, field, 0.78);
        assertEquals(top - b.getRadius(), b.getY(), EPS);
    }

    // =====================================================================
    // Field
    // =====================================================================

    @Test
    public void field_alturaDelSueloYDelTravesano() {
        assertEquals(550, field.getFloorY());
        assertEquals(370, field.getGoalTopY());
    }

    @Test
    public void field_golEnElArcoIzquierdo() {
        assertEquals(0, field.goalConcededBy(new Ball(30, 500)));
    }

    @Test
    public void field_golEnElArcoDerecho() {
        assertEquals(1, field.goalConcededBy(new Ball(970, 500)));
    }

    @Test
    public void field_centroDeCanchaNoEsGol() {
        assertEquals(-1, field.goalConcededBy(new Ball(500, 500)));
    }

    @Test
    public void field_porEncimaDelTravesanoNoEsGol() {
        assertEquals(-1, field.goalConcededBy(new Ball(30, 300)));
    }

    // =====================================================================
    // Scoreboard
    // =====================================================================

    @Test
    public void scoreboard_inicioEnCerosYEmpatado() {
        Scoreboard s = new Scoreboard();
        assertEquals(0, s.getGoals(0));
        assertEquals(0, s.getGoals(1));
        assertTrue(s.isTied());
    }

    @Test
    public void scoreboard_golRecibidoEsPuntoDelRival() {
        Scoreboard s = new Scoreboard();
        s.goalConcededBy(0);
        assertEquals(0, s.getGoals(0));
        assertEquals(1, s.getGoals(1));
    }

    @Test
    public void scoreboard_lideresYEmpate() {
        Scoreboard s = new Scoreboard();
        assertEquals(-1, s.leader());
        s.goalConcededBy(1);
        assertEquals(0, s.leader());
        s.goalConcededBy(0);
        assertEquals(-1, s.leader());
        assertTrue(s.isTied());
    }

    // =====================================================================
    // PlayerStats
    // =====================================================================

    @Test
    public void playerStats_inicioNivel1SinPuntos() {
        PlayerStats p = new PlayerStats();
        assertEquals(0, p.getPoints());
        for (PlayerStats.Stat s : PlayerStats.Stat.values()) {
            assertEquals(1, p.getLevel(s));
        }
    }

    @Test
    public void playerStats_recompensaPorPartido() {
        PlayerStats p = new PlayerStats();
        p.rewardMatch(true);
        assertEquals(PlayerStats.WIN_POINTS, p.getPoints());
        p.rewardMatch(false);
        assertEquals(PlayerStats.WIN_POINTS + PlayerStats.LOSS_POINTS, p.getPoints());
    }

    @Test
    public void playerStats_noMejoraSinPuntos() {
        PlayerStats p = new PlayerStats();
        assertFalse(p.upgrade(PlayerStats.Stat.SPEED));
        assertEquals(1, p.getLevel(PlayerStats.Stat.SPEED));
    }

    @Test
    public void playerStats_mejoraGastaPuntosYDuplicaElCosto() {
        PlayerStats p = new PlayerStats();
        p.addPoints(500);
        assertEquals(500, p.upgradeCost(PlayerStats.Stat.JUMP));
        assertTrue(p.upgrade(PlayerStats.Stat.JUMP));
        assertEquals(2, p.getLevel(PlayerStats.Stat.JUMP));
        assertEquals(0, p.getPoints());
        assertEquals(1000, p.upgradeCost(PlayerStats.Stat.JUMP));
    }

    @Test
    public void playerStats_nivelMaximoEs10YPuntosNegativosSeIgnoran() {
        PlayerStats p = new PlayerStats();
        p.addPoints(-50);
        assertEquals(0, p.getPoints());
        p.addPoints(1_000_000);
        for (int i = 0; i < 9; i++) assertTrue(p.upgrade(PlayerStats.Stat.KICK));
        assertEquals(PlayerStats.MAX_LEVEL, p.getLevel(PlayerStats.Stat.KICK));
        assertFalse(p.upgrade(PlayerStats.Stat.KICK));
    }

    // =====================================================================
    // CharacterStats
    // =====================================================================

    @Test
    public void characterStats_valoresBaseConNivel1() {
        CharacterStats s = new CharacterStats();
        assertEquals(400, s.moveSpeed(), EPS);
        assertEquals(700, s.jumpVelocity(), EPS);
        assertEquals(100, s.maxHealth(), EPS);
        assertEquals(10, s.kickDamage(), EPS);
    }

    @Test
    public void characterStats_mejorarVelocidadSubeUn10Porciento() {
        PlayerStats p = new PlayerStats();
        p.addPoints(500);
        p.upgrade(PlayerStats.Stat.SPEED);
        CharacterStats s = new CharacterStats();
        s.setProgress(p);
        assertEquals(440, s.moveSpeed(), EPS);
    }

    @Test
    public void characterStats_mejorarVidaYPatada() {
        PlayerStats p = new PlayerStats();
        p.addPoints(1000);
        p.upgrade(PlayerStats.Stat.SURVIVAL);
        p.upgrade(PlayerStats.Stat.KICK);
        CharacterStats s = new CharacterStats();
        s.setProgress(p);
        assertEquals(110, s.maxHealth(), EPS);
        assertEquals(11, s.kickDamage(), EPS);
    }

    @Test
    public void characterStats_progresoNuloCreaUnoNuevo() {
        CharacterStats s = new CharacterStats();
        s.setProgress(null);
        assertNotNull(s.getProgress());
        assertEquals(1, s.getSpeed());
    }

    // =====================================================================
    // LegPhysics
    // =====================================================================

    @Test
    public void leg_patearActivaLaPatadaYTermina() {
        LegPhysics leg = new LegPhysics();
        assertFalse(leg.isKicking());
        leg.kick();
        assertTrue(leg.isKicking());
        leg.update(0.3, 100, 300, 1, 1.0, 550);
        assertFalse(leg.isKicking());
    }

    @Test
    public void leg_marcarGolpeYReiniciarEnNuevaPatada() {
        LegPhysics leg = new LegPhysics();
        leg.kick();
        leg.markHit();
        assertTrue(leg.hasHit());
        leg.update(0.3, 100, 300, 1, 1.0, 550);
        leg.kick();
        assertFalse(leg.hasHit());
    }

    @Test
    public void leg_resetDetienePatada() {
        LegPhysics leg = new LegPhysics();
        leg.kick();
        leg.markHit();
        leg.reset();
        assertFalse(leg.isKicking());
        assertFalse(leg.hasHit());
    }

    @Test
    public void leg_elPieSeColocaSegunHaciaDondeMira() {
        LegPhysics der = new LegPhysics();
        der.update(0, 500, 300, 1, 1.0, 550);
        assertTrue(der.getFootX() > 500);
        LegPhysics izq = new LegPhysics();
        izq.update(0, 500, 300, -1, 1.0, 550);
        assertTrue(izq.getFootX() < 500);
    }

    @Test
    public void leg_elPieNuncaAtraviesaElSuelo() {
        LegPhysics leg = new LegPhysics();
        leg.update(0, 500, 540, 1, 1.0, 550);
        assertTrue(leg.getFootY() <= 550 + EPS);
    }

    // =====================================================================
    // Character (Canada / Brasil)
    // =====================================================================

    @Test
    public void character_resetLoDejaEnElSueloEnSuPosicionInicial() {
        placed(canada, 250);
        assertEquals(250, canada.getX(), EPS);
        assertEquals(0, canada.getVx(), EPS);
        assertEquals(0, canada.getVy(), EPS);
    }

    @Test
    public void character_semueveALaDerechaSegunSuVelocidad() {
        placed(canada, 250);
        canada.movement(1);
        canada.update(0.1, field);
        assertEquals(290, canada.getX(), EPS);   // 400 px/s * 0.1 s
    }

    @Test
    public void character_noSalteEnElAire() {
        placed(canada, 250);
        canada.jump();
        canada.update(0.01, field);
        double vy = canada.getVy();
        assertTrue(vy < 0);
        canada.jump();                       // en el aire: debe ignorarse
        assertEquals(vy, canada.getVy(), EPS);
    }

    @Test
    public void character_danoYAturdimiento() {
        placed(canada, 250);
        canada.takeDamage(10);
        assertEquals(90, canada.getHealth(), EPS);
        canada.takeDamage(1000);
        assertEquals(0, canada.getHealth(), EPS);
        assertTrue(canada.isStunned());
        canada.takeDamage(10);               // aturdido: no recibe más daño
        assertEquals(0, canada.getHealth(), EPS);
        canada.update(Character.STUN_DURATION + 0.1, field);
        assertFalse(canada.isStunned());
        assertEquals(canada.getMaxHealth(), canada.getHealth(), EPS);
    }

    @Test
    public void character_superSoloDisponibleTrasElCooldown() {
        placed(canada, 250);
        assertFalse(canada.isSuperReady());
        canada.update(Character.SUPER_COOLDOWN + 0.5, field);
        assertTrue(canada.isSuperReady());
        placed(brasil, 750);
        canada.activateSuper(brasil);
        assertFalse(canada.isSuperReady());
        assertTrue(brasil.isShrink());        // el poder de Canada es ShrinkPower
    }

    @Test
    public void character_controlesInvertidosPorElPoder() {
        placed(brasil, 500);
        new InvertPower().use(brasil);
        assertTrue(brasil.isInverted());
        brasil.movement(1);                   // pide derecha, va a la izquierda
        brasil.update(0.1, field);
        assertTrue(brasil.getX() < 500);
    }

    @Test
    public void character_encogimientoReduceElRadioYTermina() {
        placed(brasil, 500);
        double normal = brasil.getRadius();
        new ShrinkPower().use(brasil);
        assertEquals(normal / 2, brasil.getRadius(), EPS);
        brasil.update(6.1, field);
        assertFalse(brasil.isShrink());
        assertEquals(normal, brasil.getRadius(), EPS);
    }

    @Test
    public void character_facingNuncaEsCero() {
        canada.setFacing(0);
        assertEquals(1, canada.getFacing());
        canada.setFacing(-5);
        assertEquals(-1, canada.getFacing());
    }

    @Test
    public void character_cadaPersonajeTieneSuPais() {
        assertEquals("Canada", canada.getCountry());
        assertEquals("Brasil", brasil.getCountry());
    }

    // =====================================================================
    // Los personajes nuevos (SouthKorea, Cameroon, Nigeria, UnitedStates)
    // =====================================================================

    @Test
    public void personajesNuevos_seCreanConPaisYEstadisticasBase() {
        List<Character> nuevos = new ArrayList<>(Arrays.asList(
                new SouthKorea(), new Cameroon(), new Nigeria(), new UnitedStates()));
        for (Character c : nuevos) {
            assertNotNull(c.getCountry());
            assertFalse(c.getCountry().isEmpty());
            assertEquals(100, c.getMaxHealth(), EPS);
            assertEquals(c.getMaxHealth(), c.getHealth(), EPS);
        }
    }

    @Test
    public void personajesNuevos_tienenPoderFuncional() {
        List<Character> nuevos = new ArrayList<>(Arrays.asList(
                new SouthKorea(), new Cameroon(), new Nigeria(), new UnitedStates()));
        for (Character c : nuevos) {
            Character rival = placed(new Brasil(), 750);
            placed(c, 250);
            c.update(Character.SUPER_COOLDOWN + 0.5, field);
            c.activateSuper(rival);
            assertTrue(c.getCountry() + " debía afectar al rival", rival.isInverted() || rival.isShrink());
        }
    }

    // =====================================================================
    // Power
    // =====================================================================

    @Test
    public void power_invertPowerInvierteAlRival() {
        placed(brasil, 500);
        new InvertPower().use(brasil);
        assertTrue(brasil.isInverted());
    }

    @Test
    public void power_shrinkPowerEncogeAlRival() {
        placed(brasil, 500);
        new ShrinkPower().use(brasil);
        assertTrue(brasil.isShrink());
        assertEquals(0.5, brasil.getScale(), EPS);
    }

    @Test
    public void power_elEfectoDeInvertirDuraSeisSegundos() {
        placed(brasil, 500);
        new InvertPower().use(brasil);
        brasil.update(5.9, field);
        assertTrue(brasil.isInverted());
        brasil.update(0.2, field);
        assertFalse(brasil.isInverted());
    }

    // =====================================================================
    // MatchPhysics
    // =====================================================================

    @Test
    public void matchPhysics_losPersonajesNoSeAtraviesan() {
        Character a = placed(new Canada(), 500);
        Character b = placed(new Brasil(), 500);   // mismo punto a propósito
        MatchPhysics world = new MatchPhysics(field, new Ball(500, 100), Arrays.asList(a, b));
        world.step(0.001);
        double dist = Math.hypot(b.getX() - a.getX(), b.getY() - a.getY());
        assertTrue(dist >= a.getRadius() + b.getRadius() - 1e-6);
    }

    @Test
    public void matchPhysics_laPelotaCaeConLaGravedad() {
        Character a = placed(new Canada(), 250);
        Character b = placed(new Brasil(), 750);
        Ball ball = new Ball(500, 100);
        MatchPhysics world = new MatchPhysics(field, ball, Arrays.asList(a, b));
        world.step(0.05);
        assertTrue(ball.getY() > 100);
    }

    @Test
    public void matchPhysics_laPelotaTocandoElCuerpoSeSepara() {
        Character a = placed(new Canada(), 250);
        Character b = placed(new Brasil(), 750);
        Ball ball = new Ball(a.getX() + 10, a.getY());   // metida dentro de la cabeza
        MatchPhysics world = new MatchPhysics(field, ball, Arrays.asList(a, b));
        world.step(0.001);
        double dist = Math.hypot(ball.getX() - a.getX(), ball.getY() - a.getY());
        assertTrue(dist >= ball.getRadius() + a.getRadius() - 1e-3);
    }

    // =====================================================================
    // Match (a través de TimedMatch)
    // =====================================================================

    @Test
    public void match_alCrearseUbicaAJugadoresYBalon() {
        TimedMatch m = newMatch(60);
        assertTrue(m.isRunning());
        assertFalse(m.isPaused());
        assertEquals(250, m.getPlayer(0).getX(), EPS);
        assertEquals(750, m.getPlayer(1).getX(), EPS);
        assertEquals(1, m.getPlayer(0).getFacing());
        assertEquals(-1, m.getPlayer(1).getFacing());
        assertEquals(500, m.getBall().getX(), EPS);
    }

    @Test
    public void match_enPausaNoAvanzaElTiempo() {
        TimedMatch m = newMatch(60);
        m.pause();
        assertTrue(m.isPaused());
        m.updateMatch(1.0);
        assertEquals(60, m.getTimeLeft(), EPS);
        m.resume();
        m.updateMatch(1.0);
        assertEquals(59, m.getTimeLeft(), 1e-6);
    }

    @Test
    public void match_enPausaIgnoraLasOrdenes() {
        TimedMatch m = newMatch(60);
        m.pause();
        m.move(0, 1);
        m.updateMatch(0.5);
        assertEquals(250, m.getPlayer(0).getX(), EPS);
    }

    @Test
    public void match_finalizarDeclaraGanadorYDaPuntosUnaSolaVez() {
        TimedMatch m = newMatch(60);
        m.getScoreboard().goalConcededBy(1);     // anota el jugador 0
        m.endMatch();
        m.endMatch();                            // repetirlo no debe duplicar puntos
        assertFalse(m.isRunning());
        assertSame(m.getPlayer(0), m.getWinner());
        assertEquals(PlayerStats.WIN_POINTS, m.getPlayer(0).getPlayerStats().getPoints());
        assertEquals(PlayerStats.LOSS_POINTS, m.getPlayer(1).getPlayerStats().getPoints());
    }

    @Test
    public void match_empateNoTieneGanador() {
        TimedMatch m = newMatch(60);
        m.endMatch();
        assertNull(m.getWinner());
        assertEquals(PlayerStats.LOSS_POINTS, m.getPlayer(0).getPlayerStats().getPoints());
    }

    @Test
    public void match_usarPoderSoloConElSuperListo() {
        TimedMatch m = newMatch(60);
        m.usePower(0);                           // aún no está listo
        assertFalse(m.getPlayer(1).isShrink());
        m.getPlayer(0).update(Character.SUPER_COOLDOWN + 1, m.getField());
        m.usePower(0);
        assertTrue(m.getPlayer(1).isShrink());
    }

    // =====================================================================
    // TimedMatch
    // =====================================================================

    @Test
    public void timedMatch_empateAlAcabarElTiempoActivaGolDeOro() {
        TimedMatch m = newMatch(1);
        m.updateMatch(1.0);
        assertTrue(m.isGoldenGoal());
        assertTrue(m.isRunning());
    }

    @Test
    public void timedMatch_conVentajaAlAcabarElTiempoTerminaYGana() {
        TimedMatch m = newMatch(1);
        m.getScoreboard().goalConcededBy(1);
        m.updateMatch(1.0);
        assertFalse(m.isRunning());
        assertSame(m.getPlayer(0), m.getWinner());
    }

    @Test
    public void timedMatch_golSumaPuntoYReiniciaLaRonda() {
        TimedMatch m = newMatch(60);
        m.getBall().reset(30, 500);              // dentro del arco izquierdo
        m.updateMatch(0.01);
        assertEquals(1, m.getScoreboard().getGoals(1));
        assertEquals(500, m.getBall().getX(), EPS);
        assertTrue(m.isRunning());
    }

    @Test
    public void timedMatch_golDeOroTerminaElPartido() {
        TimedMatch m = newMatch(1);
        m.updateMatch(1.0);                      // empate -> gol de oro
        m.getBall().reset(970, 500);             // gol en el arco derecho
        m.updateMatch(0.01);
        assertFalse(m.isRunning());
        assertSame(m.getPlayer(0), m.getWinner());
    }

    // =====================================================================
    // GoalMatch
    // =====================================================================

    @Test
    public void goalMatch_terminaAlLlegarALosGolesObjetivo() {
        GoalMatch m = new GoalMatch(new Canada(), new Brasil(), new Field(1000, 600, 180), new Ball(0, 0), 2);
        m.getBall().reset(970, 500);
        m.updateMatch(0.01);
        assertTrue(m.isRunning());
        assertEquals(1, m.getScoreboard().getGoals(0));
        m.getBall().reset(970, 500);
        m.updateMatch(0.01);
        assertFalse(m.isRunning());
        assertSame(m.getPlayer(0), m.getWinner());
    }

    @Test
    public void goalMatch_enPausaNoProcesaGoles() {
        GoalMatch m = new GoalMatch(new Canada(), new Brasil(), new Field(1000, 600, 180), new Ball(0, 0), 1);
        m.pause();
        m.getBall().reset(970, 500);
        m.updateMatch(0.01);
        assertEquals(0, m.getScoreboard().getGoals(0));
    }

    // =====================================================================
    // BasicBot
    // =====================================================================

    @Test
    public void bot_seMueveHaciaElBalon() {
        TimedMatch m = newMatch(60);
        m.setBot(0, new BasicBot(0));
        assertTrue(m.isBot(0));
        assertFalse(m.isBot(1));
        m.getBall().reset(800, 100);             // balón lejos, a la derecha
        m.updateMatch(0.1);
        assertTrue(m.getPlayer(0).getX() > 250);
    }

    @Test
    public void bot_usaElPoderCuandoEstaListo() {
        TimedMatch m = newMatch(60);
        m.getPlayer(0).update(Character.SUPER_COOLDOWN + 1, m.getField());
        new BasicBot(0).Decide(m, 0, 0.016);
        assertTrue(m.getPlayer(1).isShrink());
    }

    @Test
    public void bot_noReaccionaAntesDeSuTiempoDePensar() {
        TimedMatch m = newMatch(60);
        BasicBot bot = new BasicBot(5.0);
        bot.Decide(m, 0, 0.016);                 // primera decisión: sí actúa
        m.getPlayer(0).update(Character.SUPER_COOLDOWN + 1, m.getField());
        bot.Decide(m, 0, 0.016);                 // aún "piensa": no usa el poder
        assertFalse(m.getPlayer(1).isShrink());
    }

    // =====================================================================
    // Persistencia: guardar y abrir (Match.save / Match.open)
    // =====================================================================

    @Test
    public void persistencia_guardarYAbrirConservaElEstado() throws Exception {
        TimedMatch m = newMatch(60);
        m.setBot(1, new BasicBot(0.2));
        m.getScoreboard().goalConcededBy(1);
        m.updateMatch(2.0);
        m.pause();
        File f = new File(tmp.getRoot(), "partida.poob");
        m.save(f);

        TimedMatch r = (TimedMatch) Match.open(f);
        assertEquals(m.getTimeLeft(), r.getTimeLeft(), EPS);
        assertEquals(1, r.getScoreboard().getGoals(0));
        assertEquals(m.getBall().getX(), r.getBall().getX(), EPS);
        assertEquals("Canada", r.getPlayer(0).getCountry());
        assertEquals("Brasil", r.getPlayer(1).getCountry());
        assertTrue(r.isPaused());
        assertTrue(r.isBot(1));
        assertFalse(r.isBot(0));
    }

    @Test
    public void persistencia_laPartidaAbiertaSePuedeSeguirJugando() throws Exception {
        TimedMatch m = newMatch(60);
        m.updateMatch(1.0);
        File f = new File(tmp.getRoot(), "seguir.poob");
        m.save(f);
        TimedMatch r = (TimedMatch) Match.open(f);
        r.updateMatch(1.0);
        assertEquals(58, r.getTimeLeft(), 1e-6);
    }

    @Test
    public void persistencia_elGanadorSigueSiendoUnoDeLosJugadores() throws Exception {
        TimedMatch m = newMatch(60);
        m.getScoreboard().goalConcededBy(1);
        m.endMatch();
        File f = new File(tmp.getRoot(), "fin.poob");
        m.save(f);
        Match r = Match.open(f);
        assertSame(r.getPlayer(0), r.getWinner());
    }

    @Test
    public void persistencia_extensionIncorrecta() throws Exception {
        File f = tmp.newFile("partida.txt");
        assertMessage(POOBSoccerException.WRONG_EXTENSION, opening(f));
        assertMessage(POOBSoccerException.WRONG_EXTENSION, saving(newMatch(60), f));
    }

    @Test
    public void persistencia_archivoNuloOInexistente() {
        assertMessage(POOBSoccerException.FILE_NULL, opening(null));
        assertMessage(POOBSoccerException.FILE_NULL, saving(newMatch(60), null));
        assertMessage(POOBSoccerException.FILE_NOT_FOUND, opening(new File(tmp.getRoot(), "no_existe.poob")));
    }

    @Test
    public void persistencia_archivoVacio() throws Exception {
        File f = tmp.newFile("vacio.poob");
        assertMessage(POOBSoccerException.FILE_EMPTY, opening(f));
    }

    @Test
    public void persistencia_archivoQueNoEsDelJuego() throws Exception {
        File f = tmp.newFile("falso.poob");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write("esto no es una partida".getBytes());
        }
        assertMessage(POOBSoccerException.NOT_POOBSccoer_FILE, opening(f));
    }

    @Test
    public void persistencia_archivoCorrupto() throws Exception {
        TimedMatch m = newMatch(60);
        File ok = new File(tmp.getRoot(), "ok.poob");
        m.save(ok);
        byte[] bytes = Files.readAllBytes(ok.toPath());
        File roto = new File(tmp.getRoot(), "roto.poob");
        Files.write(roto.toPath(), Arrays.copyOf(bytes, bytes.length / 2));
        assertMessage(POOBSoccerException.CORRUPT_DATA, opening(roto));
    }
}