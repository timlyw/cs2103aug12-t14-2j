package mhs.test;

import static org.junit.Assert.assertEquals;

import mhs.src.Change;
import mhs.src.StringChangeTracker;

import org.junit.Test;

public class RevisionControlTest {

	@Test
	public void testUndo() {
		StringChangeTracker stringChangeTracker = new StringChangeTracker();
		String oldString = "hello world";
		String newString = "hi world";
		stringChangeTracker.trackChange(oldString, newString);
		String previousString = stringChangeTracker
				.getPreviousString(newString);
		assertEquals(previousString, oldString);
	}

	@Test
	public void testGetStartChangeIndex() {
		StringChangeTracker stringChangeTracker = new StringChangeTracker();
		int startChangeIndex = stringChangeTracker.getStartChangeIndex(
				"hello world", "hi world");
		assertEquals(1, startChangeIndex);
		startChangeIndex = stringChangeTracker.getStartChangeIndex("aabbcc",
				"aabbzc");
		assertEquals(4, startChangeIndex);
		startChangeIndex = stringChangeTracker
				.getStartChangeIndex("abc", "abc");
		assertEquals(2, startChangeIndex);
		startChangeIndex = stringChangeTracker.getStartChangeIndex("bc", "abc");
		assertEquals(0, startChangeIndex);
	}

	@Test
	public void testGetEndChangeIndex() {
		StringChangeTracker stringChangeTracker = new StringChangeTracker();
		int endChangeIndex = stringChangeTracker.getOldEndChangeIndex(
				"hello world", "hi world");
		assertEquals(4, endChangeIndex);
		endChangeIndex = stringChangeTracker.getOldEndChangeIndex("abcdz",
				"abcez");
		assertEquals(3, endChangeIndex);
		endChangeIndex = stringChangeTracker.getOldEndChangeIndex("abc", "def");
		assertEquals(2, endChangeIndex);

	}

	@Test
	public void testGetChange() {
		StringChangeTracker stringChangeTracker = new StringChangeTracker();
		Change change = stringChangeTracker
				.getChange("hello world", "hi world");
		assertEquals("ello", change.getChangedString());
	}

}
