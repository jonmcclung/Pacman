package pkg;

import java.util.ArrayList;
import java.util.Collections;

public class Path {
	Path() {}

	public ArrayList<Box> open = new ArrayList<Box>();
	public ArrayList<Box> closed = new ArrayList<Box>();
	
	public ArrayList<Movement> findEscape(Box start, Box death, int length, Game game) {
		
		Box buffer = new Box(start);
		buffer.G = 0;
		int highestG = 0;
		open.clear();
		closed.clear();
		open.add(new Box(buffer));
			while (!(highestG > length)) {
				////System.out.println("\n\n\nbuffer is "+buffer.x+", "+buffer.y);
				for (int i = 0; i < Game.sides.length; i++) {
					buffer.add(Game.sides[i]);
					////System.out.println("checking box at "+buffer.x+", "+buffer.y);
					if (!game.map[buffer.x][buffer.y].ghostSolid(Game.sides[i].opposite(), game)) {
						int index = 0;
						if (contains(open, game.map[buffer.x][buffer.y])) {
							//////System.out.println("already open");
							index = indexOf(open, game.map[buffer.x][buffer.y]);
							if (open.get(index).G > buffer.G+1) {
								open.get(index).G = buffer.G+1;
								buffer.subtract(Game.sides[i]);
								open.get(index).parent = new Box(buffer);
								buffer.add(Game.sides[i]);
								open.get(index).parent.path = Game.sides[i].opposite();
								open.get(index).F = buffer.G+1+(Math.abs(buffer.x-death.x)+(Math.abs(buffer.y-death.y)));
							}
						}
						else if (!contains(closed, game.map[buffer.x][buffer.y])) {
								open.add(new Box(game.map[buffer.x][buffer.y]));
								buffer.subtract(Game.sides[i]);
								open.get(open.size()-1).parent = new Box(buffer);
								buffer.add(Game.sides[i]);
								open.get(open.size()-1).parent.path = Game.sides[i].opposite();
								open.get(open.size()-1).G = buffer.G+1;
								if (buffer.G+1 > highestG) {
									highestG = buffer.G+1;
								}
								open.get(open.size()-1).F = buffer.G+1+Math.abs(buffer.x-death.x)+Math.abs(buffer.y-death.y);

								////System.out.println("adding Box "+buffer.x+", "+buffer.y+" ("+open.get(open.size()-1).F+")");
								}
						//else ////System.out.println("closed");
					}
					//else ////System.out.println("blocked");
					buffer.subtract(Game.sides[i]);
				}
				closed.add(new Box(buffer));
				////System.out.println("closed contains: ");
				for (int i = 0; i < closed.size(); i++) {
					////System.out.println(closed.get(i).x+", "+closed.get(i).y);
				}
				open.remove(indexOf(open, buffer));
				////System.out.println("open contains: ");
				for (int i = 0; i < open.size(); i++) {
					////System.out.println(open.get(i).x+", "+open.get(i).y+" ("+open.get(i).F+")");
				}
				//////System.out.println("removed "+buffer.x+", "+buffer.y+" from open and added to closed");
				/*if (contains(open, buffer)) //////System.out.println("but it's still here?!");
				else //////System.out.println("and it's really gone");
				*/
				int highestFIndex = 0;
				int highestF = 0;
				for (int i = 0; i < open.size(); i++) {
					if (open.get(i).F > highestF) {
						highestF = open.get(i).F;
						highestFIndex = i;
					}
				}
				if (highestG <= length) {
					buffer.set(open.get(highestFIndex));
					////System.out.println("next highest F score: "+buffer.F+" at "+buffer.x+", "+buffer.y+". ");
				}
			}

			ArrayList<Movement> path = new ArrayList<Movement>();
			////System.out.println("the path in reverse: "+highestG);
			for (int i = 0; i <= highestG; i++) {
				////System.out.println(i);
				//print(buffer.parent.path);
				path.add(buffer.parent.path.opposite());
				////System.out.println("("+buffer.x+", "+buffer.y+"), ("+start.x+", "+start.y+")");
				buffer.set(new Box(buffer.parent));
				if (buffer.isEqual(start)) break;
			}
			Collections.reverse(path);

			////System.out.println("Best path: ");
			//print(path.get(0));
		return path;
	}
	
	public void ensureContinuity(Game game) {
		
		open.clear();
		closed.clear();
		Box buffer = new Box(0, 0);
		open.add(new Box(buffer));
		////System.out.println("adding box at "+buffer.x+", "+buffer.y);

		int k = 1;
		while (k == 1) {
			open.remove(indexOf(open, buffer));
			closed.add(new Box(buffer));
			////System.out.println("\n\nbuffer is "+buffer.x+", "+buffer.y);
			for (int i = 0; i < Game.sides.length; i++) {
				////System.out.println("checking box at "+(buffer.x+Game.sides[i].x)+", "+(buffer.y+Game.sides[i].y));
				if (
					!game.map[buffer.x][buffer.y].isPerimeter(Game.sides[i]) &&
					!game.map[buffer.x][buffer.y].ghostSolid(Game.sides[i], game) &&
					!contains(closed, game.map[buffer.x+Game.sides[i].x][buffer.y+Game.sides[i].y])&&
					!contains(open, game.map[buffer.x+Game.sides[i].x][buffer.y+Game.sides[i].y])) {
					open.add(new Box(buffer.x+Game.sides[i].x, buffer.y+Game.sides[i].y));
					////System.out.println("               adding box at "+(buffer.x+Game.sides[i].x)+", "+(buffer.y+Game.sides[i].y));
				}
			}			
			
			if (open.size() > 0) {
				buffer.set(new Box(open.get(0)));
			}
			else if (closed.size() != Game.scale*Game.scale) {
				
				mapLoop:
				for (int y = 0; y < Game.scale; y++) {
					for (int x = 0; x < Game.scale; x++) {
						if (!contains(closed, game.map[x][y])) {

							//System.out.println("fixing a spot at "+x+", "+y);
							
							if (!(
								(x == Game.ghostSpawn.x && y == Game.ghostSpawn.y) || 
								(x-1 == Game.ghostSpawn.x && y == Game.ghostSpawn.y) ||
								(game.map[x][y].isPerimeter(Game.left))
								)) {
								game.map[x][y].setSolid(new Movement(Game.left, false, false), true, game);
								//System.out.println("by adding transparency at left");
							}
							
							else {
								game.map[x][y].setSolid(new Movement(Game.up, false, false), true, game);
								//System.out.println("by adding transparency at up");
							}
							
							open.add(new Box(game.map[x][y]));
							buffer.set(new Box(game.map[x][y]));
							break mapLoop;
							
						}
					}
				}
			}
			else break;
		}
	}
	
	public ArrayList<Movement> findPath(Box start, Box end, int length, Game game) {
		//////System.out.println("starting path search");
		Box buffer = new Box(start);
		boolean found = false;
		buffer.G = 0;
		int highestG = 0;
		open.clear();
		closed.clear();
		open.add(new Box(buffer));
		while (!(highestG > length)) {
			//////System.out.println("\n\n\nbuffer is "+buffer.x+", "+buffer.y);
			//////System.out.println("l: "+l);
			for (int i = 0; i < Game.sides.length; i++) {
				buffer.add(Game.sides[i]);
				////////System.out.println("checking box at "+buffer.x+", "+buffer.y);
				if (!game.map[buffer.x][buffer.y].ghostSolid(Game.sides[i].opposite(), game)) {
					int index = 0;
					if (contains(open, game.map[buffer.x][buffer.y])) {
						////////System.out.println("already open");
						index = indexOf(open, game.map[buffer.x][buffer.y]);
						if (open.get(index).G > buffer.G+1) {
							open.get(index).G = buffer.G+1;
							buffer.subtract(Game.sides[i]);
							open.get(index).parent = new Box(buffer);
							buffer.add(Game.sides[i]);
							open.get(index).parent.path = Game.sides[i].opposite();
							open.get(index).F = buffer.G+1+(Math.abs(buffer.x-end.x)+(Math.abs(buffer.y-end.y)));
						}
					}
					else if (!contains(closed, game.map[buffer.x][buffer.y])) {
							open.add(new Box(game.map[buffer.x][buffer.y]));
							buffer.subtract(Game.sides[i]);
							open.get(open.size()-1).parent = new Box(buffer);
							buffer.add(Game.sides[i]);
							open.get(open.size()-1).parent.path = Game.sides[i].opposite();
							open.get(open.size()-1).G = buffer.G+1;
							if (buffer.G+1 > highestG) {
								highestG = buffer.G+1;
							}
							open.get(open.size()-1).F = buffer.G+1+Math.abs(buffer.x-end.x)+Math.abs(buffer.y-end.y);

							//////System.out.println("adding Box "+buffer.x+", "+buffer.y+" ("+open.get(open.size()-1).F+")");
							}
					//else //////System.out.println("closed");
				}
				//else //////System.out.println("blocked");
				buffer.subtract(Game.sides[i]);
			}
			closed.add(new Box(buffer));
			//////System.out.println("closed contains: ");
			for (int i = 0; i < closed.size(); i++) {
				//////System.out.println(closed.get(i).x+", "+closed.get(i).y);
			}
			open.remove(indexOf(open, buffer));
			//////System.out.println("open contains: ");
			for (int i = 0; i < open.size(); i++) {
				//////System.out.println(open.get(i).x+", "+open.get(i).y+" ("+open.get(i).F+")");
			}
			////////System.out.println("removed "+buffer.x+", "+buffer.y+" from open and added to closed");
			/*if (contains(open, buffer)) //////System.out.println("but it's still here?!");
			else //////System.out.println("and it's really gone");
			*/
			int lowestFIndex = 0;
			int lowestF = Game.scale*Game.scale;
			for (int i = 0; i < open.size(); i++) {
				if (open.get(i).F < lowestF) {
					lowestF = open.get(i).F;
					lowestFIndex = i;
				}
			}
			buffer.set(open.get(lowestFIndex));
			////////System.out.println("next shortest F score: "+buffer.F+" at "+buffer.x+", "+buffer.y+". "+open.size()+" boxes in open list.");
			if (buffer.isEqual(end)) {
				found = true;
				break;
			}
		}
		ArrayList<Movement> path = new ArrayList<Movement>(0);
		path.clear();
		if (found) {
			//////System.out.println("the path in reverse: "+l);
			for (int i = 0; i < highestG; i++) {
				//////System.out.println(i);
				//print(buffer.parent.path);
				path.add(buffer.parent.path.opposite());
				//////System.out.println("("+buffer.x+", "+buffer.y+"), ("+start.x+", "+start.y+")");
				buffer.set(new Box(buffer.parent));
				if (buffer.isEqual(start)) break;
			}
			Collections.reverse(path);
		}
		else path.add(new Movement(0, 0));
		//////System.out.println("Best path: ");
		//print(path.get(0));
		return path;
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
	
	public int indexOf(ArrayList<Box> list, Box b) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).x == b.x && list.get(i).y == b.y) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean contains (ArrayList<Box> list, Box b) {
		////////System.out.println("size of list: "+list.size());
		for (int i = 0; i < list.size(); i++) {
			////////System.out.println("checking list for "+b.x+", "+b.y+" against "+list.get(i).x+", "+list.get(i).y);
			if (list.get(i).x == b.x && list.get(i).y == b.y) {
				////////System.out.println("FOUND");
				return true;
			}
			//else //////System.out.println("not found");
		}
		////////System.out.println("completed list check");
		return false;
	}
	
}
