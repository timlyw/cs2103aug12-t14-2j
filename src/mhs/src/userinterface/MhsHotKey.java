package mhs.src.userinterface;

import java.util.logging.Logger;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import mhs.src.common.MhsLogger;

public class MhsHotKey implements HotkeyListener {

	static final Logger logger = MhsLogger.getLogger();

	public MhsHotKey() {
		// Initialize JIntellitype
		JIntellitype.getInstance();
		assignHotKey();
		assignHotKeyListener();
	}

	// assign this class to be a HotKeyListener
	private void assignHotKeyListener() {
		JIntellitype.getInstance().addHotKeyListener(this);
	}

	public void assignHotKey() {
		JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_WIN,
				(int) 'A');
	}

	public void unregisterhotKey() {
		JIntellitype.getInstance().unregisterHotKey(1);
	}

	// listen for hotkey
	public void onHotKey(int aIdentifier) {
		if (aIdentifier == 1)
			System.out.println("WINDOWS+A hotkey pressed");
	}

	// Termination, make sure to call before exiting
	public void cleanup() {
		JIntellitype.getInstance().cleanUp();
	}
}
