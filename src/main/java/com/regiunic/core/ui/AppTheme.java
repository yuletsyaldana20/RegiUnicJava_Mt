package com.regiunic.core.ui;

import com.regiunic.core.ui.components.AnimatedButton;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

/**
 * Tokens visuales y estilos compartidos para la interfaz de escritorio de RegiUnic.
 */
public final class AppTheme {
    public static final Color NAVY = new Color(22, 33, 62);
    public static final Color NAVY_HOVER = new Color(43, 76, 130);
    public static final Color NAVY_PRESSED = new Color(15, 35, 70);
    public static final Color BLUE_FOCUS = new Color(50, 130, 220);
    public static final Color WINDOW_BACKGROUND = new Color(205, 213, 235);
    public static final Color PANEL_BACKGROUND = new Color(245, 247, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_HOVER = new Color(232, 239, 250);
    public static final Color SURFACE_PRESSED = new Color(215, 227, 245);
    public static final Color TEXT_MUTED = new Color(70, 82, 100);
    public static final Color TEXT_DARK = new Color(25, 30, 45);
    public static final Color DANGER = new Color(180, 40, 40);
    public static final Color DANGER_HOVER = new Color(120, 20, 20);
    public static final Color DANGER_PRESSED = new Color(90, 10, 10);
    public static final Color SUCCESS = new Color(34, 139, 34);

    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font MENU_ITEM = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SECTION = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font ACTION = new Font("Segoe UI", Font.BOLD, 25);
    public static final Font FORM_HEADING = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font PAGE_TITLE = new Font("Segoe UI", Font.BOLD, 36);

    private AppTheme() {}

    /**
     * Aplica la tipografía y colores base a todos los componentes de Swing.
     */
    public static void install() {
        String[] components = {
            "Button", "CheckBox", "ComboBox", "Label", "List", "Menu",
            "MenuItem", "OptionPane", "PasswordField", "RadioButton", "TabbedPane",
            "Table", "TableHeader", "TextArea", "TextField", "TextPane",
            "TitledBorder", "ToolTip", "Tree"
        };
        for (String component : components) {
            UIManager.put(component + ".font", BODY);
        }
        UIManager.put("Button.focus", BLUE_FOCUS);
        UIManager.put("Component.focusColor", BLUE_FOCUS);
    }

/** Ejecuta la operación indicada por este método. */
    public static AnimatedButton primaryButton(String text) {
        return new AnimatedButton(text, NAVY, NAVY_HOVER, NAVY_PRESSED, Color.WHITE, Color.WHITE);
    }

/** Ejecuta la operación indicada por este método. */
    public static AnimatedButton secondaryButton(String text) {
        return new AnimatedButton(text, SURFACE, SURFACE_HOVER, SURFACE_PRESSED, NAVY, NAVY);
    }

/** Ejecuta la operación indicada por este método. */
    public static AnimatedButton dangerButton(String text) {
        return new AnimatedButton(text, DANGER, DANGER_HOVER, DANGER_PRESSED, Color.WHITE, Color.WHITE);
    }

/** Ejecuta la operación indicada por este método. */
    public static AnimatedButton menuButton(String text) {
        return new AnimatedButton(text, SURFACE, NAVY, NAVY_PRESSED, NAVY, Color.WHITE);
    }

    /**
     * Carga y escala imágenes conservando relación de aspecto para logos e íconos.
     */
    public static ImageIcon loadScaledIcon(String path, int targetWidth, int targetHeight) {
        ImageIcon icon = new ImageIcon(path);
        int origW = icon.getIconWidth();
        int origH = icon.getIconHeight();
        if (origW <= 0 || origH <= 0) {
            return icon;
        }

        double scale = Math.min(1.0, Math.min((double) targetWidth / origW, (double) targetHeight / origH));
        int finalW = Math.max(1, (int) Math.round(origW * scale));
        int finalH = Math.max(1, (int) Math.round(origH * scale));

        if (finalW == origW && finalH == origH) {
            return icon;
        }

        Image img = icon.getImage().getScaledInstance(finalW, finalH, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
