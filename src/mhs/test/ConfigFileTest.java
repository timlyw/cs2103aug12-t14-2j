package mhs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import mhs.src.ConfigFile;

import org.junit.Before;
import org.junit.Test;

public class ConfigFileTest {

	private static final String TEST_PARAM_VALUE = "testParamValue";
	private static final String TEST_PARAM = "testParam";
	private static final String TEST_CONFIG_FILE_NAME = "testConfigFile.json";

	@Before
	public void configFileTestSetup() {
		File configFile = new File(TEST_CONFIG_FILE_NAME);
		if (configFile.exists()) {
			configFile.delete();
		}
	}

	@Test
	public void configFileTest() throws IOException {
		ConfigFile configFile = new ConfigFile(TEST_CONFIG_FILE_NAME);

		configFile.setConfigParameter(TEST_PARAM, TEST_PARAM_VALUE);

		assertTrue(configFile.hasConfigParameter(TEST_PARAM));
		assertEquals(TEST_PARAM_VALUE,
				configFile.getConfigParameter(TEST_PARAM));

		configFile.save();
	}

	@Test
	public void configFileTestLoad() throws IOException {
		ConfigFile configFile = new ConfigFile(TEST_CONFIG_FILE_NAME);

		configFile.setConfigParameter(TEST_PARAM, TEST_PARAM_VALUE);

		assertTrue(configFile.hasConfigParameter(TEST_PARAM));
		assertEquals(TEST_PARAM_VALUE,
				configFile.getConfigParameter(TEST_PARAM));
	}
}
