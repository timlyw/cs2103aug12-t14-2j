//@author A0087048X
package mhs.test;

import java.io.IOException;

import mhs.src.common.exceptions.DatabaseAlreadyInstantiatedException;
import mhs.src.common.exceptions.DatabaseFactoryNotInstantiatedException;
import mhs.src.common.exceptions.TaskNotFoundException;
import mhs.src.storage.Database;
import mhs.src.storage.DatabaseFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gdata.util.ServiceException;

/**
 * DatabaseFactoryTest
 * 
 * jUnit tests for DatabaseFactoryTest
 *  
 * Exceptions
 * 1. testDatabaseFactoryAlreadyInstantiatedExceptionExceptions
 * 2. testDatabaseFactoryNotInstantiatedExceptionExceptions
 * 3. testDatabaseFactoryIllegalArugmentsExceptions
 * 
 * Functionality
 * 4. testDatabaseFactory
 * 
 * @author Timothy Lim Yi Wen A0087048X
 */

public class DatabaseFactoryTest {
	private final static String TEST_TASK_RECORD_FILENAME = "testTaskRecordFile.json";
	private final static String TEST_2_TASK_RECORD_FILENAME = "test2TaskRecordFile.json";
	private static final String PARAMETER_TASK_RECORD_FILE_NAME = "taskRecordFileName";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED = "DatabaseFactory not instantiated. Call getDatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_DATABASE_FACTORY_ALREADY_INSTANTIATED = "DatabaseFactory already instantiated. Destory DatabaseFactory first.";
	private static final String EXCEPTION_MESSAGE_NULL_PARAMETER = "%1$s cannot be null!";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void databaseFactoryCleanup() {
		DatabaseFactory.destroy();
	}

	@Test
	/**
	 * Test DatabaseFactoryAlreadyInstantiatedExceptionExceptions
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws DatabaseAlreadyInstantiatedException
	 * @throws DatabaseFactoryNotInstantiatedException
	 */
	public void testDatabaseFactoryAlreadyInstantiatedExceptionExceptions()
			throws IllegalArgumentException, IOException, ServiceException,
			TaskNotFoundException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(DatabaseAlreadyInstantiatedException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_DATABASE_FACTORY_ALREADY_INSTANTIATED);		
		DatabaseFactory.initializeDatabaseFactory(TEST_TASK_RECORD_FILENAME, true);
		// Initialize DatabaseFactory again without destroying
		DatabaseFactory.initializeDatabaseFactory(TEST_2_TASK_RECORD_FILENAME, true);		
	}

	@Test
	/**
	 * Test DatabaseFactoryNotInstantiatedException
	 *  
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws DatabaseAlreadyInstantiatedException
	 * @throws DatabaseFactoryNotInstantiatedException
	 */
	public void testDatabaseFactoryNotInstantiatedExceptionExceptions()
			throws IllegalArgumentException, IOException, ServiceException,
			TaskNotFoundException, DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		thrown.expect(DatabaseFactoryNotInstantiatedException.class);
		thrown.expectMessage(EXCEPTION_MESSAGE_DATABASE_FACTORY_NOT_INSTANTIATED);
		// Get database instance without intializing DatabaseFactory
		DatabaseFactory.getDatabaseInstance();
	}

	@Test
	/**
	 * Tests for IllegalArgumentException 
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws TaskNotFoundException
	 * @throws DatabaseAlreadyInstantiatedException
	 */
	public void testDatabaseFactoryIllegalArugmentsExceptions()
			throws IllegalArgumentException, IOException, ServiceException,
			TaskNotFoundException, DatabaseAlreadyInstantiatedException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage(String.format(EXCEPTION_MESSAGE_NULL_PARAMETER,
				PARAMETER_TASK_RECORD_FILE_NAME));
		DatabaseFactory.initializeDatabaseFactory(null, true);
	}

	@Test
	/**
	 * Test DatabaseFactory functionality
	 * 
	 * 1. DatabaseFactory initialization
	 * 
	 * 2. Database Creation 
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws DatabaseAlreadyInstantiatedException
	 * @throws DatabaseFactoryNotInstantiatedException
	 */
	public void testDatabaseFactory() throws IllegalArgumentException,
			IOException, ServiceException,
			DatabaseAlreadyInstantiatedException,
			DatabaseFactoryNotInstantiatedException {
		DatabaseFactory.initializeDatabaseFactory(TEST_TASK_RECORD_FILENAME, true);
		Database testDatabase = DatabaseFactory.getDatabaseInstance();
		assert (testDatabase != null);
	}
	
	@After
	public void testCleanup(){
		DatabaseFactory.destroy();
	}
}
