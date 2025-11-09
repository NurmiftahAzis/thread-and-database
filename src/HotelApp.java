import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HotelApp extends JFrame {

    private JTextField txtNama, txtKamar, txtCheckIn, txtCheckOut, txtHapus;
    private DefaultTableModel model;
    private JTable table;
    private final int HARGA_PER_MALAM = 250000;

    public HotelApp() {
        setTitle("🏨 Manajemen Hotel Sederhana");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 245, 245));

        // ===== PANEL ATAS =====
        JPanel panelInput = new JPanel(new GridBagLayout());
        panelInput.setBackground(Color.WHITE);
        panelInput.setBorder(BorderFactory.createTitledBorder("Form Tambah / Hapus Tamu"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNama = new JLabel("Nama:");
        JLabel lblKamar = new JLabel("Nomor Kamar:");
        JLabel lblCheckIn = new JLabel("Check-in (YYYY-MM-DD):");
        JLabel lblCheckOut = new JLabel("Check-out (YYYY-MM-DD):");
        JLabel lblHapus = new JLabel("ID (hapus):");

        txtNama = new JTextField(15);
        txtKamar = new JTextField(15);
        txtCheckIn = new JTextField(15);
        txtCheckOut = new JTextField(15);
        txtHapus = new JTextField(15);

        JButton btnTambah = new JButton("Tambah Tamu");
        JButton btnHapus = new JButton("Hapus Tamu");
        JButton btnRefresh = new JButton("🔄 Refresh Data");

        btnTambah.setBackground(new Color(63, 153, 227));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFocusPainted(false);

        btnHapus.setBackground(new Color(227, 63, 63));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFocusPainted(false);

        btnRefresh.setBackground(new Color(63, 153, 100));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);

        // ===== Tambah komponen ke panel =====
        gbc.gridx = 0; gbc.gridy = 0; panelInput.add(lblNama, gbc);
        gbc.gridx = 1; panelInput.add(txtNama, gbc);
        gbc.gridx = 2; panelInput.add(lblKamar, gbc);
        gbc.gridx = 3; panelInput.add(txtKamar, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panelInput.add(lblCheckIn, gbc);
        gbc.gridx = 1; panelInput.add(txtCheckIn, gbc);
        gbc.gridx = 2; panelInput.add(lblCheckOut, gbc);
        gbc.gridx = 3; panelInput.add(txtCheckOut, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panelInput.add(lblHapus, gbc);
        gbc.gridx = 1; panelInput.add(txtHapus, gbc);
        gbc.gridx = 2; panelInput.add(btnTambah, gbc);
        gbc.gridx = 3; panelInput.add(btnHapus, gbc);

        // ===== TABEL =====
        String[] kolom = {"ID", "Nama", "Kamar", "Check-in", "Check-out", "Total Biaya"};
        model = new DefaultTableModel(kolom, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // ===== PANEL BAWAH =====
        JPanel panelBawah = new JPanel();
        panelBawah.setBackground(Color.WHITE);
        panelBawah.add(btnRefresh);

        // ===== EVENT LISTENER =====
        btnTambah.addActionListener(e -> tambahTamu());
        btnHapus.addActionListener(e -> hapusTamu());
        btnRefresh.addActionListener(e -> loadData());

        // ===== TAMBAH SEMUA PANEL =====
        add(panelInput, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBawah, BorderLayout.SOUTH);

        loadData();
    }

    private void tambahTamu() {
        String nama = txtNama.getText();
        String kamar = txtKamar.getText();
        String checkin = txtCheckIn.getText();
        String checkout = txtCheckOut.getText();

        if (nama.isEmpty() || kamar.isEmpty() || checkin.isEmpty() || checkout.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap isi semua data!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate in = LocalDate.parse(checkin);
            LocalDate out = LocalDate.parse(checkout);
            long lamaMenginap = ChronoUnit.DAYS.between(in, out);
            if (lamaMenginap <= 0) {
                JOptionPane.showMessageDialog(this, "Tanggal check-out harus setelah check-in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long totalBiaya = lamaMenginap * HARGA_PER_MALAM;

            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO tamu (nama, kamar, tanggal_checkin, tanggal_checkout) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nama);
            stmt.setInt(2, Integer.parseInt(kamar));
            stmt.setString(3, checkin);
            stmt.setString(4, checkout);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Tamu berhasil ditambahkan!\nTotal Biaya: Rp " + totalBiaya);
            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    private void hapusTamu() {
        String id = txtHapus.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan ID tamu yang ingin dihapus!");
            return;
        }
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM tamu WHERE id=?");
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tamu berhasil dihapus!");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tamu");
            while (rs.next()) {
                LocalDate in = rs.getDate("tanggal_checkin").toLocalDate();
                LocalDate out = rs.getDate("tanggal_checkout").toLocalDate();
                long hari = ChronoUnit.DAYS.between(in, out);
                long total = hari * HARGA_PER_MALAM;
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getInt("kamar"),
                        rs.getString("tanggal_checkin"),
                        rs.getString("tanggal_checkout"),
                        "Rp " + total
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }
}
