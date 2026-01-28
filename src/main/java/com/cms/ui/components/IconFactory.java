package com.cms.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Factory class that creates custom vector icons using Graphics2D.
 * These icons are resolution-independent and render crisp at any size.
 */
public class IconFactory {

    // Icon sizes
    public static final int SMALL = 16;
    public static final int MEDIUM = 20;
    public static final int LARGE = 24;

    /**
     * Creates a plus/add icon
     */
    public static Icon createPlusIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = size / 4;
                int cx = x + size / 2;
                int cy = y + size / 2;

                // Horizontal line
                g2.drawLine(x + margin, cy, x + size - margin, cy);
                // Vertical line
                g2.drawLine(cx, y + margin, cx, y + size - margin);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a search/magnifying glass icon
     */
    public static Icon createSearchIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int circleSize = (int) (size * 0.55);
                int circleX = x + 2;
                int circleY = y + 2;

                // Draw circle
                g2.drawOval(circleX, circleY, circleSize, circleSize);

                // Draw handle
                int handleStartX = circleX + (int) (circleSize * 0.75);
                int handleStartY = circleY + (int) (circleSize * 0.75);
                g2.drawLine(handleStartX, handleStartY, x + size - 3, y + size - 3);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a users/people icon (similar to 👥 emoji)
     */
    public static Icon createUsersIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                // Person on the left (back)
                int headSize1 = size / 4;
                g2.fillOval(x + 2, y + 2, headSize1, headSize1);
                g2.fillArc(x, y + headSize1 + 1, headSize1 + 4, size / 2, 0, 180);

                // Person on the right (front, slightly larger)
                int headSize2 = size / 3;
                g2.fillOval(x + size / 2 - 1, y + 1, headSize2, headSize2);
                g2.fillArc(x + size / 2 - 3, y + headSize2, headSize2 + 6, (int) (size * 0.55), 0, 180);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a single user/person icon (similar to 👤 emoji)
     */
    public static Icon createUserIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                // Head
                int headSize = size / 3;
                int headX = x + size / 2 - headSize / 2;
                g2.fillOval(headX, y + 1, headSize, headSize);

                // Body (upper torso arc)
                int bodyWidth = (int) (size * 0.7);
                int bodyX = x + size / 2 - bodyWidth / 2;
                int bodyY = y + headSize + 2;
                g2.fillArc(bodyX, bodyY, bodyWidth, (int) (size * 0.6), 0, 180);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a document/clipboard icon
     */
    public static Icon createDocumentIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 3;
                int w = size - margin * 2;
                int foldSize = w / 3;

                // Document shape with folded corner
                Path2D path = new Path2D.Float();
                path.moveTo(x + margin, y + margin);
                path.lineTo(x + size - margin - foldSize, y + margin);
                path.lineTo(x + size - margin, y + margin + foldSize);
                path.lineTo(x + size - margin, y + size - margin);
                path.lineTo(x + margin, y + size - margin);
                path.closePath();
                g2.draw(path);

                // Fold line
                g2.drawLine(x + size - margin - foldSize, y + margin,
                        x + size - margin - foldSize, y + margin + foldSize);
                g2.drawLine(x + size - margin - foldSize, y + margin + foldSize,
                        x + size - margin, y + margin + foldSize);

                // Lines (text)
                int lineY = y + margin + foldSize + 4;
                int lineSpacing = 3;
                for (int i = 0; i < 2 && lineY < y + size - margin - 3; i++) {
                    g2.drawLine(x + margin + 3, lineY, x + size - margin - 3, lineY);
                    lineY += lineSpacing;
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a chart/stats icon (similar to 📊 emoji - bar chart)
     */
    public static Icon createChartIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;
                int barWidth = (size - margin * 2 - 4) / 3;
                int baseY = y + size - margin;
                int barGap = 2;

                // Three bars with different heights (like bar chart emoji)
                int[] heights = { (int) (size * 0.45), (int) (size * 0.75), (int) (size * 0.55) };
                for (int i = 0; i < 3; i++) {
                    int barX = x + margin + i * (barWidth + barGap);
                    int barH = heights[i];
                    g2.fillRoundRect(barX, baseY - barH, barWidth, barH, 2, 2);
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a clock/pending icon
     */
    public static Icon createClockIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 2;
                int diameter = size - margin * 2;
                int cx = x + size / 2;
                int cy = y + size / 2;

                // Clock circle
                g2.drawOval(x + margin, y + margin, diameter, diameter);

                // Clock hands
                int hourHandLen = diameter / 4;
                int minuteHandLen = diameter / 3;

                g2.drawLine(cx, cy, cx, cy - minuteHandLen); // 12 o'clock
                g2.drawLine(cx, cy, cx + hourHandLen, cy); // 3 o'clock

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates an edit/pencil icon - clearer design
     */
    public static Icon createEditIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 2;

                // Pencil body (diagonal line with square top)
                int topX = x + size - margin - 3;
                int topY = y + margin + 1;
                int bottomX = x + margin + 1;
                int bottomY = y + size - margin - 3;

                // Main pencil line
                g2.drawLine(topX, topY, bottomX, bottomY);

                // Pencil top cap (small square rotated)
                g2.drawLine(topX - 2, topY + 2, topX + 2, topY - 2);

                // Pencil tip (triangle at bottom)
                g2.drawLine(bottomX, bottomY, x + margin - 1, y + size - margin + 1);
                g2.drawLine(bottomX, bottomY, bottomX + 2, bottomY + 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a delete/trash icon
     */
    public static Icon createDeleteIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 3;
                int topY = y + margin + 3;

                // Trash can body
                g2.drawRect(x + margin + 1, topY, size - margin * 2 - 2, size - margin - topY + y - 1);

                // Lid
                g2.drawLine(x + margin - 1, topY - 1, x + size - margin + 1, topY - 1);

                // Handle
                int handleWidth = size / 3;
                int handleX = x + size / 2 - handleWidth / 2;
                g2.drawRect(handleX, y + margin, handleWidth, 2);

                // Lines inside
                int lineY1 = topY + 3;
                int lineY2 = y + size - margin - 3;
                int lineSpacing = (size - margin * 2 - 4) / 3;
                for (int i = 0; i < 2; i++) {
                    int lineX = x + margin + 4 + i * lineSpacing;
                    g2.drawLine(lineX, lineY1, lineX, lineY2);
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a calendar/today icon
     */
    public static Icon createCalendarIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 2;
                int topMargin = margin + 3;

                // Calendar body
                g2.drawRoundRect(x + margin, y + topMargin, size - margin * 2, size - topMargin - margin, 2, 2);

                // Top line (header)
                g2.drawLine(x + margin, y + topMargin + 5, x + size - margin, y + topMargin + 5);

                // Binding loops
                g2.drawLine(x + margin + 4, y + margin, x + margin + 4, y + topMargin + 2);
                g2.drawLine(x + size - margin - 4, y + margin, x + size - margin - 4, y + topMargin + 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a report/analytics icon
     */
    public static Icon createReportIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 2;

                // Axes
                g2.drawLine(x + margin + 2, y + margin, x + margin + 2, y + size - margin);
                g2.drawLine(x + margin + 2, y + size - margin, x + size - margin, y + size - margin);

                // Trend line going up
                Path2D trend = new Path2D.Float();
                trend.moveTo(x + margin + 4, y + size - margin - 3);
                trend.lineTo(x + size / 2, y + size / 2);
                trend.lineTo(x + size - margin - 4, y + margin + 4);
                g2.draw(trend);

                // Arrow at top
                g2.drawLine(x + size - margin - 4, y + margin + 4, x + size - margin - 7, y + margin + 5);
                g2.drawLine(x + size - margin - 4, y + margin + 4, x + size - margin - 5, y + margin + 7);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a key icon (for password reset) - more visible design
     */
    public static Icon createKeyIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int circleSize = (int) (size * 0.4);
                int circleX = x + 1;
                int circleY = y + size / 2 - circleSize / 2;

                // Key head (filled circle with hole)
                g2.fillOval(circleX, circleY, circleSize, circleSize);
                g2.setColor(g2.getBackground() != null ? g2.getBackground() : Color.WHITE);
                g2.fillOval(circleX + circleSize / 4, circleY + circleSize / 4, circleSize / 2, circleSize / 2);
                g2.setColor(color);

                // Key shaft
                int shaftStartX = circleX + circleSize - 2;
                int shaftY = y + size / 2;
                g2.drawLine(shaftStartX, shaftY, x + size - 2, shaftY);

                // Key teeth (more visible)
                g2.drawLine(x + size - 3, shaftY, x + size - 3, shaftY + 4);
                g2.drawLine(x + size - 6, shaftY, x + size - 6, shaftY + 3);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a power/toggle icon - larger and more visible
     */
    public static Icon createPowerIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = 2;
                int cx = x + size / 2;
                int diameter = size - margin * 2;

                // Power arc (open circle at top)
                g2.drawArc(x + margin, y + margin + 1, diameter, diameter, -50, 280);

                // Vertical line at top (power button line)
                g2.drawLine(cx, y + 2, cx, y + size / 2 + 1);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a settings/gear icon
     */
    public static Icon createSettingsIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = x + size / 2;
                int cy = y + size / 2;
                int outerRadius = (size - 4) / 2;
                int innerRadius = outerRadius / 2;

                // Draw center circle
                g2.drawOval(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2);

                // Draw gear teeth (8 teeth around the circle)
                int teethCount = 8;
                for (int i = 0; i < teethCount; i++) {
                    double angle = (2 * Math.PI * i) / teethCount;
                    int x1 = (int) (cx + Math.cos(angle) * (innerRadius + 2));
                    int y1 = (int) (cy + Math.sin(angle) * (innerRadius + 2));
                    int x2 = (int) (cx + Math.cos(angle) * outerRadius);
                    int y2 = (int) (cy + Math.sin(angle) * outerRadius);
                    g2.drawLine(x1, y1, x2, y2);
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a trash icon (alias for createDeleteIcon)
     */
    public static Icon createTrashIcon(int size, Color color) {
        return createDeleteIcon(size, color);
    }

    /**
     * Creates a chevron left arrow icon for navigation
     */
    public static Icon createChevronLeftIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = size / 4;
                int cx = x + size / 2;
                int cy = y + size / 2;

                // Draw < shape
                g2.drawLine(cx + margin / 2, y + margin, cx - margin / 2, cy);
                g2.drawLine(cx - margin / 2, cy, cx + margin / 2, y + size - margin);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a chevron right arrow icon for navigation
     */
    public static Icon createChevronRightIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int margin = size / 4;
                int cx = x + size / 2;
                int cy = y + size / 2;

                // Draw > shape
                g2.drawLine(cx - margin / 2, y + margin, cx + margin / 2, cy);
                g2.drawLine(cx + margin / 2, cy, cx - margin / 2, y + size - margin);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a check circle icon (✓ inside a circle) - for success messages
     */
    public static Icon createCheckCircleIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;
                int diameter = size - margin * 2;

                // Draw filled circle
                g2.fillOval(x + margin, y + margin, diameter, diameter);

                // Draw checkmark in contrasting color
                g2.setColor(new Color(34, 197, 94)); // Green for the circle
                g2.setColor(color.equals(Color.WHITE) ? new Color(34, 197, 94) : Color.WHITE);
                g2.setStroke(new BasicStroke(size / 8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = x + size / 2;
                int cy = y + size / 2;
                int checkSize = size / 4;

                // Checkmark path
                g2.drawLine(cx - checkSize, cy, cx - checkSize / 3, cy + checkSize);
                g2.drawLine(cx - checkSize / 3, cy + checkSize, cx + checkSize, cy - checkSize);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates an error icon (X inside a circle) - for error messages
     */
    public static Icon createErrorIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;
                int diameter = size - margin * 2;

                // Draw filled circle
                g2.fillOval(x + margin, y + margin, diameter, diameter);

                // Draw X in contrasting color
                g2.setColor(color.equals(Color.WHITE) ? new Color(239, 68, 68) : Color.WHITE);
                g2.setStroke(new BasicStroke(size / 8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = x + size / 2;
                int cy = y + size / 2;
                int crossSize = size / 5;

                g2.drawLine(cx - crossSize, cy - crossSize, cx + crossSize, cy + crossSize);
                g2.drawLine(cx + crossSize, cy - crossSize, cx - crossSize, cy + crossSize);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a warning icon (triangle with exclamation mark)
     */
    public static Icon createWarningIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;

                // Draw triangle
                int[] xPoints = { x + size / 2, x + margin, x + size - margin };
                int[] yPoints = { y + margin, y + size - margin, y + size - margin };
                g2.fillPolygon(xPoints, yPoints, 3);

                // Draw exclamation mark
                g2.setColor(color.equals(Color.WHITE) ? new Color(245, 158, 11) : Color.WHITE);
                g2.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = x + size / 2;
                int lineTop = y + size / 3;
                int lineBottom = y + size - margin - size / 4;
                int dotY = y + size - margin - size / 10;

                g2.drawLine(cx, lineTop, cx, lineBottom);
                g2.fillOval(cx - size / 16, dotY, size / 8, size / 8);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates an info icon (i inside a circle) - for information messages
     */
    public static Icon createInfoIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;
                int diameter = size - margin * 2;

                // Draw filled circle
                g2.fillOval(x + margin, y + margin, diameter, diameter);

                // Draw "i" in contrasting color
                g2.setColor(color.equals(Color.WHITE) ? new Color(59, 130, 246) : Color.WHITE);
                g2.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = x + size / 2;
                int dotY = y + size / 3;
                int lineTop = y + size / 2 - size / 10;
                int lineBottom = y + size - margin - size / 5;

                // Dot
                g2.fillOval(cx - size / 14, dotY - size / 14, size / 7, size / 7);
                // Line
                g2.drawLine(cx, lineTop, cx, lineBottom);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /**
     * Creates a question mark icon (? inside a circle) - for confirmation dialogs
     */
    public static Icon createQuestionIcon(int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(color);

                int margin = 2;
                int diameter = size - margin * 2;

                // Draw filled circle
                g2.fillOval(x + margin, y + margin, diameter, diameter);

                // Draw "?" in contrasting color
                g2.setColor(color.equals(Color.WHITE) ? new Color(59, 130, 246) : Color.WHITE);

                Font questionFont = new Font("Segoe UI", Font.BOLD, (int) (size * 0.55));
                g2.setFont(questionFont);
                FontMetrics fm = g2.getFontMetrics();
                String q = "?";
                int textX = x + (size - fm.stringWidth(q)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(q, textX, textY);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }
}
