package mhs.src;

public class CommandExtractor {
	enum commands {
		add("add"), 
		remove("remove"), delete("remove"), 
		update("edit"), edit("edit"), postpone("edit"), 
		search("search"), find("search"), 
		sync("sync"),
		undo("undo");

		private final String command;

		commands(String command) {
			this.command = command;
		}

	}

	private static String commandString;
	
	public static boolean checkCommandFormat(String printString) {
		return false;
	}
	
	public String getCommand(){
		return commandString;
	}
}
