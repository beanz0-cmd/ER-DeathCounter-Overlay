package fordeathcounter;

import java.io.File;

import javafx.application.Application;

public class Main {
	public static void main(String[] args) {
		System.getProperty("jna.tmpdir", new File("assets").getAbsolutePath());
		HotkeyConfig.load();
		System.out.println("=== DeathCounter gestartet ====");
		System.out.println("Aktuelle Zeit: " + java.time.LocalDateTime.now());
		Application.launch(ManDeathCounterOverlay.class, args);
	}
}