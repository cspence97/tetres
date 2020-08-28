package testrit;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame window = new JFrame();
			tetrsd game = new tetrsd();
			//window.setBounds(10, 10, 650, 820);
			window.setBackground(Color.gray);

			window.setExtendedState(JFrame.MAXIMIZED_BOTH);
			window.setResizable(false);
			//window.setAlwaysOnTop(true);
			window.setVisible(true);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setLocationRelativeTo(null);
			window.add(game);



		});
	}
}
/*
TODO LIST
-fix rotation collision on vertical axes like when we're one above or below and rotate inside it PRolly done
-Fix weird gameover when blocks are at the top but not colliding
*/
