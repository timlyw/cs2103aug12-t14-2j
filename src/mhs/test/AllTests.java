package mhs.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DatabaseTest.class, TaskRecordFileTest.class })
public class AllTests {

}
