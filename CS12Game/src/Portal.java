import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
public class Portal {
	private int TILE_SIZE = 50;
	private int x;
	private int y;
	private int width;
	private int height;
	private Sprite [] frames;
	private int frameIndex = 0;
	private long lastFrameTime = 0;
	private long frameDelay = 100; // milliseconds between frames
	
	private Rectangle hitbox;
	private Sprite portal; // Add this to store the sprite
	public Portal(int x, int y) {
		
		frames = loadFrames();
		
		this.x = x;
		this.y = y;
		this.width = frames[0].getWidth();
		this.height = frames[0].getHeight();
		
		int hitboxWidth = width - 40;  
		int hitboxHeight = height - 20; 
		
		hitbox = new Rectangle(x + 40, y + (height / 2), hitboxWidth, hitboxHeight);
	} // Spike constructor
	private Sprite[] loadFrames() {
		int count = 8;
		String location = "sprites/animations/end_portal/end_portal";
		Sprite[] frames = new Sprite[count];
		for (int i = 0; i < count; i++) {
			String ref = (location + (i + 1) + ".png");
			frames[i] = SpriteStore.get().getSprite(ref);
		}
		return frames;
	}
	
	public void draw(Graphics2D gtd, int xLvlOffset) {
		updateFrame();
		portal = frames[frameIndex];
		int drawWidth = (int) (portal.getWidth() * Game.SCALE);
		int drawHeight = (int) (portal.getHeight() * Game.SCALE);
				
		gtd.drawImage(portal.getImage(), x - xLvlOffset, y, drawWidth, drawHeight, null);
		
		
	} // draw
	
	private void updateFrame() {
		long now = System.currentTimeMillis();
		if (now - lastFrameTime > frameDelay) {
			lastFrameTime = now;
			frameIndex = (frameIndex + 1) % frames.length;
		}
		
		
	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public Rectangle getHitbox() {
		return hitbox;
	}
}



