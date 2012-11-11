package mhs.test;

import mhs.src.storage.persistence.remote.GoogleCalendarMhs;

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
@SuiteClasses({ DatabaseFactoryTest.class, DatabaseSyncTest.class, DatabaseTest.class,
		ConfigFileTest.class, TaskRecordFileTest.class,
		CommandParserTest.class, TaskListsTest.class })
public class AllTests {

}
