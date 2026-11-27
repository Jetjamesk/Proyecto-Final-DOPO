package domain;

/**
 * Poder de South Korea
 *
 */
public class AuraPower extends ShotPower {
    /**
     * Crea el tiro de este poder
     * @param user EL personaje que usa el poder
     * @return un nuevo AuraShot asociado al persoanje que lo lanzo
     */
    @Override
    protected Shot createShot(Character user) {
        return new AuraShot(user);
    }

    /**
     * Indica cuando sale el tiro
     * @return Queda listo el poder. Pero queda armado y solo se lanza hasta que lo toque.
     */
    @Override
    protected boolean firesOnTouch() { return true; }
}