package com.library.gui;

import com.library.controller.StudentController;
import com.library.facade.LibraryFacade;
import com.library.media.IDCardGenerator;
import com.library.model.Student;
import com.library.security.Permissions;
import com.library.security.Session;
import com.library.service.StudentImportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

/**
 * Executive Student Registry Panel — Manage student records, membership status,
 * instant search, and registration workflows.
 *
 * @author University Central Library — Software Engineering Division
 * @version 2.0.0
 */
public final class StudentsPanel extends JPanel {

    private final LibraryFacade facade;
    private final StudentController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private Session session;

    private static final String[] COLS = {"Reg No.", "Name", "Department", "Course", "Sem", "Status", "Borrows", "Fine Balance"};

    public StudentsPanel(LibraryFacade facade) {
        this.facade = facade;
        this.ctrl = new StudentController(facade);
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);

        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Student Records"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Directory of registered library members and active standing"));

        searchField = AppTheme.textField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, reg number, department...");
        searchField.setPreferredSize(new Dimension(320, 38));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JButton addBtn = AppTheme.primaryBtn("+ Add Student");
        addBtn.setPreferredSize(new Dimension(140, 38));
        addBtn.addActionListener(e -> addStudent());

        JButton genCardBtn = AppTheme.secondaryBtn("Generate Card");
        genCardBtn.setPreferredSize(new Dimension(140, 38));
        genCardBtn.addActionListener(e -> generateCard());

        JButton importCsvBtn = AppTheme.secondaryBtn("Import CSV");
        importCsvBtn.setPreferredSize(new Dimension(120, 38));
        importCsvBtn.addActionListener(e -> importCsv());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acts.setOpaque(false);
        acts.add(searchField);
        acts.add(genCardBtn);
        acts.add(importCsvBtn);
        acts.add(addBtn);

        hdr.add(title, BorderLayout.WEST);
        hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        AppTheme.styleTable(table);

        // Status column pill renderer
        table.getColumnModel().getColumn(5).setCellRenderer((tbl, val, isSelected, hasFocus, row, col) -> {
            String statusStr = val != null ? val.toString() : "UNKNOWN";
            JPanel pill = AppTheme.createStatusPill(statusStr);
            if (isSelected) {
                pill.setOpaque(true);
                pill.setBackground(tbl.getSelectionBackground());
            } else {
                pill.setOpaque(false);
            }
            return pill;
        });

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false);
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH);
        add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s;
        setBackground(AppTheme.bg());
        load(facade.userRepo().findAllStudents());
    }

    private void load(List<Student> list) {
        model.setRowCount(0);
        for (Student s : list) {
            model.addRow(new Object[]{
                    s.getRegistrationNumber(),
                    s.getFirstName() + " " + s.getLastName(),
                    s.getDepartment() != null ? s.getDepartment() : "-",
                    s.getCourse() != null ? s.getCourse() : "-",
                    s.getSemester(),
                    s.getMembershipStatus().name(),
                    s.getCurrentBorrowCount(),
                    String.format("₹%.2f", s.getFineBalancePaise() / 100.0)
            });
        }
    }

    private void filter() {
        String q = searchField.getText().trim().toLowerCase();
        List<Student> all = facade.userRepo().findAllStudents();
        if (q.isEmpty()) { load(all); return; }
        load(all.stream().filter(s ->
                (s.getFirstName() + " " + s.getLastName()).toLowerCase().contains(q)
                || s.getRegistrationNumber().toLowerCase().contains(q)
                || (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q))
        ).toList());
    }

    private void generateCard() {
        if (session == null) return;

        // 1. Permission check
        try {
            facade.rbac().require(session, Permissions.STUDENT_GENERATE_CARD);
        } catch (Exception ex) {
            AppTheme.error(this, ex.getMessage());
            return;
        }

        // 2. Get selected student
        int row = table.getSelectedRow();
        if (row < 0) {
            AppTheme.error(this, "Please select a student first.");
            return;
        }
        String regNo = (String) model.getValueAt(row, 0);
        Student student = facade.userRepo().findStudentByRegistrationNumber(regNo);
        if (student == null) {
            AppTheme.error(this, "Student record not found.");
            return;
        }

        // 3. Generate in SwingWorker
        new SwingWorker<Path, Void>() {
            @Override
            protected Path doInBackground() throws Exception {
                return new IDCardGenerator().generateAndSave(student, facade.config().get());
            }

            @Override
            protected void done() {
                try {
                    Path saved = get();
                    JOptionPane.showMessageDialog(StudentsPanel.this,
                            "ID Card saved to:\n" + saved.toAbsolutePath(),
                            "ID Card Generated", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    AppTheme.error(StudentsPanel.this, "Failed to generate ID card: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void importCsv() {
        if (session == null) return;

        // 1. File chooser
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CSV File");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();

        // 2. Import in SwingWorker
        new SwingWorker<StudentImportService.ImportResult, Void>() {
            @Override
            protected StudentImportService.ImportResult doInBackground() throws Exception {
                return facade.studentImport().importFromCsv(session, file.toPath());
            }

            @Override
            protected void done() {
                try {
                    StudentImportService.ImportResult result = get();
                    StringBuilder msg = new StringBuilder();
                    msg.append("Imported: ").append(result.imported()).append(" students\n");
                    msg.append("Skipped: ").append(result.skipped()).append(" rows");
                    if (!result.skipReasons().isEmpty()) {
                        msg.append("\n\nSkip reasons:\n");
                        result.skipReasons().forEach(r -> msg.append("  • ").append(r).append("\n"));
                    }
                    JOptionPane.showMessageDialog(StudentsPanel.this, msg.toString(),
                            "CSV Import Complete", JOptionPane.INFORMATION_MESSAGE);
                    if (result.imported() > 0) refresh(session);
                } catch (Exception ex) {
                    AppTheme.error(StudentsPanel.this, "Import failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void addStudent() {
        if (session == null) return;

        JPanel f = new JPanel(new GridLayout(0, 2, 12, 12));
        f.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField fn = AppTheme.textField(15);
        JTextField ln = AppTheme.textField(15);
        JTextField em = AppTheme.textField(15);
        JTextField ph = AppTheme.textField(15);
        JTextField dp = AppTheme.textField(15);
        JTextField co = AppTheme.textField(15);
        JTextField sm = AppTheme.textField(15); sm.setText("1");
        JTextField sc = AppTheme.textField(15);

        f.add(lbl("First Name:")); f.add(fn);
        f.add(lbl("Last Name:"));  f.add(ln);
        f.add(lbl("Email:"));      f.add(em);
        f.add(lbl("Phone:"));      f.add(ph);
        f.add(lbl("Department:")); f.add(dp);
        f.add(lbl("Course:"));     f.add(co);
        f.add(lbl("Semester:"));   f.add(sm);
        f.add(lbl("Section:"));    f.add(sc);

        if (JOptionPane.showConfirmDialog(this, f, "Register New Student Record",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Student s = ctrl.register(session, fn.getText().trim(), ln.getText().trim(),
                        em.getText().trim(), ph.getText().trim(), dp.getText().trim(),
                        co.getText().trim(), Integer.parseInt(sm.getText().trim()), sc.getText().trim());
                refresh(session);
                AppTheme.success(this, "Student successfully registered!\nRegistration No: " + s.getRegistrationNumber());
            } catch (Exception ex) {
                AppTheme.error(this, ex.getMessage());
            }
        }
    }

    private JLabel lbl(String s) {
        var l = new JLabel(s);
        l.setFont(AppTheme.BODY_B);
        l.setForeground(AppTheme.fg());
        return l;
    }
}
