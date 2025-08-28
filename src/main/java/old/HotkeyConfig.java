package old;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class HotkeyConfig {
	
	//Default-Key-Codes
	private final static int OPEN_CONFIG_KEY = NativeKeyEvent.VC_INSERT;
	private final static int OPEN_SLOT_SELECTION_KEY = NativeKeyEvent.VC_BACKSPACE;
	private final static int CLOSE_OVERLAY_KEY = NativeKeyEvent.VC_END;
	
	private final static int MOVE_LEFT_KEY = NativeKeyEvent.VC_LEFT;
	private final static int MOVE_RIGHT_KEY = NativeKeyEvent.VC_RIGHT;
	private final static int MOVE_UP_KEY = NativeKeyEvent.VC_UP;
	private final static int MOVE_DOWN_KEY = NativeKeyEvent.VC_DOWN;
	
	private final static int INCREASE_SIZE_KEY = NativeKeyEvent.VC_EQUALS;
	private final static int DECREASE_SIZE_KEY = NativeKeyEvent.VC_MINUS;
	
	private final static int INCREASE_OPA_KEY = NativeKeyEvent.VC_T;
	private final static int DECREASE_OPA_KEY = NativeKeyEvent.VC_R;
	
	//Getters and Setters
	public static int getOpenkonfigkey() {
		return OPEN_CONFIG_KEY;
	}
	
	public static String getOpenkonfigkeystr() {
		return NativeKeyEvent.getKeyText(OPEN_CONFIG_KEY);
	}
	
	public static int getOpenSlotSelectionKey() {
		return OPEN_SLOT_SELECTION_KEY;
	}
	
	public static String getOpenSlotSelectionKeyStr() {
		return NativeKeyEvent.getKeyText(OPEN_SLOT_SELECTION_KEY);
	}
	
	public static int getCloseOverlayKey() {
		return CLOSE_OVERLAY_KEY;
	}
	
	public static String getCloseOverlayKeyStr() {
		return NativeKeyEvent.getKeyText(CLOSE_OVERLAY_KEY);
	}
	
	public static int getMoveLeftKey() {
		return MOVE_LEFT_KEY;
	}

	public static int getMoveRightKey() {
		return MOVE_RIGHT_KEY;
	}

	public static int getMoveUpKey() {
		return MOVE_UP_KEY;
	}

	public static int getMoveDownKey() {
		return MOVE_DOWN_KEY;
	}

	public static int getIncreaseSizeKey() {
		return INCREASE_SIZE_KEY;
	}

	public static int getDecreaseSizeKey() {
		return DECREASE_SIZE_KEY;
	}

	public static int getIncreaseOpaKey() {
		return INCREASE_OPA_KEY;
	}

	public static int getDecreaseOpaKey() {
		return DECREASE_OPA_KEY;
	}
}
