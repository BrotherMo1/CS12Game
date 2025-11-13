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
	
	public Platform(int x, int y, int TILES_DEFAULT_SIZE, int height) {
		
		this.x = x;
		this.y = y;
		this.width = TILES_DEFAULT_SIZE;
		this.height = TILES_DEFAULT_SIZE;
		
		hitBox = new Rectangle(x, y, TILES_DEFAULT_SIZE, TILES_DEFAULT_SIZE);
	} // Platform
	
	public void draw(Graphics2D gtd, int xLvlOffset) {
		 gtd.setColor(Color.GREEN);
	     gtd.fillRect(x - xLvlOffset, y, width, height);
	     gtd.setColor(Color.DARK_GRAY);
	     gtd.drawRect(x - xLvlOffset, y, width, height);
	} // draw
	
	 public int getX() { return x; }
	 public int getY() { return y; }
	 public int getWidth() { return width; }
	 public int getHeight() { return height; }

}
