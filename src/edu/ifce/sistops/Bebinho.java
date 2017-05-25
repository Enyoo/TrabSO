package edu.ifce.sistops;

import java.awt.Graphics2D;
import java.util.concurrent.Semaphore;

public class Bebinho extends Thread {

	private long t1, t2, tb, tc, tempoCorrido = 0;
	private SituacaoBebinho situacao = SituacaoBebinho.NO_BAR;
	private Buteco buteco;
	private String id;
	private boolean expulso;
	private Semaphore n;
	
	

	public Bebinho(Buteco buteco, long tempoBebendo, long tempoEmCasa, Semaphore n) {
		this.id = this.toString();
		this.tb = tempoBebendo;
		this.tc = tempoEmCasa;
		this.buteco = buteco;
		this.n = n;
	}

	@Override
	public void run() {
		try {
			buteco.log(id + " acabou de chegar no bar");
			n.acquire();
			buteco.entrarButeco(this);
			t1 = System.currentTimeMillis();
			while (!expulso) {
				t2 = System.currentTimeMillis();
				if (t2 - t1 > 1000) {
					step(); // roda uma vez por segundo
					t1 = System.currentTimeMillis();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void step() throws Exception {
		tempoCorrido++;
		if (situacao == SituacaoBebinho.NO_BAR) {
			stepBar();
		} else if (situacao == SituacaoBebinho.EM_CASA) {
			stepCasa();
		}
		buteco.repaint();
	}

	private void stepBar() throws Exception {
		buteco.log(id + " est�o bebendo");
		if (tempoCorrido >= tb) {
			buteco.log(id + "quer ir pra casa pois n�o consegue mais beber");
			n.release();
			buteco.sairButeco(this);
		}
	}

	private void stepCasa() throws Exception {
		buteco.log(id + " est�o em casa");
		if (tempoCorrido >= tc) {
			buteco.log(id + " quer uma cadeira pra poder beber");
			n.acquire();
			buteco.entrarButeco(this);
		}
	}

	public void beber() {
		tempoCorrido = 0;
		situacao = SituacaoBebinho.NO_BAR;
	}

	public void irPraCasa() {
		tempoCorrido = 0;
		situacao = SituacaoBebinho.EM_CASA;
	}

	public void expulsa() {
		expulso = true;
		this.interrupt();
	}
	
  
	public void paint(Graphics2D g2) {
		
	}
}
