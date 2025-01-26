import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;


public class MainFrame extends JFrame{

    // File I/O
    File patientsFile = new File("./patientData.ser"); // This file may be stored outside of current folder
    
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

        tableModel = new DefaultTableModel(new String[] {"Name", "Details", "Priority", "Patient ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane listPane = new JScrollPane(patientTable);

        // Adjust Columns
        patientTable.getColumnModel().getColumn(0).setMinWidth(100); // Name Column
        patientTable.getColumnModel().getColumn(0).setMaxWidth(150);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Details Column
        patientTable.getColumnModel().getColumn(2).setMaxWidth(50); // Priority Column
        patientTable.getColumnModel().getColumn(3).setMaxWidth(100); // ID Column

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

        JTextField nameField = new JTextField();
        JTextField detailsField = new JTextField();
        JTextField priorityField = new JTextField();

        Object[] fields = {
            "Name:", nameField,
            "Details", detailsField,
            "Priority", priorityField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add Patient", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            if (nameField.getText().trim().isEmpty()) {
                displayErrorMessage("Patient's name cannot be blank");
                return;
            }

            try {
                String name = nameField.getText().trim();
                String details = detailsField.getText().trim();
                int priority = Integer.parseInt(priorityField.getText());
                // Only uses the first 8 characters from generated UUID (11.7% chance of collision at 100,000 patients)
                String patientID = UUID.randomUUID().toString().toUpperCase().substring(0, 8); 
                for (Patient p: patients) {
                    if (patientID.equals(p.getID())) patientID = patientID.toLowerCase(); // Current protocol for collision, modify if necessary
                }

                Patient newPatient = new Patient(name, details, priority, patientID);
                patients.add(newPatient);

                saveData();

                refreshPage();
                resetSearch();
            } catch (NumberFormatException e) {
                displayErrorMessage("Priority must only be a number");
            }
        } 
    }

    private void deletePatient() {
        int selectedRow = patientTable.getSelectedRow();

        if (selectedRow != -1) {
            String selectedID = (String) patientTable.getValueAt(selectedRow, 3);

            for (int i = 0; i < patients.size(); i++) {
                if (selectedID.equals(patients.get(i).getID())) {
                    patients.remove(i);
                    break;
                }
            }
            saveData();

            refreshPage();
            resetSearch();
        } else {
            displayErrorMessage("You must select a patient to delete.");
        }
    }

    private void updatePatient() {

        int selectedRow = patientTable.getSelectedRow();

        if (selectedRow != -1) {
            String selectedID = (String) patientTable.getValueAt(selectedRow, 3);
            Patient selectedPatient = null;

            for (int i = 0; i < patients.size(); i++) {
                if (selectedID.equals(patients.get(i).getID())) {
                    selectedPatient = patients.get(i);
                }
            }

            JTextField nameField = new JTextField(selectedPatient.getName());
            JTextField detailsField = new JTextField(selectedPatient.getDetails());
            JTextField priorityField = new JTextField(String.valueOf(selectedPatient.getPriority()));

            Object[] fields = {
                "Name:", nameField,
                "Details:", detailsField,
                "Priority", priorityField
            };

            int option = JOptionPane.showConfirmDialog(this, fields, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                if (nameField.getText().trim().isEmpty()) {
                    displayErrorMessage("Patient's name cannot be blank");
                    return;
                }

                try {
                    selectedPatient.setName(nameField.getText().trim());
                    selectedPatient.setDetails(detailsField.getText().trim());
                    selectedPatient.setPriority(Integer.parseInt(priorityField.getText()));

                    saveData();

                    refreshPage();
                    resetSearch();
                } catch (NumberFormatException e) {
                    displayErrorMessage("Priority must only be a number.");
                }
            }

        } else {
            displayErrorMessage("You must select a patient to update.");
        }
    }

    private void searchPatients() {
        // Search field text modified for case insensivity
        String name = searchField.getText().toLowerCase().strip();

        if (name != null && !name.trim().isEmpty()) {
            tableModel.setRowCount(0);
            for (Patient p: patients) {
                if (p.getName().toLowerCase().contains(name)) {
                    tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority(), p.getID()});
                }
            }
            // Priority sorting is set to false as to not break the data
            searchToggled = true;
            sortedByPriority = false;
        } else {
            refreshPage();
            searchToggled = false;
        }
        
    }

    private void sortPatients() {
        if (!sortedByPriority) {
            displaySortedPatients();
            sortedByPriority = true;
            resetSearch();
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
            tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority(), p.getID()});
        }
    }

    private void displaySortedPatients() {
        tableModel.setRowCount(0);

        // A new Array List is always initialized for toggleability
        List<Patient> sortedPatients = new ArrayList<>();

        for (Patient p: patients) {
            sortedPatients.add(new Patient(p.getName(), p.getDetails(), p.getPriority(), p.getID()));
        }

        sortedPatients.sort(Comparator.comparingInt(Patient::getPriority));

        for (Patient p: sortedPatients) {
            tableModel.addRow(new Object[] {p.getName(), p.getDetails(), p.getPriority(), p.getID()});
        }
    }

    private void refreshPage() {
        if (sortedByPriority) {
            displaySortedPatients();
        } else {
            displayPatients();
        }
    }

    private void resetSearch() {
        searchToggled = false;
        searchField.setText("");
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
