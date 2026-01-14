package com.cms;

import com.cms.di.AppFactory;
import com.cms.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppFactory.getInstance();
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
