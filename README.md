# Tetres
This is my take on cloning Tetris.  I like Tetris and wanted to try and recreate it and implement some modern functions like T-Spin triples and all clears.

You can download a playable jar [here](https://clcs.me/tetres.jar)

## Classes
### Main
Sets up our window and starts an instance of our game

### tetrsd
Our game script.  Holds the board and facilitates the timer and all of the gameplay.  

### Piece
Code for pieces. Holds all the information for individual pieces like its coordinates, shape, rotation, etc.  Also includes some methods for things like rotating and falling

## Notes
The graphics uses 42x42 images for the individual squares.  The board is 10x20 and this is why most of the math is in terms of multiples of 42

