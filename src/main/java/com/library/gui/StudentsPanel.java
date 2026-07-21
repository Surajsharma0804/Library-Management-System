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
 * Student members management — registry, search, status tracking.
 */
public final class StudentsPanel extends JPanel {

    private final LibraryFacade facade;
    private final StudentController ctrl;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private Session session;

    private static final String[] COLS = {"Reg No.", "Name", "Department", "Course", "Sem", "Status", "Borrows", "Fine"};

    public StudentsPanel(LibraryFacade facade) {
        this.facade = facade;
        this.ctrl = new StudentController(facade);
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        build();
    }

    private void build() {
        JPanel hdr = new JPanel(new BorderLayout(16, 0));
        hdr.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(AppTheme.heading("Students"));
        title.add(Box.createVerticalStrut(4));
        title.add(AppTheme.label2("Manage student members and memberships"));

        searchField = AppTheme.textField(22);
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JButton addBtn = AppTheme.primaryBtn("+ Add Student");
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e -> addStudent());

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acts.setOpaque(false);
        acts.add(searchField); acts.add(addBtn);
        hdr.add(title, BorderLayout.WEST); hdr.add(acts, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0);
        table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rn, int r, int c) {
                Component comp = super.prepareRenderer(rn, r, c);
                if (!isRowSelected(r)) comp.setBackground(r % 2 == 0 ? AppTheme.bgCard() : AppTheme.tableAlt());
                else comp.setBackground(new Color(AppTheme.ACCENT.getRed(), AppTheme.ACCENT.getGreen(), AppTheme.ACCENT.getBlue(), 40));
                comp.setForeground(AppTheme.fg());
                if (c == 5) { String v = String.valueOf(getValueAt(r, c));
                    if ("ACTIVE".equals(v)) comp.setForeground(AppTheme.GREEN);
                    else comp.setForeground(AppTheme.RED);
                }
                return comp;
            }
        };
        AppTheme.styleTable(table);

        JPanel tbl = new JPanel(new BorderLayout());
        tbl.setOpaque(false); tbl.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        tbl.add(AppTheme.scroll(table), BorderLayout.CENTER);

        add(hdr, BorderLayout.NORTH); add(tbl, BorderLayout.CENTER);
    }

    public void refresh(Session s) {
        this.session = s; setBackground(AppTheme.bg());
        load(facade.userRepo().findAllStudents());
    }

    private void load(List<Student> list) {
        model.setRowCount(0);
        for (Student s : list) model.addRow(new Object[]{
                s.getRegistrationNumber(),
                s.getFirstName() + " " + s.getLastName(),
                s.getDepartment() != null ? s.getDepartment() : "-",
                s.getCourse() != null ? s.getCourse() : "-",
                s.getSemester(), s.getMembershipStatus().name(),
                s.getCurrentBorrowCount(),
                String.format("\u20B9%.2f", s.getFineBalancePaise() / 100.0)});
    }

    private void filter() {
        String q = searchField.getText().trim().toLowerCase();
        List<Student> all = facade.userRepo().findAllStudents();
        if (q.isEmpty()) { load(all); return; }
        load(all.stream().filter(s -> (s.getFirstName() + " " + s.getLastName()).toLowerCase().contains(q)
                || s.getRegistrationNumber().toLowerCase().contains(q)
                || (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q))).toList());
    }

    private void addStudent() {
        if (session == null) return;
        JPanel f = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField fn = t(), ln = t(), em = t(), ph = t(), dp = t(), co = t(), sm = t("1"), sc = t();
        f.add(lbl("First Name:")); f.add(fn);  f.add(lbl("Last Name:"));  f.add(ln);
        f.add(lbl("Email:"));      f.add(em);  f.add(lbl("Phone:"));      f.add(ph);
        f.add(lbl("Department:")); f.add(dp);  f.add(lbl("Course:"));     f.add(co);
        f.add(lbl("Semester:"));   f.add(sm);  f.add(lbl("Section:"));    f.add(sc);
        if (JOptionPane.showConfirmDialog(this, f, "Register New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Student s = ctrl.register(session, fn.getText().trim(), ln.getText().trim(),
                        em.getText().trim(), ph.getText().trim(), dp.getText().trim(),
                        co.getText().trim(), Integer.parseInt(sm.getText().trim()), sc.getText().trim());
                refresh(session);
                AppTheme.success(this, "Student registered!\nReg: " + s.getRegistrationNumber());
            } catch (Exception ex) { AppTheme.error(this, ex.getMessage()); }
        }
    }

    private JTextField t()        { return new JTextField(); }
    private JTextField t(String v){ var tf = new JTextField(v); return tf; }
    private JLabel lbl(String s)  { var l = new JLabel(s); l.setFont(AppTheme.BODY); return l; }
}
