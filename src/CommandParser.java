
import org.joda.time.DateTime;


public class CommandParser {

	private boolean taskNameFlag = false;
	private boolean timeFlag = false;
	private boolean dateFlag= false;
	
	private String command = null;
	private String taskName = null;
	private String edittedName = null;
	
	private DateTime startDate = null;
	private DateTime startTime = null;
	private DateTime endDate = null;
	private DateTime endTime = null;
	
	private Command commandObject = null;
	
	public Command parseCommand(){
		DateExtractor dateParser = new DateExtractor();
		CommandExtractor commandParser = new CommandExtractor();
		TimeExtractor timeExtractor = new TimeExtractor();


		return commandObject;
	}

}
