package testrit;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class tetrsd extends JPanel implements KeyListener, ActionListener{
	
	
	
	Random rnd = new Random();//optimize pls
	
	Piece piece = new Piece(1, 210, 0);
	Piece[] queue = new Piece[3];//block queue array; only shows 3 next blocks
	Piece hPiece = null;
	Piece gPiece;
	
	//list of types.  used for fair rng, holds one of every type and pops random type when spawning new piece
	ArrayList<Integer> tQueue = new ArrayList<>();
	
	private final Timer timer;
	
	//gameboard grid 20row x 10column with extra indices allowed to prevent ArrayIndexOtOfBounds errors checking empty columns
	int[][] board = new int[25][15];
	
	private int delay = 800;
	int level = 0;
	int score = 0;
	int linesCleared = 0;//this value only holds temporary lines cleared
	int totalLines = 0;
	int tSpinCount = 0;//count for t spin triples  ###UNIMPLEMENTED tspin triples only happen in sequence with tspin single move possible implementation is check tspin triple coords when trying to rotate if t spin count > 0
	int boardCount = 0;
	
	
	boolean starting = true;//for init purposes
	boolean fastfall = false;
	boolean upPressed = false;
	boolean leftPressed = false;
	boolean rightPressed = false;
	public boolean gameOver = false;
	boolean canHold = true;
	boolean allClearPoss = false;

	//import our block images;  each must be 42px x 42px if changing graphics game size is not scalable
	ImageIcon lBlue = new ImageIcon("blue.jpg");
	ImageIcon blue = new ImageIcon("dBlue.jpg");
	ImageIcon red = new ImageIcon("red.jpg");
	ImageIcon orange = new ImageIcon("orange.jpg");
	ImageIcon yellow = new ImageIcon("yellow.jpg");
	ImageIcon purp = new ImageIcon("purple.jpg");
	ImageIcon green = new ImageIcon("green.jpg");
	
	//Initializes game
	public tetrsd() {
		System.out.println("should be printing");
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		timer = new Timer(delay, this);
		timer.start();
	}
	//TODO implement more fair rng when spawning pieces
	public void paint(Graphics g) {
		requestFocus(true);
		
		//draw all the static graphics and scoreboards
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, 420, 840);
		g.setColor(Color.gray);
		g.fillRect(420, 0, 650, 840);
		g.setColor(Color.black);
		g.drawString("Score: " + score, 520, 12);//scoreboard
		g.drawString("Level: " + level, 450, 12);
		g.drawString("Lines: " + totalLines, 450, 25);
		g.setColor(Color.black);
		g.drawRoundRect(440, 30, 176, 176, 5, 5);

		//logarithmically speeds up gravity so it's contingent on level, but also not insanely fast at higher levels
		if(fastfall)
			timer.setDelay((int)(29 * Math.log(delay)));
		else
			timer.setDelay(delay);

		if (allClearPoss){
			if(boardCount == 0){
				score += 512;
				allClearPoss = false;
			}
		}

		if(!piece.isFalling){
			allClearPoss = true;
		}

		if(starting) {//initialize game
			piece = new Piece(returnType(), 210, 0);//first piece
			
			gPiece = new Piece(piece.t, 210, 840);
			gPiece.isFalling = false;
			gPiece.y = 840;
			if(piece.t == 0) {
				piece.rotate(true);
				gPiece.rotate(true);
			}
			for(int i = 0; i < 3; i++) {
				queue[i] = (new Piece(returnType(), 490, 225 + i*200));//initialize first queue pieces
				queue[i].isFalling = false;//make sure they don't have gravity otherwise this ends the next conditional early
			}
			starting = false;
		}
			
		if(!piece.isFalling) {
			System.out.println("Adding blocks to board");
			for(int r = 0; r < 4; r++) {
				for(int c = 0; c < 4; c++) {
					if(piece.cube[c][r] > 0) {
						board[(piece.y + (42*r))/42][(piece.x + (42*c))/42] = piece.t + 1;//set board coords to piece color
					}
				}
			}
			queuePiece();
			canHold = true;
			checkLines();			
			piece.isFalling = true;
		}
		
		//paint current block
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(piece.cube[c][r] > 0) {
					switch (piece.t) {
						case 0 -> lBlue.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 1 -> orange.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 2 -> blue.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 3 -> green.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 4 -> purp.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 5 -> red.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
						case 6 -> yellow.paintIcon(this, g, piece.x + (42 * c), piece.y + (42 * r));
					}
				}
			}
		}
		
		paintGhost(g);
		
		//paint hold block
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(hPiece != null && hPiece.cube[c][r] > 0) {
					//176x176 fitting 168x168
					switch (hPiece.t) {
						case 0 -> lBlue.paintIcon(this, g, 465 + (42 * c), 34 + (42 * r));
						case 1 -> orange.paintIcon(this, g, 486 + (42 * c), 55 + (42 * r));
						case 2 -> blue.paintIcon(this, g, 486 + (42 * c), 55 + (42 * r));
						case 3 -> green.paintIcon(this, g, 486 + (42 * c), 55 + (42 * r));
						case 4 -> purp.paintIcon(this, g, 486 + (42 * c), 55 + (42 * r));
						case 5 -> red.paintIcon(this, g, 486 + (42 * c), 55 + (42 * r));
						case 6 -> yellow.paintIcon(this, g, 486 + (42 * c), 76 + (42 * r));
					}
				}
			}
		}
		
		//paint old blocks

		for(int r = 0; r < 20; r++) {
			for(int c = 0; c < 10; c++) {
				try {

					switch(board[r][c]) {
						case 1 -> lBlue.paintIcon(this, g, (42*c), (42*r));
						case 2 -> orange.paintIcon(this, g, (42*c), (42*r));
						case 3 -> blue.paintIcon(this, g, (42*c), (42*r));
						case 4 -> green.paintIcon(this, g, (42*c), (42*r));
						case 5 -> purp.paintIcon(this, g, (42*c), (42*r));
						case 6 -> red.paintIcon(this, g, (42*c), (42*r));
						case 7 -> yellow.paintIcon(this, g, (42*c), (42*r));
					}
				}catch(NullPointerException e) {
					return;
				}
			}
		}
		
		//paint queue blocks
		for(Piece p : queue) {
			for(int r = 0; r < 4; r++) {
				for(int c = 0; c < 4; c++) {
					if(p.cube[c][r] > 0) {
						switch (p.t) {
							case 0 -> lBlue.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 1 -> orange.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 2 -> blue.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 3 -> green.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 4 -> purp.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 5 -> red.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
							case 6 -> yellow.paintIcon(this, g, p.x + (42 * c), p.y + (42 * r));
						}
					}
				}
			}
		}
		
		
		g.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		timer.start();//commence gameplay based on timer set
		if(gameOver)
			timer.stop();//stop gameplay when game's over
		//implement 9 speed levels
		if(level < 9)
			delay = (int) (((48-(5*(float)level))/60)*1000);
		else
			delay = (int)(((float)2/level)*500);
		timer.setDelay(delay);	

		if(canFall()) {//otherwise start dropping em
			piece.Gravity();
			System.out.println("X: " + piece.x/42 + " Y: " + piece.y/42);
		}
		repaint();	//animate by redrawing everything with updated coordinates on main piece
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			fastfall = false;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			upPressed = false;
		if(e.getKeyCode() == KeyEvent.VK_A)
			leftPressed = false;
		if(e.getKeyCode() == KeyEvent.VK_D)
			rightPressed = false;
	}
	
	//TODO make rotations only happen once per button press
	@Override
	public void keyPressed(KeyEvent e) {
		//move right/left.  !gameOver so we can't control anything when we lose
		if(e.getKeyCode() == KeyEvent.VK_RIGHT && canRight() && !gameOver) {
			piece.x += 42;		
			gPiece.x += 42;
			this.repaint();
		}
		if(e.getKeyCode() == KeyEvent.VK_LEFT && canLeft() && !gameOver) {
			piece.x -= 42;
			gPiece.x -= 42;
			this.repaint();
		}
		
		//rotate when keypressed; boolean for clockwise/anticlockwise
		if(e.getKeyCode() == KeyEvent.VK_A && !leftPressed && !gameOver && canRotateLeft()) {
			piece.rotate(true);
			gPiece.rotate(true);
			leftPressed = true;
			repaint();
		}
		if(e.getKeyCode() == KeyEvent.VK_D && !rightPressed && !gameOver && canRotateRight()) {//ditto
			piece.rotate(false);
			gPiece.rotate(false);
			rightPressed = true;
			repaint();
		}

		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			System.exit(0);
			return;
		}

		//controls for holding piece
		if(e.getKeyCode() == KeyEvent.VK_W && canHold && !gameOver) {
			holdPiece();
			canHold = false;
		}
		
		//hard drop. our ghost piece method already did all the work
		if(e.getKeyCode() == KeyEvent.VK_UP && !gameOver && !upPressed) {

			piece = gPiece;
			piece.isFalling = false;
			boardCount+=4;
			upPressed = true;
			repaint();
		}

		//soft drop.
		//TODO figure out why there's a delay
		if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			fastfall = true;
			repaint();
		}
		
	}
	
	//Checks to see if there are blocks below or if there is even gameboard below
	private boolean canFall() {
		for(int r = 0; r < 4; r++) {         //cycle through tetrimino matrix 
			for(int c = 0; c < 4; c++) {     
				try{if(piece.cube[c][r] > 0) {   //to find active blocks forming tetrimino
					if(board[(piece.y + (42*r) + 42)/42][(piece.x + (42*c))/42] > 0) {//if gameboard matrix at block one below tetrimino exists; converts coordinates to 20x10 grid
						if(piece.y == 0) { //can't move cuz block below and also we're at top
							gameOver = true;
						}//otherwise stop the gravity and say no canFall
						piece.isFalling = false;
						boardCount+=4;
						return false;
					}
					//checks if we're at bottom of board;  no idea why 17 and not 20, but don't fix what isn't broken
					if((piece.y + (42*r) + 42)/42 > 19) {
						piece.isFalling = false;
						boardCount+=4;
						return false;    //why on earth does this work
					}

				}}catch(ArrayIndexOutOfBoundsException e){
					piece.isFalling = false;
					repaint();
				}
			}
		}
		return true;
	}

	//iterates through individual blocks of tetrimino matrix and compares blocks directly right of them.  ensures collision is accurate to block shape
	private boolean canRight() {
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(piece.cube[c][r] > 0) {
					if(board[(piece.y + (42*r))/42][(piece.x + (42*c) + 42)/42] > 0) {
						return false;
					}
					if((piece.x + (42*c))/42 > 8) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	//iterates through individual blocks of tetrimino matrix and compares blocks directly left of them.  ensures collision is accurate to block shape
	private boolean canLeft() {
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(piece.cube[c][r] > 0) {
					if(c == 0 && piece.x <= 0) {
						return false;
					}
					if(board[(piece.y + (42*r))/42][(piece.x + (42*c) - 42)/42] > 0) {
						return false;
					}
					if((piece.x + (42*c)-42)/42 < 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	//use invisible piece to check potential moves.  function is same as canRotateRight check comments there for any confusion
	/*private boolean canRotateLeft() {
		//sometimes overlaps other blocks when rotating from bottom to right side but why?
		Piece temp = new Piece(piece.t, piece.x, piece.y);
		temp.rotate(false);
		int tX = temp.x;
		int tY = temp.y;
		System.out.println("for Reference Piece Type: " + piece.t + "  X: " + (piece.x / 42) + " Y: " + (piece.y / 42) + " R: " + piece.rotation);
		System.out.println("for Reference Temp Type: " + temp.t + "  X: " + (temp.x / 42) + " Y: " + (temp.y / 42) + " R: " + temp.rotation);
		if(isColliding(temp, true)) {
			if(temp.t != 0 & temp.t != 6) {
				//126 used for t-spin triple testing.  normal circumstances will return between -42 and 42/84
				//for(int x = temp.x + 42; x > tX - 168; x -= 42) {
				//	temp.x=x;
				for(int x = 0; x < 84; x+= 42){
					temp.x = temp.x + x;
					//checks alternating lower and higher spots
					for(int y = 0; y < 126; y+=42) {
						temp.y = temp.y + y;

						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}

						temp.y = temp.y - 2*y;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							return true;
						}
					}
					temp.y = tY;

					temp.x = temp.x - 2*x;
					for(int y = 0; y < 126; y+=42) {
						temp.y = temp.y + y;

						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}

						temp.y = temp.y - 2*y;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							return true;
						}
					}
					temp.y = tY;
					temp.x = tX;
				}
			}else {
				//I spinning is weird because it's 4x4 instead of 3x3
				if(piece.rotation != 2) {
					for(int x = temp.x - 84; x < tX + 84; x += 42) {
						temp.x=x;
						if(!isColliding(temp, false)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							System.out.println("Also why here?");
							return true;
						}
					}
				}else {
					//I piece in col3 makes x position 2 blocks left of visible piece, so we gotta potentially move the piece when it is on a wall or flush with another piece
					for(int x = temp.x - 42; x < tX + 126; x += 42) {
						temp.x=x;
						if(!isColliding(temp, false)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							System.out.println("def shouldn't be here");
							return true;
						}
					}
				}
			}
			return false;
		}
		System.out.println(piece.rotation);
		if(piece.rotation == 3){
			System.out.println("checking gravity now");
			temp.Gravity();
			if(isColliding(temp, true))
				return false;

		}

		if(tSpinCount != 0){
			piece.x = temp.x + 42;
			piece.y = temp.y + 84;
		}
		//I blocks always giving spacial troubles; checks the extra dimension
		if(temp.t == 0){
			temp.y -=42;
			if(isColliding(temp, true))
				return false;
		}
		System.out.println("can rotate left by default");
		return true;
	}*/
	
	//TODO implement J/L spins and S/Z spins, T-Spin triples
	//use invisible piece to check potential rotation.  helpful because invisible means we only see pre-existing pieces
	private boolean canRotateRight() {
		Piece temp = new Piece(piece.t, piece.x, piece.y);
		temp.rotate(false);
		int tX = temp.x;
		int tY = temp.y;
		System.out.println("for Reference Piece Type: " + piece.t + "  X: " + (piece.x / 42) + " Y: " + (piece.y / 42) + " R: " + piece.rotation);
		System.out.println("for Reference Temp Type: " + temp.t + "  X: " + (temp.x / 42) + " Y: " + (temp.y / 42) + " R: " + temp.rotation);
		if(isColliding(temp, false)) {
			//System.out.println("we can;t rotate in place");
			if(temp.t != 0) {
				//checks to see if we can be one block to side of where we are when rotated.  for when rotation should be possible but we're close to edge
				/*for(int x = temp.x + 42; x > tX - 84; x -= 42) {
					temp.x=x;
					//checks alternating lower and higher spots
					for(int y = 0; y < 84; y+=42) {
						temp.y = temp.y + y;
						System.out.println("Checking x coordinates at X: " + (x / 42) + " Y: " + (temp.y / 42));
						if(!isColliding(temp, true)) {
							if(piece.t == 3 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 5)
								temp.y += 42;
							System.out.println("t spin piece x: " + (temp.x / 42) + " temp y: " + (temp.y / 42));
							//System.out.println("X: " + (temp.x / 42));

							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}

						temp.y = temp.y - 2*y;
						System.out.println("Checking x coordinates at X: " + (x / 42) + " Y: " + (temp.y / 42));
						if(!isColliding(temp, true)) {
							System.out.println("t spin piece y: " + (piece.y / 42) + " temp y: " + (temp.y / 42));
							System.out.println("X: " + (temp.x / 42));
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							return true;
						}
					}
					temp.y = tY;
				}*/
				for(int y = 0; y <84; y+=42){
					temp.y = tY + y;
					for(int x = 0; x < 84; x+= 42){
						temp.x = tX + x;
						if(!isColliding(temp, true)) {


							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("problem");


							return true;
						}
						temp.x = tX - x;
						if(!isColliding(temp, true)) {
							/*if(temp.t == 3 && temp.y > 17*42){
								temp.y -= 42;
							}*/
							//if(piece.t == 3)
							//	temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("this problem");
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}
						//temp.x = tX;
					}
					temp.y = tY - y;
					for(int x = 0; x < 84; x+= 42){
						temp.x = tX + x;
						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("problem");
							return true;
						}
						temp.x = tX - x;
						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("this problem");
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}
						//temp.x = tX;
					}
					//temp.y = tY;
				}
			}else {
				if(piece.rotation != 2) {
					for(int x = temp.x - 84; x < tX + 84; x += 42) {//check for a spot to go in two block radius left and right
						temp.x=x;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							return true;
						}
					}
				}else {
					for(int x = temp.x - 42; x < tX + 126; x += 42) {
						temp.x=x;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							return true;
						}
					}
				}
			}
			return false;
		}

		//t spin triple integreation
		if(tSpinCount > 0 && piece.t == 4){
			temp.x = temp.x - 42;
			temp.y = temp.y + 84;
			//bug in collision function detecting collision that isn't real this fixes it, with minimal chance of error
			temp.cube[0][1] = 0;
			System.out.println("Board coords: " + temp.x/42 + ", " + (temp.y + 42)/42);
			if(board[(temp.y + 42)/42][temp.x/42] > 0)
				System.out.println("piece here duh");
			else
				System.out.println("No piece here duh");
			if(!isColliding(temp, true) && temp.y < 18*42) {
				piece.x = temp.x;
				piece.y = temp.y;
				gPiece.x = temp.x;
				piece.cube = temp.cube;
				tSpinCount = 0;
			}
			else return false;
		}
		return true;
	}

	private boolean canRotateLeft() {
		Piece temp = new Piece(piece.t, piece.x, piece.y);
		temp.rotate(false);
		int tX = temp.x;
		int tY = temp.y;
		System.out.println("for Reference Piece Type: " + piece.t + "  X: " + (piece.x / 42) + " Y: " + (piece.y / 42) + " R: " + piece.rotation);
		System.out.println("for Reference Temp Type: " + temp.t + "  X: " + (temp.x / 42) + " Y: " + (temp.y / 42) + " R: " + temp.rotation);
		if(isColliding(temp, false)) {
			//System.out.println("we can;t rotate in place");
			if(temp.t != 0) {
				//checks to see if we can be one block to side of where we are when rotated.  for when rotation should be possible but we're close to edge
				/*for(int x = temp.x + 42; x > tX - 84; x -= 42) {
					temp.x=x;
					//checks alternating lower and higher spots
					for(int y = 0; y < 84; y+=42) {
						temp.y = temp.y + y;
						System.out.println("Checking x coordinates at X: " + (x / 42) + " Y: " + (temp.y / 42));
						if(!isColliding(temp, true)) {
							if(piece.t == 3 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 5)
								temp.y += 42;
							System.out.println("t spin piece x: " + (temp.x / 42) + " temp y: " + (temp.y / 42));
							//System.out.println("X: " + (temp.x / 42));

							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}

						temp.y = temp.y - 2*y;
						System.out.println("Checking x coordinates at X: " + (x / 42) + " Y: " + (temp.y / 42));
						if(!isColliding(temp, true)) {
							System.out.println("t spin piece y: " + (piece.y / 42) + " temp y: " + (temp.y / 42));
							System.out.println("X: " + (temp.x / 42));
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							return true;
						}
					}
					temp.y = tY;
				}*/
				for(int y = 0; y <84; y+=42){
					temp.y = tY + y;
					for(int x = 0; x < 84; x+= 42){
						temp.x = tX + x;
						if(!isColliding(temp, true)) {
							/*if(temp.t == 3 && temp.y > 17*42){
								temp.y -= 42;
							}*/
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("problem");
							if(piece.y > tY){
								tSpinCount++;
							}

							return true;
						}
						temp.x = tX - x;
						if(!isColliding(temp, true)) {
							/*if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}*/
							//if(piece.t == 3)
							//	temp.y += 42;
							if(piece.t == 5)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("this problem");


							return true;
						}
						//temp.x = tX;
					}
					temp.y = tY - y;
					for(int x = 0; x < 84; x+= 42){
						temp.x = tX + x;
						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							if(piece.y > tY){
								tSpinCount++;
							}
							System.out.println("problem");
							return true;
						}
						temp.x = tX - x;
						if(!isColliding(temp, true)) {
							if(temp.t == 5 && temp.y > 17*42){
								temp.y -= 42;
							}
							if(piece.t == 3)
								temp.y += 42;
							piece.x = temp.x;
							piece.y = temp.y;
							gPiece.x = temp.x;
							System.out.println("this problem");


							return true;
						}
						//temp.x = tX;
					}
					//temp.y = tY;
				}
			}else {
				if(piece.rotation != 2) {
					for(int x = temp.x - 84; x < tX + 84; x += 42) {//check for a spot to go in two block radius left and right
						temp.x=x;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							return true;
						}
					}
				}else {
					for(int x = temp.x - 42; x < tX + 126; x += 42) {
						temp.x=x;
						if(!isColliding(temp, true)) {
							piece.x = temp.x;
							gPiece.x = temp.x;
							return true;
						}
					}
				}
			}
			return false;
		}

		/*if(tSpinCount > 0){
			piece.x = tX - 42;
			piece.y = tY + 84;
		}*/

		//T Spin triple integration
		System.out.println("Spinning right by default");
		if(tSpinCount > 0 && piece.t == 4){
			temp.x = temp.x + 42;
			temp.y = temp.y + 84;
			temp.cube[2][1] = 0;
			if(!isColliding(temp, true) && temp.y < 18*42) {
				piece.x = temp.x;
				piece.y = temp.y;
				gPiece.x = temp.x;
				//tSpinCount = 0;
			}
			else {
				return false;
			}
		}
		return true;
	}




	//checks if a Piece's block location are occupied.  Only will work with invisible pieces used for hypothetical moves
	private boolean isColliding(Piece p, boolean test) {//Might need optimization for integration of T-Spin Triples but also meh
		if(test)
			System.out.println("checking collision at " + p.x/42 + "," + p.y/42);
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(p.cube[c][r] > 0) {
					try{
						//iterates across individual blocks of hypothetical piece and checks if block already in that grid spot
						if(board[(p.y + (42*r))/42][(p.x + (42*c))/42] > 0) {
							if (test){
								System.out.println("Colliding with piece at X: " + (p.x / 42) + " Y: " + (p.y / 42));
								System.out.println("X: " + (p.x + (42 * c)) / 42 + " Y: " + (p.y + (42 * r)) / 42 + "Board T: " + (board[(p.y + (42 * r)) / 42][(p.x + (42 * c)) / 42] - 1) + " R: " + p.rotation + " Block(r, c): (" + r + "," + c + ")");
							}
							return true;
						}
					}catch(ArrayIndexOutOfBoundsException e) {//x goes down to -2/-1 at left side depending on piece/rotation
						System.out.println("ArrayIndexOutOfBounds yikes we didn't notice this lol");
						return true;
					}
					if((p.x + (42*c)+42)/42 > 10) {//add 42 to account for block width
						System.out.println("colliding right border");
						return true;
					}
					if((p.x + (42*c))/42 < 0 ) {
						System.out.println("colliding left border");
						return true;
					}
					if((p.y + (42*r))/42 > 19) {
						if(test) {
							System.out.println("OUr lowest piece + 1 is Greater than 19: " + ((p.y + (42*r)+42)/42));
							//mini t-spins at absolute bottom need this function
							return (p.y + (42 * r) + 42) / 42 != 20;
						}

						return true;
					}
				}
			}
		}
		if(test)
			System.out.println("not Colliding");
		return false;
	}
	
	//checks for line clears by iterating across the board row by row.  clears when it finds a full row
	private void checkLines() {

		ArrayList<Integer> lines = new ArrayList<>();//lines to be cleared
		
		int lineCount = 0;//counter for scoring purposes only
		
		//counts blocks in a row and adds full lines to list of lines cleared in a turn
		for(int r = 0; r < 20; r++) {
			int count = 0;
			for(int c = 0; c < 10; c++) {
				if(board[r][c] != 0)
					count++;
			}
			if(count >= 10) {
				lines.add(r);
				lineCount++;
				for(int c = 0; c < 10; c++) {//clear row
					board[r][c] = 0;
				}
				boardCount -= 10;

			}
		}

		if(tSpinCount != 0){
			score = score + 128*lineCount;//makes t spin doubles same worth as tetrises and t spin triples aren't ridiculously better
			tSpinCount = 0;
		}else{
			score = (int) (score + (Math.pow(4, lineCount)));//scoring is arbitrarily exponential of 4; makes for good rewarding of tetris and triples
		}
		//move all blocks above row cleared down for all rows cleared
		for(int i : lines) {
			for(int r = i; r > 0; r--) {
				System.arraycopy(board[r - 1], 0, board[r], 0, 10);
			}
		}
		lines.clear();//empty array for next time blocks clear
		linesCleared = linesCleared + lineCount;
		
		//level up after 60 lines or 2048 points = 8 tetrises or 32 triples
		if(linesCleared % 60 == 0 && linesCleared != 0){
			level++;
			linesCleared = 0;
		}
		if(score / 2048 > level)
			level++;
		totalLines += linesCleared;
		linesCleared = 0;
	}
	
	//updated rng; creates chunks of 7 containing one of each block and then queues them randomly.  ensures we don't see 3+ in a row 
	private int returnType() {
		int c;
		
		if(tQueue.isEmpty()){
			for(int i = 0; i < 7; i++) {
				tQueue.add(i);
			}
		}
		
		c = rnd.nextInt(tQueue.size());
		int r = tQueue.get(c);
		tQueue.remove(c);
		
		
		return r;
		
	}
	
	//Creates our next playable piece from the queue.  takes top block makes it playable, shifts all pieces up and spawns a new queue piece
	private void queuePiece() {
		queue[0].x = 210;
		queue[0].y = 0;
		
		piece = queue[0];
		piece.isFalling = true;
		
		gPiece = new Piece(piece.t, 210, 840);
		gPiece.isFalling = false;
		gPiece.y = 840;
		if(piece.t == 0 && piece.rotation % 2 == 0){//makes sure the I piece always starts horizontal
			piece.rotate(true);
			gPiece.rotate(true);
		}
		for (int i = 0; i < queue.length - 1; i++) {
			queue[i] = queue[i + 1];		
			queue[i].y = queue[i].y - 200;
		}
		
		queue[2] = new Piece(returnType(), 490, 625);//queue is always 3 tetriminos
	}
	
	//our method for holding a piece.  if nothing held put our block in hold spot and queue next piece, otherwise use a temp piece for storage to swap pieces
	private void holdPiece() {
		Piece temp;
		if(hPiece == null) {
			hPiece = new Piece(piece.t, 444, 34);
			hPiece.isFalling = false;
			queuePiece();
		}else {
			temp = hPiece;
			hPiece = new Piece(piece.t, 444, 34);
			piece = temp;
			piece.x = 210;
			piece.y = 0;
			
			piece.isFalling = true;
			gPiece = new Piece(piece.t, 210, 840);
			gPiece.isFalling = false;
			gPiece.y = 840;
			if(piece.t == 0 && piece.rotation % 2 == 0){//makes sure the I piece always starts horizontal
				piece.rotate(true);
				gPiece.rotate(true);
			}
		}
		
		repaint();
	}
	
	
	//logic for the shadow piece at bottom.  Ensures same x coordinate as playable piece at whatever spot the block would hit if up is pressed
	private void paintGhost(Graphics g) {
		//for transparency purposes
		Graphics2D g2d = (Graphics2D)g;
		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.25);
		g2d.setComposite(composite);
		gPiece.y = piece.y;
		while(!isColliding(gPiece, false)) {//quite literally put the piece at our current location and immediately drop it every frame
			gPiece.y += 42;
		}
		if(isColliding(gPiece, false)) {
			gPiece.y-=42;
		}
		if(gPiece.x != piece.x)
			gPiece.x = piece.x;
		if(piece.y >= gPiece.y)
			gPiece.y = piece.y;
		
		//paint piece
		for(int r = 0; r < 4; r++) {
			for(int c = 0; c < 4; c++) {
				if(gPiece != null && gPiece.cube[c][r] > 0) {
					switch (gPiece.t) {
						case 0 -> lBlue.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 1 -> orange.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 2 -> blue.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 3 -> green.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 4 -> purp.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 5 -> red.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
						case 6 -> yellow.paintIcon(this, g, gPiece.x + (42 * c), gPiece.y + (42 * r));
					}
				}
			}
		}
		//set our opacity back to fully opaque for rest of blocks
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1);
		g2d.setComposite(composite);
	}



}
