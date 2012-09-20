package mhs.src;
import java.util.ArrayList;



public class StringChangeTracker {
	ArrayList<Change> undoList = new ArrayList<Change>();
	
	public void trackChange(String oldText, String newText) {
		Change change = getChange(oldText, newText);
		undoList.add(change);
	}
	
	public String getPreviousString(String currText) {
		Change lastChange = undoList.get(undoList.size() - 1);
		int startChangeIndex = lastChange.getStartChangeIndex();
		int endChangeIndex = lastChange.getEndChangeIndex();
		String changedString = lastChange.getChangedString();
		String previousString = currText.substring(0, startChangeIndex) + changedString + currText.substring(endChangeIndex);
		return previousString;
	}
	
	public Change getChange(String oldText, String newText) {
		int startChangeIndex = getStartChangeIndex(oldText, newText);
		int newEndChangeIndex = getNewEndChangeIndex(oldText, newText) + 1;
		int oldEndChangeIndex = getOldEndChangeIndex(oldText, newText);
		String previousString = oldText.substring(startChangeIndex, oldEndChangeIndex + 1);
		Change change = new Change(startChangeIndex, newEndChangeIndex, previousString);
		return change;
	}
	
	public int getStartChangeIndex(String oldText, String newText) {
		int startChangeIndex = 0;
		char newChar, oldChar;
		boolean indexWithinRange;
		
		do {
			newChar = newText.charAt(startChangeIndex);
			oldChar = oldText.charAt(startChangeIndex);
			startChangeIndex++;
			indexWithinRange = (startChangeIndex < oldText.length())
					&& (startChangeIndex < newText.length());
		} while(newChar == oldChar && indexWithinRange);
		
		return startChangeIndex - 1;
	}
	
	public int getNewEndChangeIndex(String oldText, String newText) {
		int count = 1;
		int newCharIndex, oldCharIndex;
		char newChar, oldChar;
		boolean indexWithinRange;
		
		do {
			newCharIndex = newText.length() - count;
			oldCharIndex = oldText.length() - count;
			
			newChar = newText.charAt(newCharIndex);
			oldChar = oldText.charAt(oldCharIndex);
			
			indexWithinRange = (oldCharIndex > 0)
					&& (newCharIndex > 0);
			
			count++;
		} while(newChar == oldChar && indexWithinRange);
		
		return newCharIndex;
	}
	
	public int getOldEndChangeIndex(String oldText, String newText) {
		int count = 1;
		int newCharIndex, oldCharIndex;
		char newChar, oldChar;
		boolean indexWithinRange;
		
		do {
			newCharIndex = newText.length() - count;
			oldCharIndex = oldText.length() - count;
			
			newChar = newText.charAt(newCharIndex);
			oldChar = oldText.charAt(oldCharIndex);
			
			indexWithinRange = (oldCharIndex > 0)
					&& (newCharIndex > 0);
			
			count++;
		} while(newChar == oldChar && indexWithinRange);
		
		return oldCharIndex;
	}
	
}
