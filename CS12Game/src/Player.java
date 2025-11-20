public class Player extends Entity{
	
	private Game game;
    protected double dx = 0;        // Horizontal speed
    protected double dy = 0;        // Vertical speed (gravity)
    private boolean onGround = true;    // Is the player on the ground or a platform?
    private static final double GRAVITY = 1500;  // The strength of gravity
    private static final double JUMP_STRENGTH = -500;  // The strength of the jump
    private boolean takenFallDamage = false;
    private double impactSpeed = 0;
    
	private long lastDamageTimeSpike = 0;
	private long damageCooldownSpike = 800; // ms
	
	// fall dmg
	private final double FALL_DAMAGE_THRESHOLD = 500;

	public Player(Game g, String r, int newX, int newY) {
	    super(r, newX, newY);		
	    game = g;

	    this.maxHealth = 100;
	    this.currentHealth = this.maxHealth;
	    this.healthWidth = game.healthBarWidth;

	    me.setBounds(newX, newY, sprite.getWidth(), sprite.getHeight());
	}

	
	public void dmgChecker() { 
		
	    // FALL DAMAGE CHECK
	    	
		// Player has JUST landed this frame
		
		
		// Reset vertical velocity on landing
		if (onGround) {
			takenFallDamage = false;
		}
		// check if player falls out of bounds, if true kill them
		if (y > game.GAME_HEIGHT - 50) {
			changeHealth(-this.maxHealth);
	    } // if
		
		for (Spike spike : game.getSpikes()) {
			if (me.intersects(spike.getHitBox())) {
				takeSpikeDamage(-10);
			}
		}
	} // dmgChecker

    
    public void updateHealthBar() {
 		this.healthWidth = (int) ((this.currentHealth / (double) this.maxHealth) * game.healthBarWidth);
    }
    
    // change health if player falls out of bounds or gets hit
    private void changeHealth(int value) {

		if (value < 0) {
			this.currentHealth += value;
			this.currentHealth = Math.max(Math.min(this.currentHealth, this.maxHealth), 0);

			if (this.currentHealth <= 0) game.notifyDeath();
		} // if
		updateHealthBar();
	} // changeHealth

    // add delay in taking spike damage
    public void takeSpikeDamage(int value) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTimeSpike < damageCooldownSpike) return;
        lastDamageTimeSpike = now;

        changeHealth(value);
    } // takeSpikeDamage

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
	    			
	    			impactSpeed = dy;
	    			if (impactSpeed > FALL_DAMAGE_THRESHOLD && !takenFallDamage) {
//	    		    	        int damage = (int)(impactSpeed - FALL_DAMAGE_THRESHOLD) * 2;
	    				int damage = -10;
	    				changeHealth(damage);
	    				takenFallDamage = true;
	    			} // if 
	    			
	    			
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
