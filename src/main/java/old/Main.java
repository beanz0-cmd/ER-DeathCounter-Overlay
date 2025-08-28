package old;

import java.io.File;
import javafx.application.Application;

public class Main {
	public static void main(String[] args) {
		System.getProperty("jna.tmpdir", new File("assets").getAbsolutePath());
		System.out.println("=== DeathCounter gestartet ====");
		System.out.println("Aktuelle Zeit: " + java.time.LocalDateTime.now());
		Application.launch(ERDC_Overlay.class, args);
	}
}