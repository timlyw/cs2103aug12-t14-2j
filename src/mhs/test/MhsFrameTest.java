//@author A0088015H

package mhs.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import mhs.src.userinterface.MhsFrame;

public class MhsFrameTest {
	
	/**
	 * basic component initialization test
	 * tests if the various components of MhsFrame are properly added
	 * to the interface
	 */
	@Test
	public void testMhsFrameInitialization() {
		MhsFrame mhsFrame = MhsFrame.getInstance();
		assertTrue(mhsFrame.framePanelInitialized());
		assertTrue(mhsFrame.titleScreenInitialized());
		assertTrue(mhsFrame.displayScreenInitialized());
		assertTrue(mhsFrame.feedbackScreenInitialized());
		assertTrue(mhsFrame.plainTextBoxInitialized());
	}
}
