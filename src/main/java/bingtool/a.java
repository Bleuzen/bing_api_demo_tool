package bingtool;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@SuppressWarnings("serial")
public class a extends JFrame {

	private JPanel contentPane;
	private JPanel panelAuswertung;
	private JLabel lblUrl;
	private JTextField txtUrl;
	private JButton btnAuswerten;
	private JLabel lblKey;
	private JTextField txtKey;
	private JButton btnAuswSubdomains;
	private JButton btnAuswDuplikate;
	private JButton btnAuswFundorte;
	private JTextArea txtrLog;
	private JSpinner spinnerSeiten;
	private JLabel lblSeiten;
	private JScrollPane scrollPane;
	
	private static Logger logger;
	static String site;
	private JButton btnReset;

	public static void main(String[] args) {
		
    	// setup own logger
    	logger = (Logger) LoggerFactory.getLogger(a.class);
    	logger.setLevel(Level.DEBUG);
    	
    	// setup apache http client logger
    	Logger apacheLogger = (Logger) LoggerFactory.getLogger("org.apache.http");
    	apacheLogger.setLevel(Level.WARN);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if(System.getProperty("os.name").toLowerCase().equals("linux") && System.getenv("XDG_CURRENT_DESKTOP").toLowerCase().equals("kde")) {
						// KDE theme fix
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
					} else {
						// Use the systems theme
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					
					a frame = new a();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public a() {
		setResizable(false);
		setTitle("Schnuffi Tuul 4 Bing");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(900, 400);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panelAuswertung = new JPanel();
		panelAuswertung.setBorder(new TitledBorder(null, "Auswertung", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelAuswertung.setBounds(10, 66, 874, 294);
		contentPane.add(panelAuswertung);
		
		lblUrl = new JLabel("URL:");
		lblUrl.setBounds(10, 36, 40, 18);
		contentPane.add(lblUrl);
		
		txtUrl = new JTextField();
		txtUrl.setBounds(60, 34, 616, 24);
		contentPane.add(txtUrl);
		
		btnAuswerten = new JButton("Mach");
		btnAuswerten.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(txtKey.getText().isEmpty() || txtUrl.getText().isEmpty()) {
					String txtBefore = btnAuswerten.getText();
					btnAuswerten.setText("doch selber");
					JOptionPane.showMessageDialog(null, "Da fehlt was.", getTitle(), JOptionPane.ERROR_MESSAGE);
					btnAuswerten.setText(txtBefore);
					return;
				}
				
				btnAuswerten.setEnabled(false);
				txtUrl.setEnabled(false);
				txtKey.setEnabled(false);
				spinnerSeiten.setEnabled(false);
				
				site = txtUrl.getText();
				new Thread(new Runnable() {
					@Override
					public void run() {
						boolean ok = OldMain.download((int) spinnerSeiten.getValue(), txtKey.getText(), site);
						if(ok) {
							Utils.setJPanelEnabled(panelAuswertung, true);
						}
					}
				}).start();
			}
		});
		btnAuswerten.setBounds(774, 34, 108, 24);
		contentPane.add(btnAuswerten);
		
		lblKey = new JLabel("Key:");
		lblKey.setBounds(10, 11, 40, 18);
		contentPane.add(lblKey);
		
		txtKey = new JTextField();
		txtKey.setBounds(60, 8, 710, 24);
		contentPane.add(txtKey);
		
		panelAuswertung.setLayout(null);
		
		btnAuswSubdomains = new JButton("Subdomains");
		btnAuswSubdomains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtrLog.append(System.lineSeparator());
				OldMain.subdomainsAusgeben();
			}
		});
		btnAuswSubdomains.setBounds(10, 18, 120, 24);
		panelAuswertung.add(btnAuswSubdomains);
		
		btnAuswDuplikate = new JButton("Duplikate");
		btnAuswDuplikate.setToolTipText("Finde URLs mit unterschiedlichen Protokollen");
		btnAuswDuplikate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtrLog.append(System.lineSeparator());
				OldMain.duplikateMitAnderemProtokollAusgeben();
			}
		});
		btnAuswDuplikate.setBounds(362, 18, 120, 24);
		panelAuswertung.add(btnAuswDuplikate);
		
		btnAuswFundorte = new JButton("Fundorte");
		btnAuswFundorte.setToolTipText("Auf welchen Bing Seiten die URL gefunden wurde");
		btnAuswFundorte.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtrLog.append(System.lineSeparator());
				OldMain.seitennummernZuUrlsAusgeben();
			}
		});
		btnAuswFundorte.setBounds(742, 18, 120, 24);
		panelAuswertung.add(btnAuswFundorte);
		
		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(10, 48, 854, 238);
		panelAuswertung.add(scrollPane);
		
		txtrLog = new JTextArea();
		scrollPane.setViewportView(txtrLog);
		txtrLog.setEditable(false);
		txtrLog.setLineWrap(true);
		
    	// setup GUI logger
    	new LogbackAppenderForGUI(txtrLog);
		
		spinnerSeiten = new JSpinner();
		spinnerSeiten.setModel(new SpinnerNumberModel(30, 1, 50, 1));
		spinnerSeiten.setBounds(824, 8, 56, 24);
		contentPane.add(spinnerSeiten);
		
		lblSeiten = new JLabel("Seiten:");
		lblSeiten.setBounds(776, 10, 44, 18);
		contentPane.add(lblSeiten);
		
		Utils.setJPanelEnabled(panelAuswertung, false);
		
		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAuswerten.setEnabled(true);
				txtUrl.setEnabled(true);
				txtKey.setEnabled(true);
				spinnerSeiten.setEnabled(true);
				txtUrl.setText("");
				txtKey.setText("");
				spinnerSeiten.setValue(30);
				Utils.setJPanelEnabled(panelAuswertung, false);
			}
		});
		btnReset.setBounds(680, 34, 90, 24);
		contentPane.add(btnReset);
	}
}
