

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Botón con imagen que crece al pasar el mouse por encima y avisa
 * qué texto explicativo mostrar.
 *
 * Uso:
 *   HoverImageButton b = new HoverImageButton(imagen, "Texto explicativo", 1.12);
 *   b.setOnHoverChange(texto -> { ... mostrar texto, o nada si es null ... });
 *   b.setBaseBounds(x, y, ancho, alto);   // en doLayout(), en vez de setBounds
 */
public class HoverImageButton extends JButton {

    private final Image image;          // imagen original, sin escalar
    private final String description;   // texto que se muestra al pasar el mouse
    private final double hoverScale;    // cuánto crece (1.12 = 12 % más grande)

    private final Rectangle base = new Rectangle();   // posición y tamaño normales
    private ImageIcon normalIcon, bigIcon;            // íconos ya escalados (se calculan una vez por tamaño)
    private boolean hovered = false;
    private Consumer<String> onHoverChange = text -> { };   // por defecto no hace nada

    public HoverImageButton(Image image, String description, double hoverScale) {
        this.image = image;
        this.description = description;
        this.hoverScale = hoverScale;

        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setHovered(true); }

            @Override
            public void mouseExited(MouseEvent e)  { setHovered(false); }
        });
    }

    /**
     * Quién quiera mostrar el texto explicativo se suscribe aquí.
     * Recibe la descripción al entrar el mouse, y null al salir.
     */
    public void setOnHoverChange(Consumer<String> listener) {
        this.onHoverChange = (listener != null) ? listener : text -> { };
    }

    /**
     * Posición y tamaño normales del botón. Úsalo en doLayout() en lugar de setBounds:
     * el botón se encarga de agrandarse cuando tiene el mouse encima.
     */
    public void setBaseBounds(int x, int y, int width, int height) {
        if (width != base.width || height != base.height) {
            rescaleIcons(width, height);   // solo si cambió el tamaño
        }
        base.setBounds(x, y, width, height);
        applyBounds();
    }

    /** Quita el efecto (útil si se abre algo encima y el mouse nunca "sale"). */
    public void clearHover() {
        setHovered(false);
    }

    public boolean isHovered()       { return hovered; }
    public String getDescription()   { return description; }
    public Rectangle getBaseBounds() { return new Rectangle(base); }


    private void setHovered(boolean value) {
        if (hovered == value) return;
        hovered = value;
        applyBounds();
        onHoverChange.accept(hovered ? description : null);
    }

    /** Coloca el botón: tamaño normal, o agrandado desde su centro si tiene el mouse encima. */
    private void applyBounds() {
        if (hovered) {
            int bigW = (int) (base.width * hoverScale);
            int bigH = (int) (base.height * hoverScale);
            setBounds(base.x - (bigW - base.width) / 2, base.y - (bigH - base.height) / 2, bigW, bigH);
            if (bigIcon != null) setIcon(bigIcon);
        } else {
            setBounds(base);
            if (normalIcon != null) setIcon(normalIcon);
        }
    }

    private void rescaleIcons(int width, int height) {
        if (image == null || image.getWidth(null) <= 0 || width <= 0 || height <= 0) return;
        normalIcon = new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        bigIcon = new ImageIcon(image.getScaledInstance(
                (int) (width * hoverScale), (int) (height * hoverScale), Image.SCALE_SMOOTH));
    }
}