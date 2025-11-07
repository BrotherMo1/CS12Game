
public class Player extends Entity{
	
	private Game game;
    private double dx = 0;        // Horizontal speed
    private double dy = 0;        // Vertical speed (gravity)
    private boolean isJumping = false;   // Is the player currently jumping?
    private boolean isFalling = false;   // Is the player falling due to gravity?
    private boolean onGround = false;    // Is the player on the ground or a platform?
    
    private static final double GRAVITY = 80;  // The strength of gravity
    private static final double JUMP_STRENGTH = -500;  // The strength of the jump

	public Player(Game g, String r, int newX, int newY) {
		super(r, newX, newY);
		game = g;
		// TODO Auto-generated constructor stub
	}
	

	public void move (long delta){
	    if (!onGround) {
	    	dy += GRAVITY * delta / 1000.0;  // Apply gravity (affected by time)
	    }
	    if (y > 900) {  // If y goes below a certain point, consider it as the "ground"
	    	y = 900;
	    	isJumping = false;
	    	dy = 0;  // Reset vertical speed
	    	onGround = true;
	    }

		 
		super.move(delta);
	} // move
	

	public void jump () {
		super.jump();

	} // jump
	
	// check if collisions;

	
	
	  
	  
	  /* collidedWith
	   * input: other - the entity with which the ship has collided
	   * purpose: notification that the player's ship has collided
	   *          with something
	   */
	   public void collidedWith(Entity other) {
	     if (other instanceof AlienEntity) {
	        game.notifyDeath();
	     } // if
	   } // collidedWith    

}
