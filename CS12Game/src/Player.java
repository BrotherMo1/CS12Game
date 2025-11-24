import java.awt.Graphics;

public class Player extends Entity{
	
	private Game game;
    protected double dx = 0;        // Horizontal speed
    protected double dy = 0;        // Vertical speed (gravity)
    private double nextY = 0;
    private double nextX = 0;
    private boolean onGround = true;    // Is the player on the ground or a platform?
    private static final double GRAVITY = 1500;  // The strength of gravity
    private static final double JUMP_STRENGTH = -500;  // The strength of the jump
    private boolean takenFallDamage = false;
    private double impactSpeed = 0;
    
    private int hitboxWidth = (int)(24 * Game.SCALE);
    private int hitboxHeight = (int)(38 * Game.SCALE);
    private int offsetX = 0;
    private int offsetY = 0;
    
    
	private long lastDamageTimeSpike = 0;
	private long damageCooldownSpike = 500; // ms
	
	private boolean animationFinished = false;
	private boolean facingRight = true;
	
	private Sprite[] idleFrames;
	private Sprite[] runFrames;
	private Sprite[] jumpFrames;
	private Sprite[] fallFrames;
	private Sprite[] portalFrames;
	private Sprite[] deathFrames;
	private Sprite[] hurtFrames;

	private int animationIndex = 1;
	private long lastFrameTime = 0;
	private long frameSpeed = 100; // ms per frame
	
	public enum PlayerState{
		IDLE,
		RUN,
		JUMP,
		FALL,
		PORTAL,
		HURT,
		DEATH
	}
	
	public PlayerState state = PlayerState.IDLE;
	
	// fall dmg
	private final double FALL_DAMAGE_THRESHOLD = 800;

	public Player(Game g, String r, int newX, int newY) {
	    super(r, newX, newY);		
	    game = g;

	    loadAnimations();
	    
	    this.maxHealth = 100;
	    this.currentHealth = this.maxHealth;
	    this.healthWidth = game.healthBarWidth;
	    
	    offsetX = (int)((sprite.getWidth() * Game.SCALE) - hitboxWidth) / 2;
	    offsetY = (int)((sprite.getHeight() * Game.SCALE) - hitboxHeight) / 2;
	    
	    me.setBounds((int)x, (int)y, hitboxWidth, hitboxHeight);
	}
	
	public void printAnimationDebug() {
	    System.out.println("State: " + state + 
	                      ", Index: " + animationIndex + 
	                      ", Finished: " + animationFinished +
	                      ", dy: " + dy + 
	                      ", onGround: " + onGround);
	}
	
	private void loadAnimations() {
		idleFrames = loadFrames("sprites/animations/idle/idle", 7); 
		jumpFrames = loadFrames("sprites/animations/jump/jump", 2);
		fallFrames = loadFrames("sprites/animations/fall/fall", 3);
		portalFrames = loadFrames("sprites/animations/portal/portal", 10);
		runFrames = loadFrames("sprites/animations/run/run", 8);
		deathFrames = loadFrames("sprites/animations/death/death", 12);
		hurtFrames = loadFrames("sprites/animations/hurt/hurt", 4);
		
	}
	
	private void updateAnimationState(){
	    PlayerState previous = state;

	    if (state == PlayerState.DEATH) return;   // never change
	    if (state == PlayerState.HURT && !animationFinished) return; // wait till hurt finishes
	    
	    if (game.isShifting()) {
	        state = PlayerState.PORTAL; 
	    } else if (!onGround) {
	        if (dy < 0) state = PlayerState.JUMP;
	        else if (dy > 0) state = PlayerState.FALL;
	    }  else if (Math.abs(dx) > 0.1) {  // Small threshold to prevent jitter
	        state = PlayerState.RUN;
	    } else {
	        state = PlayerState.IDLE;
	    }

	    if (state != previous) {
	        animationIndex = 1;
	        animationFinished = false;
	        lastFrameTime = System.currentTimeMillis();
	    } // if
	}
	
	public Sprite getCurrentAnimationFrame() {
 	    switch (state) {

 	        case IDLE:
 	            return idleFrames[animationIndex % idleFrames.length];

 	        case RUN:
 	            return runFrames[animationIndex % runFrames.length];

 	        case JUMP:
 	            return jumpFrames[animationIndex];

 	        case FALL:
 	            return fallFrames[animationIndex];

 	        case PORTAL:
 	            return portalFrames[animationIndex % portalFrames.length];
 	            
 	        case DEATH:
 	        	return deathFrames[animationIndex];
 	        case HURT:
 	        	return hurtFrames[animationIndex];
 	        	
 	        default:
 	        	break;
 	    }

 	    return idleFrames[1]; 
 	}
	
	private void updateAnimationFrame() {
	    long now = System.currentTimeMillis();
	    if (now - lastFrameTime > frameSpeed) {
	        lastFrameTime = now;

	        // If the animation should NOT loop
	        if (state == PlayerState.JUMP || state == PlayerState.FALL || 
	        		state == PlayerState.HURT || state == PlayerState.DEATH) {

	            if (!animationFinished) {
	                animationIndex++;

	                // Once we reach last frame, stop
	                int frameCount = getCurrentFrameCount();
	                if (animationIndex >= frameCount - 1) {
	                    animationIndex = frameCount - 1;  // stay on last frame
	                    animationFinished = true;
	                }
	            }

	        } else {
	            // Looping animations
	            animationIndex++;
	        }
	    }
	}
	
	private int getCurrentFrameCount() {
	    return switch (state) {
	        case IDLE -> idleFrames.length;
	        case RUN -> runFrames.length;
	        case JUMP -> jumpFrames.length;
	        case FALL -> fallFrames.length;
	        case PORTAL -> portalFrames.length;
	        case DEATH -> deathFrames.length;
	        case HURT -> hurtFrames.length;
	        default -> throw new IllegalArgumentException("Unexpected value: " + state);
	    };
	}
	
	@Override
	public void draw (Graphics g, int xLvlOffset) {
		Sprite current = getCurrentAnimationFrame();

		
		g.drawRect(
		        (int) x - xLvlOffset,
		        (int) y,
		        me.width,
		        me.height
		    );
		int drawX = (int)x - offsetX - xLvlOffset;
		int drawY = (int)y - offsetY;
		int drawWidth = (int) (current.getWidth() * Game.SCALE);
		int drawHeight = (int) (current.getHeight() * Game.SCALE);

		if (!facingRight) {
		    g.drawImage(current.getImage(),
		        drawX + (int)(sprite.getWidth() * Game.SCALE), // shift for flip
		        drawY,
		        -drawWidth,
		        drawHeight,
		        null);
		} else {
		    g.drawImage(current.getImage(), drawX, drawY, drawWidth, drawHeight, null);
		}
    }  // draw
	
	
	private Sprite[] loadFrames(String location, int count){
		Sprite[] frames = new Sprite[count];
		
		for (int i = 0; i < count; i++) {
			String ref = (location + (i + 1) + ".png");
			frames[i] = SpriteStore.get().getSprite(ref);
		}
		
		return frames;
	}

	
	
	public void dmgChecker() { 
		
	    // FALL DAMAGE CHECK
	    	
		// Player has JUST landed this frame
		
		
		// Reset vertical velocity on landing
		if (onGround) {
			takenFallDamage = false;
		}
		// check if player falls out of bounds, if true kill them
		if (y > game.GAME_HEIGHT + 50) {
			changeHealth(-this.maxHealth);
	    } // if
		
		for (Spike spike : game.getSpikes()) {
			if (me.intersects(spike.hitBox)) {
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
			state = PlayerState.HURT;
			animationIndex = 0;
			animationFinished = false;
			

			if (this.currentHealth <= 0) {
				state = PlayerState.DEATH;
				animationIndex = 0;
	            animationFinished = false;
				game.notifyDeath();
				

			}
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

    public void move(long delta) {
        updateAnimationState();
        updateAnimationFrame();

        if (game.isShifting()) {
            // Only run vertical collision logic, not horizontal movement
            for (Platform platform : game.getPlatforms()) {
                if (me.intersects(platform.hitBox)) {
                    if (dy > 0) { // falling
                        y = platform.y - me.height;
                        dy = 0;
                        onGround = true;
                        me.setLocation((int)x, (int)y);
                        break;
                    }
                }
            }
            return; // skip normal horizontal movement
        }

        // Horizontal movement
        x += (dx * delta) / 1000.0;
        me.setLocation((int)x, (int)y);

        // Horizontal collisions
        for (Platform platform : game.getPlatforms()) {
            if (me.intersects(platform.hitBox)) {
                if (dx > 0) x = platform.x - me.width;
                else if (dx < 0) x = platform.x + platform.width;
                me.setLocation((int)x, (int)y);
                break;
            }
        }

        // Apply gravity
        if (!onGround) dy += GRAVITY * delta / 1000.0;

        // Vertical movement
        y += (dy * delta) / 1000.0;
        me.setLocation((int)x, (int)y);

        // Vertical collisions
        for (Platform platform : game.getPlatforms()) {
            if (me.intersects(platform.hitBox)) {
                if (dy > 0) { // falling
                    impactSpeed = dy;
                    if (impactSpeed > FALL_DAMAGE_THRESHOLD && !takenFallDamage) {
                        changeHealth(-10);
                        takenFallDamage = true;
                    }
                    y = platform.y - me.height;
                    dy = 0;
                    me.setLocation((int)x, (int)y);
                } else if (dy < 0) { // hitting head
                    y = platform.y + platform.height;
                    dy = 0;
                    me.setLocation((int)x, (int)y);
                }
            }
        }

        // STABLE onGround CHECK
        boolean grounded = false;
        for (Platform platform : game.getPlatforms()) {
            boolean horizontalMatch = x + me.width > platform.x && x < platform.x + platform.width;
            boolean feetClose = Math.abs((y + me.height) - platform.y) <= 2.0; // 2px tolerance
            if (horizontalMatch && feetClose) {
                grounded = true;
                break;
            }
        }
        onGround = grounded;

        if (onGround) takenFallDamage = false;

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
	
	
//	public void takeDamage(int amount) {
//		
//	    currentHealth -= amount;
//	    if (currentHealth < 0) currentHealth = 0;
//
//	    if (currentHealth == 0) {
//	    	
//	        game.notifyDeath();
//	    }
//	}

	
	public void setHorizontalMovement(double dx) {
	    this.dx = dx;
	    if (dx > 0) facingRight = true;
	    if (dx < 0) facingRight = false;
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
