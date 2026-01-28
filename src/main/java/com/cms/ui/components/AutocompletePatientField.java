package com.cms.ui.components;

import com.cms.domain.Patient;
import com.cms.repository.PatientRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Autocomplete text field for patient search.
 * Searches by name or cédula as the user types.
 */
public class AutocompletePatientField extends JPanel {

    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color HOVER_BG = new Color(241, 245, 249);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    private final PatientRepository patientRepository;
    private final JTextField searchField;
    private final JPopupMenu popup;
    private final JPanel resultsPanel;

    private List<Patient> allPatients = new ArrayList<>();
    private Patient selectedPatient;
    private Consumer<Patient> onPatientSelected;
    private boolean suppressPopup = false;

    public AutocompletePatientField(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;

        setLayout(new BorderLayout());
        setOpaque(false);

        // Search field
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)));
        searchField.putClientProperty("JTextField.placeholderText", "Buscar por nombre o cédula...");

        // Results popup
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        popup.setFocusable(false);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        popup.add(scrollPane);

        // Document listener for search-as-you-type
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onSearchTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchTextChanged();
            }
        });

        // Keyboard navigation
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popup.isVisible()) {
                    if (resultsPanel.getComponentCount() > 0) {
                        resultsPanel.getComponent(0).requestFocus();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                }
            }
        });

        // Focus listener to show popup on focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (!suppressPopup && searchField.getText().isEmpty()) {
                    loadAllPatients();
                    showResults(allPatients);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Delay hiding to allow click on popup
                SwingUtilities.invokeLater(() -> {
                    if (!popup.isVisible())
                        return;
                    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    if (focusOwner == null || !SwingUtilities.isDescendingFrom(focusOwner, popup)) {
                        popup.setVisible(false);
                    }
                });
            }
        });

        add(searchField, BorderLayout.CENTER);

        // Load patients on init
        loadAllPatients();
    }

    private void loadAllPatients() {
        allPatients = patientRepository.findAll();
    }

    private void onSearchTextChanged() {
        if (suppressPopup) {
            suppressPopup = false;
            return;
        }

        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            showResults(allPatients);
        } else {
            List<Patient> filtered = allPatients.stream()
                    .filter(p -> matchesQuery(p, query))
                    .limit(20)
                    .toList();
            showResults(filtered);
        }
    }

    private boolean matchesQuery(Patient patient, String query) {
        String fullName = (patient.getNombre() + " " + patient.getApellido()).toLowerCase();
        String cedula = patient.getCedula() != null ? patient.getCedula().toLowerCase() : "";
        return fullName.contains(query) || cedula.contains(query);
    }

    private void showResults(List<Patient> patients) {
        resultsPanel.removeAll();

        if (patients.isEmpty()) {
            JLabel noResults = new JLabel("No se encontraron pacientes");
            noResults.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResults.setForeground(TEXT_SECONDARY);
            noResults.setBorder(new EmptyBorder(15, 15, 15, 15));
            noResults.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(noResults);
        } else {
            for (Patient patient : patients) {
                JPanel item = createPatientItem(patient);
                resultsPanel.add(item);
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();

        if (!popup.isVisible() && searchField.hasFocus()) {
            popup.show(searchField, 0, searchField.getHeight());
        }
    }

    private JPanel createPatientItem(Patient patient) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(10, 15, 10, 15));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Name label
        JLabel nameLabel = new JLabel(patient.getNombreCompleto());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        // Cédula label
        JLabel cedulaLabel = new JLabel("Cédula: " + patient.getCedula());
        cedulaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cedulaLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(cedulaLabel);

        item.add(textPanel, BorderLayout.CENTER);

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectPatient(patient);
            }
        });

        // Keyboard navigation
        item.setFocusable(true);
        item.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectPatient(patient);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                    searchField.requestFocus();
                }
            }
        });

        return item;
    }

    private void selectPatient(Patient patient) {
        this.selectedPatient = patient;
        suppressPopup = true;
        searchField.setText(patient.getNombreCompleto() + " - " + patient.getCedula());
        popup.setVisible(false);

        if (onPatientSelected != null) {
            onPatientSelected.accept(patient);
        }
    }

    /**
     * Pre-selects a patient by ID (used when opening dialog with patient
     * pre-selected)
     */
    public void selectPatient(Integer patientId) {
        if (patientId == null)
            return;

        loadAllPatients();
        for (Patient p : allPatients) {
            if (p.getId().equals(patientId)) {
                selectPatient(p);
                break;
            }
        }
    }

    /**
     * Gets the currently selected patient
     */
    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    /**
     * Clears the selection
     */
    public void clear() {
        selectedPatient = null;
        suppressPopup = true;
        searchField.setText("");
    }

    /**
     * Sets callback for when a patient is selected
     */
    public void setOnPatientSelected(Consumer<Patient> callback) {
        this.onPatientSelected = callback;
    }

    /**
     * Refreshes the patient list from database
     */
    public void refresh() {
        loadAllPatients();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        searchField.setEnabled(enabled);
    }
}
