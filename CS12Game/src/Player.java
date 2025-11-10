
public class Player extends Entity{
	
	private Game game;
    private double dx = 0;        // Horizontal speed
    private double dy = 0;        // Vertical speed (gravity)
    private boolean isJumping = false;   // Is the player currently jumping?
    private boolean onGround = true;    // Is the player on the ground or a platform?
    
    private static final double GRAVITY = 1500;  // The strength of gravity
    private static final double JUMP_STRENGTH = -500;  // The strength of the jump

	public Player(Game g, String r, int newX, int newY) {
		super(r, newX, newY);
		game = g;
		// TODO Auto-generated constructor stub
	}
	

	public void move (long delta){
		
	    if (!onGround) {
	    	dy += GRAVITY * delta / 1000;
	    }else {
	    	dy = 0;
	    }
	    this.setVerticalMovement(dy);
	    
		
		super.move(delta);
		if (y > 950) {
			onGround = true;
			isJumping = false;
			y = 950;
		}
	} // move
	

	public void jump () {
		System.out.println("test1");
		System.out.println(isJumping);
		System.out.println(onGround);
		
		
		
		if (!isJumping && onGround) {
			isJumping = true;
			onGround = false;
			dy = JUMP_STRENGTH;
			this.setVerticalMovement(dy);
		}

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
