package domain;

/**
 * Niveles de dificultad del bot
 */
public enum BotDifficulty {
    TORPE("Torpe") {
        @Override
        public Bot createBot() { return new BasicBot(1); }
    },
    DEFENSIVA("Defensiva") {
        @Override
        public Bot createBot() { return new DefensiveBot(0.12); }
    },
    OFENSIVA("Ofensiva") {
        @Override
        public Bot createBot() { return new BasicBot(0.12); }
    },
    EXPERTA("Experta") {
        @Override
        public Bot createBot() { return new BasicBot(0.05); }
    };

    private final String displayName;  // Nombre que se muestra al usuario

    /**
     * Crea un nivel de dificultad
     * @param displayName
     */
    BotDifficulty(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return El nombre del nivel por mostar al usuario
     */
    public String getDisplayName() {
        return displayName;
    }

    /** Cada opción decide qué bot crear. */
    public abstract Bot createBot();
}