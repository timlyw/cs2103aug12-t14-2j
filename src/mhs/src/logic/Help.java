package mhs.src.logic;

import mhs.src.common.HtmlCreator;

public class Help {

	private String screenState;
	private String feedback;

	public Help() {

		screenState = "1. Add Help" + HtmlCreator.NEW_LINE + "2. Edit Help"
				+ HtmlCreator.NEW_LINE + "3. Search Help"
				+ HtmlCreator.NEW_LINE + "4. Supported Date Formats"
				+ HtmlCreator.NEW_LINE + "5. Supported Time Formats"
				+ HtmlCreator.NEW_LINE + "6. Supported Name Formats"
				+ HtmlCreator.NEW_LINE + "7. Supported Command List";

		feedback = "How would you like me to assist you?";
	}

	public void HelpAdd() {
		HtmlCreator htmlCreator = new HtmlCreator();

		screenState = "Commands : add" + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + "Enter "
				+ htmlCreator.color("Add", "green") + " a "
				+ htmlCreator.color("task name", "red")
				+ " for adding a floating task." + HtmlCreator.NEW_LINE
				+ "Enter " + htmlCreator.color("Add", "green") + " a "
				+ htmlCreator.color("task name", "red") + " with "
				+ htmlCreator.color("date", "red") + " and / or "
				+ htmlCreator.color("time", "red")
				+ " for adding a deadline task." + HtmlCreator.NEW_LINE
				+ "Enter " + htmlCreator.color("Add", "green") + " a "
				+ htmlCreator.color("task name", "red") + " with "
				+ htmlCreator.color("date", "red") + " and / or "
				+ htmlCreator.color("time", "red")
				+ htmlCreator.color(" to ", "green")
				+ htmlCreator.color("date", "red") + " and / or "
				+ htmlCreator.color("time", "red")
				+ " for adding a timed task." + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + "Examples" + HtmlCreator.NEW_LINE
				+ "Add laundry" + HtmlCreator.NEW_LINE
				+ "Add private meeting at 5pm today" + HtmlCreator.NEW_LINE
				+ "Add honeymoon 3pm sunday to 23:00 23/11";

		feedback = "Please fill me up! :D";

	}

	public void HelpEdit() {
		HtmlCreator htmlCreator = new HtmlCreator();

		screenState = "Commands : Update, edit, postpone"
				+ HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE
				+ "Enter "
				+ htmlCreator.color("Edit", "green")
				+ " a "
				+ htmlCreator.color("task name / Index", "red")
				+ htmlCreator.color(" to ", "red")
				+ htmlCreator.color("editted name", "red")
				+ " for editting a task to a floating task with editted name as the new task name."
				+ HtmlCreator.NEW_LINE
				+ "Enter "
				+ htmlCreator.color("Edit", "green")
				+ " a "
				+ htmlCreator.color("task name / Index", "red")
				+ htmlCreator.color(" to ", "green")
				+ htmlCreator.color("date", "red")
				+ " and / or "
				+ htmlCreator.color("time", "red")
				+ " for editting a task to a deadline task with the new date time specified."
				+ HtmlCreator.NEW_LINE
				+ "Enter "
				+ HtmlCreator.NEW_LINE
				+ "Enter "
				+ htmlCreator.color("Edit", "green")
				+ " a "
				+ htmlCreator.color("task name / Index", "red")
				+ htmlCreator.color(" to ", "green")
				+ htmlCreator.color("date", "red")
				+ " and / or "
				+ htmlCreator.color("time", "red")
				+ htmlCreator.color(" to ", "green")
				+ htmlCreator.color("date", "red")
				+ " and / or "
				+ htmlCreator.color("time", "red")
				+ " for editting a task to a timed task with the new date time range specified."
				+ HtmlCreator.NEW_LINE + HtmlCreator.NEW_LINE + "Examples"
				+ HtmlCreator.NEW_LINE + "Edit 5 to homework"
				+ HtmlCreator.NEW_LINE
				+ "postpone assignment submission to 23 10"
				+ HtmlCreator.NEW_LINE + "update holiday this weekend";

		feedback = "Please fill me up! :D";

	}

	public void HelpSearch() {
		HtmlCreator htmlCreator = new HtmlCreator();

		screenState = "Commands : Search, find, display" + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + "Enter "
				+ htmlCreator.color("Search", "green") + " a "
				+ htmlCreator.color("task name", "red")
				+ " to search for a floating task." + HtmlCreator.NEW_LINE
				+ "Enter " + htmlCreator.color("search", "green") + " a "
				+ htmlCreator.color("date", "red") + " and / or "
				+ htmlCreator.color("time", "red")
				+ htmlCreator.color(" to ", "green")
				+ htmlCreator.color("date", "red") + " and / or "
				+ htmlCreator.color("time", "red")
				+ " to search for tasks in a date range."
				+ HtmlCreator.NEW_LINE + "Enter "
				+ htmlCreator.color("floating", "green")
				+ " to search for floating tasks." + HtmlCreator.NEW_LINE
				+ "Enter " + htmlCreator.color("deadline", "green")
				+ " to search for deadline tasks." + HtmlCreator.NEW_LINE
				+ "Enter " + htmlCreator.color("timed", "green")
				+ " to search for timed tasks." + HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE + "Examples" + HtmlCreator.NEW_LINE
				+ "find laundry" + HtmlCreator.NEW_LINE
				+ "display this weekend" + HtmlCreator.NEW_LINE
				+ "search 7/11 to 10/11/2012" + HtmlCreator.NEW_LINE
				+ "floating";

		feedback = "You looking for something? :?";

	}

	public void HelpDateFormat() {

		screenState = "Full support for all months and days in both words and numbers"
				+ HtmlCreator.NEW_LINE
				+ "Dates that are not set are defaulted to todays date "
				+ HtmlCreator.NEW_LINE
				+ HtmlCreator.NEW_LINE
				+ "Examples "
				+ HtmlCreator.NEW_LINE
				+ "10 nov 2012"
				+ HtmlCreator.NEW_LINE
				+ "today"
				+ HtmlCreator.NEW_LINE
				+ "tomorrow"
				+ HtmlCreator.NEW_LINE
				+ "this week"
				+ HtmlCreator.NEW_LINE
				+ "this month"
				+ HtmlCreator.NEW_LINE
				+ "this year"
				+ HtmlCreator.NEW_LINE
				+ "this weekend"
				+ HtmlCreator.NEW_LINE
				+ "5/11" + HtmlCreator.NEW_LINE + "5/11/2012";

		feedback = "These are the dates you can find me.  :)";

	}

	public void HelpTimeFormat() {

		screenState = "Full support for 12hrs and 24hrs timing"
				+ HtmlCreator.NEW_LINE
				+ "Times that are not set are defaulted to 23:59"
				+ HtmlCreator.NEW_LINE + HtmlCreator.NEW_LINE + "Examples "
				+ HtmlCreator.NEW_LINE + "2pm" + HtmlCreator.NEW_LINE + "4am"
				+ HtmlCreator.NEW_LINE + "23:50";

		feedback = "I will be able to find you at these times.  :)";

	}

	public void HelpNameFormat() {
		screenState = "Name retrival may confict with date time parameters entered, for more acurate task names, enter the name withing quotation marks.";
		feedback = "You may address me by these names. :)";
	}

	public void HelpCommands() {

		screenState = "Commands add - Adding a task" + HtmlCreator.NEW_LINE
				+ "remove, delete - Deleting a task" + HtmlCreator.NEW_LINE
				+ "search, find, display - Searching for tasks"
				+ HtmlCreator.NEW_LINE
				+ "sync - Sync tasks with google calendar"
				+ HtmlCreator.NEW_LINE + "undo - Undo last command"
				+ HtmlCreator.NEW_LINE + "redo - Redo last command"
				+ HtmlCreator.NEW_LINE + "rename - Rename a task"
				+ HtmlCreator.NEW_LINE
				+ "login, signin - Login to google calendar"
				+ HtmlCreator.NEW_LINE
				+ "logout, signout - Logout to google calendar"
				+ HtmlCreator.NEW_LINE + "help - Get help"
				+ HtmlCreator.NEW_LINE + "mark - Mark a task done"
				+ HtmlCreator.NEW_LINE + "unmark - Unmark a done task"
				+ HtmlCreator.NEW_LINE + "n - Scroll to next page"
				+ HtmlCreator.NEW_LINE + "p - Scroll to previos page"
				+ HtmlCreator.NEW_LINE + "floating - Search for floating tasks"
				+ HtmlCreator.NEW_LINE + "deadline - Search for deadline tasks"
				+ HtmlCreator.NEW_LINE + "timed - Searcb for timed tasks"
				+ HtmlCreator.NEW_LINE + "exit - exit the program"
				+ HtmlCreator.NEW_LINE + "minimize - minimize program";

		feedback = "How would u like to command me?  :)";

	}

	public String getCommandFeedback() {
		return feedback;
	}

	public String getState() {
		return screenState;
	}

}
