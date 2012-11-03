package mhs.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@SuiteClasses({ DatabaseFactoryTest.class, DatabaseTest.class, ConfigFileTest.class,
		GoogleCalendarMhsTest.class, TaskRecordFileTest.class,
		CommandParserTest.class, TaskListsTest.class, ProcessorTest.class })
public class AllTests {

}
