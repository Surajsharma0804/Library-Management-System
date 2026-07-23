package com.library.gui;

import com.library.enums.RoomReservationStatus;
import com.library.enums.UserRole;
import com.library.facade.LibraryFacade;
import com.library.model.RoomReservation;
import com.library.model.StudyRoom;
import com.library.security.Permissions;
import com.library.security.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Room Reservation Panel — Study room booking for students.
 * Requirements: 24.1, 24.2
 */
public final class RoomReservationPanel extends JPanel {

    private final LibraryFacade facade;
    private Session session;

    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JTable availabilityTable;
    private DefaultTableModel availabilityModel;
    private PaginatedTable<RoomReservation> myReservationsTable;

    private List<StudyRoom> allRooms;
    private StudyRoom selectedRoom;

    public RoomReservationPanel(LibraryFacade facade) {
        this.facade = facade;
        setBackground(AppTheme.bg());
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        build();
    }

    private void build() {
        removeAll();

        // Header
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(AppTheme.heading("Room Reservations"));
        hdr.add(Box.createVerticalStrut(4));
        hdr.add(AppTheme.label2("Book study rooms and collaborative spaces"));
        add(hdr, BorderLayout.NORTH);

        // Main split: left (room list) + right (availability grid + my reservations)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setDividerLocation(300);
        split.setResizeWeight(0.3);

        // LEFT: Room list
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(AppTheme.BODY);
        roomList.setBackground(AppTheme.bgCard());
        roomList.setForeground(AppTheme.fg());
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setBorder(new EmptyBorder(8, 8, 8, 8));
        roomList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRoomSelected();
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setOpaque(false);
        leftPanel.add(AppTheme.label("Select Study Room:"), BorderLayout.NORTH);
        leftPanel.add(AppTheme.scroll(roomList), BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);

        // RIGHT: Availability grid + Book Slot + My Reservations
        JPanel rightPanel = new JPanel(new BorderLayout(0, 16));
        rightPanel.setOpaque(false);

        // Availability grid (7-day)
        availabilityModel = new DefaultTableModel(
                new String[]{"Time Slot", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availabilityTable = new JTable(availabilityModel);
        AppTheme.styleTable(availabilityTable);
        availabilityTable.setRowHeight(28);

        JPanel availPanel = new JPanel(new BorderLayout(0, 8));
        availPanel.setOpaque(false);

        JPanel availHdr = new JPanel(new BorderLayout());
        availHdr.setOpaque(false);
        availHdr.add(AppTheme.label("7-Day Availability (CONFIRMED slots shown)"), BorderLayout.WEST);
        JButton bookSlotBtn = AppTheme.primaryBtn("Book Slot");
        bookSlotBtn.setPreferredSize(new Dimension(110, 34));
        bookSlotBtn.addActionListener(e -> doBookSlot());
        availHdr.add(bookSlotBtn, BorderLayout.EAST);

        availPanel.add(availHdr, BorderLayout.NORTH);
        availPanel.add(AppTheme.scroll(availabilityTable), BorderLayout.CENTER);
        availPanel.setPreferredSize(new Dimension(600, 300));

        // My Reservations table
        myReservationsTable = new PaginatedTable<>(
                new String[]{"Room", "Date", "Start", "End", "Status"},
                res -> new Object[]{
                        getRoomName(res.getRoomId()),
                        res.getDate().toString(),
                        res.getStartTime(),
                        res.getEndTime(),
                        res.getStatus().name()
                },
                10
        );

        JPanel myResPanel = new JPanel(new BorderLayout(0, 8));
        myResPanel.setOpaque(false);

        JPanel myResHdr = new JPanel(new BorderLayout());
        myResHdr.setOpaque(false);
        myResHdr.add(AppTheme.label("My Reservations"), BorderLayout.WEST);
        JButton cancelBtn = AppTheme.dangerBtn("Cancel");
        cancelBtn.setPreferredSize(new Dimension(90, 34));
        cancelBtn.addActionListener(e -> doCancelReservation());
        myResHdr.add(cancelBtn, BorderLayout.EAST);

        myResPanel.add(myResHdr, BorderLayout.NORTH);
        myResPanel.add(myReservationsTable, BorderLayout.CENTER);

        rightPanel.add(availPanel, BorderLayout.CENTER);
        rightPanel.add(myResPanel, BorderLayout.SOUTH);

        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);
    }

    public void refresh(Session session) {
        this.session = session;
        setBackground(AppTheme.bg());

        // STUDENT only
        if (session.role() != UserRole.STUDENT) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("Room reservations are only available for students.");
            denied.setFont(AppTheme.H3);
            denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Check ROOM_RESERVATION_CREATE permission
        try {
            facade.rbac().require(session, Permissions.ROOM_RESERVATION_CREATE);
        } catch (Exception ex) {
            removeAll();
            setLayout(new BorderLayout());
            JLabel denied = new JLabel("ROOM_RESERVATION_CREATE permission required.");
            denied.setFont(AppTheme.H3);
            denied.setForeground(AppTheme.RED);
            denied.setHorizontalAlignment(SwingConstants.CENTER);
            add(denied, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Load rooms and reservations
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                allRooms = facade.studyRoomRepo().findAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    populateRoomList();
                    refreshMyReservations();
                } catch (Exception e) {
                    AppTheme.error(RoomReservationPanel.this, "Failed to load rooms: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void populateRoomList() {
        roomListModel.clear();
        if (allRooms != null) {
            for (StudyRoom room : allRooms) {
                if (room.isActive()) {
                    roomListModel.addElement(room.getRoomName() + " (Cap: " + room.getCapacity() + ")");
                }
            }
        }
    }

    private void onRoomSelected() {
        int idx = roomList.getSelectedIndex();
        if (idx < 0 || allRooms == null || idx >= allRooms.size()) {
            selectedRoom = null;
            clearAvailability();
            return;
        }
        selectedRoom = allRooms.get(idx);
        loadAvailability(selectedRoom.getId());
    }

    private void loadAvailability(String roomId) {
        new SwingWorker<Map<LocalDate, List<RoomReservation>>, Void>() {
            @Override
            protected Map<LocalDate, List<RoomReservation>> doInBackground() {
                return facade.roomReservations().getAvailabilityForRoom(roomId, LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    Map<LocalDate, List<RoomReservation>> avail = get();
                    displayAvailability(avail);
                } catch (Exception e) {
                    AppTheme.error(RoomReservationPanel.this, "Failed to load availability: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void displayAvailability(Map<LocalDate, List<RoomReservation>> avail) {
        availabilityModel.setRowCount(0);
        
        // Build simple time slots representation (sample slots)
        String[] slots = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00",
                          "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"};
        
        // For simplicity, show confirmed reservations count per day
        // (A full implementation would show time-slot occupancy)
        List<LocalDate> dates = avail.keySet().stream().sorted().toList();
        Object[] row = new Object[8];
        row[0] = "CONFIRMED";
        for (int i = 0; i < dates.size() && i < 7; i++) {
            LocalDate d = dates.get(i);
            List<RoomReservation> confirmed = avail.get(d);
            row[i + 1] = confirmed.size() + " booked";
        }
        availabilityModel.addRow(row);
        
        // Show dates as second row
        Object[] dateRow = new Object[8];
        dateRow[0] = "Date";
        for (int i = 0; i < dates.size() && i < 7; i++) {
            dateRow[i + 1] = dates.get(i).toString();
        }
        availabilityModel.insertRow(0, dateRow);
    }

    private void clearAvailability() {
        availabilityModel.setRowCount(0);
    }

    private void doBookSlot() {
        if (session == null || selectedRoom == null) {
            AppTheme.error(this, "Please select a study room first.");
            return;
        }

        JTextField dateField = AppTheme.textField(12);
        dateField.setToolTipText("yyyy-MM-dd");
        JTextField startField = AppTheme.textField(8);
        startField.setToolTipText("HH:mm");
        JTextField endField = AppTheme.textField(8);
        endField.setToolTipText("HH:mm");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Date (yyyy-MM-dd):"));
        form.add(dateField);
        form.add(new JLabel("Start Time (HH:mm):"));
        form.add(startField);
        form.add(new JLabel("End Time (HH:mm):"));
        form.add(endField);

        int result = JOptionPane.showConfirmDialog(this, form, "Book Study Room Slot",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String dateStr = dateField.getText().trim();
        String start = startField.getText().trim();
        String end = endField.getText().trim();

        if (dateStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
            AppTheme.error(this, "All fields are required.");
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            AppTheme.error(this, "Date must be in yyyy-MM-dd format.");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                facade.roomReservations().reserve(session, selectedRoom.getId(), date, start, end);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    AppTheme.success(RoomReservationPanel.this, "Room slot reserved successfully.");
                    loadAvailability(selectedRoom.getId());
                    refreshMyReservations();
                } catch (IllegalStateException e) {
                    AppTheme.error(RoomReservationPanel.this, "Conflict: " + e.getMessage());
                } catch (Exception e) {
                    AppTheme.error(RoomReservationPanel.this, "Failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void doCancelReservation() {
        if (session == null) return;

        RoomReservation selected = myReservationsTable.getSelectedItem();
        if (selected == null) {
            AppTheme.error(this, "Please select a reservation to cancel.");
            return;
        }

        if (selected.getStatus() == RoomReservationStatus.CANCELLED) {
            AppTheme.error(this, "This reservation is already cancelled.");
            return;
        }

        if (!AppTheme.confirm(this, "Cancel this reservation?")) return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                facade.roomReservations().cancel(session, selected.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    AppTheme.success(RoomReservationPanel.this, "Reservation cancelled.");
                    refreshMyReservations();
                    if (selectedRoom != null) {
                        loadAvailability(selectedRoom.getId());
                    }
                } catch (Exception e) {
                    AppTheme.error(RoomReservationPanel.this, "Failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void refreshMyReservations() {
        if (session == null) return;
        new SwingWorker<List<RoomReservation>, Void>() {
            @Override
            protected List<RoomReservation> doInBackground() {
                return facade.roomReservations().findByStudent(session);
            }

            @Override
            protected void done() {
                try {
                    myReservationsTable.load(get());
                } catch (Exception e) {
                    // Silently ignore
                }
            }
        }.execute();
    }

    private String getRoomName(String roomId) {
        if (allRooms == null) return roomId;
        return allRooms.stream()
                .filter(r -> r.getId().equals(roomId))
                .findFirst()
                .map(StudyRoom::getRoomName)
                .orElse(roomId);
    }
}
