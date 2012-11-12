//@author A0088015H

package mhs.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import mhs.src.userinterface.MhsFrame;

/**
 * This class test if MhsFrame properly initializes its frame components
 * 
 * @author John Wong
 */

public class MhsFrameTest {
	
	/**
	 * basic component initialization test
	 * tests if the various components of MhsFrame are properly added
	 * to the interface
	 */
	@Test
	public void testMhsFrameInitialization() {
		MhsFrame mhsFrame = MhsFrame.getInstance();
		assertTrue(mhsFrame.isFramePanelInitialized());
		assertTrue(mhsFrame.isTitleScreenInitialized());
		assertTrue(mhsFrame.isDisplayScreenInitialized());
		assertTrue(mhsFrame.isFeedbackScreenInitialized());
		assertTrue(mhsFrame.isInputBoxInitialized());
	}
}
