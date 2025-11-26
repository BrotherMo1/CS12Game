import java.awt.Graphics;
import java.awt.Rectangle;

public class Button {
	private int x;
	private int y;
	private int width;
	private int height;
	private Sprite normalSprite;
	private Sprite hoveredSprite;
	private String buttonType;
	private Rectangle bounds;
	private boolean mouseOver;

	// for button with picture
	public Button(String r, String s, int x, int y) {
		this.normalSprite = (SpriteStore.get()).getSprite(r);
		this.hoveredSprite = (SpriteStore.get()).getSprite(s);
		this.x = x;
		this.y = y;
		this.width = normalSprite.getWidth();
		this.height = normalSprite.getHeight();
		buttonType = r;

		initBounds();
	} // Main Constructor

	private void initBounds() {
		bounds = new Rectangle(x, y, normalSprite.getWidth(), normalSprite.getHeight());
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void draw(Graphics g) {
		if (mouseOver) {
			hoveredSprite.draw(g, x, y);
		} else {
			normalSprite.draw(g, x, y);
		} // else
	} // draw

	public int getX() {
		return x;
	} // getX

	public int getY() {
		return y;
	} // getY

	public int getWidth() {
		return width;
	} // getWidth

	public int getHeight() {
		return height;
	} // getHeight

	public Sprite gethoveredSprite() {
		return hoveredSprite;
	} // gethoveredSprite

	public Sprite getNormalSprite() {
		return normalSprite;
	} // getNormalSprite

	public String getButtonType() {
		return buttonType;
	} // getButtonType

	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}

} // class Button