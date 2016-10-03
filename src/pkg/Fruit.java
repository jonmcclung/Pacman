package pkg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

public class Fruit {
	int pointVal = 100, x, y;
	BufferedImage img = null;

	Timer timer;
	
	Fruit(int x, int y, int pointVal, BufferedImage img) {
		this.x = x;
		this.y = y;
		this.pointVal = pointVal;
		this.img = img;
		
		 timer = new Timer(10000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				}
			});
		timer.setRepeats(false);
		timer.start();
	}
	
	public int getPointVal() {
		return pointVal;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public BufferedImage getImg() {
		return img;
	}
}
