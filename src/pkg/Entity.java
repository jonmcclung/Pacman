package pkg;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Entity {
	int x, y, index = 0, animCount = 0, pathIndex = -1, fpm = 24, moveCount = 0;
	boolean isGhost = false, useScared = false;
	Path pathFinder = new Path();
	ArrayList<Movement> path = new ArrayList<Movement>();
	BufferedImage[] anim = new BufferedImage[2];
	BufferedImage[] scaredAnim = new BufferedImage[2];
	Movement oD = new Movement(0, 0), d = new Movement(0, 0);
	public enum State {
		dead, scared, scary
	};
	
	State state = State.scary;
	
	Entity(Movement m) {
		x = m.x; y = m.y;	
		pathIndex = -1;
	}
	
	public void setPosition(int x, int y) {
		this.x = x; this.y = y;		
	}
	
	public void setPosition(Movement m) {
		x = m.x; y = m.y;		
	}
	
	public int positive(int i) {
		if (i > 0) return i;
		return 0;
	}
	
	public void move(Game game) {
		add(oD);
		if (state != State.dead) {
			if (state == State.scary && position().reaches(Game.pac.position())) {
				path = pathFinder.findPath(game.map[x][y], game.map[Game.pac.x][Game.pac.y], Game.AI, game);
				d.set(path.get(0));
				add(d);
				if (!d.isEqual(0, 0) && (!position().isEqual(Game.ghostSpawn))) {
					oD.set(d);
				}
				add(d.opposite());
			}
			else if (state == State.scared && position().reaches(Game.pac.position())){
				path = pathFinder.findEscape(game.map[x][y], game.map[Game.pac.x][Game.pac.y], Game.AI, game);
				d.set(path.get(0));
				add(d);
				if (!position().isEqual(Game.ghostSpawn)) {
					oD.set(d);
				}
				add(d.opposite());
			}
			else {
				d.set(0, 0);
			}
			if (d.isEqual(0, 0)) {
				int r = Main.rand.nextInt(Game.sides.length);
				while (game.map[x][y].ghostSolid(Game.sides[r], game) ||
						(game.map[x][y].solid(Game.sides[r], game) && !oD.isEqual(0, 0)) ||
						(oD.isOpposite(Game.sides[r]) && !oD.isEqual(0, 0))) {
					r = (r+1)%Game.sides.length;
				}
				d.set(Game.sides[r]);
				}
				oD.set(d);
		}
		else {
			if (pathIndex == -1) {
				//path.clear();
				path = pathFinder.findPath(game.map[x][y], game.map[Game.ghostSpawn.x][Game.ghostSpawn.y], Game.scale*Game.scale, game);
			}
			if (pathIndex != path.size()-1) {
				pathIndex++;
				oD.set(path.get(pathIndex));
			}
			else {
				dead(false);
				oD.set(path.get(pathIndex).opposite());
				pathIndex = -1;
			}
		}
	}
	
	public void dead(boolean dead) {
		if (dead && state != State.dead) {
			fpm = (Game.pac.fpm*3/2) - Game.AI/2;
			state = State.dead;			
		}
		else if (!dead && state == State.dead) {
			state = State.scared;
			scared(false);
		}
	}
	
	public void scared(boolean scared) {
		if (scared && state != State.dead) {
			fpm = (Game.pac.fpm*3/2) - Game.AI/2;
			state = State.scared;
			useScared = true;
		}
		else if (!scared && state == State.scared) {
			fpm = Game.pac.fpm*9/8 - Game.AI/2;
			state = State.scary;
			useScared = false;
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
	
	public void print(List<Movement> list) {
		String output = "";
		for (Movement m : list) {
			if (m.isEqual(Game.up)) output += "up, ";
			else if (m.isEqual(Game.down)) output += "down, ";
			else if (m.isEqual(Game.left)) output += "left, ";
			else if (m.isEqual(Game.right)) output += "right, ";
		}
		//System.out.println(output);
	}
	
	public boolean collide(Entity e) {	
		for (int i = 0; i <  Game.sides.length; i++) {
				if (contains(e.edge(Game.sides[i]))) {
					return true;
				}
		}
		return false;
	}
	
	public boolean contains(Movement m) {
		if (
				m.x + 4 <= X()+Game.spriteWidth &&
				m.x - 4 >= X() &&				
				m.y + 4 <= Y()+Game.spriteWidth &&
				m.y - 4 >= Y()
				)
		{
			return true;
		}
		else return false;
	}


	public Movement edge(Movement m) {
			return new Movement(x*Game.boxWidth+(oD.x*animCount*Game.boxWidth/fpm)+(positive(m.x)*Game.boxWidth)+(Math.abs(m.y)*Game.boxWidth/2), 
					y*Game.boxWidth+(oD.y*animCount*Game.boxWidth/fpm)+(positive(m.y)*Game.boxWidth)+(Math.abs(m.x)*Game.boxWidth/2));
	}
	
	public Movement position() {
		return new Movement(x, y);
	}
	
	
	public int X() {
			return x*Game.boxWidth+2+(oD.x*animCount*Game.boxWidth/fpm);
	}
	
	public int Y() {
			return y*Game.boxWidth+2+(oD.y*animCount*Game.boxWidth/fpm);
	}

	public boolean isEqual(int tx, int ty) {
		if (x != tx) return false;
		if (y != ty) return false;
		return true;
	}
	public boolean isEqual(Movement m) {
		if (x != m.x) return false;
		if (y != m.y) return false;
		return true;
	}

	public void add(Movement m1) {
		x += m1.x;
		y += m1.y;
		if (x < 0) x += Game.scale;
		if (y < 0) y += Game.scale;
		if (!(x < Game.scale)) x -= Game.scale;
		if (!(y < Game.scale)) y -= Game.scale;
	}
	
}
