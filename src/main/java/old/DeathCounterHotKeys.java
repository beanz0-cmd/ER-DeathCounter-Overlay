package old;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DeathCounterHotKeys implements NativeKeyListener {
	private ERDC_Overlay overlay;

	private final static Label INFO_LABEL_FIN1 = new Label(
			"Overlay Controls: \n" +
			"Move Overlay \nUp/Down/Left/Right: \t\t" + "Str + " + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_UP) + "/" +
			NativeKeyEvent.getKeyText(NativeKeyEvent.VC_DOWN) + "/" + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_LEFT) +
			"/" + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_RIGHT) + 
			"\nChange Opacity +/-: \t\t" + "Str + " + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_T) + 
			"/" + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_R) + 
			"\nResize +/-: \t\t\t\t" + NativeKeyEvent.getKeyText(NativeKeyEvent.VC_EQUALS) + "(Plus)" + "/" +
			NativeKeyEvent.getKeyText(NativeKeyEvent.VC_MINUS)
			);
	private final static Label INFO_LABEL_FIN2 = new Label(
			"Open Slot Selection: \n" + 
			"Press " + HotkeyConfig.getOpenSlotSelectionKeyStr() + " to open Slot Selection.\n" +
			"Press again to close."
			);
	
	private Stage stage;	
	
	public DeathCounterHotKeys(ERDC_Overlay overlay) {
		this.overlay = overlay;
	}
	
	public void toggleWindow() {
		Platform.runLater(() -> {
			if (stage != null && stage.isShowing()) {
				stage.close();
			} else {
				showWindow();
			}
		});
	}
	
	public void showWindow() {
		
		if(stage == null) {		
		stage = new Stage();
		stage.setTitle("Help Menu (Press " + HotkeyConfig.getOpenkonfigkeystr() + " again to close)" );
		stage.initStyle(StageStyle.UTILITY);
		stage.initModality(Modality.NONE);
		stage.setX(200);
		stage.setY(20);
		stage.setAlwaysOnTop(true);
		stage.setResizable(false);
		stage.requestFocus();
		
		FlowPane root = new FlowPane();
		root.setPadding(new Insets(10));
		root.setHgap(10);
		root.setVgap(10);
		root.setBackground(new Background(new BackgroundFill(Color.rgb(128, 128, 128), new CornerRadii(0), Insets.EMPTY)));

		root.getChildren().addAll(INFO_LABEL_FIN1, INFO_LABEL_FIN2);

		Scene scene = new Scene(root, 350, 200);
		stage.setScene(scene);
		stage.show();
		}
		
		if (!stage.isShowing()) {
			stage.show();
			stage.toFront();
		}
	}
	
	public void nativeKeyPressed(NativeKeyEvent e) {
		int code = e.getKeyCode();
		
//		System.out.println("Key pressed: " + NativeKeyEvent.getKeyText(code)); //Debug
		
		if (code == HotkeyConfig.getOpenkonfigkey()) {
			toggleWindow();
			return;
		}
		
		EldenRingSaveParser parser = new EldenRingSaveParser(overlay);
		
		if (code == HotkeyConfig.getOpenSlotSelectionKey()) {
			parser.toggleWindow();
			return;
		}

		if(code == HotkeyConfig.getCloseOverlayKey()) {
			try {
			GlobalScreen.unregisterNativeHook();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
			System.exit(0);
		}
		
		int modifiers = e.getModifiers();
		if ((modifiers & NativeKeyEvent.CTRL_MASK) != 0 
				&& (modifiers & NativeKeyEvent.SHIFT_MASK) == 0 &&
				(modifiers & NativeKeyEvent.ALT_MASK) == 0) {
		    if (code == HotkeyConfig.getMoveLeftKey()) {
		        overlay.moveOverlay(-10, 0);
		    } else if (code == HotkeyConfig.getMoveRightKey()) {
		        overlay.moveOverlay(10, 0);
		    } else if (code == HotkeyConfig.getMoveUpKey()) {
		        overlay.moveOverlay(0, -10);
		    } else if (code == HotkeyConfig.getMoveDownKey()) {
		    	overlay.moveOverlay(0, 10);
		    } else if (code == HotkeyConfig.getIncreaseSizeKey()) {
		        overlay.changeScale(0.1);
		    } else if (code == HotkeyConfig.getDecreaseSizeKey()) {
		        overlay.changeScale(-0.1);
		    } else if (code == HotkeyConfig.getDecreaseOpaKey()) {
		        overlay.changeOpacity(-0.05);
		    } else if (code == HotkeyConfig.getIncreaseOpaKey()) {
		        overlay.changeOpacity(0.05);
		    }
		}
	}
}