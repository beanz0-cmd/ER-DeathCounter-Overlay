package autodeathcounter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import java.util.logging.Logger;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Hotkeys implements NativeKeyListener {
//	private static final Logger LOGGER = Logger.getLogger(Hotkeys.class.getName());
	
	//General-Key-Codes
	public final static int CLOSE_OVERLAY_KEY = NativeKeyEvent.VC_END;
	public final static int OPEN_CONFIG_KEY = NativeKeyEvent.VC_INSERT;
	
	//Move-Key-Codes
	public final static int MOVE_LEFT_KEY = NativeKeyEvent.VC_LEFT;
	public final static int MOVE_RIGHT_KEY = NativeKeyEvent.VC_RIGHT;
	public final static int MOVE_UP_KEY = NativeKeyEvent.VC_UP;
	public final static int MOVE_DOWN_KEY = NativeKeyEvent.VC_DOWN;
	
	//Size-Key-Codes
	public final static int INCREASE_SIZE_KEY = NativeKeyEvent.VC_EQUALS; //plus
	public final static int DECREASE_SIZE_KEY = NativeKeyEvent.VC_MINUS;
	
	//Opacity-Key-Codes
	public final static int INCREASE_OPA_KEY = NativeKeyEvent.VC_T;
	public final static int DECREASE_OPA_KEY = NativeKeyEvent.VC_R;
	
	//Overlay
	private final Overlay overlay;
	
	//Key repeat guard
	private final Map<Integer, Long> lastKeyAt = new ConcurrentHashMap<>();
	private static final long REPEAT_GUARD_MS = 10;
	
	public Hotkeys (Overlay overlay) {
		this.overlay = overlay;
	}
	
	//Hotkey-Methods
	public void nativeKeyPressed(NativeKeyEvent e) {
	    int code = e.getKeyCode();
//	    LOGGER.info("Key pressed: " + NativeKeyEvent.getKeyText(code)); // Debug
	    
	    if(!acceptEvent(e)) return;

	    if (code == OPEN_CONFIG_KEY) {
	        javafx.application.Platform.runLater(overlay::toggleSlotSelection);
	        return;
	    }

	    if (code == CLOSE_OVERLAY_KEY) {
	        try { GlobalScreen.unregisterNativeHook(); } catch (Exception ignore) {}
	        overlay.shutdownExecutors();
	        System.exit(0);
	    }
	    
	    if (isOnlyCtrlPressed(e)) {
	        switch (code) {
	            case MOVE_LEFT_KEY -> javafx.application.Platform.runLater(() -> overlay.moveOverlay(-10, 0));
	            case MOVE_RIGHT_KEY -> javafx.application.Platform.runLater(() -> overlay.moveOverlay(10, 0));
	            case MOVE_UP_KEY -> javafx.application.Platform.runLater(() -> overlay.moveOverlay(0, -10));
	            case MOVE_DOWN_KEY -> javafx.application.Platform.runLater(() -> overlay.moveOverlay(0, 10));
	            case INCREASE_SIZE_KEY -> javafx.application.Platform.runLater(() -> overlay.changeScale(0.1));
	            case DECREASE_SIZE_KEY -> javafx.application.Platform.runLater(() -> overlay.changeScale(-0.1));
	            case DECREASE_OPA_KEY -> javafx.application.Platform.runLater(() -> overlay.changeOpacity(-0.05));
	            case INCREASE_OPA_KEY -> javafx.application.Platform.runLater(() -> overlay.changeOpacity(0.05));
	            default -> {}
	        }
	    }
	}
	
	private boolean acceptEvent(NativeKeyEvent e) {
		long now = System.currentTimeMillis();
		long last = lastKeyAt.getOrDefault(e.getKeyCode(), 0L);
		if(now - last < REPEAT_GUARD_MS) return false;
		lastKeyAt.put(e.getKeyCode(), now);
		return true;
	}

	private boolean isOnlyCtrlPressed(NativeKeyEvent e) {
	    int modifiers = e.getModifiers();
	    return (modifiers & NativeKeyEvent.CTRL_MASK) != 0
	        && (modifiers & NativeKeyEvent.SHIFT_MASK) == 0
	        && (modifiers & NativeKeyEvent.ALT_MASK) == 0;
	}
	
	//unused
	public void nativeKeyReleased(NativeKeyEvent e) {}

	public void nativeKeyTyped(NativeKeyEvent e) {}
}
