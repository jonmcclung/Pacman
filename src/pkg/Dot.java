package pkg;

public class Dot {
	boolean eaten = false;
	boolean power = false;
	boolean isFruit = false;
	String fruitName = "";
	Dot(boolean eaten) {
		this.eaten = eaten;
	}
	public void set(boolean eaten) {
		this.eaten = eaten;
	}
	
	Dot(String fruitName) {
		isFruit = true;
		this.fruitName = fruitName;
	}
	
}
