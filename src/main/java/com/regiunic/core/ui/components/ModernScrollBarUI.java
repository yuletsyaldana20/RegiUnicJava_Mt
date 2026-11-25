package com.regiunic.core.ui.components;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Barra de desplazamiento estilizada, delgada y con bordes redondeados.
 */
public final class ModernScrollBarUI extends BasicScrollBarUI {
    private final Color trackColor = new Color(238, 241, 247);
    private final Color thumbColor = new Color(115, 133, 164);
    private final Color thumbHoverColor = new Color(22, 48, 91);

    @Override
    public Dimension getPreferredSize(JComponent component) {
        return scrollbar.getOrientation() == JScrollBar.VERTICAL
                ? new Dimension(10, 0)
                : new Dimension(0, 10);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton();
    }

    private JButton createInvisibleButton() {
        JButton button = new JButton();
        Dimension zero = new Dimension(0, 0);
        button.setPreferredSize(zero);
        button.setMinimumSize(zero);
        button.setMaximumSize(zero);
        button.setFocusable(false);
        return button;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(trackColor);
        g2.fill(new RoundRectangle2D.Float(
                bounds.x + 2, bounds.y + 1,
                Math.max(1, bounds.width - 4), Math.max(1, bounds.height - 2),
                6, 6
        ));
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isThumbRollover() ? thumbHoverColor : thumbColor);
        g2.fill(new RoundRectangle2D.Float(
                bounds.x + 2, bounds.y + 1,
                Math.max(1, bounds.width - 4), Math.max(1, bounds.height - 2),
                6, 6
        ));
        g2.dispose();
    }
}
