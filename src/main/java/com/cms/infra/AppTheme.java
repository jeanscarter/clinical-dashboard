package com.cms.infra;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class AppTheme {

    private boolean isDark = false;

    public void init() {
        FlatLightLaf.setup();
        isDark = false;
        updateUI();
    }

    public void toggleTheme() {
        if (isDark) {
            FlatLightLaf.setup();
            isDark = false;
        } else {
            FlatDarkLaf.setup();
            isDark = true;
        }
        updateUI();
    }

    private void updateUI() {
        FlatLaf.updateUI();
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
        }
    }

    public boolean isDark() {
        return isDark;
    }
}
