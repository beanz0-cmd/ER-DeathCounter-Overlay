package fordeathcounter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ManDeathCounterOverlay extends Application{
	
	private static final File CONFIG_DIR = new File("assets/configs");
	private static final File DEATHCOUNT_CONF_FILE = new File(CONFIG_DIR, "deathcounter.conf");
	private static final File OVERLAY_CONF_FILE = new File(CONFIG_DIR, "overlay.conf"); 

	private static Label counterLabel;
	private Label infoLabel;
	private Label infoLabel2;
	private static int deathCount = 0;
	
	private Stage stage;
	private Scene scene;
	private VBox root;
	
	private double baseWidth;
	private double baseHeight;
	private double scale = 1.0;
	private double offset = 10;
	private static final double BASE_FONT_SIZE_BIG = 28;
	private static final double BASE_FONT_SIZE_SMALL = 10;
	
	public void start(Stage primaryStage) throws Exception {
		if(primaryStage != null && primaryStage.isShowing()) {
			Platform.runLater(() -> {
				primaryStage.toFront();
				primaryStage.requestFocus();
			});
			return;
		}
		counterLabel = new Label("Deaths: 0");
		counterLabel.setFont(Font.font("Arial", FontWeight.BOLD, BASE_FONT_SIZE_BIG));
		counterLabel.setTextFill(Color.WHITE);
		counterLabel.setEffect(new DropShadow(2, Color.BLACK));

		infoLabel = new Label("Open Config: " + HotkeyConfig.getOpenkonfigkeystr());
		infoLabel.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL));
		infoLabel.setTextFill(Color.LIGHTGREY);
		
		infoLabel2 = new Label("Close Overlay: " + HotkeyConfig.getCloseOverlayKeyStr());
		infoLabel2.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL));
		infoLabel2.setTextFill(Color.LIGHTGREY);
		
		root = new VBox(counterLabel, infoLabel, infoLabel2);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(10));
		root.setBackground(new Background(new BackgroundFill(Color.rgb(30, 30, 30), new CornerRadii(20), Insets.EMPTY)));
		root.setMouseTransparent(true);
		
		scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		Platform.runLater(() -> {
		    initBaseSize();
		});
		
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
		
		setDeathCount(loadDeathcount());
		
		startFadeOutTimer(infoLabel, 30);
		startFadeOutTimer(infoLabel2, 30);
		
		this.stage = primaryStage;
		
		loadOverlayConfig();
		
		Platform.runLater(() -> {
		    root.setPrefWidth(baseWidth * scale);
		    root.setPrefHeight(baseHeight * scale);
		    
		    counterLabel.setFont(Font.font("Arial", FontWeight.BOLD, BASE_FONT_SIZE_BIG * scale));
		    infoLabel.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL * scale));
		    infoLabel2.setFont(Font.font("Arial", BASE_FONT_SIZE_SMALL * scale));
		    
		    stage.setWidth(baseWidth * scale);
		    stage.setHeight(baseHeight * scale);
		    stage.setOpacity(stage.getOpacity());
		});
		
		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new DeathCounterHotKeys(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		primaryStage.setOnCloseRequest(e -> {
			try {
				GlobalScreen.unregisterNativeHook();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		});
		
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
	
	public void initBaseSize() {
		baseWidth = root.getWidth() > 0 ? root.getWidth() : 200;
		baseHeight = root.getHeight() > 0 ? root.getHeight() : 75;
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
	        infoLabel.setFont(Font.font("Arial", smallFontSize));
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
	
	public void incrementCounter() {
		deathCount++;
		Platform.runLater(()-> {				
			counterLabel.setText("Deaths: " + deathCount);
		});
		updateCounterLabel();
		saveDeathCount();
	}
	
	public void resetCounter() {
		deathCount = 0;
		Platform.runLater(()-> {				
			counterLabel.setText("Deaths: " + deathCount);
		});
		updateCounterLabel();
		saveDeathCount();
	}
	
	public void updateCounterLabel() {
		Platform.runLater(() -> {
			counterLabel.setText("Deaths: " + deathCount);
			Stage stage = (Stage) counterLabel.getScene().getWindow();
			stage.sizeToScene();
		});
	}
	
	public static int loadDeathcount() {
		if(!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
		File file = DEATHCOUNT_CONF_FILE;
		if(!file.exists()) {
			deathCount = 0;
			return deathCount;
		}
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = br.readLine();
			deathCount = Integer.parseInt(line.trim());
		}catch (Exception e) {
			deathCount = 0;
			e.printStackTrace();
		}
		return deathCount;
	}
	
	public static void saveDeathCount() {
		if(!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(DEATHCOUNT_CONF_FILE))){
			bw.write(String.valueOf(deathCount));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void setDeathCount(int count) {
		ManDeathCounterOverlay.deathCount = count;
		Platform.runLater(()-> {				
			counterLabel.setText("Deaths: " + deathCount);
			updateCounterLabel();
		});
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
	
	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
