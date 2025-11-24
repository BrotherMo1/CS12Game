import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Platform {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	private BufferedImage[] tileSprite;
	private BufferedImage[] shiftedTileSprite;
	private int spriteIndex;

	protected Rectangle hitBox;

	public Platform(int x, int y, int TILES_DEFAULT_SIZE, int height, int spriteIndex) {

		this.x = x;
		this.y = y;
		this.width = TILES_DEFAULT_SIZE;
		this.height = TILES_DEFAULT_SIZE;
		this.spriteIndex = spriteIndex;
		importOutsideSprites();
		this.spriteIndex = spriteIndex - 1;

		hitBox = new Rectangle(x, y, TILES_DEFAULT_SIZE, TILES_DEFAULT_SIZE);
	} // Platform

	public void draw(Graphics2D gtd, int xLvlOffset, boolean shifted) {
		if (spriteIndex >= 0 && spriteIndex < shiftedTileSprite.length && shiftedTileSprite[spriteIndex] != null
				&& shifted) {
			gtd.drawImage(shiftedTileSprite[spriteIndex], x - xLvlOffset, y, width, height, null);
		} else {
			gtd.drawImage(tileSprite[spriteIndex], x - xLvlOffset, y, width, height, null);

		} // else
	} // draw

	private void importOutsideSprites() {
		Sprite sprite = (SpriteStore.get()).getSprite("sprites/outside_sprites.png");
		Sprite sprite2 = (SpriteStore.get()).getSprite("sprites/outside_sprites_shifted.png");
		BufferedImage img = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage img2 = new BufferedImage(sprite2.getWidth(), sprite2.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		Graphics2D g2 = img2.createGraphics();
		g.drawImage(sprite.getImage(), 0, 0, null);
		g2.drawImage(sprite2.getImage(), 0, 0, null);
		g2.dispose();
		g.dispose();

		shiftedTileSprite = new BufferedImage[48];
		tileSprite = new BufferedImage[48];

		for (int j = 0; j < 4; j++)
			for (int i = 0; i < 12; i++) {
				int index = j * 12 + i;
				tileSprite[index] = img.getSubimage(i * 32, j * 32, 32, 32);
				shiftedTileSprite[index] = img2.getSubimage(i * 32, j * 32, 32, 32);
			} // for

	} // importOutsideSprites

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

}
