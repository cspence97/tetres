package testrit;

public class Piece{
  
	//matrix for painting blocks; rotate with double for statement to new matrix?	
	public int[][] cube = new int[4][4];
	private final int[][] tPiece = new int[3][3];
	public int x;//start at column 5
	public int y;//and at top
	public int t;
	public int rotation; //0-3 increasing clockwise just holds which direction we're facing
	
	//generally leftcenter is lowest occupied c value - 1; rightcenter is highest occupied c value
	int leftcenter = 0;//0 means left most column empty
	int rightcenter = 0;//0 means right most column OCCUPIED accounts for x coord being on left side of block
	int bot = 0;//0 top 3 bottom
	
	public boolean isFalling = true;

	public Piece(int type, int a, int b) {
		x = a;
		y = b;
		//convert visual grid to 4x4 matrices
		switch(type) {
			case 0://I piece
				for (int r = 0; r < 4; r++) {//fill out shape in second column for best rotation
					for (int c = 0; c < 4; c++) {
						if (c == 1)
							cube[c][r] = 1;
					}
				}
				break;
			case 1://L Piece
				for (int r = 0; r < 3; r++) {//fill out shape in 3x3 matrix for improved spinning
					for (int c = 0; c < 3; c++) {
						if (c == 1)
							tPiece[c][r] = 1;
						if (c == 0 && r == 0)
							tPiece[c][r] = 1;
					}
				}
				for (int xr = 0; xr < 3; xr++) {//transfer 3x3 matrix to top left of 4x4 matrix
					for (int yc = 0; yc < 3; yc++) {//4x4 is necessary for already implemented physics and painting
						cube[yc][xr] = tPiece[yc][xr];
					}
				}
				break;
			case 2: //J piece
				for (int r = 0; r < 3; r++) {//3x3 for improved spinning
					for (int c = 0; c < 3; c++) {
						if (c == 1)
							tPiece[c][r] = 1;
						if (c == 0 && r == 2)
							tPiece[c][r] = 1;
					}
				}
				for (int xr = 0; xr < 3; xr++) {//transfer 3x3 to 4x4
					for (int yc = 0; yc < 3; yc++) {
						cube[yc][xr] = tPiece[yc][xr];
					}
				}
				break;
			case 3://S piece
				leftcenter = 0;
				rightcenter = 2;
				bot = 2;
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						if (c == 0 && r < 2)
							tPiece[c][r] = 1;
						if (c == 1 && r > 0)
							tPiece[c][r] = 1;
					}
				}
				for (int xr = 0; xr < 3; xr++) {
					for (int yc = 0; yc < 3; yc++) {
						cube[yc][xr] = tPiece[yc][xr];
					}
				}
				break;
			case 4://T piece
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						if (c == 1)
							tPiece[c][r] = 1;
						if (c == 0 && r == 1)
							tPiece[c][r] = 1;
					}
				}
				for (int xr = 0; xr < 3; xr++) {
					for (int yc = 0; yc < 3; yc++) {
						cube[yc][xr] = tPiece[yc][xr];
					}
				}
				break;
			case 5://z piece
				for (int r = 0; r < 3; r++) {
					for (int c = 0; c < 3; c++) {
						if (c == 0 && r > 0)
							tPiece[c][r] = 1;
						if (c == 1 && r < 2)
							tPiece[c][r] = 1;
					}
				}
				for (int xr = 0; xr < 3; xr++) {
					for (int yc = 0; yc < 3; yc++) {
						cube[yc][xr] = tPiece[yc][xr];
					}
				}
				break;
			case 6://o piece
				for (int r = 0; r < 4; r++) {
					for (int c = 0; c < 4; c++) {
						if (c == 0 || c == 1)
							if (r == 0 || r == 1)
								cube[c][r] = 1;
					}
				}
				break;
		}
		//make type accessible in all variables without paramaters
		t = type;	
	}
	
	//true for left false for right
	public void rotate(boolean a) {
		
		if(t == 0) {
			
			if(a) {
				if(rotation > 0)
					rotation--;
				else
					rotation = 3;
				for (int i = 0; i < cube[0].length; i++) {
		            for (int j = 0, k = cube[0].length - 1; j < k; j++, k--) { 
		                int temp = cube[j][i]; 
		                cube[j][i] = cube[k][i]; 
		                cube[k][i] = temp; 
		            } 
				}
				for (int i = 0; i < cube.length; i++) {
		            for (int j = i; j < cube[0].length; j++) { 
		                int temp = cube[j][i]; 
		                cube[j][i] = cube[i][j]; 
		                cube[i][j] = temp; 
		            } 
				}
			}else {
				if(rotation < 3)
					rotation++;
				else
					rotation = 0;
				for (int i = 0; i < cube[0].length; i++) {
		            for (int j = 0, k = cube[0].length - 1; j < k; j++, k--) { 
		                int temp = cube[i][j]; 
		                cube[i][j] = cube[i][k]; 
		                cube[i][k] = temp; 
		            } 
				}
				for (int i = 0; i < cube.length; i++) {
		            for (int j = i; j < cube[0].length; j++) { 
		                int temp = cube[i][j]; 
		                cube[i][j] = cube[j][i]; 
		                cube[j][i] = temp; 
		            } 
				}
			}
		}
		if(t > 0 && t < 6) {
			if(a) {
				if(rotation > 0)
					rotation--;
				else
					rotation = 3;
				for (int i = 0; i < tPiece[0].length; i++) {
		            for (int j = 0, k = tPiece[0].length - 1; j < k; j++, k--) { 
		                int temp = tPiece[j][i]; 
		                tPiece[j][i] = tPiece[k][i]; 
		                tPiece[k][i] = temp; 
		            } 
				}
				for (int i = 0; i < tPiece.length; i++) {
		            for (int j = i; j < tPiece[0].length; j++) { 
		                int temp = tPiece[j][i]; 
		                tPiece[j][i] = tPiece[i][j]; 
		                tPiece[i][j] = temp; 
		            } 
				}
			}else {
				if(rotation < 3)
					rotation++;
				else
					rotation = 0;
				for (int i = 0; i < tPiece[0].length; i++) {
		            for (int j = 0, k = tPiece[0].length - 1; j < k; j++, k--) { 
		                int temp = tPiece[i][j]; 
		                tPiece[i][j] = tPiece[i][k]; 
		                tPiece[i][k] = temp; 
		            } 
				}
				for (int i = 0; i < tPiece.length; i++) {
		            for (int j = i; j < tPiece[0].length; j++) { 
		                int temp = tPiece[i][j]; 
		                tPiece[i][j] = tPiece[j][i]; 
		                tPiece[j][i] = temp; 
		            } 
				}
			}
			//change values from 3x3 matrix to main 4x4 matrix for easier referencing in tetrsd class
			for(int xr = 0; xr < 3; xr++) {
				for(int yc = 0; yc < 3; yc++) {
					cube[yc][xr] = tPiece[yc][xr];
				}
			}
		}
	}
	
	//move blocks down one block
	public void Gravity() {
		if(y<756) {
			y+=42;
		}else{
			isFalling = false;
		}
	}
}
