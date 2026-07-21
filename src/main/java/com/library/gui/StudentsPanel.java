package com.library.gui;

import com.library.controller.StudentController;
import com.library.facade.LibraryFacade;
import com.library.model.Student;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student management panel with search and registration.
 */
public final class StudentsPanel extends JPanel {

    private final LibraryFacade facade;
    private final StudentController studentCtrl;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private Session session;

    private static final String[] COLUMNS = {"Reg No.", "Name", "Department", "Course", "Semester", "Membership", "Borrows", "Fine Balance"};

    public StudentsPanel(LibraryFacade facade) {
        this.facade = facade;
        this.studentCtrl = new StudentController(facade);
        setBackground(AppTheme.bgPrimary());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(AppTheme.heading("Students"));
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(AppTheme.secondaryLabel("Manage student members and memberships"));

        searchField = AppTheme.styledTextField(20);
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.addActionListener(e -> filterStudents());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
        });

        JButton addBtn = AppTheme.primaryButton("+ Add Student");
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e -> showAddDialog());

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(searchField);
        actionsPanel.add(addBtn);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionsPanel, BorderLayout.EAST);

        tableModel = new DefaultTableModel(COLUMNS, 0);
        table = new JTable(tableModel) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? AppTheme.bgSecondary() : AppTheme.tableRowAlt());
                } else {
                    c.setBackground(AppTheme.ACCENT_DARK);
                }
                c.setForeground(AppTheme.textPrimary());
                if (col == 5) {
                    String val = String.valueOf(getValueAt(row, col));
                    if ("ACTIVE".equals(val)) c.setForeground(AppTheme.SUCCESS);
                    else if ("EXPIRED".equals(val) || "INACTIVE".equals(val)) c.setForeground(AppTheme.DANGER);
                    else c.setForeground(AppTheme.WARNING);
                }
                return c;
            }
        };
        AppTheme.styleTable(table);

        JScrollPane sp = AppTheme.styledScrollPane(table);
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        tableContainer.add(sp, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        loadStudents(facade.userRepo().findAllStudents());
    }

    private void loadStudents(List<Student> students) {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getRegistrationNumber(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getDepartment() != null ? s.getDepartment() : "-",
                    s.getCourse() != null ? s.getCourse() : "-",
                    s.getSemester(),
                    s.getMembershipStatus().name(),
                    s.getCurrentBorrowCount(),
                    String.format("\u20B9%.2f", s.getFineBalancePaise() / 100.0)
            });
        }
    }

    private void filterStudents() {
        String query = searchField.getText().trim().toLowerCase();
        List<Student> students = facade.userRepo().findAllStudents();
        if (query.isEmpty()) {
            loadStudents(students);
            return;
        }
        List<Student> filtered = students.stream().filter(s -> {
            String fullName = (s.getFirstName() + " " + s.getLastName()).toLowerCase();
            return fullName.contains(query)
                    || s.getRegistrationNumber().toLowerCase().contains(query)
                    || (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(query));
        }).toList();
        loadStudents(filtered);
    }

    private void showAddDialog() {
        if (session == null) return;
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField fnF = new JTextField(); JTextField lnF = new JTextField();
        JTextField emailF = new JTextField(); JTextField phoneF = new JTextField();
        JTextField deptF = new JTextField(); JTextField courseF = new JTextField();
        JTextField semF = new JTextField("1"); JTextField sectionF = new JTextField();
        form.add(lbl("First Name:")); form.add(fnF);
        form.add(lbl("Last Name:")); form.add(lnF);
        form.add(lbl("Email:")); form.add(emailF);
        form.add(lbl("Phone:")); form.add(phoneF);
        form.add(lbl("Department:")); form.add(deptF);
        form.add(lbl("Course:")); form.add(courseF);
        form.add(lbl("Semester:")); form.add(semF);
        form.add(lbl("Section:")); form.add(sectionF);

        int result = JOptionPane.showConfirmDialog(this, form, "Register New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Student s = studentCtrl.register(session,
                        fnF.getText().trim(), lnF.getText().trim(),
                        emailF.getText().trim(), phoneF.getText().trim(),
                        deptF.getText().trim(), courseF.getText().trim(),
                        Integer.parseInt(semF.getText().trim()),
                        sectionF.getText().trim());
                refresh(session);
                AppTheme.showSuccess(this, "Student registered!\nReg No: " + s.getRegistrationNumber()
                        + "\nCard: " + s.getLibraryCardNumber());
            } catch (Exception ex) {
                AppTheme.showError(this, ex.getMessage());
            }
        }
    }

    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(AppTheme.FONT_BODY); return l; }
}
