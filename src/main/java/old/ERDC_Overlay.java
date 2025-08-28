package old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.kwhat.jnativehook.GlobalScreen;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class ERDC_Overlay extends Application {
	
	private static final File CONFIG_DIR = new File("assets/configs");
	private static final File OVERLAY_CONF_FILE = new File(CONFIG_DIR, "overlay.conf");
	private static final File DIR = new File("C:\\Users\\Nutzer\\AppData\\Roaming\\EldenRing");
	private static File saveFile;
	
	private static Label counterLabel;
	private Label infoLabel1;
	private Label infoLabel2;
	private static ArrayList<Integer> deathCounts;
	private int currentDeathCount;
	private int oldDeathCount;
	
	private Stage stage;
	private Scene scene;
	private VBox root;
	
	private double baseWidth;
	private double baseHeight;
	private double scale = 1.0;
	private double offset = 10;
	private static final double BASE_FONT_SIZE_BIG = 28;
	private static final double BASE_FONT_SIZE_SMALL = 10;
	
	private ScheduledExecutorService saveFileWatcher;
	private volatile int currentFocusedSlot = -1;
	
	private List<SaveProfile> profiles;
	
	public void start(Stage primaryStage) throws Exception {
		System.out.println("Start-Methode gestartet."); //Debug
		
		saveFile = chooseSaveFile(primaryStage);
		profiles = EldenRingSaveParser.extractProfiles(saveFile);
		
		if(profiles.isEmpty()) throw new Exception("Error while reading profiles."); //Check
		
		deathCounts = new ArrayList<Integer>();
		for(SaveProfile p : profiles) {
			deathCounts.add(p.deathCount);
		}
		
		EldenRingSaveParser parserWindow = new EldenRingSaveParser(this);
		
		parserWindow.showWindow(selectedSlot -> {
			
			try {
				profiles = EldenRingSaveParser.extractProfiles(saveFile);
				
				for(SaveProfile p : profiles) {
					p.isFocused = (p.slot == selectedSlot);
				}		
				
				deathCounts.clear();
				for(SaveProfile p : profiles) {
					deathCounts.add(p.deathCount);
				}
				
				setCurrentFocusedSlot(selectedSlot);
				
				if(primaryStage == null || !primaryStage.isShowing()) {
					startOverlay(primaryStage);
				} else {
					updateFocusedDeathCount();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		startSaveFileWatcher(parserWindow);
	}
	
	private void startOverlay(Stage primaryStage) throws IllegalArgumentException {
		
		counterLabel = new Label("Deaths:");
		counterLabel.setFont(Font.font("Arial", FontWeight.BOLD, BASE_FONT_SIZE_BIG));
		counterLabel.setTextFill(Color.WHITE);
		counterLabel.setEffect(new DropShadow(2, Color.BLACK));
		
		infoLabel1 = new Label("Press:  " + HotkeyConfig.getOpenkonfigkeystr() + " for Help");
		infoLabel1.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL));
		infoLabel1.setTextFill(Color.LIGHTGREY);
		
		infoLabel2 = new Label("Press: " + HotkeyConfig.getCloseOverlayKeyStr() + " to close Overlay");
		infoLabel2.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL));
		infoLabel2.setTextFill(Color.LIGHTGREY);
		
		root = new VBox(counterLabel, infoLabel1, infoLabel2);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(10));
		root.setBackground(new Background(new BackgroundFill(Color.rgb(30, 30, 30), new CornerRadii(20), Insets.EMPTY)));
		root.setMouseTransparent(true);
		
		scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		Platform.runLater(() -> {
		    initBaseSize();
		});
		
		this.stage = primaryStage;
		
		
		primaryStage.initStyle(StageStyle.TRANSPARENT);
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setScene(scene);
		primaryStage.setOpacity(0.8);
		primaryStage.setMinWidth(200);
		primaryStage.setMinHeight(75);
		primaryStage.setMaxWidth(1900);
		primaryStage.setMaxHeight(1100);
		primaryStage.setX(20);
		primaryStage.setY(20);
		primaryStage.show();
		
		startFadeOutTimer(infoLabel1, 30);
		startFadeOutTimer(infoLabel2, 30);
		
		loadOverlayConfig();
		loadDeathCounter();
		
		Platform.runLater(() -> {
		    root.setPrefWidth(baseWidth * scale);
		    root.setPrefHeight(baseHeight * scale);
		    
		    counterLabel.setFont(Font.font("Arial", FontWeight.BOLD, BASE_FONT_SIZE_BIG * scale));
		    infoLabel1.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL * scale));
		    infoLabel2.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL * scale));
		    
		    stage.setWidth(baseWidth * scale);
		    stage.setHeight(baseHeight * scale);
		    stage.setOpacity(stage.getOpacity());
		});
		
		try {
			if(!GlobalScreen.isNativeHookRegistered()) {
				GlobalScreen.registerNativeHook();				
			}
			GlobalScreen.addNativeKeyListener(new DeathCounterHotKeys(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stage.setOnCloseRequest(e -> {
			if(saveFileWatcher != null) {
				saveFileWatcher.shutdownNow();
			}
			try {
				GlobalScreen.unregisterNativeHook();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		});
	}
	
	private void startSaveFileWatcher(EldenRingSaveParser parserWindow) {
	    saveFileWatcher = Executors.newSingleThreadScheduledExecutor();
	    saveFileWatcher.scheduleAtFixedRate(() -> {
	        try {
	            List<SaveProfile> updatedProfiles = EldenRingSaveParser.extractProfiles(saveFile);

	            boolean changed = false;
	            
	            while(deathCounts.size() < updatedProfiles.size()) {
        			deathCounts.add(0);
        		}
	            System.out.println(deathCounts.toString()); //Debug
	            
	            for (SaveProfile updated : updatedProfiles) {
	            	int slot = updated.slot;
	            	int oldCount = deathCounts.get(slot);
	            	
	                if (updated.deathCount != oldCount && updated.deathCount > oldCount) {
	                    deathCounts.set(slot, updated.deathCount);
	                    changed = true;
	                }
	            }

	            if (currentFocusedSlot >= 0 && currentFocusedSlot < deathCounts.size()) {
	               currentDeathCount = deathCounts.get(currentFocusedSlot);
	               System.out.printf("focusedCount=%d, oldFocusedCount=%d\n", currentDeathCount, oldDeathCount); //Debug
	               updateFocusedDeathCount();
	            }

	            if (changed) {
	                Platform.runLater(() -> {
	                    if (parserWindow != null) {
	                        parserWindow.updateSlots(updatedProfiles);
	                    }
						setProfiles(updatedProfiles);
	                    updateFocusedDeathCount();
	                });
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }, 0, 2, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public File chooseSaveFile(Window ownerWindow) {
		FileChooser fC = new FileChooser();
		fC.setTitle("Choose Elden Ring Save-File");
		fC.setInitialDirectory(DIR);
		fC.getExtensionFilters()
			.add(new FileChooser
			.ExtensionFilter("Elden Ring Save File (*.sl2)", "*.sl2"));
		
		return fC.showOpenDialog(ownerWindow);
	}
	
	private void initBaseSize() {
		baseWidth = root.getWidth() > 0 ? root.getWidth() : 200;
		baseHeight = root.getHeight() > 0 ? root.getHeight() : 75;
	}
	
	public void startFadeOutTimer(Label label, int secondsDelay) {
		FadeTransition fade = new FadeTransition(Duration.seconds(2), label);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		fade.setDelay(Duration.seconds(secondsDelay));
		fade.setOnFinished(e -> {
			Platform.runLater(() -> {
				root.getChildren().remove(label);
			});
		});
		fade.play();
	}
	
	private void loadOverlayConfig() {
	    if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();

	    if (!OVERLAY_CONF_FILE.exists()) {
	        return;
	    }

	    try (BufferedReader br = new BufferedReader(new FileReader(OVERLAY_CONF_FILE))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] parts = line.split("=");
	            if (parts.length != 2) continue;
	            String key = parts[0].trim();
	            String value = parts[1].trim();

	            switch (key) {
	                case "posX": stage.setX(Double.parseDouble(value)); break;
	                case "posY": stage.setY(Double.parseDouble(value)); break;
	                case "width": baseWidth = Double.parseDouble(value); break;
	                case "height": baseHeight = Double.parseDouble(value); break;
	                case "opacity": stage.setOpacity(Double.parseDouble(value)); break;
	                case "scale": scale = Double.parseDouble(value); break;
	            }
	        }
	    } catch (IOException | NumberFormatException e) {
	        System.out.println("Fehler beim Laden der Overlay-Konfiguration");
	        e.printStackTrace();
	    }
	}
	
	private void saveOverlayConfig() {
	    if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();

	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(OVERLAY_CONF_FILE))) {
	        bw.write("posX=" + stage.getX() + "\n");
	        bw.write("posY=" + stage.getY() + "\n");

	        bw.write("width=" + baseWidth + "\n");
	        bw.write("height=" + baseHeight + "\n");
	        bw.write("opacity=" + stage.getOpacity() + "\n");
	        bw.write("scale=" + scale + "\n");
	    } catch (IOException e) {
	        System.out.println("Fehler beim Speichern der Overlay-Konfiguration");
	        e.printStackTrace();
	    }
	}
	
	public void moveOverlay(double dx, double dy) {
		if(stage != null) {
			Platform.runLater(() -> {				
				stage.setX(stage.getX() + dx);
				stage.setY(stage.getY() + dy);
				saveOverlayConfig();
			});
		}
	}
	
	public void changeScale(double delta) {
	    scale = Math.max(0.5, Math.min(10.0, scale + delta));
	    
	    Platform.runLater(() -> {	        
	        double newWidth = baseWidth * scale;
	        double newHeight = baseHeight * scale;
	        
	        root.setPrefWidth(newWidth);
	        root.setPrefHeight(newHeight);
	        
	        double bigFontSize = BASE_FONT_SIZE_BIG*scale;
	        double smallFontSize = BASE_FONT_SIZE_SMALL*scale;
	        
	        counterLabel.setFont(Font.font("Arial", FontWeight.BOLD, bigFontSize));
	        infoLabel1.setFont(Font.font("Arial", smallFontSize));
	        infoLabel2.setFont(Font.font("Arial", smallFontSize));
	        
	        stage.sizeToScene();
	        
	        saveOverlayConfig();
	    });
	}
	
	public void changeOpacity(double delta) {
		double current = stage.getOpacity();
		double newOpacity = Math.min(1.0, Math.max(0.1, current + delta));
		Platform.runLater(() -> {
			stage.setOpacity(newOpacity);	
			saveOverlayConfig();
		});
		System.out.println("New Opacity: " + newOpacity); //Debug
	}
	
	public void loadDeathCounter() throws IllegalArgumentException {
		SaveProfile active = null;
		for(SaveProfile profile : profiles) {
			if(profile.isFocused) {
				active = profile;
				break;
			}
		}
		if(active == null) {
			throw new IllegalArgumentException("Kein aktives Profil gefunden.");
		}
		int slotNumber = active.slot;
		Platform.runLater(() -> {	
			setCurrentDeathCount(deathCounts.get(slotNumber));
			counterLabel.setText("Deaths: " + deathCounts.get(slotNumber));
			Stage stage = (Stage) counterLabel.getScene().getWindow();
			stage.sizeToScene();
		});
	}
	
	public void updateFocusedDeathCount() {
	    if (profiles == null || counterLabel == null) return;
	    
	    setOldDeathCount(getCurrentDeathCount());
	    
	    for (SaveProfile profile : profiles) {
	        if (profile.isFocused) {
	            setCurrentDeathCount(profile.deathCount);
	            if(currentDeathCount == oldDeathCount + 1 || deathCounts.contains(currentDeathCount)) {
	            	Platform.runLater(() -> counterLabel.setText("Deaths: " + currentDeathCount));
	            }
	            return;
	        }
	    }
	    if (currentFocusedSlot >= 0 && currentFocusedSlot <= deathCounts.size()) {
	        setCurrentDeathCount(deathCounts.get(currentFocusedSlot));
	        if(currentDeathCount == oldDeathCount + 1 || deathCounts.contains(currentDeathCount)) {
	        	Platform.runLater(() -> counterLabel.setText("Deaths: " + currentDeathCount));
	        }
	        return;
	    }
	}
	
	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}
	
	public static File getSaveFile() {
		return saveFile;
	}
	
	public List<SaveProfile> getProfiles() {
		return profiles;
	}
	
	public void setProfiles(List<SaveProfile> newProfiles) {
		profiles = newProfiles;
	}
	
	public void setCurrentFocusedSlot(int slot) {
		this.currentFocusedSlot = slot;
	}
	
	public int getCurrentDeathCount() {
		return currentDeathCount;
	}
	
	public void setCurrentDeathCount(int currentDeathCount) {
		this.currentDeathCount = currentDeathCount;
	}
	
	public int getOldDeathCount() {
		return oldDeathCount;
	}
	
	public void setOldDeathCount(int oldDeathCount) {
		this.oldDeathCount = oldDeathCount;
	}
}
