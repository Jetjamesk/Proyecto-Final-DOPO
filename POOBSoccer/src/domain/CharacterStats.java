package domain;


public class CharacterStats implements java.io.Serializable{

    public static final double BASE_MOVE_SPEED = 400;     // px/s con Speed = 1
    public static final double BASE_JUMP_VELOCITY = 700;  // px/s con Jump = 1
    public static final double BASE_MAX_HEALTH = 100;     // vida con Survival = 1
    public static final double BASE_KICK_DAMAGE = 10;     // daño de una patada con Kick = 1 (ajustable)

    private PlayerStats progress = new PlayerStats();

    
    public void setProgress(PlayerStats progress) {
        this.progress = (progress != null) ? progress : new PlayerStats();
    }

    public PlayerStats getProgress() { return progress; }



    
    private static double multiplier(int level) {
        return 1 + 0.10 * (level - 1);
    }

    
    public double moveSpeed() {
        return BASE_MOVE_SPEED * multiplier(getSpeed());
    }

    
    public double jumpVelocity() {
        return BASE_JUMP_VELOCITY * Math.sqrt(multiplier(getJump()));
    }


    public double kickMultiplier() {
        return multiplier(getKick());
    }


    public double kickDamage() {
        return BASE_KICK_DAMAGE * kickMultiplier();
    }


    public double maxHealth() {
        return BASE_MAX_HEALTH * multiplier(getSurvival());
    }

    public int getSpeed()    { return progress.getLevel(PlayerStats.Stat.SPEED); }
    public int getJump()     { return progress.getLevel(PlayerStats.Stat.JUMP); }
    public int getKick()     { return progress.getLevel(PlayerStats.Stat.KICK); }
    public int getSurvival() { return progress.getLevel(PlayerStats.Stat.SURVIVAL); }
}