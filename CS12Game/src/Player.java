public class Player extends Entity{
	
	private Game game;
    protected static double dx = 0;        // Horizontal speed
    protected static double dy = 0;        // Vertical speed (gravity)
    private boolean onGround = true;    // Is the player on the ground or a platform?
    private static final double GRAVITY = 1500;  // The strength of gravity
    private static final double JUMP_STRENGTH = -500;  // The strength of the jump
    


	public Player(Game g, String r, int newX, int newY) {
		super(r, newX, newY);		
		game = g;
		Player.maxHealth = 100;
		Player.currentHealth = maxHealth;
		me.setBounds(newX, newY, sprite.getWidth(), sprite.getHeight());
		// TODO Auto-generated constructor stub
	}
	

	public void move (long delta){
		if (game.isShifting()) {
            return;
        }
	        
	    if (!onGround) {
	    	dy += GRAVITY * delta / 1000;
	    } else {
	    	dy = 0;
	    }
	    x += (dx * delta) / 1000;
	    me.setLocation((int)x, (int)y);
	    
	    for (Spike spike : game.getSpikes()) {
	    	if (me.intersects(spike.hitBox)) {
//	    		takeSpikeDamage(-1);
	    	}
	    }
	    
		// horizontal collisions 
	    for (Platform platform : game.getPlatforms()) {
	    	if (me.intersects(platform.hitBox)) {
	    		if (dx > 0) { 
	    			x = platform.x - me.width;
	    		} else if (dx < 0) { 
	    			x = platform.x + platform.width;
	    		}
	    		me.setLocation((int)x, (int)y);
	            break;
	          }
	    }
	    y += (dy * delta) / 1000;
	    me.setLocation((int)x, (int)y);
	      
	    // vertical collisions
	    onGround = false;
	    for (Platform platform : game.getPlatforms()) {
	    	if (me.intersects(platform.hitBox)) {
	    		if (dy > 0) { 
	    			y = platform.y - me.height;
	    			dy = 0;
	    			onGround = true;
	    		} else if (dy < 0) {
	    			y = platform.y + platform.height;
	    			dy = 0;
	    		}
	    		me.setLocation((int)x, (int)y);
	    		break;
	    	}
	    }
	    
	    me.setLocation((int)x, (int)y);
	} // move
	
	public boolean isOnGround() {
		  return onGround;
	}

	public void jump () {
		if (onGround) {
			onGround = false;
			dy = JUMP_STRENGTH;
		}

	} // jump
	
	
	public void takeDamage(int amount) {
	    currentHealth -= amount;
	    if (currentHealth < 0) currentHealth = 0;

	    if (currentHealth == 0) {
	        game.notifyDeath();
	    }
	}

	
	public void setHorizontalMovement(double dx) {
	    this.dx = dx;
	}
	
	public void setVerticalMovement(double newDY) {
	       dy = newDY;
	} // setVerticalMovement
	
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
