package pkg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Game extends JPanel {

	static BufferedImage spriteSheet = null, bg = null, dot = null, powerDot = null, 
			boxSpriteSheet = null, nothing = null, transparencyFilter = null, ghostEyes = null;
	Box[][] map = new Box[scale][scale];
	Dot[][] dots = new Dot[(2*scale)][(2*scale)];
	Color[] ghostColors = {
			Color.decode("0xff93f1"), Color.decode("0x5dfff1"), Color.decode("0xffbf81"), Color.decode("0xff6367"), 
			Color.decode("0xfffd6b"), Color.decode("0x4fffaa"), Color.decode("0xcccccc"), Color.decode("0xcf67ff"),
			Color.decode("0x3557ff")};
	BufferedImage[] boxes = new BufferedImage[4];
	BufferedImage[] gBoxes = new BufferedImage[4];
	BufferedImage[] fruitImages = new BufferedImage[5];
	static ArrayList<Entity> ghosts = new ArrayList<Entity>(4);
	static ArrayList<Fruit> fruits = new ArrayList<Fruit>(1);
	public static int points = 0, scale = 10, spriteWidth = 36, boxWidth = 40,
			lives = 3, level = 1, ghostNum = 3, AI = 1, powerDuration, deadCount = 0, lifeThreshold = 0;
	public static Entity pac;
	public ArrayList<PointString> pointStrings = new ArrayList<PointString>();
	private Timer endPowerTimer, startEndPowerTimerAnim;
	public Path path = new Path();
	
	public boolean won = false, GameOver = false, switchingPower = false;
	public static boolean ready = false, shouldRender = false, moveFlag = false, waiting = false;

	public static Movement up = new Movement(0, -1), down = new Movement(0, 1), left = new Movement(-1, 0), right = new Movement(1, 0), 
			ghostSpawn = new Movement(0, 0), pacSpawn = new Movement(0, 0);
	public static Movement[] sides = {up, down, left, right};
	
	public static List<List<Movement>> paths = new ArrayList<List<Movement>>();
	
	public void print(List<Movement> list) {
		String output = "";
		for (Movement m : list) {
			if (m.isEqual(up)) output += "up, ";
			else if (m.isEqual(down)) output += "down, ";
			else if (m.isEqual(left)) output += "left, ";
			else if (m.isEqual(right)) output += "right, ";
		}
		////System.out.println(output);
	}
	
	Game() {
		powerDuration = 42000/AI;
		setSize(new Dimension(boxWidth*scale, boxWidth*scale));
		setBackground(Main.dark);
		for (int j = 0; j < Math.pow(sides.length, AI); j++) {
			paths.add(new ArrayList<Movement>(AI));
			for (int i = AI; i > 0; i--) {
				paths.get(j).add(sides[((j/(int)(Math.pow(sides.length, AI - i))))%sides.length]);
			}
		}
		addPoints(0);
		try {
			spriteSheet = ImageIO.read(this.getClass().getResourceAsStream("res/spriteSheet.png"));
		} catch (IOException e) {
			////System.out.println("failure to load spriteSheet");
			e.printStackTrace();
		}

		transparencyFilter = spriteSheet.getSubimage(2*spriteWidth, 4*spriteWidth, spriteWidth, spriteWidth);
		
		int spY = 4;
		for (int spX = 0; spX < 2; spX++) {
				for (int x = 0; x < spriteWidth; x++) {
					for (int y = 0; y < spriteWidth; y++) {
						int color = spriteSheet.getRGB(spX*spriteWidth+x, spY*spriteWidth+y);
						int alpha = (transparencyFilter.getRGB(x, y) >>> 24)*(color >>> 24)/256;
						color = (color & 0x00FFFFFF) | alpha << 24;
						spriteSheet.setRGB(spX*spriteWidth+x, spY*spriteWidth+y, color);
					}
				}
			}
		
		try {
			bg = ImageIO.read(this.getClass().getResourceAsStream("res/bg.png"));
		} catch (IOException e) {
			////System.out.println("failure to load bg");
			e.printStackTrace();
		}
		try {
			boxSpriteSheet = ImageIO.read(this.getClass().getResourceAsStream("res/boxes.png"));
		} catch (IOException e) {
			////System.out.println("failure to load maze");
			e.printStackTrace();
		}
		//necessary to make it really simple to find the right image later
		int biggerBox = boxWidth+4;
		
		for (int i = 0; i < boxes.length; i++) {
			boxes[i] = boxSpriteSheet.getSubimage(i*biggerBox, 0, biggerBox, biggerBox);
		}		
		for (int i = 0; i < gBoxes.length; i++) {
			gBoxes[i] = boxSpriteSheet.getSubimage(i*biggerBox, biggerBox, biggerBox, biggerBox);
		}

		ghostEyes = spriteSheet.getSubimage(0, 3*spriteWidth, spriteWidth, spriteWidth);
		nothing = boxSpriteSheet.getSubimage(0, biggerBox*2, biggerBox, biggerBox);
		
		pac = new Entity(pacSpawn);
		
		for (int i = 0; i<2; i++) {
			pac.anim[i] = spriteSheet.getSubimage(i*spriteWidth, 0, spriteWidth, spriteWidth);
		}
		
		dot = spriteSheet.getSubimage(0*spriteWidth, 1*spriteWidth, spriteWidth, spriteWidth);
		powerDot = spriteSheet.getSubimage(0, 2*spriteWidth, spriteWidth, spriteWidth);
		
		for (int i = 0; i < fruitImages.length; i++) {
			fruitImages[i] = spriteSheet.getSubimage(3*spriteWidth, i*spriteWidth, spriteWidth, spriteWidth);
		}
		
		for (int i = 0; i < ghostNum; i++) {
			ghosts.add(new Entity(ghostSpawn));
			for (int spX = 0; spX<2; spX++) {
				BufferedImage ghostImg  = spriteSheet.getSubimage((spX)*spriteWidth, 4*spriteWidth, spriteWidth, spriteWidth);
				for (int x = 0; x < spriteWidth; x++) {
					for (int y = 0; y < spriteWidth; y++) {
						ghostImg.setRGB(x, y, (ghostImg.getRGB(x, y) & 0xFF000000) | (ghostColors[i].getRGB() & 0x00FFFFFF));
					}
				}
				BufferedImage combined = new BufferedImage(spriteWidth, spriteWidth, BufferedImage.TYPE_INT_ARGB);
				Graphics g = combined.getGraphics();
				g.drawImage(ghostImg, 0, 0, null);
				g.drawImage(ghostEyes, 0, 0, null);
				ghosts.get(i).anim[spX] = combined;
				
				
				ghostImg  = spriteSheet.getSubimage((spX)*spriteWidth, 4*spriteWidth, spriteWidth, spriteWidth);
				for (int x = 0; x < spriteWidth; x++) {
					for (int y = 0; y < spriteWidth; y++) {
						ghostImg.setRGB(x, y, (ghostImg.getRGB(x, y) & 0xFF000000) | (ghostColors[ghostColors.length-1].getRGB() & 0x00FFFFFF));
					}
				}
				combined = new BufferedImage(spriteWidth, spriteWidth, BufferedImage.TYPE_INT_ARGB);
				g = combined.getGraphics();
				g.drawImage(ghostImg, 0, 0, null);
				g.drawImage(ghostEyes, 0, 0, null);
				ghosts.get(i).scaredAnim[spX] = combined;
			}
		}
		
		endPowerTimer = new Timer((powerDuration/4), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchingPower = false;
				Main.powerSwitchCount = 1+Game.powerDuration/16;
				Main.powerSwitchCountAccelerator = 1000;
				Main.imageToggler = !Main.imageToggler;
				Main.toggleImages(Main.imageToggler);
				power(false);
			}
		});
		endPowerTimer.setRepeats(false);	
		
		startEndPowerTimerAnim = new Timer((powerDuration*3/4), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				endPowerTimer.setInitialDelay((powerDuration/4));
				if (!endPowerTimer.isRunning()){
					endPowerTimer.start();
				}
				else {
					endPowerTimer.restart();
				}
				switchingPower = true;				
			}
		});
		startEndPowerTimerAnim.setRepeats(false);
		newGame();
	}
	
	class KeyAction extends AbstractAction {
		String key;
		KeyAction(String key) {this.key = key;}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (ready && !moveFlag) {
				switch(key) {
				case "SPACE": shouldRender = !shouldRender; break;
				case "UP": pac.d.set(up); break;
				case "DOWN": pac.d.set(down); break;
				case "LEFT": pac.d.set(left); break;
				case "RIGHT": pac.d.set(right); break;
			}
				
			if (key != "SPACE")
				moveFlag = true;
			}
		}	
	}
	
	public void setBindings() {
		String [] keys= {"UP","DOWN","LEFT","RIGHT", "SPACE"};
		for (int i=0;i<keys.length;i++) {
			String s = keys[i];
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(s), s);
			getActionMap().put(s, new KeyAction(s));
		}
	}
	
	public void setLevel() {
		deadCount = 0;
		power(false);
		pac.setPosition(pacSpawn);
		pac.d = new Movement(0, 0);
		pac.oD = new Movement(0, 0);
		pac.index = 0;
		
		for (int i = 0; i < fruits.size(); i++) {
			fruits.get(i).timer.stop();
		}
		
		while (ghostNum < ghosts.size()) {
			ghosts.remove(ghosts.size()-1);
		}
		while (ghostNum > ghosts.size()) {
			ghosts.add(new Entity(ghostSpawn));
			for (int spX = 0; spX<2; spX++) {
				BufferedImage ghostImg  = spriteSheet.getSubimage((spX)*spriteWidth, 4*spriteWidth, spriteWidth, spriteWidth);
				for (int x = 0; x < spriteWidth; x++) {
					for (int y = 0; y < spriteWidth; y++) {
						ghostImg.setRGB(x, y, (ghostImg.getRGB(x, y) & 0xFF000000) | (ghostColors[ghosts.size()-1].getRGB() & 0x00FFFFFF));
					}
				}
				BufferedImage combined = new BufferedImage(spriteWidth, spriteWidth, BufferedImage.TYPE_INT_ARGB);
				Graphics g = combined.getGraphics();
				g.drawImage(ghostImg, 0, 0, null);
				g.drawImage(ghostEyes, 0, 0, null);
				ghosts.get(ghosts.size()-1).anim[spX] = combined;
				
				ghostImg  = spriteSheet.getSubimage((spX)*spriteWidth, 4*spriteWidth, spriteWidth, spriteWidth);
				for (int x = 0; x < spriteWidth; x++) {
					for (int y = 0; y < spriteWidth; y++) {
						ghostImg.setRGB(x, y, (ghostImg.getRGB(x, y) & 0xFF000000) | (ghostColors[ghostColors.length-1].getRGB() & 0x00FFFFFF));
					}
				}
				combined = new BufferedImage(spriteWidth, spriteWidth, BufferedImage.TYPE_INT_ARGB);
				g = combined.getGraphics();
				g.drawImage(ghostImg, 0, 0, null);
				g.drawImage(ghostEyes, 0, 0, null);
				ghosts.get(ghosts.size()-1).scaredAnim[spX] = combined;
			}
		}
		for (int i = 0; i < ghostNum; i++) {
			ghosts.get(i).setPosition(ghostSpawn);
			ghosts.get(i).isGhost = true;
			ghosts.get(i).dead(false);
			ghosts.get(i).d = new Movement(0, 0);
			ghosts.get(i).oD = new Movement(0, 0);
			ghosts.get(i).pathIndex = -1;
		}
		powerDuration = 42000/AI;
		Main.powerSwitchCount = 1+powerDuration/16;
		Main.powerSwitchCountAccelerator = 1000;
	}
	
	public void next() {
		pac.fpm --;
		level++;
		Main.levelLabel.setText("Level: "+level);
		if (level%2 == 0) ghostNum++;
		if (level%2 == 1) AI++;
		setMap();
		setLevel();
		repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Main.staggerCount = 0;
	}
	
	public void die() {
		deadCount = 0;
		pac.index = 0;
		repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setLevel();
		Main.staggerCount = 0;
		//System.out.println("lowering lives");
		lives--;
		Main.livesLabel.setText("Lives: "+lives);
		waiting = false;
	}
	
	public void newGame() {
		setSize(new Dimension(boxWidth*scale, boxWidth*scale));
		Main.time = 0;
		Main.timeLabel.setText("Time: 0");
		AI = 3;
		ghostNum = 3;
		points = 0;
		lives = 3;
		level = 1;
		pac.fpm = 20;
		Main.pointLabel.setText("Points: "+points);
		Main.livesLabel.setText("Lives: "+lives);
		Main.levelLabel.setText("Level: "+level);	
		setMap();
		setLevel();
		repaint();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Main.staggerCount = 0;
		waiting = false;
	}
	
	public void print(Movement m) {
		String output = "";
			if (m.isEqual(Game.up)) output = "up";
			else if (m.isEqual(Game.down)) output = "down";
			else if (m.isEqual(Game.left)) output = "left";
			else if (m.isEqual(Game.right)) output = "right";
			else if (m.isEqual(0, 0)) output = "stopped";
		//System.out.println(output);
	}
	
	public void setMap() {

		for (int x = 0; x < dots[0].length; x++) {
			for (int y = 0; y < dots[1].length; y++) {
			dots[x][y] = new Dot(true);
			if (x%2 == 0 && y%2 == 0)
			dots[x][y].set(false);
			}	
		}
		
		// necessary so that we can set the sides of boxes to the right and down and not get nullPointer
		int r, r1, r2;
		for (int y = 0; y < scale; y++) {
			for (int x = 0; x < scale; x++) {
				map[x][y] = new Box(x, y);
				
				r = Main.rand.nextInt(8);
				if (r/4 == 0)
					map[x][y].solidWanted = 0;	
				else if (r < 5)
					map[x][y].solidWanted = 1;	
				else
					map[x][y].solidWanted = 2;
			
				for (int i = 0; i < sides.length; i++) {
					sides[i].solid = true;
					sides[i].ghostSolid = true;
					//if this is a perimeter to the whole maze, it must be solid. we will add random holes later
					if (map[x][y].isPerimeter(sides[i])) {
						//print(sides[i]);
						////System.out.println("is on the perimeter");
						map[x][y].setSolid(sides[i], false, this);
					}
				}					
			}
		}
		
		//creating ghost box
		
		r = Main.rand.nextInt(scale/2)+scale/4;
		r1 = Main.rand.nextInt(scale/2)+scale/4;
		
		ghostSpawn.set(r, r1);
		
		for (int i = 0; i < sides.length; i++) {
			sides[i].solid = true;
			sides[i].ghostSolid = true;
		}
		
		r2 = Main.rand.nextInt(sides.length);
				
		sides[r2].ghostSolid = false;
		////System.out.println("ghostSpawn at "+r+", "+r1);
		map[ghostSpawn.x][ghostSpawn.y].setSolid(sides, this);
		
		// done setting ghostSpawn
		
		for (int y = 0; y < scale; y++) {
			for (int x = 0; x < scale; x++) {
				
				//System.out.println("\n\nsetting solidity of box at "+x+", "+y);
				if (!(x == ghostSpawn.x && y == ghostSpawn.y)) {
					
				//we want to break up "empty blocks" that may have been formed by other boxes
					
				if (
						x > 0 && 
						y > 0 && 
						!map[x][y].solid(up, this) && 
						!map[x][y].solid(left, this) && 
						!map[x-1][y-1].solid(right, this) && 
						!map[x-1][y-1].solid(down, this)
					) {
					//now to determine where we can place a solid to break up the block
					
					////System.out.println("empty box!");
					
					for (int i = 0; i < sides.length; i++) {
						sides[i].solid = false;
						sides[i].ghostSolid = false;
					}
					
					if ((!map[x][y].solid(right, this) || !map[x][y].solid(down, this)) && (!map[x][y-1].solid(right, this) || !map[x][y-1].solid(up, this))) {
						//the right side has two openings, so we could place a solid over there. We are only setting these sides as solid so that we know what the block looks like
						sides[3].solid = true;
						sides[3].ghostSolid = true;
						////System.out.println("right is possible");
					}		
					if ((!map[x][y-1].solid(right, this) || !map[x][y-1].solid(up, this)) && (!map[x-1][y-1].solid(left, this) || !map[x-1][y-1].solid(up, this))) {
						//up
						sides[0].solid = true;
						sides[0].ghostSolid = true;
						////System.out.println("up is possible");
					}
					if ((!map[x-1][y].solid(left, this) || !map[x-1][y].solid(down, this)) && (!map[x-1][y-1].solid(left, this) || !map[x-1][y-1].solid(up, this))) {
						//left
						sides[2].solid = true;
						sides[2].ghostSolid = true;
						////System.out.println("left is possible");
					}
					if ((!map[x][y].solid(right, this) || !map[x][y].solid(down, this)) && (!map[x-1][y].solid(left, this) || !map[x-1][y].solid(down, this))) {
						//down
						sides[1].solid = true;
						sides[1].ghostSolid = true;
						////System.out.println("down is possible");
					}
					
					r = Main.rand.nextInt(sides.length);
					boolean setASide = false;
					while (!setASide && r < sides.length*2) {
						if (sides[r%sides.length].solid) {
							////System.out.println("We choose");
							//print(sides[r%sides.length]);
							////System.out.println("which means setting");
							if (sides[r%sides.length].isPositive()) {
								//changes down to left and right to up
								//print(sides[(r+1)%sides.length]);
								map[x][y].setSolid(new Movement(sides[(r+1)%sides.length], true, true), true, this);
							}
							else {
								//changes left to down and up to right
								//print(sides[(r+sides.length-1)%sides.length]);
								map[x-1][y-1].setSolid(new Movement(sides[(r+sides.length-1)%sides.length], true, true), true, this);
							}
							setASide = true;
						}
						r++;
						////System.out.println("r is! "+r);
					}
					// if there weren't enough openings, we have to go through and find somewhere to open up. We have to make sure we don't break the perimeter, 
					// break the ghostSpawn box, or create another empty block(?)
					// we'll go ahead and do two openings on the same side because testing every possible is not worth the effort
					if (!setASide) {
						//System.out.println("there were no options...");

						r = Main.rand.nextInt(sides.length);
						//xA and yA refer to the coordinates of the box I'm checking
						int xA = x+negative(sides[r].x);
						int yA = y+negative(sides[r].y);
						
						while (
								map[xA][yA].isPerimeter(sides[r])
								|| ((ghostSpawn.x == xA+sides[r].x) && (ghostSpawn.y == yA+sides[r].y)) 
								|| ((ghostSpawn.x == xA+(sides[r].x)-Math.abs(sides[r].y)) && (ghostSpawn.y == yA-Math.abs(sides[r].x)+(sides[r].y))) 
								) {
							
								r = (r+1)%sides.length;

								xA = x+negative(sides[r].x);
								yA = y+negative(sides[r].y);
							}
						//System.out.println("but we can bust open");
						print(sides[r]);
						//System.out.println("by changing the solidity of "+xA+", "+yA+" and "+(xA-Math.abs(sides[r].y))+", "+(yA-Math.abs(sides[r].x)));
						sides[r].solid = false;
						sides[r].ghostSolid = false;
						map[xA][yA].setSolid(sides[r], true, this);
						map[xA-Math.abs(sides[r].y)][yA-Math.abs(sides[r].x)].setSolid(sides[r], true, this);
						//System.out.println("now we need to make a wall at ");
						if (sides[r].isPositive()) {
							//changes down to left and right to up
							print(sides[(r+1)%sides.length]);
							sides[(r+1)%sides.length].solid = true;
							sides[(r+1)%sides.length].ghostSolid = true;
							map[x][y].setSolid(sides[(r+1)%sides.length], true, this);
						}
						else {
							//changes left to down and up to right
							print(sides[(r+sides.length-1)%sides.length]);
							sides[(r+sides.length-1)%sides.length].solid = true;
							sides[(r+sides.length-1)%sides.length].ghostSolid = true;
							map[x-1][y-1].setSolid(sides[(r+sides.length-1)%sides.length], true, this);
						}
					}
				}
				
				//done breaking up block. *phew*! now we can randomly make walls at right and/or down if they are available		
				
				//System.out.println("solidWanted: "+map[x][y].solidWanted+" solidCount: "+map[x][y].getSolidCount());
				while (map[x][y].getSolidCount() < map[x][y].solidWanted) {
					
					r = Main.rand.nextInt(2);
					//System.out.println("r is "+r);
					
					Movement[] rightDown = {right, down};
					
					while (
						map[x][y].solid(rightDown[r%2], this) ||
						map[x][y].isPerimeter(rightDown[r%2]) ||
						(2 == map[x+rightDown[r%2].x][y+rightDown[r%2].y].getSolidCount() && r < 3)
						) {
						//System.out.println("solidity at "+r%2+": "+(map[x][y].solid(rightDown[r%2], this)));
						r++;
						//System.out.println("within while loop r has become "+r);
					}
					
					if (r < 3) {
						rightDown[r%2].solid = true;
						rightDown[r%2].ghostSolid = true;
						//System.out.println("adding solidity at");
						print(rightDown[r%2]);
						map[x][y].setSolid(rightDown[r%2], true, this);
					}
					else {
						break;
					}
				}
				}
			}
		}
		
		//now we can add the loops/portals
		
		for (int i = 0; i < 3; i++) {
			
			do {
				r = Main.rand.nextInt(2);
				r1 = Main.rand.nextInt(scale);
			}
			
			while (!map[r1*sides[r*2].y*-1][r1*sides[r*2].x*-1].solid(sides[r*2], this));
			
			map[r1*sides[r*2].y*-1][r1*sides[r*2].x*-1].setSolid(new Movement(sides[r*2], false, false), false, this);
			map[scale-1-((scale-1-r1)*(sides[r*2].y*-1))][scale-1-((scale-1-r1)*(sides[r*2].x*-1))].setSolid(new Movement(sides[r*2].opposite(), false, false), false, this);
			
		}

		//now to ensure continuity
		
		path.ensureContinuity(this);

		//set pacSpawn
		
		pacSpawn.set(ghostSpawn.x, ghostSpawn.y);
		
		ArrayList<Movement> route = path.findEscape(new Box(ghostSpawn.x, ghostSpawn.y), new Box(ghostSpawn.x, ghostSpawn.y), 6, this);
		for (int i = 0; i < route.size(); i++) {
			pacSpawn.add(route.get(i));
		}
		
		// it is necessary to wait to set the image because not all the sides have set solidity until everything is decided
		for (int y = 0; y < scale; y++) {
			for (int x = 0; x < scale; x++) {
				for (int i = 0; i < sides.length; i++) {
				}
				map[x][y].setImage(this);
					
				dots[x*2][y*2].set((x == ghostSpawn.x && y == ghostSpawn.y) || (x == pacSpawn.x && y == pacSpawn.y));
				//set the dots to the right and down. the ones up and left are set by the preceding x and y values. sides[1] is down and sides[3] is right
				dots[(x*2)+1][(y*2)].set(map[x][y].solid(right, this));
				dots[(x*2)][(y*2)+1].set(map[x][y].solid(down, this));	
			}	
		}
		
		//set the four power dots randomly
		for (int i = 0; i < 4; i++) {
			do {
				r = Main.rand.nextInt(dots[0].length);
				r1 = Main.rand.nextInt(dots[1].length);
			}
			while (dots[r][r1].eaten == true || dots[r][r1].power == true); 	
			dots[r][r1].power = true;
		}	
	}
	
	public int positive(int x) {
		if (x > 0) return x;
		return 0;
	}
	
	public int negative(int x) {
		if (x < 0) return x;
		return 0;
	}
	
	public void power(boolean setPower) {
		if (setPower) {
			boolean noneScared = true;
			for (int i = 0; i < ghosts.size(); i++) {
				if (ghosts.get(i).state == Entity.State.scared) {
					noneScared = false;
				}
				ghosts.get(i).scared(true);
			}
			if (noneScared) deadCount = 0;
			startEndPowerTimerAnim.setInitialDelay(powerDuration*3/4);
			if (endPowerTimer.isRunning()) {
				endPowerTimer.stop();
				Main.powerSwitchCount = 1+Game.powerDuration/16;
				Main.powerSwitchCountAccelerator = 1000;
				switchingPower = false;
			}
			if (!startEndPowerTimerAnim.isRunning()){
				startEndPowerTimerAnim.start();
			}
			else {
				startEndPowerTimerAnim.restart();
			}
		}
		if (!setPower) {
			deadCount = 0;
			for (int i = 0; i < ghosts.size(); i++) {
				ghosts.get(i).scared(false);
			}
		}
	}
	
	public void addPoints(int x) {
		points += x;
		if (points/400 > lifeThreshold) {
			lives++;
			Main.livesLabel.setText("Lives: "+lives);
			lifeThreshold++;
			pointStrings.add(new PointString("1UP", pac.X()+boxWidth/2, pac.Y()+boxWidth/2));
			Timer timer = new Timer(4000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pointStrings.remove(0);
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
		Main.pointLabel.setText("Points: "+points);
	}
	
	
	public void move() {
		
		//here is a good place to randomly decide whether or not to add a fruit.
		if (!pac.oD.isEqual(0, 0)) {
			int r = Main.rand.nextInt(200);
			if (r == 0) {
				//create fruit.
				int r1;
				
				do {
					r = Main.rand.nextInt(scale);
					r1 = Main.rand.nextInt(scale);
				}
				
				while (
					(ghostSpawn.x == r && ghostSpawn.y == r1) ||
					(Math.abs(pac.x-r) + Math.abs(pac.y-r1) < 2)
					);
				
				
				//////
				int r2 = Main.rand.nextInt(fruitImages.length);	
				if (fruitImages.length == 5) {		
					r2 = Main.rand.nextInt(20);
					if (r2 < 6) {
						r2 = 0; 
					}
					else if (r2 < 11) {
						r2 = 1;
					}
					else if (r2 < 15) {
						r2 = 2;
					}
					else if (r2 < 18) {
						r2 = 3;
					}
					else if (r2 < 20) {
						r2 = 4;
					}
					else if (r2 < 21) {
						r2 = 5;
					}
				}
				
				fruits.add(new Fruit(r, r1, (int)(10*Math.pow(2, r2)),fruitImages[r2]));
			}
		}

		if (map[pac.x][pac.y].solid(pac.oD, this)) {
			pac.oD.x = 0;
			pac.oD.y = 0;
		}
		pac.add(pac.oD);	
		
		for (int i = 0; i < fruits.size(); i++) {
			if (pac.x == fruits.get(i).x && pac.y == fruits.get(i).y) {
				addPoints(fruits.get(i).getPointVal());
				
				pointStrings.add(new PointString(fruits.get(i).getPointVal(), fruits.get(i).getX()*boxWidth+boxWidth/4, fruits.get(i).getY()*boxWidth+boxWidth/4));
				Timer timer = new Timer(4000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						pointStrings.remove(0);
					}
				});
				timer.setRepeats(false);
				timer.start();
				
				fruits.get(i).timer.stop();
			}
		}
		
		if (!dots[pac.x*2][pac.y*2].eaten) {
			dots[pac.x*2][pac.y*2].set(true);
			addPoints(1);
			if (dots[pac.x*2][pac.y*2].power) {
				////System.out.println("dot inside square. setting power...");
				power(true);
			}
		}
		if (!pac.oD.isEqual(pac.d)) {
			int row = 0;
			switch(pac.d.x*10+pac.d.y) {
			case 1: {
				row = 1;
				break;
			}
			case -1: {
				row = 3;
				break;
			}
			case 10: {
				row = 0;
				break;
			}
			case -10: {
				row = 2;
				break;
			}
			}
			for (int i = 1; i<2; i++) {
				pac.anim[i] = spriteSheet.getSubimage((i)*spriteWidth, row*spriteWidth, spriteWidth, spriteWidth);
			}
		}
		pac.oD.set(pac.d);
		if (map[pac.x][pac.y].solid(pac.oD, this)) {
			pac.oD.x = 0;
			pac.oD.y = 0;
		}
		moveFlag = false;
	}
	
	public void render() {
		
		
		
		int dotX = pac.x*2+pac.oD.x;
		int dotY = pac.y*2+pac.oD.y;
		if (dotX < 0) dotX = dots[0].length-1;
		if (dotY < 0) dotY = dots[1].length-1;
		if (pac.animCount >= pac.fpm/2 &&
				!dots[dotX][dotY].eaten) {
			dots[dotX][dotY].set(true);
			if (dots[dotX][dotY].power) {
				////System.out.println("dot in between squares. setting power...");
				power(true);
			}
			addPoints(1);
		}
		
		if (pac.oD.isEqual(0, 0)) pac.index = 0;
		repaint();
	}
	
	class PointString {
		int x, y;
		String points;
		
		PointString(double points, int x, int y) {
			this.points = Integer.toString((int)points);
			this.x = x;
			this.y = y;
		}

		public PointString(String string, int x, int y) {
			points = string;
			this.x = x;
			this.y = y;
		}		
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(bg, 0, 0, null);

		for (int x = 0; x < map[0].length; x++) {
			for (int y = 0; y < map[1].length; y++) {
				map[x][y].image.paintIcon(null, g, map[x][y].x*boxWidth-2, map[x][y].y*boxWidth-2);
			}
		}
		
		for (int i = 0; i < ghosts.size(); i++) {
			if (ghosts.get(i).collide(pac) && ghosts.get(i).state == Entity.State.scary && !waiting) {
				GameOver = true;
				waiting = true;
			}
			if (ghosts.get(i).collide(pac) && ghosts.get(i).state == Entity.State.scared) {
				ghosts.get(i).dead(true);
				deadCount++;
				addPoints((int) (Math.pow(2, deadCount)*10));
				pointStrings.add(new PointString(Math.pow(2, deadCount)*10, pac.X()+Game.boxWidth/4, pac.Y()+Game.boxWidth/4));
				Timer timer = new Timer(4000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						pointStrings.remove(0);
					}
				});
				timer.setRepeats(false);
				timer.start();
			}
		}
		for (int i = 0; i < pointStrings.size(); i++) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setColor(Color.white);
			g2.setFont(Main.font.deriveFont(12f));
			g2.drawString(pointStrings.get(i).points, pointStrings.get(i).x, pointStrings.get(i).y);
		}

		boolean allEaten = true;
		for (int x = 0; x < dots[0].length; x++) {
			for (int y = 0; y < dots[1].length; y++) {
				if (!dots[x][y].eaten) {
					if (dots[x][y].power) {
						g.drawImage(powerDot, (x)*boxWidth/2, (y)*boxWidth/2, null);
					}
					else {
						g.drawImage(dot, (x)*boxWidth/2, (y)*boxWidth/2, null);
					}
					allEaten = false;
				}
			}
		}
		if (allEaten) won = true;
		
		for (int i = 0; i < fruits.size(); i++) {
			if (fruits.get(i).timer.isRunning()) {
				g.drawImage(fruits.get(i).getImg(), fruits.get(i).getX()*boxWidth, fruits.get(i).getY()*boxWidth, null);				
			}
			else {
				fruits.remove(fruits.get(i));
				i--;
			}
		}
		
		for (int i = 0; i < ghosts.size(); i++) {
			
			if (ghosts.get(i).state == Entity.State.dead) {
				g.drawImage(ghostEyes, ghosts.get(i).X(), ghosts.get(i).Y(), null);
			}
			else if (ghosts.get(i).useScared) {
				g.drawImage(ghosts.get(i).scaredAnim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y(), null);
				
				if (ghosts.get(i).X() < 0)
					g.drawImage(ghosts.get(i).scaredAnim[ghosts.get(i).index], ghosts.get(i).X()+(scale*boxWidth), ghosts.get(i).Y(), null);
				if (ghosts.get(i).Y() < 0)
					g.drawImage(ghosts.get(i).scaredAnim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y()+(scale*boxWidth), null);
					
				if (!(ghosts.get(i).X() < boxWidth*(scale-1)))
					g.drawImage(ghosts.get(i).scaredAnim[ghosts.get(i).index], ghosts.get(i).X()-(scale*boxWidth), ghosts.get(i).Y(), null);
				if (!(ghosts.get(i).Y() < boxWidth*(scale-1)))
					g.drawImage(ghosts.get(i).scaredAnim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y()-(scale*boxWidth), null);
			}
			else {
				g.drawImage(ghosts.get(i).anim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y(), null);
				
				if (ghosts.get(i).X() < 0)
					g.drawImage(ghosts.get(i).anim[ghosts.get(i).index], ghosts.get(i).X()+(scale*boxWidth), ghosts.get(i).Y(), null);
				if (ghosts.get(i).Y() < 0)
					g.drawImage(ghosts.get(i).anim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y()+(scale*boxWidth), null);
					
				if (!(ghosts.get(i).X() < boxWidth*(scale-1)))
					g.drawImage(ghosts.get(i).anim[ghosts.get(i).index], ghosts.get(i).X()-(scale*boxWidth), ghosts.get(i).Y(), null);
				if (!(ghosts.get(i).Y() < boxWidth*(scale-1)))
					g.drawImage(ghosts.get(i).anim[ghosts.get(i).index], ghosts.get(i).X(), ghosts.get(i).Y()-(scale*boxWidth), null);
			}
		}
		g.drawImage(pac.anim[pac.index], pac.X(), pac.Y(), null);
		
		if (pac.X() < 0)
		g.drawImage(pac.anim[pac.index], pac.X()+(scale*boxWidth), pac.Y(), null);
		if (pac.Y() < 0)
		g.drawImage(pac.anim[pac.index], pac.X(), pac.Y()+(scale*boxWidth), null);
		
		if (!(pac.X() < boxWidth*(scale-1)))
		g.drawImage(pac.anim[pac.index], pac.X()-(scale*boxWidth), pac.Y(), null);
		if (!(pac.Y() < boxWidth*(scale-1)))
		g.drawImage(pac.anim[pac.index], pac.X(), pac.Y()-(scale*boxWidth), null);
	}
}
