/* ShipEntity.java
 * March 27, 2006
 * Represents player's ship
 */


public class ShipEntity extends Entity {

  private Game game; // the game in which the ship exists
  private boolean onGround = true;

  /* construct the player's ship
   * input: game - the game in which the ship is being created
   *        ref - a string with the name of the image associated to
   *              the sprite for the ship
   *        x, y - initial location of ship
   */
  public ShipEntity(Game g, String r, int newX, int newY) {
    super(r, newX, newY);  // calls the constructor in Entity
    game = g;
    me.setBounds(newX, newY, sprite.getWidth(), sprite.getHeight());
  } // constructor

  /* move
   * input: delta - time elapsed since last move (ms)
   * purpose: move ship 
   */
  public void move(long delta) {
      // gravity
      if (!onGround) {
          dy += 0.5 * (delta / 1.5);
          if (dy > 200) dy = 200; 
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

      y += (dy * delta) / 100;
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

      // update Final position 
      me.setLocation((int)x, (int)y);
  }
  
  public boolean isOnGround() {
	  return onGround;
  }
  
  

  public void jump() {
	 
      if (onGround) {
          dy = -100; 
          onGround = false;
      }
  }
  
  
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

} // ShipEntity class