package old;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EldenRingSaveParser implements NativeKeyListener {

    private static final int SLOT_START_INDEX = 0x310;
    private static final int SLOT_LENGTH = 0x280000;
    private static final int NUM_SLOTS = 10;

    private static final int SAVE_HEADER_LENGTH = 0x24C;
    private static final int SAVE_HEADER_START_INDEX = 0x1901D0E;
    private static final int CHAR_ACTIVE_STATUS_START_INDEX = 0x1901D04;

    private static final int CHAR_NAME_LENGTH = 0x22;
    private static final int CHAR_LEVEL_OFFSET = 0x22;
    private static final int CHAR_PLAYTIME_OFFSET = 0x26;

    private List<SaveProfile> profiles;
    
    private ERDC_Overlay overlay;

    private final Label nameLabel = new Label("Name: ");
    private final Label levelLabel = new Label("Level: ");
    private final Label timeLabel = new Label("Playtime: ");
    private final Label deathLabel = new Label("Deaths: ");
    
    private Stage primaryStage;
    private Consumer<Integer> onSlotSelected;
    private ComboBox<Integer> slotSelector;

    public EldenRingSaveParser (ERDC_Overlay overlay) {
    	this.overlay = overlay;
    }
    

    public void showWindow(Consumer<Integer> onSlotSelected) {
        this.onSlotSelected = onSlotSelected;
        
        if (primaryStage == null) {
            primaryStage = new Stage();
            
            profiles = overlay.getProfiles();
            slotSelector = new ComboBox<>();

            int selectedSlot = -1;

            for (SaveProfile profile : profiles) {
                if (profile.active) {
                    slotSelector.getItems().add(profile.slot);
                    if (profile.isFocused) {
                        selectedSlot = profile.slot;
                        break;
                    }
                }
            }

            if (selectedSlot == -1 && !slotSelector.getItems().isEmpty()) {
                selectedSlot = slotSelector.getItems().get(0);
            }

            if (selectedSlot != -1) {
                slotSelector.setValue(selectedSlot);
                updateSlotInfo(selectedSlot);

                for (SaveProfile p : profiles) {
                    p.isFocused = (p.slot == selectedSlot);
                }
            } else {
                System.out.println("Keine aktiven Slots verfügbar.");
                return;
            }

            slotSelector.setOnAction(e -> {
                Integer selectedSlotValue = slotSelector.getValue();
                if (selectedSlotValue == null) return;

                for (SaveProfile p : profiles) {
                    p.isFocused = (p.slot == selectedSlotValue);
                }
                updateSlotInfo(selectedSlotValue);

                if (onSlotSelected != null) {
                    onSlotSelected.accept(selectedSlotValue);
                }

                overlay.setProfiles(profiles);
                overlay.setCurrentFocusedSlot(selectedSlotValue);
                overlay.updateFocusedDeathCount();
            });

            Button closeBtn = new Button("Schließen");
            closeBtn.setOnAction(e -> {
                primaryStage.close();
                primaryStage = null;
            });

            VBox layout = new VBox(10, new Label("Pick Character:"), slotSelector,
                    nameLabel, levelLabel, timeLabel, deathLabel, closeBtn);
            layout.setStyle("-fx-padding: 20; -fx-font-size: 14;");
            layout.setPrefSize(300, 300);
            primaryStage.setScene(new Scene(layout));
            primaryStage.setTitle("Elden Ring Slot Viewer");
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();
        }
        
        if (!primaryStage.isShowing()) {
            primaryStage.show();
            primaryStage.requestFocus();
            primaryStage.toFront();
        }
    }

    public void toggleWindow() {
        Platform.runLater(() -> {
            if (primaryStage != null && primaryStage.isShowing()) {
                primaryStage.close();
                primaryStage = null;
            } else {
                showWindow(onSlotSelected);
            }
        });
    }

    private void updateSlotInfo(int slotIndex) {
        SaveProfile profile = null;
        for (SaveProfile p : profiles) {
           if (p.slot == slotIndex) {
        	   p.isFocused = true;
        	   profile = p;
           } else {
        	   p.isFocused = false;
           }
        }

        if (profile == null) {
            nameLabel.setText("Name: -");
            levelLabel.setText("Level: -");
            timeLabel.setText("Playtime: -");
            deathLabel.setText("Deaths: -");
            return;
        }

        nameLabel.setText("Name: \t\t" + profile.name);
        levelLabel.setText("Level: \t\t" + profile.level);
        timeLabel.setText("Playtime: \t\t" + profile.formatTime());
        deathLabel.setText("Deaths: \t\t" + profile.deathCount);
    }

    public static List<SaveProfile> extractProfiles(File saveFile) throws IOException {
        byte[] fileData = Files.readAllBytes(saveFile.toPath());
        List<SaveProfile> profiles = new ArrayList<>(NUM_SLOTS);
        for (int i = 0; i < NUM_SLOTS; i++) {
            SaveProfile profile = readSlot(fileData, i);
            if (profile != null) {
                profiles.add(profile);
            }
        }
        return profiles;
    }

    private static SaveProfile readSlot(byte[] fileData, int slotIndex) {
        int slotOffset = SLOT_START_INDEX + (slotIndex * SLOT_LENGTH);
        int headerOffset = SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH);

        if (slotOffset + SLOT_LENGTH > fileData.length || headerOffset + SAVE_HEADER_LENGTH > fileData.length) {
            return null; // Defekter Slot
        }

        boolean isActive = fileData[CHAR_ACTIVE_STATUS_START_INDEX + slotIndex] == 1;
        if (!isActive) {
            return new SaveProfile(slotIndex, "", 0, 0, -1, false, null);
        }

        // Name (UTF-16LE)
        String name = new String(fileData, headerOffset, CHAR_NAME_LENGTH, StandardCharsets.UTF_16LE)
                .replace("\u0000", "").trim();

        // Level (2 Bytes LE)
        int level = ((fileData[headerOffset + CHAR_LEVEL_OFFSET] & 0xFF) |
                ((fileData[headerOffset + CHAR_LEVEL_OFFSET + 1] & 0xFF) << 8));

        // Playtime (4 Bytes LE)
        long seconds = ((fileData[headerOffset + CHAR_PLAYTIME_OFFSET] & 0xFFL) |
                ((fileData[headerOffset + CHAR_PLAYTIME_OFFSET + 1] & 0xFFL) << 8) |
                ((fileData[headerOffset + CHAR_PLAYTIME_OFFSET + 2] & 0xFFL) << 16) |
                ((fileData[headerOffset + CHAR_PLAYTIME_OFFSET + 3] & 0xFFL) << 24));

        // Death Count
        int deathCount = findDeathCountCandidate(fileData, slotOffset, SLOT_LENGTH);

        return new SaveProfile(slotIndex, name, level, seconds, deathCount, true, null);
    }

    private static int findDeathCountCandidate(byte[] data, int offset, int length) {
        for (int i = 0; i <= length - 12; i++) {
            int pos = offset + i;
            int possibleDeathCount =
                    ((data[pos] & 0xFF)) |
                            ((data[pos + 1] & 0xFF) << 8) |
                            ((data[pos + 2] & 0xFF) << 16) |
                            ((data[pos + 3] & 0xFF) << 24);

            int marker1 =
                    ((data[pos + 4] & 0xFF)) |
                            ((data[pos + 5] & 0xFF) << 8) |
                            ((data[pos + 6] & 0xFF) << 16) |
                            ((data[pos + 7] & 0xFF) << 24);

            int marker2 =
                    ((data[pos + 8] & 0xFF)) |
                            ((data[pos + 9] & 0xFF) << 8) |
                            ((data[pos + 10] & 0xFF) << 16) |
                            ((data[pos + 11] & 0xFF) << 24);

            if (marker1 == 0xFFFFFFFF && marker2 == 0x00000800 && possibleDeathCount >= 0) {
                return possibleDeathCount;
            }
        }
        return -1;
    }

    public void updateSlots(List<SaveProfile> updatedProfiles) {
        this.profiles = updatedProfiles;
        overlay.setProfiles(updatedProfiles);

        Platform.runLater(() -> {
            if (slotSelector == null) return;

            Integer current = slotSelector.getValue();
            slotSelector.getItems().clear();
            for (SaveProfile p : profiles) {
                if (p.active) slotSelector.getItems().add(p.slot);
            }

            if (current != null && slotSelector.getItems().contains(current)) {
                slotSelector.setValue(current);
                updateSlotInfo(current);
            } else {
                for (SaveProfile p : profiles) {
                    if (p.isFocused && slotSelector.getItems().contains(p.slot)) {
                        slotSelector.setValue(p.slot);
                        updateSlotInfo(p.slot);
                        break;
                    }
                }
            }
        });
    }
}
