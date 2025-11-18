/* Game.java
 * Space Invaders Main Program
 *
 */

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends Canvas {

    private BufferStrategy strategy; // take advantage of accelerated graphics
    private boolean waitingForKeyPress = true; // true if game held up until
    // a key is pressed
    private boolean leftPressed = false; // true if left arrow key currently pressed
    private boolean rightPressed = false; // true if right arrow key currently pressed
    private boolean firePressed = false; // true if firing
    private boolean upPressed = false;
    private boolean paused = false;

    public final static int TILES_DEFAULT_SIZE = 32;
    public static final float SCALE = 2f;
    public final static int TILES_IN_WIDTH = 26;
    public final static int TILES_IN_HEIGHT = 14;
    public final static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
    //TO MAKE WIDTH SCALABLE: TILES_SIZE * TILES_IN_WIDTH;
    public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;
    // TO MAKE HEIGHT SCALABLE: TILES_SIZE * TILES_IN_HEIGHT;

    private int xLvlOffset;
    private int leftBorder = (int)(0.2 * GAME_WIDTH);
    private int rightBorder = (int)(0.8 * GAME_WIDTH);
    private int lvlTilesWide = 0;
    private int maxTilesOffset = lvlTilesWide - TILES_IN_WIDTH;
    private int maxLvlOffsetX = maxTilesOffset * TILES_SIZE;


    private boolean gameRunning = false;
    private ArrayList < Entity > entities = new ArrayList < Entity > (); // list of entities
    // in game
    private ArrayList < Entity > removeEntities = new ArrayList < Entity > (); // list of entities
    // to remove this loop
    private Entity player; // the ship
    private double moveSpeed = 600; // hor. vel. of ship (px/s)
    private double yvel = 0;
    private long lastFire = 0; // time last shot fired
    private long alienLastFire = 0;
    private int random = 0;
    private long firingInterval = 300; // interval between shots (ms)
    private long alienFiringInterval = 500;
    private int alienCount; // # of aliens left on screen
    private long lastShiftTime = 0;
    private final long SHIFT_DELAY = 1000; // ms
    private boolean shifting = false;
    private long shiftStartTime = 0;
    private static final long SHIFT_DURATION = 500; // 1 second per phase
    private int shiftPhase = 0; // 0 = not shifting, 1 = pre-shift pause, 2 = post-shift pause


    private String message = ""; // message to display while waiting
    // for a key press

    private boolean logicRequiredThisLoop = false; // true if logic
    // needs to be 
    // applied this loop

    private boolean shifted = false;

    private ArrayList < Platform > platforms = new ArrayList < > ();
    private ArrayList < Spike > spikes = new ArrayList < > ();
    private ArrayList < int[][] > maps = new ArrayList < > ();
    private int currentMap = 0;
    private boolean playing = false;

    private MouseInputHandler mouseInputs = new MouseInputHandler();

    private ArrayList < Button > buttons = new ArrayList < > ();

    private Button backButton;
    private Button retryButton;
    private Button continueButton;
    
    private long gameStartTime = 0;
    private long gameEndTime = 0;
    private boolean timerRunning = false;
    
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;

	protected int healthBarWidth = (int) (150 * Game.SCALE);
	protected int healthBarHeight = (int) (4 * Game.SCALE);
	protected int healthBarXStart = (int) (34 * Game.SCALE);
	protected int healthBarYStart = (int) (14 * Game.SCALE);
	protected int healthWidth = healthBarWidth;
	
    protected static int maxHealth = 100;
    protected static int currentHealth = maxHealth;
    
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);
	
	private long lastDamageTimeSpike = 0;
	private long damageCooldownSpike = 500; // ms
	
	// fall dmg
	private boolean prevOnGround = true;
	private final double FALL_DAMAGE_THRESHOLD = 3;
	
    /*
     * Construct our game and set it running.
     */
    public Game() {
        // create a frame to contain game
        JFrame container = new JFrame("Space Invaders");

        // get hold the content of the frame
        JPanel panel = (JPanel) container.getContentPane();

        // set up the resolution of the game
        panel.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        panel.setLayout(null);

        // set up canvas size (this) and add to frame
        setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
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

        // add mouse listener to canvas
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);

        // request focus so key events are handled by this canvas
        requestFocus();

        // create buffer strategy to take advantage of accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        Button startButton = new Button("sprites/playButton.png", "sprites/playButtonHover.png", 60, 350);
	    Button quitButton = new Button("sprites/quitButton.png", "sprites/quitButtonHover.png", 60, 550);
	    backButton = new Button("sprites/backButton.png", "sprites/backButtonHover.png", GAME_WIDTH / 2 - 50, 550);
	    retryButton = new Button("sprites/retryButton.png", "sprites/retryButtonHover.png",GAME_WIDTH / 2 - 50, 410);
	    continueButton = new Button("sprites/continueButton.png", "sprites/continueButtonHover.png", GAME_WIDTH / 2 - 50, 275);
	    
	    buttons.add(backButton);	
	    buttons.add(retryButton);
	    buttons.add(continueButton);
	    buttons.add(startButton);
	    buttons.add(quitButton);

		while(true) {
			startMenu();
			
			if(playing) {
				
				initMaps();
	    		loadMap(0);
	    		lvlTilesWide = getMapWidth(currentMap);
	    		maxTilesOffset = lvlTilesWide - TILES_IN_WIDTH;
	        	maxLvlOffsetX = maxTilesOffset * TILES_SIZE;
	    
	    		// start the game
	    		gameLoop();
			} // if
		} // while
			   


    } // constructor

 // create startMenu
 		private void startMenu() {
 			
 			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 		    
 			(SpriteStore.get()).getSprite("sprites/bg.png").draw(g, 0, 0);
 			(SpriteStore.get()).getSprite("sprites/menu_background.png").draw(g, 5, 250);
 		    		   
 		 
 	       for(Button b : buttons) {
 	    	   if(b.getButtonType() == "sprites/continueButton.png" || b.getButtonType() == "sprites/retryButton.png" || b.getButtonType() == "sprites/backButton.png") {
 		    	   continue;
 	    	   } // if		    	
 	    	   b.draw(g);
 	       } // for
 		                      
 		    strategy.show();
 			
 		} // startMenu

    /* initEntities
     * input: none
     * output: none
     * purpose: Initialise the starting state of the ship and alien entities.
     *          Each entity will be added to the array of entities in the game.
     */
    private void initEntities() {
        // create the ship and put in center of screen
        player = new Player(this, "sprites/ship1.png", 70, 465);
        entities.add(player);

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
    
    public boolean isShifting() {
    	return shifting;
    }

    private int getMapWidth(int index) {
        if (index < 0 || index >= maps.size()) {
            return 0;
        } // if
        return maps.get(index)[0].length;
    }

    private void loadMap(int index) {
        if (index < 0 || index >= maps.size()) {
            return;
        } // if
        platforms = makePlatforms(maps.get(index));
        spikes = makeSpikes(maps.get(index));
    }

    private void checkCloseToBorder() {
        int playerX = player.getX();
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder) {
            xLvlOffset += diff - rightBorder;
        } else if (diff < leftBorder) {
            xLvlOffset += diff - leftBorder;
        }

        if (xLvlOffset > maxLvlOffsetX) {
            xLvlOffset = maxLvlOffsetX;
        } else if (xLvlOffset < 0) {
            xLvlOffset = 0;
        }
    }

	private void initMaps() {

		//java 1D version
		int[][] lvl1=
    		{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
    		{1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1},
    		{1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
		
		//java 1D version
		int[][] shift1=
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
		    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
		   	{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
		   	{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		   	{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
		
		
		
		//java 1D version
		int[][] lvl2=
    		{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
    		{1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
    		{1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,0,0,0,0,2,2,1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,2,1},
    		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
			
		//java 1D version
		int[][] shift2=
			{{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		    {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1},
		    {1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,2,1},
		    {1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,2,1},
		   	{1,0,0,0,0,0,0,0,2,2,1,0,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,2,1},
		   	{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		   	{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
    	
		int[][][] lvls = {lvl1, lvl2};
		int[][][] shifts = {shift1, shift2};
		
		// add them in order
		for (int i = 0; i < lvls.length; i++) {
		    maps.add(lvls[i]);
		    maps.add(shifts[i]);
		}

	}



	public ArrayList<Platform> makePlatforms(int[][] map) {
    	ArrayList<Platform> platforms = new ArrayList<>();
    	for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == 1) {
                    platforms.add(new Platform(
                        col * TILES_SIZE, 
                        row * TILES_SIZE, 
                        TILES_SIZE, 
                        TILES_SIZE, map[row][col]
                    ));
                }
            }
        }
        return platforms;
	} // makePlatforms
    
    public ArrayList < Spike > makeSpikes(int[][] map) {
        ArrayList < Spike > spikes = new ArrayList < > ();
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == 2) {
                    spikes.add(new Spike(
                        col * TILES_SIZE,
                        row * TILES_SIZE,
                        TILES_SIZE,
                        TILES_SIZE
                    ));
                }
            }
        }
        return spikes;
    } // makeSpikes
    
    


    private boolean checkLevelEnd() {
        int tileX = (int)((player.x + player.sprite.getWidth() / 2) / TILES_SIZE);
        int tileY = (int)((player.y + player.sprite.getHeight()) / TILES_SIZE); // bottom of player

        int[][] map = maps.get(currentMap);

        if (tileY < map.length && tileX < map[0].length) {
            if (map[tileY][tileX] == 2) {
                return true; // player reached the end tile
            }
        }
        return false;
    }

    private void goToNextLevel() {
        currentMap++; // move to next map
        if (currentMap >= maps.size()) {
            currentMap = 0; // or end the game
            notifyWin();
            return;
        }

        // Load the next map's platforms
        loadMap(currentMap);

        // Reset player position at the start of the new map
        player.x = 70; // starting X position
        player.y = 480; // starting Y, adjust if needed
        player.setHorizontalMovement(0);
        player.setVerticalMovement(0);

        // Reset level scrolling
        xLvlOffset = 0;

        shifted = false;
        currentHealth = maxHealth;
        updateHealthBar();
    }



    public ArrayList < Platform > getPlatforms() {
        return platforms;
    }
    
    public ArrayList < Spike > getSpikes(){
    	return spikes;
    }


    /* Notification from a game entity that the logic of the game
     * should be run at the next opportunity 
     */
    public void updateLogic() {
        logicRequiredThisLoop = true;
    } // updateLogic
    
    private void updateHealthBar() {
 		healthWidth = (int) ((currentHealth / (double) maxHealth) * healthBarWidth);
 		System.out.println("currentHealh" + currentHealth);
    }

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
    public void notifyWin() {
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
        for (int i = 0; i < entities.size(); i++) {
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
        if ((System.currentTimeMillis() - lastFire) < firingInterval) {
            return;
        } // if

        // otherwise add a shot
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "sprites/shot.gif",
            player.getX() + 10, player.getY() - 30);
        entities.add(shot);
    } // tryToFire

    public void alienTryToFire() {
        // check that we've waited long enough to fire
        if ((System.currentTimeMillis() - alienLastFire) < alienFiringInterval) {
            return;
        } // if

        // otherwise add a shot
        alienLastFire = System.currentTimeMillis();
        random = (int)(Math.random() * (entities.size() - 1) + 1);
        Entity entity = (Entity) entities.get(random);
        if (entity instanceof AlienEntity) {
            AlienShotEntity shot = new AlienShotEntity(this, "sprites/shot.gif",
                entity.getX(), entity.getY() + 30);
            entities.add(shot);
        } // if


    } // tryToFire

    
    public void resetGame() {
    	playing = false;
		gameRunning = false;
		entities.clear();
		removeEntities.clear();
		leftPressed = false;
        rightPressed = false;
        firePressed = false;
        upPressed = false;
        shifted = false;
		paused = false;
		currentMap = 0; 
		currentHealth = maxHealth;
		updateHealthBar();
    } // resetGame

    
    // helper method to determine if a collision occurs
    public void collisionChecker() {
        // brute force collisions, compare every entity
        // against every other entity.  If any collisions
        // are detected notify both entities that it has
        // occurred
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity me = (Entity) entities.get(i);
                Entity him = (Entity) entities.get(j);

                if (me.collidesWith(him)) {
                    me.collidedWith(him);
                    him.collidedWith(me);
                } // if
            } // inner for
        } // outer for
    } // collisionChecker
    
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
            long currentTime = System.currentTimeMillis();
            lastDamageTimeSpike = System.currentTimeMillis();

            // get graphics context for the accelerated surface and make it black
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            (SpriteStore.get()).getSprite("sprites/bng.png").draw(g, 0, 0);
            g.setColor(Color.red);
           	g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
           	
            if (!paused) {
            	
            	if (!timerRunning) {
        	        timerRunning = true;
        	        if (pauseStartTime > 0) {
        	            totalPausedTime += (System.currentTimeMillis() - pauseStartTime);
        	            pauseStartTime = 0;
        	        }
        	    }
        	 
            	long currentTimer = System.currentTimeMillis();
    		 	long elapsedTime = timerRunning ? currentTimer - gameStartTime - totalPausedTime : gameEndTime - gameStartTime;

           	 	long hours = elapsedTime / 3600000;
           	 	long minutes = (elapsedTime % 3600000) / 60000;
           	  	long seconds = (elapsedTime % 60000) / 1000;
           	  	long milliseconds = elapsedTime % 1000;
           	  
           	  	message = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
           	  	
           	  	// move each entity
                if (!waitingForKeyPress) {
                    for (int i = 0; i < entities.size(); i++) {
                        Entity entity = (Entity) entities.get(i);
                        entity.move(delta);
                    } // for
                } // if

                checkCloseToBorder();

                // draw all entities
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    entity.draw(g, xLvlOffset);
                } // for

                for (Platform platform : platforms) platform.draw(g, xLvlOffset);
                for (Spike spike : spikes) spike.draw(g, xLvlOffset);

                collisionChecker();

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
                    g.drawString(message, (GAME_HEIGHT - g.getFontMetrics().stringWidth(message)) / 2, 285);
                    g.drawString("Press any key", (GAME_HEIGHT - g.getFontMetrics().stringWidth("Press any key")) / 2, 300);
                } // if
                
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Time: " + message, GAME_WIDTH / 2 - 150, 30);

                // clear graphics and flip buffer
                g.dispose();
                strategy.show();

                // ship should not move without user input
                player.setHorizontalMovement(0);
                
                
                if (shifted && !shifting) {


                    // only allow shift if enough time passed since last one
                    if (currentTime - lastShiftTime >= SHIFT_DELAY) {
                        shiftStartTime = currentTime;
                        shifting = true;


                    }
                }

                if (shifting) {

                    long elapsed = currentTime - shiftStartTime;
                    if (elapsed >= SHIFT_DURATION && shifted) {
                        // toggle map
                        if (currentMap % 2 == 0) {
                            currentMap++;
                        } else {
                            currentMap--;
                        } // else
                        loadMap(currentMap);
                        shifted = false;
                    } if (elapsed >= SHIFT_DURATION * 2) {
                        shifting = false;
                        lastShiftTime = currentTime;
                    }
                }

                // respond to user moving ship
                if ((leftPressed) && (!rightPressed)) {
                    player.setHorizontalMovement(-300);
                } else if ((rightPressed) && (!leftPressed)) {
                    player.setHorizontalMovement(300);
                } else {
                    player.setHorizontalMovement(0);
                }

                if (upPressed) {
                    player.jump();
                }

                // if spacebar pressed, try to fire
                if (firePressed && player.isOnGround()) {
                    tryToFire();
                } // if
                
             // FALL DAMAGE CHECK
                if (prevOnGround && !player.isOnGround()) {
                	
                    // Player has JUST landed this frame
                    double impactSpeed = Math.abs(player.dy);
                    System.out.println("Impact Speed " + impactSpeed);
                    if (impactSpeed > FALL_DAMAGE_THRESHOLD) {
                        int damage = (int)((impactSpeed - FALL_DAMAGE_THRESHOLD) * 2);
                        changeHealth(damage);
                        System.out.println("dmg " + damage);
                        System.out.println("current health " + currentHealth);
                    } // if 

                    // Reset vertical velocity on landing
                    player.dy = 0;
                } // if

                // Update previous ground state
                prevOnGround = player.isOnGround();

                // check if player falls out of bounds, if true kill them
                if (player.y > GAME_HEIGHT - 50) {
                   	changeHealth(-100);
                } // if
                
             // Spike Collision 
                for (Spike spike : spikes) {
                	if (player.getX() < spike.x + spike.width &&
                		    player.getX() + player.sprite.getWidth() > spike.x &&
                		    player.getY() < spike.y + spike.height &&
                		    player.getY() + player.sprite.getHeight() > spike.y) {
                		    
                		   	
                		} // if spike collide with player
                } // for
                
                // Example: check if player reaches the right edge of the map
                if (checkLevelEnd()) {
                    goToNextLevel();
                }

                // pause
                try {
                    Thread.sleep(10);
                } catch (Exception e) {}
            } else {
            	if (timerRunning) {
                    timerRunning = false;
                    pauseStartTime = System.currentTimeMillis();
                }
            		
	          	  AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	          	  g.setComposite(alphaComposite);
	          	  g.setColor(new Color(0,0,0, 255));
	          	  g.fillRect(0,0,GAME_WIDTH,GAME_HEIGHT);
	          	  g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	
	          	
	          			  
	          	  for (int i = 0; i < entities.size(); i++) {
	   	               Entity entity = (Entity) entities.get(i);
	   	               entity.draw(g, xLvlOffset);
	   	          } // for
	   	        
	   	         for (Platform platform : platforms) platform.draw(g, xLvlOffset);
	   	         
	         	  (SpriteStore.get()).getSprite("sprites/pause.png").draw(g, GAME_WIDTH / 2 - 200, 100);
	         	 (SpriteStore.get()).getSprite("sprites/menu_background.png").draw(g, GAME_WIDTH / 2 - 211, 220);
	         	  
	         	 for (Button b : buttons) {
	         		 if (b.getButtonType() == "sprites/playButton.png" || b.getButtonType() == "sprites/quitButton.png") {
	         			 continue;
	         		 } // if
	         		 b.draw(g);
	         	 } // for
	         	   	          
	          	  g.dispose();
	          	  strategy.show();
            } // else
        } // while
        
        while (!gameRunning && playing) {
      	  Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            (SpriteStore.get()).getSprite("sprites/game_completed.png").draw(g, GAME_WIDTH / 2 - 250, GAME_HEIGHT / 2 - 250);
          
            Button exitButton2 = new Button("sprites/backButton.png", "sprites/backButtonHover.png", GAME_WIDTH / 2 + 50, 500);
            exitButton2.draw(g);
            
            
            
            g.dispose();
        	  strategy.show();
        } // while

    } // gameLoop

    // change health if player falls out of bounds or gets hit
    private void changeHealth(int value) {

		if (value < 0) {
			currentHealth += value;
			currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
			updateHealthBar();
			if (currentHealth <= 0) notifyDeath();
		} // if
	} // changeHealth

    // add delay in taking spike damage
    public void takeSpikeDamage(int value) {
        long now = System.currentTimeMillis();
        if (now - lastDamageTimeSpike < damageCooldownSpike) return;
        lastDamageTimeSpike = now;

        changeHealth(value);
    } // takeSpikeDamage
    

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
        currentMap = 0;
        leftPressed = false;
        rightPressed = false;
        firePressed = false;
        upPressed = false;
        paused = false;
        currentHealth = maxHealth;
        updateHealthBar();
        
        gameStartTime = System.currentTimeMillis();
        timerRunning = true;
        
    } // startGame


    /* inner class KeyInputHandler
     * handles keyboard input from the user
     */
    private class KeyInputHandler extends KeyAdapter {

        private int pressCount = 1; // the number of key presses since
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

            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            	if(!shifting) {
                shifted = !shifted;
            	}
            }

            if (shifted == false) {

                // respond to move left, right or fire
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = true;
                } // if

                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = true;
                } // if

                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                    upPressed = true;
                } // if

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    firePressed = true;
                } // if
            }

            if (e.getKeyCode() == KeyEvent.VK_P) {
                if (!paused) paused = true;
                else paused = false;
            } // if

        } // keyPressed

        public void keyReleased(KeyEvent e) {
            // if waiting for keypress to start game, do nothing
            if (waitingForKeyPress) {
                return;
            } // if

            // respond to move left, right or fire
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = false;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = false;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                upPressed = false;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                firePressed = false;
            } // if




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

    private class MouseInputHandler implements MouseListener, MouseMotionListener {
   	 
     	public void mouseClicked(MouseEvent e) {
     		for (Button mb : buttons) {
					if (isIn(e, mb)) {
						if(mb.getButtonType().equals("sprites/playButton.png")) {
							playing = true;
							gameRunning = true;
							startGame();
							break;
						} else if(mb.getButtonType().equals("sprites/quitButton.png")) {
							System.exit(0);
						} else if (mb.getButtonType().equals("sprites/backButton.png")) {
							resetGame();
							break;
						} else if (mb.getButtonType().equals("sprites/continueButton.png")) {
							paused = false;
						}  else if (mb.getButtonType().equals("sprites/retryButton.png")) {
							paused = false;
						} // else if
						
					} // if
				} // for
     		
     	} // mouseClicked

			@Override
			public void mouseDragged(MouseEvent e) {} // mouseDragged

			@Override
			public void mouseMoved(MouseEvent e) {
				for (Button b : buttons) {
					if (isIn(e, b)) {
						b.setMouseOver(true);
					} else {
						b.setMouseOver(false);
					} // else
				} // for
			} // mouseMoved

			@Override
			public void mousePressed(MouseEvent e) {} // mousePressed

			@Override
			public void mouseReleased(MouseEvent e) {} // mouseReleased

			@Override
			public void mouseEntered(MouseEvent e) {} // mouseEntered

			@Override
			public void mouseExited(MouseEvent e) {} // mouseExited
     	 
     	 
      } // MouseInputs
      
      public boolean isIn(MouseEvent e, Button mb) {
  		return mb.getBounds().contains(e.getX(), e.getY());
  	 } // isIn




    /**
     * Main Program
     */
    public static void main(String[] args) {
        // instantiate this object
        new Game();
    } // main


} // Game