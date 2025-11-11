/* Game.java
 * Space Invaders Main Program
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Game extends Canvas {
	
		private BufferStrategy strategy;   // take advantage of accelerated graphics
        private boolean waitingForKeyPress = true;  // true if game held up until
                                                    // a key is pressed
        private boolean leftPressed = false;  // true if left arrow key currently pressed
        private boolean rightPressed = false; // true if right arrow key currently pressed
        private boolean firePressed = false; // true if firing
        private boolean upPressed = false;
        private boolean pause = false;
        		
    	public final static int TILES_DEFAULT_SIZE = 32;
    	public static final float SCALE = 2f;
    	public final static int TILES_IN_WIDTH = 26;
    	public final static int TILES_IN_HEIGHT = 14;
    	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
    	public final static int GAME_WIDTH = 1000;
    	//TO MAKE WIDTH SCALABLE: TILES_SIZE * TILES_IN_WIDTH;
    	public final static int GAME_HEIGHT = 1000;
    	// TO MAKE HEIGHT SCALABLE: TILES_SIZE * TILES_IN_HEIGHT;

        
        private boolean gameRunning = true;
        private ArrayList <Entity> entities = new ArrayList<Entity>(); // list of entities
                                                      // in game
        private ArrayList<Entity> removeEntities = new ArrayList<Entity>(); // list of entities
                                                            // to remove this loop
        private Entity ship;  // the ship
        private double moveSpeed = 600; // hor. vel. of ship (px/s)
        private double yvel = 0;
        private long lastFire = 0; // time last shot fired
        private long alienLastFire = 0;
        private int random = 0;
        private long firingInterval = 300; // interval between shots (ms)
        private long alienFiringInterval = 500;
        private int alienCount; // # of aliens left on screen

        private String message = ""; // message to display while waiting
                                     // for a key press

        private boolean logicRequiredThisLoop = false; // true if logic
                                                       // needs to be 
                                                       // applied this loop

    	/*
    	 * Construct our game and set it running.
    	 */
    	public Game() {
    		// create a frame to contain game
    		JFrame container = new JFrame("Space Invaders");
    
    		// get hold the content of the frame
    		JPanel panel = (JPanel) container.getContentPane();
    
    		// set up the resolution of the game
    		panel.setPreferredSize(new Dimension(GAME_HEIGHT,GAME_WIDTH));
    		panel.setLayout(null);
    
    		// set up canvas size (this) and add to frame
    		setBounds(0,0,GAME_HEIGHT,GAME_WIDTH);
    		panel.add(this);
    
    		// Tell AWT not to bother repainting canvas since that will
            // be done using graphics acceleration
    		setIgnoreRepaint(true);
    
    		// make the window visible
    		container.pack();
    		container.setResizable(false);
    		container.setVisible(true);
    
    
            // if user closes window, shutdown game and jre
    		container.addWindowListener(new WindowAdapter() {
    			public void windowClosing(WindowEvent e) {
    				System.exit(0);
    			} // windowClosing
    		});
    
    		// add key listener to this canvas
    		addKeyListener(new KeyInputHandler());
    
    		// request focus so key events are handled by this canvas
    		requestFocus();

    		// create buffer strategy to take advantage of accelerated graphics
    		createBufferStrategy(2);
    		strategy = getBufferStrategy();
    
    		// initialize entities
    		initEntities();
    
    		// start the game
    		gameLoop();
        } // constructor
    
    
        /* initEntities
         * input: none
         * output: none
         * purpose: Initialise the starting state of the ship and alien entities.
         *          Each entity will be added to the array of entities in the game.
    	 */
    	private void initEntities() {
              // create the ship and put in center of screen
              ship = new ShipEntity(this, "sprites/ship1.png", 0, GAME_WIDTH - 50);
              entities.add(ship);
    
              // create a block of aliens (5x12)
              alienCount = 0;
              for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 12; col++) {
                  Entity alien = new AlienEntity(this, "sprites/alien.gif", 
                      100 + (col * 40),
                      50 + (row * 30));
                  entities.add(alien);
                  alienCount++;
                } // for
              } // outer for
    	} // initEntities

        /* Notification from a game entity that the logic of the game
         * should be run at the next opportunity 
         */
         public void updateLogic() {
           logicRequiredThisLoop = true;
         } // updateLogic

         /* Remove an entity from the game.  It will no longer be
          * moved or drawn.
          */
         public void removeEntity(Entity entity) {
           removeEntities.add(entity);
         } // removeEntity

         /* Notification that the player has died.
          */
		  
         public void notifyDeath() {
           message = "You lost and DEAD!  Try again?";
           waitingForKeyPress = true;
         } // notifyDeath


         /* Notification that the play has killed all aliens
          */
         public void notifyWin(){
           message = "Hoorauy you won!  You win!";
           waitingForKeyPress = true;
         } // notifyWin

        /* Notification than an alien has been killed
         */
         public void notifyAlienKilled() {
           alienCount--;
           
           if (alienCount == 0) {
             notifyWin();
           } // if
           
           // speed up existing aliens
           for (int i=0; i < entities.size(); i++) {
             Entity entity = (Entity) entities.get(i);
             if (entity instanceof AlienEntity) {
               // speed up by 2%
               entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.04);
             } // if
           } // for
         } // notifyAlienKilled
         

        /* Attempt to fire.*/
        public void tryToFire() {
          // check that we've waited long enough to fire
          if ((System.currentTimeMillis() - lastFire) < firingInterval){
            return;
          } // if

          // otherwise add a shot
          lastFire = System.currentTimeMillis();
          ShotEntity shot = new ShotEntity(this, "sprites/shot.gif", 
                            ship.getX() + 10, ship.getY() - 30);
          entities.add(shot);
        } // tryToFire
        
        public void alienTryToFire() {
            // check that we've waited long enough to fire
            if ((System.currentTimeMillis() - alienLastFire) < alienFiringInterval){
              return;
            } // if

            // otherwise add a shot
            alienLastFire = System.currentTimeMillis();
            random = (int)(Math.random()*(entities.size() - 1) + 1);
            Entity entity = (Entity) entities.get(random);
            if (entity instanceof AlienEntity) {
            	AlienShotEntity shot = new AlienShotEntity(this, "sprites/shot.gif", 
            	entity.getX(), entity.getY() + 30);
               	entities.add(shot);
            } // if

            
          } // tryToFire

	/*
	 * gameLoop
         * input: none
         * output: none
         * purpose: Main game loop. Runs throughout game play.
         *          Responsible for the following activities:
	 *           - calculates speed of the game loop to update moves
	 *           - moves the game entities
	 *           - draws the screen contents (entities, text)
	 *           - updates game events
	 *           - checks input
	 */
	public void gameLoop() {
          long lastLoopTime = System.currentTimeMillis();

          // keep loop running until game ends
          while (gameRunning) {
            
            // calc. time since last update, will be used to calculate
            // entities movement
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // get graphics context for the accelerated surface and make it black
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0,0,GAME_HEIGHT,GAME_WIDTH);
            
            if (!pause) {
	            // move each entity
	            if (!waitingForKeyPress) {
	              for (int i = 0; i < entities.size(); i++) {
	                Entity entity = (Entity) entities.get(i);
	                entity.move(delta);
	              } // for
	            } // if
	
	            // draw all entities
	            for (int i = 0; i < entities.size(); i++) {
	               Entity entity = (Entity) entities.get(i);
	               entity.draw(g);
	            } // for
	
	            // brute force collisions, compare every entity
	            // against every other entity.  If any collisions
	            // are detected notify both entities that it has
	            // occurred
	           for (int i = 0; i < entities.size(); i++) {
	             for (int j = i + 1; j < entities.size(); j++) {
	                Entity me = (Entity)entities.get(i);
	                Entity him = (Entity)entities.get(j);
	
	                if (me.collidesWith(him)) {
	                  me.collidedWith(him);
	                  him.collidedWith(me);
	                } // if
	             } // inner for
	           } // outer for
	
	           // remove dead entities
	           entities.removeAll(removeEntities);
	           removeEntities.clear();
	
	           // run logic if required
	           if (logicRequiredThisLoop) {
	             for (int i = 0; i < entities.size(); i++) {
	              // Entity entity = (Entity) entities.get(i);
	 Entity entity = entities.get(i);
	               entity.doLogic();
	             } // for
	             logicRequiredThisLoop = false;
	           } // if
	
	           // if waiting for "any key press", draw message
	           if (waitingForKeyPress) {
	             g.setColor(Color.white);
	             g.drawString(message, (GAME_HEIGHT - g.getFontMetrics().stringWidth(message))/2, 250);
	             g.drawString("Press any key", (GAME_HEIGHT - g.getFontMetrics().stringWidth("Press any key"))/2, 300);
	           	}  // if
	
	            // clear graphics and flip buffer
	            g.dispose();
	            strategy.show();
	
	            // ship should not move without user input
	            ship.setHorizontalMovement(0);
	            ship.setVerticalMovement(0);
	
	            // respond to user moving ship
	            if ((leftPressed) && (!rightPressed)) {
	              ship.setHorizontalMovement(-moveSpeed);
	            } else if ((rightPressed) && (!leftPressed)) {
	              ship.setHorizontalMovement(moveSpeed);
	            } // else
	            
	            // if spacebar pressed, try to fire
	            if (firePressed) {
	              tryToFire();
	            } // if
	            
	            // gravity
	            
	            
	            // jumping
	            if (upPressed && ship.getY() >= GAME_WIDTH - 50) {
	            	yvel = -GAME_HEIGHT;
	            	ship.setVerticalMovement(yvel);

	            }else if(ship.getY() <= GAME_WIDTH - 50 ) {
	            	yvel += 50;
	            	ship.setVerticalMovement(yvel);
	            } else {
	            	yvel = 0;
	            }
	            

	            
	            // pause
	            try { Thread.sleep(10); } catch (Exception e) {}
            } // if
	            

          } // while

	} // gameLoop


        /* startGame
         * input: none
         * output: none
         * purpose: start a fresh game, clear old data
         */
         private void startGame() {
            // clear out any existing entities and initalize a new set
            entities.clear();
            
            initEntities();
            
            // blank out any keyboard settings that might exist
            leftPressed = false;
            rightPressed = false;
            firePressed = false;
            upPressed = false;
            pause = false;
            
         } // startGame


        /* inner class KeyInputHandler
         * handles keyboard input from the user
         */
	private class KeyInputHandler extends KeyAdapter {
                 
        private int pressCount = 1;  // the number of key presses since
                                     // waiting for 'any' key press

	/* The following methods are required
	 * for any class that extends the abstract
	 * class KeyAdapter.  They handle keyPressed,
	 * keyReleased and keyTyped events.
	 */
		public void keyPressed(KeyEvent e) {

                  // if waiting for keypress to start game, do nothing
                  if (waitingForKeyPress) {
                    return;
                  } // if
                  
                  // respond to move left, right or fire
                  if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = true;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = true;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    firePressed = true;
                  } // if
                  
                  if (e.getKeyCode() == KeyEvent.VK_UP) {
                	  upPressed = true;
                  }
                  
                  if (e.getKeyCode() == KeyEvent.VK_P) {
                	  if(!pause) {
                		  pause = true;
                	  } else {
                		  pause = false;
                	  }
                  }

		} // keyPressed

		public void keyReleased(KeyEvent e) {
                  // if waiting for keypress to start game, do nothing
                  if (waitingForKeyPress) {
                    return;
                  } // if
                  
                  // respond to move left, right or fire
                  if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    leftPressed = false;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    rightPressed = false;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    firePressed = false;
                  } // if
                  
                  if (e.getKeyCode() == KeyEvent.VK_UP) {
                	  upPressed = false;
                  }

		} // keyReleased

 	        public void keyTyped(KeyEvent e) {

                   // if waiting for key press to start game
 	           if (waitingForKeyPress) {
                     if (pressCount == 1) {
                       waitingForKeyPress = false;
                       startGame();
                       pressCount = 0;
                     } else {
                       pressCount++;
                     } // else
                   } // if waitingForKeyPress

                   // if escape is pressed, end game
                   if (e.getKeyChar() == 27) {
                     System.exit(0);
                   } // if escape pressed

		} // keyTyped

	} // class KeyInputHandler


	/**
	 * Main Program
	 */
	public static void main(String [] args) {
        // instantiate this object
		new Game();
	} // main
} // Game
