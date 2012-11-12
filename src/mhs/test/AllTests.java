package mhs.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * AllTests
 * 
 * jUnit Test Suite running all tests
 * 
 * - Add jUnit test to SuiteClasses include in AllTests
 * 
 */

@RunWith(Suite.class)
@SuiteClasses({ DatabaseFactoryTest.class, DatabaseTest.class,
		ConfigFileTest.class, TaskRecordFileTest.class,
		GoogleCalendarMhsTest.class, GoogleTasksTest.class, MhsFrameTest.class,
		CommandParserTest.class, TaskListsTest.class, SystemTest.class })
public class AllTests {

}
