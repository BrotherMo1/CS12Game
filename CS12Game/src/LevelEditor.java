import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;


public class LevelEditor extends Canvas {
	
  	private BufferStrategy strategy;   // take advantage of accelerated graphics
	final int tileSize = 40;
	final int rows = 20;
	final int cols = 32;
	final int margin = 100;
	final int SCREEN_HEIGHT = tileSize * cols + margin;
	final int SCREEN_WIDTH = tileSize * rows;
	
	
	public LevelEditor() {
		JFrame container = new JFrame("Space Invaders");
	
		JPanel panel = (JPanel) container.getContentPane();
		
		panel.setPreferredSize(new Dimension(SCREEN_HEIGHT,SCREEN_WIDTH));
		panel.setLayout(null);
		
		// set up canvas size (this) and add to frame
		setBounds(0,0,SCREEN_HEIGHT,SCREEN_WIDTH);
		panel.add(this);
		
		setIgnoreRepaint(true);
		
		container.pack();
		container.setResizable(false);
		container.setVisible(true);

		
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
		
		
	} // LevelEditor 
	
	private class KeyInputHandler extends KeyAdapter {
        
        private int pressCount = 1;  // the number of key presses since
                                     // waiting for 'any' key press

        private void pressed(KeyEvent e) {
        	        	
        } // pressed
        

	} // class KeyInputHandler
	
	
	public static void main(String [] args) {
        // instantiate this object
		new LevelEditor();
	} // main
	
} // class