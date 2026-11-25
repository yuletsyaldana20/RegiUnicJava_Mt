package com.regiunic.core.ui.components;

import com.regiunic.core.ui.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Botón moderno con animación fluida de color en hover y respuesta táctil sutil.
 */
public final class AnimatedButton extends JButton {
    private static final int TRANSITION_MS = 140;
    private static final int TIMER_MS = 15;
    private static final float PRESSED_SCALE = 0.98f;

    private final Color normalBackground;
    private final Color hoverBackground;
    private final Color pressedBackground;
    private final Color normalForeground;
    private final Color hoverForeground;
    private final Timer transitionTimer;

    private Color currentBackground;
    private Color currentForeground;
    private Color transitionStart;
    private Color transitionTarget;
    private Color foregroundStart;
    private Color foregroundTarget;
    private long transitionStartedAt;
    private boolean pointerInside;
    private boolean pointerPressed;

/** Crea e inicializa este componente. */
    public AnimatedButton(String text, Color normalBackground, Color hoverBackground,
                          Color pressedBackground, Color normalForeground, Color hoverForeground) {
        super(text);
        this.normalBackground = normalBackground;
        this.hoverBackground = hoverBackground;
        this.pressedBackground = pressedBackground;
        this.normalForeground = normalForeground;
        this.hoverForeground = hoverForeground;
        this.currentBackground = normalBackground;
        this.currentForeground = normalForeground;
        this.transitionStart = normalBackground;
        this.transitionTarget = normalBackground;
        this.foregroundStart = normalForeground;
        this.foregroundTarget = normalForeground;

        setFont(AppTheme.BUTTON);
        setForeground(normalForeground);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        transitionTimer = new Timer(TIMER_MS, event -> advanceTransition());
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                pointerInside = true;
                pointerPressed = false;
                transitionTo(hoverBackground, hoverForeground);
            }

            @Override public void mouseExited(MouseEvent event) {
                pointerInside = false;
                pointerPressed = false;
                transitionTo(normalBackground, normalForeground);
            }

            @Override public void mousePressed(MouseEvent event) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(event)) {
                    pointerPressed = true;
                    transitionTo(pressedBackground, hoverForeground);
                    repaint();
                }
            }

            @Override public void mouseReleased(MouseEvent event) {
                pointerPressed = false;
                if (isEnabled()) {
                    transitionTo(pointerInside ? hoverBackground : normalBackground,
                            pointerInside ? hoverForeground : normalForeground);
                }
                repaint();
            }
        });
    }

    private void transitionTo(Color targetBackground, Color targetForeground) {
        if (!isEnabled()) return;
        transitionStart = currentBackground;
        transitionTarget = targetBackground;
        foregroundStart = currentForeground;
        foregroundTarget = targetForeground;
        transitionStartedAt = System.nanoTime();

        if (transitionStart.equals(transitionTarget) && foregroundStart.equals(foregroundTarget)) {
            currentBackground = transitionTarget;
            currentForeground = foregroundTarget;
            transitionTimer.stop();
            repaint();
        } else {
            transitionTimer.restart();
        }
    }

    private void advanceTransition() {
        float progress = Math.min(1f, (System.nanoTime() - transitionStartedAt) / (TRANSITION_MS * 1_000_000f));
        float eased = 1f - (float) Math.pow(1f - progress, 3);
        currentBackground = blend(transitionStart, transitionTarget, eased);
        currentForeground = blend(foregroundStart, foregroundTarget, eased);
        repaint();
        if (progress >= 1f) {
            transitionTimer.stop();
        }
    }

    private Color blend(Color c1, Color c2, float ratio) {
        float inverse = 1f - ratio;
        int r = Math.round(c1.getRed() * inverse + c2.getRed() * ratio);
        int g = Math.round(c1.getGreen() * inverse + c2.getGreen() * ratio);
        int b = Math.round(c1.getBlue() * inverse + c2.getBlue() * ratio);
        int a = Math.round(c1.getAlpha() * inverse + c2.getAlpha() * ratio);
        return new Color(r, g, b, a);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (pointerPressed && isEnabled()) {
            double cx = w / 2.0;
            double cy = h / 2.0;
            g2.translate(cx, cy);
            g2.scale(PRESSED_SCALE, PRESSED_SCALE);
            g2.translate(-cx, -cy);
        }

        g2.setColor(isEnabled() ? currentBackground : new Color(200, 205, 215));
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14));

        // Borde sutil
        g2.setColor(isEnabled() ? new Color(0, 0, 0, 20) : new Color(180, 185, 195));
        g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 14, 14));

        g2.dispose();

        setForeground(isEnabled() ? currentForeground : new Color(140, 145, 155));
        super.paintComponent(g);
    }
}
