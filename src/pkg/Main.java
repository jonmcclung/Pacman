package pkg;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class Main extends Canvas implements Runnable {
	
	static JFrame frame;
	static JLabel timeLabel, pointLabel, livesLabel, levelLabel;
	public static JLayeredPane pane;
	public static Game game;
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	static final int xCenter = (int)screenSize.getWidth()/2;
	static final int yCenter = (int)screenSize.getHeight()/2;
	public static int time = 0, fps = 60, staggerCount = 0, powerSwitchCount, powerSwitchCountAccelerator;
	public static Random rand = new Random();
	public boolean running = false;
	public static Color dark = Color.decode("0x596569"), light = Color.decode("0x28c5ff");
	public static Font font;
	public PopupWindow popup;
	public static JPanel gamePanel, bar;
	boolean playFlag = false;
	public static boolean wonFlag = false, imageToggler;
	public Reset reset;
	
	public void reStarter() {
		playFlag = false;
		String message = "Would you like to quit this game and start another?";
		popup = new PopupWindow(message, 375, 150, 130, new String[] {"Restart", "Cancel"}, new String[] {"restart", "continue"}, 1, this);
	}

	public void won() {
		playFlag = false;
		String message = "Nice Job! You won!\n Would you like to keep playing?";
		popup = new PopupWindow(message, 375, 320,110, new String[] {"Keep Playing", "Restart", "Exit"}, new String[] {"nextLevel", "restart", "exit"}, 0, this);
	}
	
	public void gameOver() {
		playFlag = false;
		String message = "Game Over!\nPoints: " + Game.points+"\nTime: "+time;
		popup = new PopupWindow(message, 375, 270, 110, new String[] {"Restart", "Exit"}, new String[] {"restart", "exit"}, 0, this);
	}
	
	Main() {
	frame = new JFrame("Pacman"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setResizable(false); 
	frame.setLocationRelativeTo(null);
	frame.setVisible(true); pane = new JLayeredPane();
	try {
		font = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("res/ebrimaBold.ttf"));
	     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	     ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("res/ebrimaBold.ttf")));
	} catch (IOException|FontFormatException e) {
	     //System.out.println("failure");
	}
	gamePanel = new JPanel();
	gamePanel.setLayout(new BorderLayout());
	pane.setLayer(gamePanel, new Integer(0));
	pane.add(gamePanel);
	float textSize = 18f;
	if (Game.scale < 4) {
		textSize = 8f;
	}
	else if (Game.scale < 10) {
		textSize = 14f;
	}
	levelLabel = labelMaker(textSize);
	timeLabel = labelMaker(textSize);
	pointLabel = labelMaker(textSize);
	livesLabel = labelMaker(textSize);
	game = new Game();
	frame.setLocation(Main.xCenter-240, Main.yCenter-240);
	MigLayout mig = new MigLayout("ins 0","","");
	bar = new JPanel(mig);
	bar.setBackground(dark);
	gamePanel.add(bar, BorderLayout.SOUTH);
	bar.add(pointLabel, "gapright 4, gapleft 4");
	bar.add(livesLabel, "gapright 4, gapleft 4");
	bar.add(levelLabel, "gapright 4, gapleft 4");
	bar.add(timeLabel, "push");
	reset = new Reset();
	bar.add(reset, "w 30!,h 30!, right");
	bar.setSize(new Dimension((int)game.getWidth(), 30));
	gamePanel.add(game, BorderLayout.CENTER);
	gamePanel.setBounds(0, 0, (int) game.getWidth(), (int) (game.getHeight() + bar.getHeight()));
	pane.setPreferredSize(new Dimension(gamePanel.getWidth(), gamePanel.getHeight()));
	String message = "This is Pacman!\n"
			+ "Eat the dots and escape the ghosts! "
			+ "Have fun!";
	popup = new PopupWindow(message, 375, 170, 130, new String[] {"OK"}, new String[] {"continue"}, 0, this);
	frame.add(pane);
	frame.pack();
}
	
private JLabel labelMaker(float size) {
	JLabel l = new JLabel();
	l.setBackground(dark);
	l.setForeground(light);
	l.setOpaque(true);
	l.setFont(font.deriveFont(size));
	return l;
}
public static void main(String[] args)
{
	new Main().start();
}

public synchronized void start() {
	running = true;
	new Thread(this).start();
}

public synchronized void stop() {
	running = false;
}
public void doAction(String command) {
	switch(command) {
	case "continue": {if (!playFlag) {play(); playFlag = true; break;}}
	case "restart": {if (!playFlag) newGame(); break;}
	case "exit": {System.exit(0); break;}
	case "nextLevel": {if (!playFlag) game.next(); play(); playFlag = true; break;}
	}
	game.setBindings();
}

public static void toggleImages(boolean scary) {
	if (scary) {
		for (int i = 0; i < Game.ghosts.size(); i++) {
			if (Game.ghosts.get(i).state == Entity.State.scared) {
					Game.ghosts.get(i).useScared = false;
			}
		}
	}
	else {
		for (int i = 0; i < Game.ghosts.size(); i++) {
			if (Game.ghosts.get(i).state == Entity.State.scared) {
				Game.ghosts.get(i).useScared = true;
			}
		}
	}
}

@Override
public void run() {
	long lastTime = System.nanoTime();
	double nsPerTick = 1000000000D/(double)fps;
	powerSwitchCount = 1+Game.powerDuration/16;
	powerSwitchCountAccelerator = 1000;
	////System.out.println(powerSwitchCountAccelerator);
	imageToggler = true;
	
	long lastTimer = System.currentTimeMillis();
	double delta = 0;	

	timeLabel.setText("Time: "+time);
	
	
	while (running)
	{
		long now = System.nanoTime();
		delta += (now - lastTime)/nsPerTick;
		lastTime = now;
		boolean shouldRender = false;
		
		while (delta >= 1) {
			delta -= 1;
			shouldRender = true;
		}
		if (shouldRender) {
			reset.ready = false;
			if (reset.pressed) {
				reset.pressed = false;
				Game.shouldRender = false;
				Game.ready = false;
				reStarter(); 
				}
			if (game.GameOver) {
				if (Game.lives == 0) {
					Game.shouldRender = false;
					Game.ready = false;
					gameOver();
				}
				else {
					game.die();
				}
				game.GameOver = false;
			}
			if (game.won) {
				game.won = false;
				game.next(); /*
				Game.shouldRender = false;
				Game.ready = false;
				game.won = false;
				won();*/
			}
			if (Game.shouldRender) {
			if (game.switchingPower) {
				////System.out.println("starting: "+powerSwitchCountAccelerator);
				powerSwitchCount += (1000/60)+1+(powerSwitchCountAccelerator/1500);
				powerSwitchCountAccelerator *= 1.035;
				////System.out.println("count: "+powerSwitchCount+" accelerator: "+powerSwitchCountAccelerator+" Game.powerDuration/16: "+(Game.powerDuration/16));
				if (powerSwitchCount/(Game.powerDuration/16) > 0) {
					imageToggler = !imageToggler;
					toggleImages(imageToggler);
					powerSwitchCount = 0;
				}
			}
			Game.pac.animCount++;
			Game.pac.moveCount++;
			for (int i = 0; i < Game.ghosts.size(); i++) {
				Game.ghosts.get(i).moveCount++;
				//staggers ghosts
				if (i<staggerCount) {
				Game.ghosts.get(i).animCount++;
				if (Game.ghosts.get(i).animCount>Game.ghosts.get(i).fpm) {
					Game.ghosts.get(i).move(game);
					Game.ghosts.get(i).animCount = 0;
					}
				}
			}			
			if(Game.pac.oD.isEqual(0,0) || Game.pac.animCount>Game.pac.fpm) {
				////System.out.println(Game.ghosts.get(0).fpm+" pac: "+Game.pac.fpm);
				game.move();
				Game.pac.animCount=0;
			}
			if (Game.pac.moveCount>=Game.pac.fpm/4) {
				Game.pac.index = (Game.pac.index+1)%2;
				Game.pac.moveCount=0;
			}
			for (int i = 0; i < Game.ghosts.size(); i++) {
				if (Game.ghosts.get(i).moveCount >= Game.ghosts.get(i).fpm/4)
					Game.ghosts.get(i).index = (Game.ghosts.get(i).index+1)%2;
				Game.ghosts.get(i).moveCount = 0;
			}
			game.render();
			reset.ready = true;
			}
		}
		if (System.currentTimeMillis() - lastTimer >= 1000) {
			if (Game.ready && Game.shouldRender) {
				time++;
				staggerCount++;
				timeLabel.setText("Time: "+time);
				lastTimer += 1000;
			}
			else lastTimer = System.currentTimeMillis();
		}
	}
	}

public void newGame() {
	game.newGame();
	play(); playFlag = true;
}

public void play() {
	game.setSize(new Dimension(Game.boxWidth*Game.scale, Game.boxWidth*Game.scale));
	gamePanel.setBounds(0, 0, (int) game.getWidth(), (int) (game.getHeight() + bar.getHeight()));
	pane.setPreferredSize(new Dimension(gamePanel.getWidth(), gamePanel.getHeight()));
	popup.removeAll();
	frame.remove(popup);
	pane.removeAll();
	pane.setLayer(gamePanel, new Integer(0));
	pane.add(gamePanel);
	reset.ready = true;
	Game.ready = true;
	Game.shouldRender = true;
	frame.revalidate();
	frame.pack();
	frame.repaint();
	game.requestFocusInWindow();
}
}
