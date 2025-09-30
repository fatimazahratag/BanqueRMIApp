package server.gui;

import server.BanqueImpl;
import server.IBanque;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ServeurGui extends JFrame {
    private static final long serialVersionUID = 1L;

    private final IBanque banque;
    private final BanqueImpl impl;
    private final DefaultTableModel accountsModel;
    private final DefaultTableModel historyModel;

    public ServeurGui() throws Exception {
        super("🔒 Admin InnovBank – Serveur RMI");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        LocateRegistry.createRegistry(2099);
        impl   = new BanqueImpl();
        banque = impl;
        Naming.rebind("rmi://localhost:2099/BanqueService", banque);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(5,5,5,5));

        JButton btnRefreshAccounts = styledButton("Rafraîchir Comptes");
        JButton btnRefreshHistory  = styledButton("Voir Historique");
        JTextField txtAccountId    = new JTextField(5);
        txtAccountId.setMaximumSize(txtAccountId.getPreferredSize());

        toolbar.add(btnRefreshAccounts);
        toolbar.addSeparator(new Dimension(10,0));
        toolbar.add(new JLabel("Compte ID pour historique: "));
        toolbar.add(txtAccountId);
        toolbar.addSeparator(new Dimension(10,0));
        toolbar.add(btnRefreshHistory);

        add(toolbar, BorderLayout.NORTH);

        accountsModel = new DefaultTableModel(new String[]{"Compte ID", "Solde"}, 0);
        JTable tblAccounts = styledTable(accountsModel);

        historyModel = new DefaultTableModel(new String[]{"Opérations"}, 0);
        JTable tblHistory = styledTable(historyModel);

        JScrollPane spAccounts = new JScrollPane(tblAccounts);
        spAccounts.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(135,206,250)),
            "Comptes",
            0, 0,
            new Font("SansSerif", Font.BOLD, 14),
            new Color(255,165,0)
        ));

        JScrollPane spHistory = new JScrollPane(tblHistory);
        spHistory.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(135,206,250)),
            "Historique",
            0, 0,
            new Font("SansSerif", Font.BOLD, 14),
            new Color(255,165,0)
        ));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spAccounts, spHistory);
        split.setResizeWeight(0.6);
        split.setBorder(null);
        ((BasicSplitPaneUI)split.getUI()).getDivider().setBorder(null);
        add(split, BorderLayout.CENTER);

        btnRefreshAccounts.addActionListener(e -> loadAccounts());
        btnRefreshHistory.addActionListener(e -> {
            historyModel.setRowCount(0);
            try {
                int id = Integer.parseInt(txtAccountId.getText().trim());
                for (String op : banque.listerOperations(id)) {
                    historyModel.addRow(new Object[]{op});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                  "Erreur historique:\n" + ex.getMessage(),
                  "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadAccounts();
        setVisible(true);
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(255,165,0));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(255,165,0));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(135,206,250));
        return table;
    }

    private void loadAccounts() {
        accountsModel.setRowCount(0);
        int maxId = impl.getMaxCompteId();
        for (int id = 1; id <= maxId; id++) {
            try {
                double solde = banque.consulterSolde(id);
                accountsModel.addRow(new Object[]{id, String.format("%.2f", solde)});
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ServeurGui();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                  "Impossible de démarrer l’interface serveur:\n" + e.getMessage(),
                  "Erreur", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
