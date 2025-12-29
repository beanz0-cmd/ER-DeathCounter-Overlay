package autodeathcounter;

import com.github.kwhat.jnativehook.GlobalScreen;

import javafx.application.Application;

public class Main {
	public static void main(String[] args) {
		System.out.println("=== DeathCounter started ====");
		System.out.println("Current Time: " + java.time.LocalDateTime.now());
		
		System.setProperty("jnativehook.lib.location", System.getProperty("java.io.tmpdir"));
		
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(java.util.logging.Level.WARNING);
		for(var h : logger.getHandlers()) h.setLevel(java.util.logging.Level.WARNING);
		
		Application.launch(Overlay.class, args);
	}
}
