import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Platform {
	int TILE_SIZE = 50;
	int x;
	int y;
	int width;
	int height;
	
	Rectangle hitBox;
	
	public Platform(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		hitBox = new Rectangle(x, y, width, height);
	} // Platform
	
	public void draw(Graphics2D gtd) {
		 gtd.setColor(Color.GREEN);
	     gtd.fillRect(x, y, width, height);
	     gtd.setColor(Color.DARK_GRAY);
	     gtd.drawRect(x, y, width, height);
	} // draw
	
	 public int getX() { return x; }
	 public int getY() { return y; }
	 public int getWidth() { return width; }
	 public int getHeight() { return height; }

}
