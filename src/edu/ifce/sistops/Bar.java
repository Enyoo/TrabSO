package edu.ifce.sistops;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class Bar extends JPanel {

	private static final long serialVersionUID = -7065575459579164850L;

	private List<Cliente> clientes = new LinkedList<Cliente>();
	private List<String> logMensagens = new LinkedList<String>();
	private BufferedImage mesa;
        private BufferedImage casa;
	private int x,y,w,z;
	private int numClientes = 0;
	private JTextArea jta = new JTextArea();
	private Semaphore mutex = new Semaphore(1), n; // numero de cadeiras

	public Bar() throws Exception {
		setLayout(null);
		setSize(800, 600);
		String s = JOptionPane.showInputDialog("Informe o numero de cadeiras");
		int num = Integer.parseInt(s);
		this.n = new Semaphore(num);

		JFrame jf = new JFrame("O bar tem [ " + num + " ] cadeiras");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setLayout(new BorderLayout(5, 5));
		jf.add(this, BorderLayout.CENTER);
		jf.setSize(800, 650);

		JPanel jp2 = new JPanel();
		jp2.setLayout(new FlowLayout());
		jf.add(jp2, BorderLayout.NORTH);
        
		JButton bt = new JButton("Adicionar Cliente");
		jp2.add(bt);
		// bota o log
		JFrame jf1 = new JFrame("Log de Atividades");
		jf1.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JScrollPane scroll = new JScrollPane(jta);
		DefaultCaret caret = (DefaultCaret) jta.getCaret();
		caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
		jf1.add(scroll);
		jf1.setSize(800, 480);
		JButton log = new JButton("Ver Log");
		add(log);
		log.setBounds(140, 440, 230, 30);
		log.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jf1.setVisible(true);
			}
		});

		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String id_cliente = JOptionPane.showInputDialog("Qual o id do cliente?");
				long tempoBebendo = Long.parseLong(JOptionPane.showInputDialog("Quanto tempo " + id_cliente + " vai ficar no bar?"));
				long tempoEmCasa = Long.parseLong(JOptionPane.showInputDialog("Quanto tempo " + id_cliente + " vai ficar em casa?"));
				
				Bar.this.addCliente(id_cliente,tempoBebendo, tempoEmCasa);
			}
		});

		jf.setVisible(true);
                casa = Loader.INSTANCE.assetImg("house.jpg");
                w = 550;
                z = 50;
                mesa=Loader.INSTANCE.assetImg("table.jpg");
		x=50;
		y=50;
		jf.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.clearRect(0, 0, 800, 600);
		Graphics2D g2 = (Graphics2D) g;
		g.drawImage(mesa,x,y,null);
		g.drawImage(casa,w,z,null);
		g.drawString("tem " + numClientes + " bebendo", 10, 10);
		int i = clientes.size();
		int j = 0;
		while (i-- > 0) {
			Cliente b = clientes.get(i);
			b.paint(g2, i, j++);
			if(j > 3) j = 0;
		}
	}

	public void addCliente(String id_cliente,long tempoBebendo, long tempoEmCasa) {
		Cliente b = new Cliente(this,id_cliente, tempoBebendo, tempoEmCasa, n);
		clientes.add(b);
		b.start();
	}

	public void removeCliente(Cliente b) {
		clientes.remove(b);
		b.expulsa();
	}

	// TODO se um cliente chega e todas as cadeiras estiverem ocupadas,
	// significa que todos os clientes sentados estaÌƒo jantando juntos e o
	// cliente que chegou devera esperar (bloqueado) ate que todas as
	// cadeiras sejam desocupadas para so entao se sentar.
	public void entrarBar(Cliente b) throws Exception {
		mutex.acquire();
		b.beber();
		numClientes++;
		mutex.release();
	}

	public void sairBar(Cliente b) throws Exception {
		mutex.acquire();
		b.irPraCasa();
		numClientes--;
		mutex.release();
	}

	public void log(String string) {
		logMensagens.add(string);
		if (logMensagens.size() > 1000) {
			while (logMensagens.size() > 1000)
				logMensagens.remove(0);
			jta.setText("");
		}
		jta.append("\n" + string);
	}
}
