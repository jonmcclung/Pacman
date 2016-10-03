package pkg;

public class Movement {
	int x, y;
	boolean solid = false, ghostSolid = false;
	
	Movement(int x, int y) {
		this.x = x; this.y = y;
	}
	
	Movement(Movement m) {
		x = m.x;
		y = m.y;
		solid = m.solid;
		ghostSolid = m.ghostSolid;
	}
	
	Movement(Movement m, boolean solid, boolean ghostSolid) {
		x = m.x;
		y = m.y;
		this.solid = solid;
		this.ghostSolid = ghostSolid;
	}
	
	public boolean reaches(Movement m) {
		if (Game.AI >= Math.abs(m.x-x)+Math.abs(m.y-y))
		return true;
		return false;
	}
	
	public void add(Movement m1) {
		x += m1.x;
		y += m1.y;
		if (x < 0) x += Game.scale;
		if (y < 0) y += Game.scale;
		if (!(x < Game.scale)) x -= Game.scale;
		if (!(y < Game.scale)) y -= Game.scale;
	}
	
	public void set(int x, int y) {
		this.x = x; this.y = y;
	}
	
	public void set(Movement m) {
		x = m.x; y = m.y;
	}
	
	public boolean isEqual(int tx, int ty) {
		if (x != tx) return false;
		if (y != ty) return false;
		return true;
	}
	
	public boolean isPositive() {
		if (x < 0 || y < 0)
			return false;
		return true;
	}
	
	public boolean isEqual(Movement m) {
		if (x != m.x) return false;
		if (y != m.y) return false;
		return true;
	}	
	
	public Movement opposite() {
		Movement m = new Movement(this);
		m.x *= -1;
		m.y *= -1;
		return m;
	}
	
	public boolean isOpposite(Movement m) {
		if (x*m.x == -1) return true; 
		if (y*m.y == -1) return true; 
		return false;
	}	
}
