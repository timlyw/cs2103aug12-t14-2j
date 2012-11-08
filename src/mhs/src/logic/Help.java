package mhs.src.logic;

public class Help {

	private String screenState;
	private String feedback;

	public Help() {
		screenState = "HELLO WORLD";
		feedback = "GOODBYE WORLD";
	}

	public String getCommandFeedback() {
		return feedback;
	}

	public String getState() {
		return screenState;
	}

}
