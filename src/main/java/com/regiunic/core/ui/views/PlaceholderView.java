package com.regiunic.core.ui.views;

import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Vista de cortesía para módulos del sistema que se encuentran en fase de desarrollo.
 */
public class PlaceholderView extends JPanel {
    private final ViewNavigator navigator;
    private final JLabel lblTitle;

/** Crea e inicializa este componente. */
    public PlaceholderView(ViewNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        lblTitle = new JLabel("Módulo");
        lblTitle.setFont(AppTheme.PAGE_TITLE);
        lblTitle.setForeground(AppTheme.NAVY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblTitle);

        add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblDesc = new JLabel("Esta funcionalidad está en fase de desarrollo para la versión de escritorio.");
        lblDesc.setFont(AppTheme.SECTION);
        lblDesc.setForeground(AppTheme.TEXT_MUTED);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblDesc);

        add(Box.createRigidArea(new Dimension(0, 40)));

        AnimatedButton btnVolver = AppTheme.primaryButton("← Regresar al Menú Principal");
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));
        add(btnVolver);
    }

/** Actualiza los datos indicados después de validar la información necesaria. */
    public void setFeatureTitle(String title) {
        lblTitle.setText(title);
    }
}
