package edu.berkeley.gamesman.testing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.*;

import edu.berkeley.gamesman.Gamesman;
import edu.berkeley.gamesman.core.Configuration;
import edu.berkeley.gamesman.core.Database;
import edu.berkeley.gamesman.database.RecordDatabase;
import edu.berkeley.gamesman.util.Util;

/**
 * A testing class for playing against a perfect play database
 * 
 * @author dnspies
 */
public class C4Container extends JPanel implements ActionListener, KeyListener,
		WindowListener {
	private static final long serialVersionUID = -8073360248394686305L;

	ConnectFour game;

	private Configuration conf;

	JRadioButton xButton;

	JRadioButton oButton;

	/**
	 * @param conf
	 *            The configuration object
	 */
	public C4Container(Configuration conf) {
		super();
		this.conf = conf;
		setLayout(new BorderLayout());
		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(2, 3));
		add(jp, BorderLayout.SOUTH);
		add(new JLabel("Press 'r' to restart"), BorderLayout.NORTH);
		ButtonGroup bg = new ButtonGroup();
		jp.add(new JLabel("Red"));
		xButton = new JRadioButton("Computer");
		xButton.addKeyListener(this);
		JRadioButton jrb = new JRadioButton("Human");
		jrb.addKeyListener(this);
		bg.add(xButton);
		bg.add(jrb);
		jp.add(jrb);
		jp.add(xButton);
		jrb.setSelected(true);
		xButton.setSelected(false);
		jrb.addActionListener(this);
		xButton.addActionListener(this);
		bg = new ButtonGroup();
		jp.add(new JLabel("Black"));
		oButton = new JRadioButton("Computer");
		oButton.addKeyListener(this);
		jrb = new JRadioButton("Human");
		jrb.addKeyListener(this);
		bg.add(oButton);
		bg.add(jrb);
		jp.add(jrb);
		jp.add(oButton);
		jrb.setSelected(false);
		oButton.setSelected(true);
		jrb.addActionListener(this);
		oButton.addActionListener(this);
		jp.setFocusable(true);
		jp.addKeyListener(this);
	}

	private void setGame(ConnectFour cf) {
		add(cf.getDisplay(), BorderLayout.CENTER);
		game = cf;
	}

	/**
	 * @param args
	 *            The job file
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			Util.fatalError("Please specify a jobfile as the only argument");
		}
		Configuration conf;
		Database fd;

		try {
			File dataFile = new File(args[0]);
			FileInputStream fis = new FileInputStream(dataFile);
			int confLength = 0;
			for (int i = 28; i >= 0; i -= 8) {
				confLength <<= 8;
				confLength |= fis.read();
			}
			byte[] confBytes = new byte[confLength];
			fis.read(confBytes);
			fis.close();
			conf = Configuration.load(confBytes);
			if (conf.getProperty("gamesman.database").equals("RecordDatabase")) {
				Gamesman.main(args);
				fd = RecordDatabase.rd;
				conf = fd.getConfiguration();
			} else {
				fd = conf.openDatabase();
			}
		} catch (ClassNotFoundException e) {
			Util.fatalError("failed to load class", e);
			return;
		} catch (IOException e) {
			Util.fatalError("IO Error", e);
			return;
		}
		int width = conf.getInteger("gamesman.game.width", 7);
		int height = conf.getInteger("gamesman.game.height", 6);
		System.out.println(fd.getRecord(0));
		DisplayFour df = new DisplayFour(height, width);
		ConnectFour cf = new ConnectFour(conf, df);
		JFrame jf = new JFrame();
		Container c = jf.getContentPane();
		C4Container c4c = new C4Container(conf);
		c4c.setGame(cf);
		c.add(c4c);
		jf.addKeyListener(c4c);
		jf.setFocusable(true);
		jf.requestFocus();
		jf.setSize(width * 100, height * 100 + 125);
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.setVisible(true);
		jf.addWindowListener(c4c);
	}

	public void actionPerformed(ActionEvent ae) {
		game.compX = xButton.isSelected();
		game.compO = oButton.isSelected();
		game.startCompMove();
	}

	public void keyPressed(KeyEvent arg0) {
	}

	public void keyReleased(KeyEvent arg0) {
	}

	public void keyTyped(KeyEvent ke) {
		if (ke.getKeyChar() == 'r') {
			for (int c = 0; c < game.gameWidth; c++) {
				for (int r = 0; r < game.gameHeight; r++) {
					game.getDisplay().slots[r][c].removeMouseListener(game);
				}
			}
			setGame(new ConnectFour(conf, game.getDisplay(), xButton
					.isSelected(), oButton.isSelected()));
			repaint();
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
		conf.db.close();
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
