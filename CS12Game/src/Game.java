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
	private boolean waitingForKeyPress = false; // true if game held up until
	// a key is pressed
	private boolean leftPressed = false; // true if left arrow key currently pressed
	private boolean rightPressed = false; // true if right arrow key currently pressed
	private boolean upPressed = false;
	private boolean paused = false;

	public final static int TILES_DEFAULT_SIZE = 32;
	public static final float SCALE = 1.75f;
	public final static int TILES_IN_WIDTH = 26;
	public final static int TILES_IN_HEIGHT = 14;
	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;

	private int xLvlOffset;
	private int leftBorder = (int) (0.2 * GAME_WIDTH);
	private int rightBorder = (int) (0.8 * GAME_WIDTH);
	private int lvlTilesWide = 0;
	private int maxTilesOffset = lvlTilesWide - TILES_IN_WIDTH;
	private int maxLvlOffsetX = maxTilesOffset * TILES_SIZE;

	private boolean gameRunning = false;
	private ArrayList<Entity> entities = new ArrayList<Entity>(); // list of entities
	// in game
	private ArrayList<Entity> removeEntities = new ArrayList<Entity>(); // list of entities
	// to remove this loop
	private Entity player; // the ship
	private long lastShiftTime = 0;
	private final long SHIFT_DELAY = 0; // ms
	private boolean shifting = false;
	private long shiftStartTime = 0;
	private static final long SHIFT_DURATION = 1000; // 1 second per phase
	private boolean shiftedState = false;

	private String message = ""; // message to display while waiting
	// for a key press

	private boolean logicRequiredThisLoop = false; // true if logic
	// needs to be
	// applied this loop

	private boolean shifted = false;

	private Portal portal;
	private ArrayList<Platform> platforms = new ArrayList<>();
	private ArrayList<Spike> spikes = new ArrayList<>();
	private ArrayList<int[][]> maps = new ArrayList<>();
	private int currentMap = 0;
	private boolean playing = false;

	private MouseInputHandler mouseInputs = new MouseInputHandler();

	private ArrayList<Button> buttons = new ArrayList<>();

	private Button backButton;
	private Button retryButton;
	private Button continueButton;
	private Button continueButton2;
	private Button backButton2;

	private long gameStartTime = 0;
	private long gameEndTime = 0;
	private boolean timerRunning = false;

	private long pauseStartTime = 0;
	private long totalPausedTime = 0;

	protected int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healthBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);

	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private boolean dead = false;

	private boolean showGameTutorial = false;
	private boolean tutorialShowed = false;

	/*
	 * Construct our game and set it running.
	 */
	public Game() {
		// create a frame to contain game
		JFrame container = new JFrame("Dimension Drift");

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
		container.setLocationRelativeTo(null);
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
		retryButton = new Button("sprites/retryButton.png", "sprites/retryButtonHover.png", GAME_WIDTH / 2 - 50, 410);
		continueButton = new Button("sprites/continueButton.png", "sprites/continueButtonHover.png",
				GAME_WIDTH / 2 - 50, 275);
		continueButton2 = new Button("sprites/continueButton2.png", "sprites/continueButtonHover2.png",
				GAME_WIDTH / 2 + 60, 420);
		backButton2 = new Button("sprites/backButton2.png", "sprites/backButtonHover2.png", GAME_WIDTH / 2 - 160, 420);

		buttons.add(backButton);
		buttons.add(retryButton);
		buttons.add(continueButton);
		buttons.add(continueButton2);
		buttons.add(backButton2);
		buttons.add(startButton);
		buttons.add(quitButton);

		while (true) {
			startMenu();

			if (playing) {

				initMaps();
				loadMap(0);

				if (!tutorialShowed) {
					showTutorial();
				} // if
				startGame();

				// start the game
				gameLoop();
			} // if
		} // while

	} // constructor

	private void showTutorial() {
		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
		(SpriteStore.get()).getSprite("sprites/tutorialMenu.png").draw(g, 0, 0);

		strategy.show();

		if (!tutorialShowed) {
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			} // catch
			gameStartTime = System.currentTimeMillis();
			gameRunning = true;
			tutorialShowed = true;
		} // if
	} // showTutorial

	// create startMenu
	private void startMenu() {

		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

		(SpriteStore.get()).getSprite("sprites/bg.png").draw(g, 0, 0);
		(SpriteStore.get()).getSprite("sprites/menu_background.png").draw(g, 5, 250);

		for (Button b : buttons) {
			if (b.getButtonType() == "sprites/continueButton.png" || b.getButtonType() == "sprites/retryButton.png"
					|| b.getButtonType() == "sprites/backButton.png" || b.getButtonType() == "sprites/backButton2.png"
					|| b.getButtonType() == "sprites/continueButton2.png") {
				continue;
			} // if
			b.draw(g);
		} // for

		strategy.show();

	} // startMenu

	/*
	 * initEntities input: none output: none purpose: Initialise the starting state
	 * of the ship and alien entities. Each entity will be added to the array of
	 * entities in the game.
	 */
	private void initEntities() {

		// create the ship and put in center of screen
		player = new Player(this, "sprites/animations/idle/idle1.png", 70, 550);
		entities.add(player);
	} // initEntities

	public boolean isShifting() {
		return shifting;
	} // isShifting

	private int getMapWidth(int index) {
		if (index < 0 || index >= maps.size()) {
			return 0;
		} // if
		return maps.get(index)[0].length;
	} // getMapWidth

	private void loadMap(int index) {
		if (index < 0 || index >= maps.size()) {
			return;
		} // if
		platforms = makePlatforms(maps.get(index));
		spikes = makeSpikes(maps.get(index));
		portal = null;
		portal = makePortal(maps.get(index));
	} // loadMap

	private void checkCloseToBorder() {
		int playerX = player.getX();
		int diff = playerX - xLvlOffset;

		if (diff > rightBorder) {
			xLvlOffset += diff - rightBorder;
		} else if (diff < leftBorder) {
			xLvlOffset += diff - leftBorder;
		} // else if

		if (xLvlOffset > maxLvlOffsetX) {
			xLvlOffset = maxLvlOffsetX;
		} else if (xLvlOffset < 0) {
			xLvlOffset = 0;
		} // else if
	} // checkCloseToBorder

	// initialize maps
	private void initMaps() {

		// java 1D version
		int[][] lvl1 = { { 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 49, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 2, 2, 2 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 3, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14, 14 },
				{ 13, 37, 38, 38, 38, 38, 39, 0, 0, 0, 0, 0, 0, 13, 14, 14, 14, 14, 15, 0, 0, 0, 16, 0, 0, 0, 25, 26,26, 26 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 26, 26, 26, 26, 27, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] shift1 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 2, 2, 2 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 2, 2, 2, 2, 3, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14, 14 },
				{ 13, 37, 38, 38, 38, 38, 39, 0, 0, 16, 0, 0, 0, 13, 14, 14, 14, 14, 15, 0, 0, 0, 16, 0, 0, 0, 25, 26,26, 26 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 25, 26, 26, 26, 26, 27, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] lvl2 = { { 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 2, 2 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 40, 0, 0, 40, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 4, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 49, 49, 49, 0, 0, 0, 0, 49, 49, 49, 0, 0, 0, 13, 14, 14 },
				{ 13, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 13, 14, 14 },
				{ 13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 0, 13, 14, 14 } };

		// java 1D version
		int[][] shift2 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 1, 2, 2 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 40, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 28, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 0, 4, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 0, 13, 14, 14 },
				{ 13, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 13, 14, 14 },
				{ 13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 0, 13, 14, 14 } };

		// java 1D version
		int[][] lvl3 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 100, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 49, 49, 0, 0, 0, 37, 38, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] shift3 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 37, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] lvl4 = {
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 4, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 37, 16, 38, 38, 38, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 39 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 40, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 38, 38, 38, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 37, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 4, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] shift4 = {
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 4, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100,
						0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0 },
				{ 13, 0, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						37, 38, 39 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 38, 38, 38, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 40, 0,
						0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0 },
				{ 13, 0, 0, 0, 0, 16, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 4, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0 },
				{ 13, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0 } };

		// java 1D version
		int[][] lvl5 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 40, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 37, 38, 39 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0,
						0, 40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38,
						38, 39, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] shift5 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 37, 38, 39 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0,
						0, 40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0,
						0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38,
						38, 39, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0 } };

		// java 1D version
		int[][] lvl6 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 16, 0, 0, 0, 0, 0, 0, 0, 37, 39, 0, 40, 0, 37, 39,
						0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 40, 0, 0, 16, 0, 0, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 37, 38, 39, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 40, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 4, 16, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0,
						0, 37, 38, 38, 39, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39,
						0, 0, 0, 0, 0, 0, 0, 37, 38, 38 } };

		// java 1D version
		int[][] shift6 = {
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 40, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 16, 0, 0, 0, 0, 0, 0, 0, 37, 39, 0, 40, 0, 37, 39,
						0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 37, 38, 39, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 39, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 16, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 40, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 4, 16, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0,
						0, 37, 38, 38, 39, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39,
						0, 0, 0, 0, 0, 0, 0, 37, 38, 38 } };

		// java 1D version
		int[][] lvl7 = {
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 4, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 49, 49,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 38,
						39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 16, 0, 0, 40, 0, 16, 0, 0, 40, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0,
						0, 0, 0, 0, 0, 0, 40, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 16, 0, 0, 0, 37, 16, 0, 40, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 16, 0, 0, 40, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0,
						0, 40, 0, 0, 0, 0, 0, 0, 37, 38 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 40, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 16, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						37, 38, 39, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 39, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 39, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
		// java 1D version
		int[][] shift7 = {
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 4, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 39, 0, 0, 0, 0, 0, 49, 49,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 38, 38,
						39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 16, 0, 0, 0, 37, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 16, 0, 0, 0, 0, 16, 0, 0, 40, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 40, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 16, 39, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0,
						0, 0, 0, 0, 0, 0, 40, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 16, 0, 0, 0, 37, 16, 0, 40, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 37, 16, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0, 0, 0, 0, 0,
						0, 40, 0, 0, 0, 0, 0, 0, 37, 38 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0, 0, 0, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 16, 39, 0, 0, 0, 0, 0, 0, 37, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 40, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 4, 39, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37,
						38, 39, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 37, 38, 39, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 13, 37, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 39, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

		int[][][] lvls = { lvl1, lvl2, lvl3, lvl4, lvl5, lvl6, lvl7 };
		int[][][] shifts = { shift1, shift2, shift3, shift4, shift5, shift6, shift7 };

		// add them in order
		for (int i = 0; i < lvls.length; i++) {
			maps.add(lvls[i]);
			maps.add(shifts[i]);
		} // for

	} // initMaps

	private ArrayList<Platform> makePlatforms(int[][] map) {
		ArrayList<Platform> platforms = new ArrayList<>();
		spikes.clear(); // Clear existing spikes

		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				int tileValue = map[row][col];

				if (tileValue >= 1 && tileValue <= 48) {
					platforms.add(new Platform(col * TILES_SIZE, row * TILES_SIZE, TILES_SIZE, TILES_SIZE, tileValue));
				} // if
			} // for
		} // for
		return platforms;
	} // makePlatforms

	private ArrayList<Spike> makeSpikes(int[][] map) {
		ArrayList<Spike> spikes = new ArrayList<>();
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				if (map[row][col] == 49) {
					spikes.add(new Spike(col * TILES_SIZE, row * TILES_SIZE, TILES_SIZE, TILES_SIZE));
				} // if spikes
			} // for
		} // for
		return spikes;
	} // makeSpikes

	
	private Portal makePortal(int[][]map) {
		
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[row].length; col++) {
				if (map[row][col] == 100) {
					portal = new Portal(col * TILES_SIZE, row * TILES_SIZE);
					return portal;
				} // if
			} // for
		} // for
		return new Portal(-100, -100);
	}

	// goes to the next level
	private void goToNextLevel() {
		if (shiftedState)
			currentMap++;
		else if (!shiftedState)
			currentMap += 2;
		if (currentMap >= maps.size()) {
			notifyWin();
			return;
		}

		// Load the next map's platforms
		loadMap(currentMap);

		// Reset player position at the start of the new map
		player.x = 80; // starting X position
		player.y = 300; // starting Y, adjust if needed
		player.setHorizontalMovement(0);
		player.setVerticalMovement(0);

		// Reset level scrolling
		xLvlOffset = 0;

		if (shiftedState)
			shiftedState = false;
	} // goToNextLevel

	public ArrayList<Platform> getPlatforms() {
		return platforms;
	} // getPlatforms

	public ArrayList<Spike> getSpikes() {
		return spikes;
	} // getSpikes


	public Portal getPortal() {
		return portal;
	}
	/*
	 * Notification from a game entity that the logic of the game should be run at
	 * the next opportunity
	 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	} // updateLogic

	/*
	 * Remove an entity from the game. It will no longer be moved or drawn.
	 */
	public void removeEntity(Entity entity) {
		removeEntities.add(entity);
	} // removeEntity

	/*
	 * Notification that the player has died.
	 */
	public void notifyDeath() {
		dead = true;
		if (timerRunning) {
			timerRunning = false;
			gameEndTime = System.currentTimeMillis();
		}
	} // notifyDeath

	/*
	 * Notification that the play has killed all aliens
	 */
	public void notifyWin() {
		gameRunning = false;
	} // notifyWin

	// resets the game
	public void resetGame() {
		playing = false;
		gameRunning = false;
		entities.clear();
		removeEntities.clear();
		leftPressed = false;
		rightPressed = false;
		upPressed = false;
		shifted = false;
		paused = false;
		dead = false;
		shiftedState = false;
		currentMap = 0;
		player.healthWidth = healthBarWidth;
		player.updateHealthBar();

	} // resetGame

	// helper method to determine if a collision occurs
	public void collisionChecker() {

		// brute force collisions, compare every entity
		// against every other entity. If any collisions
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
	 * gameLoop input: none output: none purpose: Main game loop. Runs throughout
	 * game play. Responsible for the following activities: - calculates speed of
	 * the game loop to update moves - moves the game entities - draws the screen
	 * contents (entities, text) - updates game events - checks input
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

			lvlTilesWide = getMapWidth(currentMap);
			maxTilesOffset = lvlTilesWide - TILES_IN_WIDTH;
			maxLvlOffsetX = maxTilesOffset * TILES_SIZE;

			// get graphics context for the accelerated surface and make it black
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.clearRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

			if (!shiftedState) {
				(SpriteStore.get()).getSprite("sprites/bng.png").draw(g, 0, 0);
			} else {
				(SpriteStore.get()).getSprite("sprites/bngs.png").draw(g, 0, 0);
			} // else;

			// check for dmg
			player.dmgChecker();

			if (!paused) {

				if (!timerRunning) {
					timerRunning = true;
					if (pauseStartTime > 0) {
						totalPausedTime += (System.currentTimeMillis() - pauseStartTime);
						pauseStartTime = 0;
					} // if
				} // if

				long currentTimer = System.currentTimeMillis();
				long elapsedTime;

				if (dead) {
					// When dead, use the time up to death
					elapsedTime = gameEndTime - gameStartTime - totalPausedTime;
				} else if (paused) {
					// When paused, use the time up to when pause started
					elapsedTime = pauseStartTime - gameStartTime - totalPausedTime;
				} else {
					// When running normally
					elapsedTime = currentTimer - gameStartTime - totalPausedTime;
				} // else

				long hours = elapsedTime / 3600000;
				long minutes = (elapsedTime % 3600000) / 60000;
				long seconds = (elapsedTime % 60000) / 1000;
				long milliseconds = elapsedTime % 1000;

				message = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);

				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.move(delta);
				} // for

				checkCloseToBorder();

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g, xLvlOffset);
				} // for

				for (Platform platform : platforms)
					platform.draw(g, xLvlOffset, shiftedState);
				for (Spike spike : spikes)
					spike.draw(g, xLvlOffset);

				portal.draw(g, xLvlOffset);

				
				collisionChecker();

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();

				// run logic if required
				if (logicRequiredThisLoop) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = entities.get(i);
						entity.doLogic();
					} // for
					logicRequiredThisLoop = false;
				} // if

				// draw health bar
				g.setColor(Color.red);
				g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, player.healthWidth,
						healthBarHeight);
				g.setColor(Color.white);
				String health = player.currentHealth + "%";
				g.drawString(health, player.healthWidth + 85, healthBarYStart + 25);
				g.setFont(new Font("Arial", Font.PLAIN, 24));
				g.drawString("Health:", healthBarXStart + statusBarX, healthBarYStart + 10);

				// if waiting for "any key press", draw message
				if (waitingForKeyPress) {
					g.setColor(Color.white);
					g.drawString(message, (GAME_HEIGHT - g.getFontMetrics().stringWidth(message)) / 2, 285);
					g.drawString("Press any key", (GAME_HEIGHT - g.getFontMetrics().stringWidth("Press any key")) / 2,
							300);
				} // if

				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, 30));
				g.drawString("Time: " + message, GAME_WIDTH / 2 - 150, 30);

				if (dead && player.animationDone()) {
					(SpriteStore.get()).getSprite("sprites/death_screen.png").draw(g, GAME_WIDTH / 2 - 250, 200);

					for (Button b : buttons) {
						if (b.getButtonType() == "sprites/playButton.png"
								|| b.getButtonType() == "sprites/quitButton.png"
								|| b.getButtonType() == "sprites/continueButton.png"
								|| b.getButtonType() == "sprites/backButton.png"
								|| b.getButtonType() == "sprites/retryButton.png") {
							continue;
						} // if
						b.draw(g);
					} // for
				} // if

				if (showGameTutorial) {
					showTutorial();
				}

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
					} // if
				} // if

				if (shifting) {

					long elapsed = currentTime - shiftStartTime;
					if (elapsed >= SHIFT_DURATION && shifted) {
						// toggle map
						shiftedState = !shiftedState;
						if (currentMap % 2 == 0) {
							currentMap++;
						} else {
							currentMap--;
						} // else
						loadMap(currentMap);
						shifted = false;
					}
					if (elapsed >= SHIFT_DURATION * 2) {
						shifting = false;
						lastShiftTime = currentTime;
					} // if
				} // if shifting

				if (!dead) {
					// respond to user moving ship
					if ((leftPressed) && (!rightPressed)) {
						player.setHorizontalMovement(-300);
					} else if ((rightPressed) && (!leftPressed)) {
						player.setHorizontalMovement(300);
					} else {
						player.setHorizontalMovement(0);
					} // else

					if (upPressed) {
						player.jump();
					} // if
				}

				// Example: check if player reaches the right edge of the map
				if (player.atPortal()) goToNextLevel();
				

				// pause
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
			} else {
				if (timerRunning) {
					timerRunning = false;
					pauseStartTime = System.currentTimeMillis();
				}

				AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
				g.setComposite(alphaComposite);
				g.setColor(new Color(0, 0, 0, 255));
				g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g, xLvlOffset);
				} // for

				for (Platform platform : platforms)
					platform.draw(g, xLvlOffset, shiftedState);

				(SpriteStore.get()).getSprite("sprites/pause.png").draw(g, GAME_WIDTH / 2 - 200, 100);
				(SpriteStore.get()).getSprite("sprites/menu_background.png").draw(g, GAME_WIDTH / 2 - 211, 220);

				for (Button b : buttons) {
					if (b.getButtonType() == "sprites/playButton.png" || b.getButtonType() == "sprites/quitButton.png"
							|| b.getButtonType() == "sprites/continueButton2.png"
							|| b.getButtonType() == "sprites/backButton2.png") {
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
			(SpriteStore.get()).getSprite("sprites/game_completed.png").draw(g, GAME_WIDTH / 2 - 250,
					GAME_HEIGHT / 2 - 250);

			g.dispose();
			strategy.show();

			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			} // catch

			System.exit(0);
		} // while

	} // gameLoop

	/*
	 * startGame input: none output: none purpose: start a fresh game, clear old
	 * data
	 */
	private void startGame() {
		// clear out any existing entities and initalize a new set
		entities.clear();

		initEntities();

		// blank out any keyboard settings that might exist
		currentMap = 0;
		loadMap(currentMap);
		leftPressed = false;
		rightPressed = false;
		upPressed = false;
		paused = false;
		dead = false;
		gameStartTime = System.currentTimeMillis();
		timerRunning = true;
		player.healthWidth = healthBarWidth;
		player.updateHealthBar();
		gameRunning = true;
		playing = true;

	} // startGame

	// make player retry level on failure
	private void retryLevel() {
		entities.clear();

		initEntities();

		// blank out any keyboard settings that might exist
		leftPressed = false;
		rightPressed = false;
		upPressed = false;
		paused = false;
		dead = false;
		player.healthWidth = healthBarWidth;
		player.updateHealthBar();
		if (shiftedState) {
			currentMap--;
		} // if
		loadMap(currentMap);
		shiftedState = false;
		if (!timerRunning) {
			timerRunning = true;
			// Adjust gameStartTime to account for the elapsed time so far
			long currentElapsed = gameEndTime - gameStartTime - totalPausedTime;
			gameStartTime = System.currentTimeMillis() - currentElapsed - totalPausedTime;
		} // if
	} // retryLevel

	/*
	 * inner class KeyInputHandler handles keyboard input from the user
	 */
	private class KeyInputHandler extends KeyAdapter {

		private int pressCount = 1; // the number of key presses since
		// waiting for 'any' key press

		/*
		 * The following methods are required for any class that extends the abstract
		 * class KeyAdapter. They handle keyPressed, keyReleased and keyTyped events.
		 */
		public void keyPressed(KeyEvent e) {

			// if waiting for keypress to start game, do nothing
			if (waitingForKeyPress) {
				return;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == KeyEvent.VK_S) {
				if (!shifting) {
					shifted = !shifted;
//                shiftedState = !shiftedState;
				}
			}
			
			if (e.getKeyCode() == KeyEvent.VK_0) {
				goToNextLevel();
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

				if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_H) {
					showGameTutorial = !showGameTutorial;
				} // if
			}

			if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				paused = !paused;
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

		} // keyTyped

	} // class KeyInputHandler

	private class MouseInputHandler implements MouseListener, MouseMotionListener {

		public void mouseClicked(MouseEvent e) {
			for (Button mb : buttons) {
				if (isIn(e, mb)) {
					if (mb.getButtonType().equals("sprites/playButton.png")) {
						playing = true;
						gameRunning = true;
						waitingForKeyPress = false;
						startGame();
						break;
					} else if (mb.getButtonType().equals("sprites/quitButton.png")) {
						System.exit(0);
					} else if (mb.getButtonType().equals("sprites/backButton.png")
							|| mb.getButtonType().equals("sprites/backButton2.png")) {
						resetGame();
						break;
					} else if (mb.getButtonType().equals("sprites/continueButton.png")) {
						paused = false;
					} else if (mb.getButtonType().equals("sprites/retryButton.png")
							|| mb.getButtonType().equals("sprites/continueButton2.png")) {
						retryLevel();
					} // else if
				} // if
			} // for

		} // mouseClicked

		@Override
		public void mouseDragged(MouseEvent e) {
		} // mouseDragged

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
		public void mousePressed(MouseEvent e) {
		} // mousePressed

		@Override
		public void mouseReleased(MouseEvent e) {
		} // mouseReleased

		@Override
		public void mouseEntered(MouseEvent e) {
		} // mouseEntered

		@Override
		public void mouseExited(MouseEvent e) {
		} // mouseExited

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