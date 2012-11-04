package mhs.test;

import java.io.IOException;

import mhs.src.storage.Database;
import mhs.src.storage.DatabaseAlreadyInstantiatedException;
import mhs.src.storage.DatabaseFactory;
import mhs.src.storage.DatabaseFactoryNotInstantiatedException;
import mhs.src.storage.TaskNotFoundException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gdata.util.ServiceException;

public class DatabaseFactoryTest {
	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";
	private final static String TEST_2_TASK_RECORD_FILENAME = "test2TaskRecordFile.json";
	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "DatabaseFactory not instantiated. Call getDatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_DATABASE_WITH_PARAMETERS_ALREADY_INSTANTIATED = "Database with parameters %1$s already instantiated";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void databaseFactoryCleanup() {
		DatabaseFactory.destroy();
	}

	@Test
	public void testDatabaseFactoryNotInstantiatedExceptionExceptions()
			throws IllegalArgumentException, IOException, ServiceException,
			TaskNotFoundException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(DatabaseFactoryNotInstantiatedException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED);
		Database testDatabase = DatabaseFactory.getDatabaseInstance();
	}

	@Test
	public void testDatabaseFactoryIllegalArugmentsExceptions()
			throws IllegalArgumentException, IOException, ServiceException,
			TaskNotFoundException, DatabaseAlreadyInstantiatedException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(String.format(EXCEPTION_MESSAGE_NULL_PARAMETER,
				PARAMETER_TASK_RECORD_FILE_NAME));
		DatabaseFactory.getDatabaseFactory(null, true);
	}

	@Test
	public void testDatabaseFactory() throws IllegalArgumentException,
			IOException, ServiceException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		DatabaseFactory.getDatabaseFactory(TEST_TASK_RECORD_FILENAME, true);
		Database testDatabase = DatabaseFactory.getDatabaseInstance();
		assert (testDatabase != null);
	}
}
