package mhs.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DatabaseTest.class, ConfigFileTest.class,
		GoogleCalendarTest.class, TaskRecordFileTest.class, CommandParserTest.class})
public class AllTests {

}
