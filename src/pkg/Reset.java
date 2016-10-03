package pkg;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Reset extends JPanel implements MouseListener{
	public boolean ready = false, pressed = false;
	BufferedImage image = null;
	
	Reset() {
		addMouseListener(this);
		try {
			image = ImageIO.read(this.getClass().getResourceAsStream("res/restart.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, null);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (ready) pressed = true;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (ready) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (ready) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}

