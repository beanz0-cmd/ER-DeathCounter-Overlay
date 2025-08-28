package fordeathcounter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public class HotkeyConfig {
		
	private static final File CONFIG_DIR = new File("assets/configs");
	private static final File HOTKEY_CONF_FILE = new File(CONFIG_DIR, "hotkey.conf");
	
	//Default-Key-Codes
	private static final int DEFAULT_INCREMENT_KEY = NativeKeyEvent.VC_1;
	private static final int DEFAULT_RESET_KEY = NativeKeyEvent.VC_2;
	
	private final static int OPEN_CONFIG_KEY = NativeKeyEvent.VC_INSERT;
	private final static int CLOSE_OVERLAY_KEY = NativeKeyEvent.VC_END;
	
	private final static int MOVE_LEFT_KEY = NativeKeyEvent.VC_LEFT;
	private final static int MOVE_RIGHT_KEY = NativeKeyEvent.VC_RIGHT;
	private final static int MOVE_UP_KEY = NativeKeyEvent.VC_UP;
	private final static int MOVE_DOWN_KEY = NativeKeyEvent.VC_DOWN;
	
	private final static int INCREASE_SIZE_KEY = NativeKeyEvent.VC_EQUALS;
	private final static int DECREASE_SIZE_KEY = NativeKeyEvent.VC_MINUS;
	
	private final static int INCREASE_OPA_KEY = NativeKeyEvent.VC_T;
	private final static int DECREASE_OPA_KEY = NativeKeyEvent.VC_R;
	
	//Key-Codes-Text
	private static int incrementKey = DEFAULT_INCREMENT_KEY;
	private static int resetKey = DEFAULT_RESET_KEY;	

	public static void load() {
		if(!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
		
		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(HOTKEY_CONF_FILE)){
			props.load(in);
			incrementKey = Integer.parseInt(props.getProperty("incrementKey", String.valueOf(incrementKey)));
			resetKey = Integer.parseInt(props.getProperty("resetKey", String.valueOf(resetKey)));
		} catch (IOException | NumberFormatException ioe) {
			incrementKey = DEFAULT_INCREMENT_KEY;
			resetKey = DEFAULT_RESET_KEY;
			ioe.printStackTrace();
		}
	}
	
	public static void save() {
		Properties props = new Properties();
		props.setProperty("incrementKey", String.valueOf(incrementKey));
		props.setProperty("resetKey", String.valueOf(resetKey));
		
		if(!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
		
		try (FileOutputStream out = new FileOutputStream(HOTKEY_CONF_FILE)){
			props.store(out, "Hotkey configuration");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	//Getters and Setters	
	public static int getIncrementKey() {
		return incrementKey;
	}
	public static void setIncrementKey(int incrementKey) {
		HotkeyConfig.incrementKey = incrementKey;
		save();
	}
	public static int getResetKey() {
		return resetKey;
	}
	public static void setResetKey(int resetKey) {
		HotkeyConfig.resetKey = resetKey;
		save();
	}
	public static String getIncrementKeyStr() {
		return NativeKeyEvent.getKeyText(incrementKey);
	}
	public static String getResetKeyStr() {
		return NativeKeyEvent.getKeyText(resetKey);
	}
	public static int getOpenkonfigkey() {
		return OPEN_CONFIG_KEY;
	}
	public static String getOpenkonfigkeystr() {
		return NativeKeyEvent.getKeyText(OPEN_CONFIG_KEY);
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
