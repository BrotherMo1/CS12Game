import java.awt.Graphics;
import java.awt.Graphics2D;
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
	
	// for button from rect
	public Button(String r, int x, int y, int width, int height) {
		this.buttonType = r;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		bounds = new Rectangle(x, y, width, height);
		
	} // Constructor
	
	private void initBounds() {
		bounds = new Rectangle(x, y, normalSprite.getWidth(), normalSprite.getHeight());
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void draw(Graphics g) {
		normalSprite.draw(g, x, y);
	} // draw
	
	public void drawRect(Graphics gtd) {
		gtd.drawRect(x, y, width, height);
	} // draw
	
	public void drawHovered(Graphics g) {
		hoveredSprite.draw(g, x, y);
	} // drawHovered
	
	public int getX() {
		 return x;
	} //getX
	
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
	
	public boolean isTouching() {
		return false;
	} // isTouching
	
	public String getButtonType() {
		return buttonType;
	} // getButtonType
	
	public boolean isHovered(int mouseX, int mouseY) {
	    return bounds.contains(mouseX, mouseY);
	} // isHovered
	
	
} // class Button