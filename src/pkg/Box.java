package pkg;

import javax.swing.ImageIcon;

public class Box {
	public Movement up = new Movement(0, -1), down = new Movement(0, 1), left = new Movement(-1, 0), right = new Movement(1, 0), path;
	public Movement[] sides = {up, down, left, right};
	public CompoundIcon image;
	public Box parent;
	int F, G, H, x, y, solidWanted = 2;
	ImageIcon[] sideImages = new ImageIcon[4];
	
	public void add(Movement m1) {
		x += m1.x;
		y += m1.y;
		if (x < 0) x += Game.scale;
		if (y < 0) y += Game.scale;
		if (!(x < Game.scale)) x -= Game.scale;
		if (!(y < Game.scale)) y -= Game.scale;
	}
	
	public void subtract(Movement m1) {
		x -= m1.x;
		y -= m1.y;
		if (x < 0) x += Game.scale;
		if (y < 0) y += Game.scale;
		if (!(x < Game.scale)) x -= Game.scale;
		if (!(y < Game.scale)) y -= Game.scale;
	}
	
	Box(Box b) {
		parent = b.parent;
		F = b.F;
		G = b.G;
		H = b.H;
		x = b.x;
		y = b.y;
		for (int i = 0; i < sides.length; i++) {
			sides[i].solid = b.sides[i].solid;
			sides[i].ghostSolid = b.sides[i].ghostSolid;
		}
	}
	
	public void set(Box b) {
		parent = b.parent;
		F = b.F;
		G = b.G;
		H = b.H;
		x = b.x;
		y = b.y;
		for (int i = 0; i < sides.length; i++) {
			sides[i].solid = b.sides[i].solid;
			sides[i].ghostSolid = b.sides[i].ghostSolid;
		}
	}
	
	Box(int x, int y) {
		this.x = x;
		this.y = y;
		for (int i = 0; i < sides.length; i++) {
			sides[i].solid = false;
			sides[i].ghostSolid = false;
		}
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
	
	public void setSolid(Movement m, boolean setOtherSide, Game game) {
		////System.out.println("setting up a solid");
		//print(m);
		for (int i = 0; i < Game.sides.length; i++) {
			if (sides[i].isEqual(m)) {
				sides[i].solid = m.solid; 
				sides[i].ghostSolid = m.ghostSolid;
				if (!isPerimeter(m) && setOtherSide) {
					game.map[x+m.x][y+m.y].setSolid(m.opposite(), false, game);
				}
			}
		}

	}
	
	public int getSolidCount() {
		int sC = 0;
		for (int i = 0; i < sides.length; i++) {
			if (sides[i].solid)
				sC++;
		}
		return sC;
	}
	
	public boolean isPerimeter(Movement m) {
		if (
			(m.isEqual(up) && y == 0) ||
			(m.isEqual(down) && y == Game.scale-1) ||
			(m.isEqual(left) && x == 0) ||
			(m.isEqual(right) && x == Game.scale-1) 
			) {
			return true;
		}
		return false;
	}
	
	public void setSolid(Movement[] m, Game game) {
		
		for (int i = 0; i < Game.sides.length; i++) {
				setSolid(m[i], true, game);
			}				
		}	
	
	public void setImage(Game game) {
		
		for (int i = 0; i < sides.length; i++) {
			if (sides[i].solid && sides[i].ghostSolid) {
				sideImages[i] = new ImageIcon(game.boxes[i]);
				}
			if (sides[i].solid && !sides[i].ghostSolid) {
					sideImages[i] = new ImageIcon(game.gBoxes[i]);
				}
			if (!sides[i].solid) {
				sideImages[i] = new ImageIcon(Game.nothing);
				}
		}
		
		image = new CompoundIcon(CompoundIcon.Axis.Z_AXIS, 0, sideImages);
	}
	
	public boolean isEqual(Box b) {
		if (x != b.x) return false;
		if (y != b.y) return false;
		return true;
	}
	
	public boolean solid(Movement m, Game game) {
		for (int i = 0; i < sides.length; i++) {
			if (sides[i].isEqual(m) && sides[i].solid) return true; 
		}
		return false;
	}
	
	public boolean ghostSolid(Movement m, Game game) {
		for (int i = 0; i < sides.length; i++) {
			if (sides[i].isEqual(m) && sides[i].ghostSolid) return true; 
		}
		return false;
	}
	
}
