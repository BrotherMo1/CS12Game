import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Spike {
	private int x;
	private int y;
	private int width;
	private int height;

	private Rectangle hitbox;
	private Sprite spikeSprite; // Add this to store the sprite

	public Spike(int x, int y, int TILES_DEFAULT_SIZE, int height) {

		this.x = x;
		this.y = y + (TILES_DEFAULT_SIZE / 2);
		this.width = TILES_DEFAULT_SIZE;
		this.height = TILES_DEFAULT_SIZE / 2;

		loadSpikeImage(); // This should set the spikeSprite

		hitbox = new Rectangle(x, y + (TILES_DEFAULT_SIZE / 2), TILES_DEFAULT_SIZE, TILES_DEFAULT_SIZE / 2);
	} // Spike constructor

	public void draw(Graphics2D gtd, int xLvlOffset) {

		// Draw the actual sprite image instead of a colored rectangle
		if (spikeSprite != null) {
			spikeSprite.draw(gtd, x - xLvlOffset, y - 20);
		} // if
	} // draw

	private void loadSpikeImage() {
		// Store the sprite in the class variable
		spikeSprite = (SpriteStore.get()).getSprite("sprites/trap.png");
	} // loadSpikeImage

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

	public Rectangle getHitBox() {
		return hitbox;
	}
}