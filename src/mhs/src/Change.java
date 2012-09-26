package mhs.src;

public class Change {
	int _startChangeIndex;
	int _endChangeIndex;
	String _changedString;
	
	Change(int startChangeIndex, int endChangeIndex, String changedString) {

		_startChangeIndex = startChangeIndex;
		_endChangeIndex = endChangeIndex;
		_changedString = changedString;
	}
	
	public String getChangedString() {
		return _changedString;
	}
	
	public int getStartChangeIndex() {
		return _startChangeIndex;
	}
	
	public int getEndChangeIndex() {
		return _endChangeIndex;
	}
}
