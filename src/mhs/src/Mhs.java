package mhs.src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mhs {
	static LoginWindow lw;
	
	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
		ui.open();
		ui.setInputBoxToActive();

		ConfirmInputAction cia = new ConfirmInputAction();
		lw = new LoginWindow(cia);
		lw.open();
	}
	
	public static class ConfirmInputAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println(lw.getEmail());
			System.out.println(lw.getPassword());	
		}
	}
}
