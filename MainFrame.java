import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;


public class MainFrame extends JFrame{

    // File I/O
    File patientsFile = new File("./patients.ser"); // This file may be stored outside of current folder
    
    List<Patient> patients = new ArrayList<>();

    // Global variables
    JTable patientTable;
    JTextField searchField;
    DefaultTableModel tableModel;
    boolean searchToggled = false;
    boolean sortedByPriority = false;
   

    public MainFrame(int width, int height) {

        // Window settings
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Hospital Patient Queue");
        this.setSize(width, height);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        // Initiate Buttons
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton updateButton = new JButton("Update");
        JButton searchButton = new JButton("Search");
        JButton sortButton = new JButton("Sort by Priority");

        addButton.setPreferredSize(new Dimension(90, 25));
        deleteButton.setPreferredSize(new Dimension(90, 25));
        searchButton.setPreferredSize(new Dimension(100, 25));

        addButton.addActionListener(e -> addPatient());
        deleteButton.addActionListener(e -> deletePatient());
        updateButton.addActionListener(e -> updatePatient());
        searchButton.addActionListener(e -> searchPatients());
        sortButton.addActionListener(e -> sortPatients());


        // Initiate Textfields
        searchField = new JTextField();

        searchField.setPreferredSize(new Dimension(600, 25));

        // Initiate panels, etc.
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 13));
        JPanel bottomPanel = new JPanel();
        tableModel = new DefaultTableModel(new String[] {"Name", "Details", "Priority"}, 0);
        patientTable = new JTable(tableModel);
        JScrollPane listPane = new JScrollPane(patientTable);

        // Panel Deco
        searchPanel.setBackground(new Color(0x1f1e33));
        bottomPanel.setBackground(new Color(0x1f1e33));

        searchPanel.setPreferredSize(new Dimension(100, 50));       
        bottomPanel.setPreferredSize(new Dimension(100, 50));

        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(sortButton);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add Panels to main frame
        this.add(searchPanel, BorderLayout.NORTH);
        this.add(listPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // File I/O
        if (patientsFile.exists()) {
            loadData();
            displayPatients();
        } else {
            saveData();
        }

        this.setVisible(true);
    }


    // Button Methods
    private void addPatient() {
        if (searchToggled) {
            displayErrorMessage("Please empty the Search Field first");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField detailsField = new JTextField();
        JTextField priorityField = new JTextField();

        Object[] fields = {
            "Name:", nameField,
            "Details", detailsField,
            "Priority", priorityField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Patient", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            try {
                String name = nameField.getText().trim();
                String details = detailsField.getText().trim();
                int priority = Integer.parseInt(priorityField.getText());

                Patient newPatient = new Patient(name, details, priority);
                patients.add(newPatient);

                saveData();

                if (sortedByPriority) {displaySortedPatients();} else {displayPatients();}
            } catch (NumberFormatException e) {
                displayErrorMessage("Priority must only be a number");
            }
        } else {
            displayErrorMessage("Name cannot be blank");
        }
    }

    private void deletePatient() {
        if (searchToggled) {
            displayErrorMessage("Please empty the Search Field first");
            return;
        }

        if (sortedByPriority) {
            displayErrorMessage("List must be unsorted first");
            return;
        }

        int selectedRow = patientTable.getSelectedRow();

        if (selectedRow != -1) {
            patients.remove(selectedRow);
            saveData();

            tableModel.removeRow(selectedRow);
        } else {
            displayErrorMessage("You must select a patient to delete.");
        }
    }

    private void updatePatient() {
        if (searchToggled) {
            displayErrorMessage("Please empty the Search Field first");
            return;
        }

        if (sortedByPriority) {
            displayErrorMessage("List must be unsorted first");
            return;
        }

        int selectedRow = patientTable.getSelectedRow();

        if (selectedRow != -1) {
            Patient selectedPatient = patients.get(selectedRow);

            JTextField nameField = new JTextField(selectedPatient.getName());
            JTextField detailsField = new JTextField(selectedPatient.getDetails());
            JTextField priorityField = new JTextField(String.valueOf(selectedPatient.getPriority()));

            Object[] fields = {
                "Name:", nameField,
                "Details:", detailsField,
                "Priority", priorityField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
                try {
                    selectedPatient.setName(nameField.getText().trim());
                    selectedPatient.setDetails(detailsField.getText().trim());
                    selectedPatient.setPriority(Integer.parseInt(priorityField.getText()));

                    saveData();

                    tableModel.setValueAt(selectedPatient.getName(), selectedRow, 0);
                    tableModel.setValueAt(selectedPatient.getDetails(), selectedRow, 1);
                    tableModel.setValueAt(selectedPatient.getPriority(), selectedRow, 2);
                } catch (NumberFormatException e) {
                    displayErrorMessage("Priority must only be a number.");
                }
            } else {
                displayErrorMessage("Name cannot be blank");
            }

        } else {
            displayErrorMessage("You must select a patient to update.");
        }
    }

    private void searchPatients() {
        // Search field modified for case insensivity
        String name = searchField.getText().toLowerCase().strip();

        if (name != null && !name.trim().isEmpty()) {
            tableModel.setRowCount(0);
            for (Patient p: patients) {
                if (p.getName().toLowerCase().contains(name)) {
                    tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority()});
                }
            }
            // Priority sorting is set to false as to not break the data
            searchToggled = true;
            sortedByPriority = false;
        } else {
            displayPatients();
            searchToggled = false;
        }
        
    }

    private void sortPatients() {
        if (searchToggled) {
            displayErrorMessage("Please empty the Search Field first");
            return;
        }

        if (!sortedByPriority) {
            displaySortedPatients();
            sortedByPriority = true;
        } else {
            displayPatients();
            sortedByPriority = false;
        }
    }


    // General Methods
    private void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void displayPatients() {
        tableModel.setRowCount(0);
        for (Patient p: patients) {
            tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority()});
        }
    }

    private void displaySortedPatients() {
        tableModel.setRowCount(0);

        List<Patient> sortedPatients = new ArrayList<>();

        for (Patient p: patients) {
            sortedPatients.add(new Patient(p.getName(), p.getDetails(), p.getPriority()));
        }

        sortedPatients.sort(Comparator.comparingInt(Patient::getPriority));

        for (Patient p: sortedPatients) {
            tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority()});
        }
    }


    // FIle I/O Methods
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(patientsFile))) {
            oos.writeObject(patients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(patientsFile))) {
            patients = (ArrayList<Patient>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
