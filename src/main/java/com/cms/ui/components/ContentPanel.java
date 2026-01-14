package com.cms.ui.components;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {

    private JPanel currentContent;

    public ContentPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 245, 249));
    }

    public void setContent(JPanel content) {
        if (currentContent != null) {
            remove(currentContent);
        }
        currentContent = content;
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public JPanel getCurrentContent() {
        return currentContent;
    }
}
