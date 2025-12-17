package app;

import model.*;
import service.Hopital;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HopitalManagementProUI extends JFrame {

    // --- PALETTE COULEURS ---
    private final Color COL_BG = new Color(240, 244, 248);
    private final Color COL_SIDEBAR = new Color(30, 41, 59);
    private final Color COL_TEXT_MAIN = new Color(30, 41, 59);
    private final Color COL_TEXT_SUB = new Color(100, 116, 139);

    private final Color COL_BLUE = new Color(59, 130, 246);
    private final Color COL_PURPLE = new Color(139, 92, 246);
    private final Color COL_TEAL = new Color(20, 184, 166);
    private final Color COL_ORANGE = new Color(249, 115, 22);
    private final Color COL_RED = new Color(239, 68, 68);

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private Hopital manager;
    private Map<String, MenuButton> menuButtons = new HashMap<>();
    private String currentView = "Dashboard";

    public HopitalManagementProUI() {
        manager = Hopital.getInstance();
        setTitle("MediFlow Pro - Gestion Hospitalière & Statistiques");
        setSize(1400, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COL_BG);
        setContentPane(rootPanel);

        rootPanel.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        refreshViews();
        rootPanel.add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void refreshViews() {
        mainContentPanel.removeAll();
        mainContentPanel.add(createDashboard(), "Dashboard");
        mainContentPanel.add(createAdminView(), "Admin");
        mainContentPanel.add(createPersonnelView(), "Personnel");
        mainContentPanel.add(createExamenView(), "Examen");

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        cardLayout.show(mainContentPanel, currentView);
    }

    // =========================================================================
    // VUE 1 : DASHBOARD AVEC STATISTIQUES (MODIFIÉ)
    // =========================================================================
    private JPanel createDashboard() {
        JPanel dashboard = new JPanel();
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setOpaque(false);

        JLabel title = new JLabel("Tableau de Bord Analytique");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        dashboard.add(title);
        dashboard.add(Box.createVerticalStrut(20));

        // 1. KPI CARDS (HAUT)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        kpiPanel.add(createKPICard("Total Patients", String.valueOf(manager.getTotalPatients()), COL_BLUE));
        kpiPanel.add(createKPICard("Services Actifs", String.valueOf(manager.getTotalServices()), COL_PURPLE));
        kpiPanel.add(createKPICard("Personnel", String.valueOf(manager.getTotalStaff()), COL_TEAL));
        kpiPanel.add(createKPICard("Occupation", (manager.getTotalPatients() * 5) + "%", COL_ORANGE));

        dashboard.add(kpiPanel);
        dashboard.add(Box.createVerticalStrut(30));

        // 2. GRILLE DE GRAPHIQUES (BAS) - 2x2
        JPanel gridGraph = new JPanel(new GridLayout(2, 2, 25, 25));
        gridGraph.setOpaque(false);

        // Jauge de capacité
        int tauxOccupation = Math.min(100, manager.getTotalPatients() * 10);
        gridGraph.add(createCard("Taux d'Occupation (%)", new RadialGauge(tauxOccupation)));

        // Charge des médecins
        gridGraph.add(createCard("Charge de Travail par Médecin", new DoctorWorkloadChart()));

        // Donut Chart - Status
        gridGraph.add(createCard("Répartition de l'État Civil", new PatientStatusDonut()));

        // Area Chart - Admissions
        int[] history = {12, 18, 15, 25, 20, 30, 28};
        gridGraph.add(createCard("Admissions (7 derniers jours)", new SmoothAreaChart(history)));

        dashboard.add(gridGraph);

        // JScrollPane pour permettre de scroller si l'écran est petit
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        JScrollPane scroll = new JScrollPane(dashboard);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        container.add(scroll);

        return container;
    }

    // =========================================================================
    // CLASSES INTERNES POUR LES GRAPHIQUES (AJOUTÉES)
    // =========================================================================

    class RadialGauge extends JPanel {
        int percent;
        public RadialGauge(int p) { this.percent = p; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()) - 40;
            int x = (getWidth()-s)/2; int y = (getHeight()-s)/2;
            g2.setColor(new Color(241, 245, 249));
            g2.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x, y, s, s, 0, 360);
            g2.setPaint(new GradientPaint(x, y, COL_BLUE, x+s, y+s, COL_PURPLE));
            g2.drawArc(x, y, s, s, 90, -(int)(3.6 * percent));
            g2.setColor(COL_TEXT_MAIN); g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
            String txt = percent + "%";
            g2.drawString(txt, getWidth()/2 - g2.getFontMetrics().stringWidth(txt)/2, getHeight()/2 + 10);
        }
    }

    class DoctorWorkloadChart extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            List<Personnel> docs = manager.getStaff().stream().filter(p -> p instanceof Medecin).limit(3).collect(Collectors.toList());
            int y = 20;
            int[] vals = {85, 60, 45};
            for(int i=0; i<Math.min(docs.size(), 3); i++) {
                g2.setColor(COL_TEXT_MAIN); g2.drawString(docs.get(i).getNom(), 0, y+14);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(100, y, getWidth()-150, 18, 18, 18);
                g2.setColor(COL_TEAL);
                g2.fillRoundRect(100, y, (int)((getWidth()-150)*(vals[i]/100.0)), 18, 18, 18);
                y+=50;
            }
        }
    }

    class PatientStatusDonut extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight()) - 60;
            int x = (getWidth()-d)/2; int y = (getHeight()-d)/2;
            g2.setStroke(new BasicStroke(22, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(COL_RED); g2.drawArc(x, y, d, d, 90, 110);
            g2.setColor(COL_TEAL); g2.drawArc(x, y, d, d, 200, 160);
            g2.setColor(COL_ORANGE); g2.drawArc(x, y, d, d, 0, 90);
        }
    }

    class SmoothAreaChart extends JPanel {
        int[] d; public SmoothAreaChart(int[] d) { this.d = d; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight()-20;
            GeneralPath path = new GeneralPath();
            int step = w/(d.length-1);
            path.moveTo(0, h - (d[0]*4));
            for(int i=0; i<d.length-1; i++) {
                path.curveTo(i*step + step/2, h - (d[i]*4), i*step + step/2, h - (d[i+1]*4), (i+1)*step, h - (d[i+1]*4));
            }
            g2.setColor(COL_BLUE); g2.setStroke(new BasicStroke(3f));
            g2.draw(path);
        }
    }

    // =========================================================================
    // AUTRES VUES (EXISTANTES DANS VOTRE CODE)
    // =========================================================================

    private JPanel createAdminView() {
        JPanel panel = createBasePanel("Administration");
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // --- SERVICES ---
        JPanel servForm = new JPanel(new FlowLayout(FlowLayout.LEFT)); servForm.setOpaque(false);
        JTextField tS = createStyledTextField();
        ModernButton btnS = new ModernButton("Créer Service", COL_BLUE);
        btnS.addActionListener(e -> { if(!tS.getText().isEmpty()){ manager.ajouterService(tS.getText()); refreshViews(); } });
        servForm.add(new JLabel("Nom :")); servForm.add(tS); servForm.add(btnS);
        DefaultTableModel mS = new DefaultTableModel(new String[]{"Service", "Capacité"}, 0);
        manager.getNomsServices().forEach(s -> mS.addRow(new Object[]{s, manager.getPatientsDuService(s).size() + " patients"}));

        // --- PATIENTS ---
        JPanel patForm = new JPanel(new FlowLayout(FlowLayout.LEFT)); patForm.setOpaque(false);
        JTextField tP = createStyledTextField();
        JComboBox<String> cbS = new JComboBox<>(manager.getNomsServices().toArray(new String[0]));
        ModernButton btnP = new ModernButton("Admettre Patient", COL_TEAL);
        btnP.addActionListener(e -> {
            if(!tP.getText().isEmpty() && cbS.getSelectedItem() != null){
                manager.ajouterPatient(new Patient("P" + (manager.getTotalPatients()+1), tP.getText()), (String)cbS.getSelectedItem());
                refreshViews();
            }
        });
        patForm.add(new JLabel("Nom Patient :")); patForm.add(tP); patForm.add(new JLabel("Service :")); patForm.add(cbS); patForm.add(btnP);
        DefaultTableModel mP = new DefaultTableModel(new String[]{"ID", "Nom", "Diagnostic"}, 0);
        manager.getTousLesPatients().forEach(p -> mP.addRow(new Object[]{p.getId(), p.getNom(), p.getDiagnostic()}));

        content.add(createCard("Gestion des Services", servForm));
        content.add(new JScrollPane(createStyledTable(mS)));
        content.add(Box.createVerticalStrut(20));
        content.add(createCard("Admission Patients", patForm));
        content.add(new JScrollPane(createStyledTable(mP)));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPersonnelView() {
        JPanel panel = createBasePanel("Ressources Humaines");
        JPanel form = new JPanel(new GridLayout(3, 4, 10, 10)); form.setOpaque(false);
        JTextField tId = createStyledTextField(); JTextField tNom = createStyledTextField(); JTextField tSpec = createStyledTextField();
        JComboBox<String> cbS = new JComboBox<>(manager.getNomsServices().toArray(new String[0]));
        JRadioButton rMed = new JRadioButton("Médecin", true); JRadioButton rInf = new JRadioButton("Infirmier");
        ButtonGroup bg = new ButtonGroup(); bg.add(rMed); bg.add(rInf);
        rMed.setOpaque(false); rInf.setOpaque(false);
        ModernButton btn = new ModernButton("Recruter", COL_PURPLE);
        btn.addActionListener(e -> {
            Personnel p;
            if(rMed.isSelected()) p = new Medecin(tId.getText(), tNom.getText(), tSpec.getText());
            else p = new Infirmier(tId.getText(), tNom.getText(), (String)cbS.getSelectedItem(), 5);
            p.setService((String)cbS.getSelectedItem()); manager.ajouterPersonnel(p); refreshViews();
        });
        form.add(new JLabel("ID :")); form.add(tId); form.add(new JLabel("Nom :")); form.add(tNom);
        form.add(rMed); form.add(rInf); form.add(new JLabel("Spécialité/Exp :")); form.add(tSpec);
        form.add(new JLabel("Service :")); form.add(cbS); form.add(new JLabel("")); form.add(btn);
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Nom", "Rôle", "Service"}, 0);
        manager.getStaff().forEach(s -> m.addRow(new Object[]{s.getId(), s.getNom(), (s instanceof Medecin ? "Médecin":"Infirmier"), s.getService()}));
        panel.add(createCard("Nouveau Personnel", form), BorderLayout.NORTH);
        panel.add(new JScrollPane(createStyledTable(m)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExamenView() {
        JPanel panel = createBasePanel("Consultation Médicale");
        JPanel split = new JPanel(new GridLayout(1, 2, 20, 0)); split.setOpaque(false);
        JComboBox<String> cbMed = new JComboBox<>(); manager.getStaff().forEach(s -> cbMed.addItem(s.getNom()));
        JComboBox<String> cbPat = new JComboBox<>(); manager.getTousLesPatients().forEach(p -> cbPat.addItem(p.getNom()));
        JTextPane report = new JTextPane(); report.setContentType("text/html"); report.setEditable(false);
        ModernButton btnExec = new ModernButton("LANCER L'EXAMEN", COL_BLUE);
        btnExec.addActionListener(e -> {
            int idxM = cbMed.getSelectedIndex(); int idxP = cbPat.getSelectedIndex();
            if(idxM != -1 && idxP != -1) {
                Personnel s = manager.getStaff().get(idxM); Patient p = manager.getTousLesPatients().get(idxP);
                s.examiner(p); // Polymorphisme Strict [cite: 7, 23]
                report.setText("<html><body style='font-family:Segoe UI; padding:15px;'><h2 style='color:#3b82f6;'>Rapport Médical</h2><hr>" +
                        "<b>Praticien :</b> Dr. " + s.getNom() + "<br><b>Patient :</b> " + p.getNom() + "<br><br>" +
                        "<div style='background:#f1f5f9; padding:10px; border-left:4px solid #3b82f6;'><b>Résultat :</b> " + p.getDiagnostic() + "</div></body></html>");
            }
        });
        JPanel left = new JPanel(new GridLayout(6, 1, 0, 10)); left.setOpaque(false);
        left.add(new JLabel("Choisir Médecin/Infirmier :")); left.add(cbMed); left.add(new JLabel("Choisir Patient :")); left.add(cbPat);
        left.add(Box.createVerticalStrut(10)); left.add(btnExec);
        split.add(createCard("Actions", left)); split.add(createCard("Rapport d'Examen", new JScrollPane(report)));
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // UTILITAIRES GRAPHIQUES
    // =========================================================================
    private JTable createStyledTable(DefaultTableModel m) {
        JTable t = new JTable(m); t.setRowHeight(35); t.setGridColor(new Color(230, 235, 240));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        DefaultTableCellRenderer r = new DefaultTableCellRenderer(); r.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setCellRenderer(r);
        return t;
    }

    private JPanel createKPICard(String title, String value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(color); g2.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel lV = new JLabel(value); lV.setFont(new Font("Segoe UI", Font.BOLD, 28)); lV.setForeground(COL_TEXT_MAIN);
        JLabel lT = new JLabel(title); lT.setForeground(COL_TEXT_SUB);
        card.add(lV); card.add(lT); return card;
    }

    private JPanel createCard(String title, JComponent comp) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false); card.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel l = new JLabel(title.toUpperCase()); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(COL_TEXT_SUB);
        card.add(l, BorderLayout.NORTH); card.add(comp, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBasePanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 20)); p.setOpaque(false);
        JLabel l = new JLabel(title); l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        p.add(l, BorderLayout.NORTH); return p;
    }

    private JTextField createStyledTextField() {
        JTextField t = new JTextField(10);
        t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 5, 5, 5)));
        return t;
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(); side.setBackground(COL_SIDEBAR);
        side.setPreferredSize(new Dimension(250, 0)); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(30, 20, 30, 20));
        JLabel logo = new JLabel("MEDIFLOW"); logo.setFont(new Font("Segoe UI", Font.BOLD, 22)); logo.setForeground(Color.WHITE);
        side.add(logo); side.add(Box.createVerticalStrut(40));
        String[] menu = {"Dashboard", "Admin", "Personnel", "Examen"};
        for(String m : menu) {
            MenuButton b = new MenuButton(m);
            b.addActionListener(e -> { currentView = m; updateMenuState(m); refreshViews(); });
            menuButtons.put(m, b); side.add(b); side.add(Box.createVerticalStrut(10));
        }
        updateMenuState("Dashboard"); return side;
    }

    private void updateMenuState(String active) { menuButtons.forEach((k, v) -> v.setActive(k.equals(active))); }

    class MenuButton extends JButton {
        boolean active = false;
        public MenuButton(String t) {
            super(t); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(new Color(148, 163, 184)); setFont(new Font("Segoe UI", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setActive(boolean b) { active = b; setForeground(active ? Color.WHITE : new Color(148, 163, 184)); }
        protected void paintComponent(Graphics g) {
            if(active) {
                Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        Color c;
        public ModernButton(String t, Color c) {
            super(t); this.c = c; setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 12)); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(HopitalManagementProUI::new);
    }
}