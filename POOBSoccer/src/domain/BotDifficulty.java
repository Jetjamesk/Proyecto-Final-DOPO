package domain;

public enum BotDifficulty {
    TORPE("Torpe") {
        @Override
        public Bot createBot() { return new BasicBot(0.35); }
    },
    DEFENSIVA("Defensiva") {
        @Override
        public Bot createBot() { return new BasicBot(0.12); }
    },
    OFENSIVA("Ofensiva") {
        @Override
        public Bot createBot() { return new BasicBot(0.12); }
    },
    EXPERTA("Experta") {
        @Override
        public Bot createBot() { return new BasicBot(0.05); }
    };

    private final String displayName;

    BotDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Cada opción decide qué bot crear. */
    public abstract Bot createBot();
}