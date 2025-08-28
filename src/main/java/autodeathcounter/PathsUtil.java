package autodeathcounter;

import java.io.File;

final class PathsUtil {
	private PathsUtil() {}
	
	static File resolveEldenRingDir() {
		 String os = System.getProperty("os.name").toLowerCase();
		    
		 if (os.contains("win")) {
			 String appdata = System.getenv("APPDATA");
			 if (appdata != null && !appdata.isBlank()) return new File(appdata, "EldenRing");
			 return new File(System.getProperty("user.home"), "AppData/Roaming/EldenRing");
		 } else if (os.contains("mac")) {
			 return new File(System.getProperty("user.home"), "Library/Application Support/EldenRing");
		 } else {
			 return new File(System.getProperty("user.home"), ".local/share/EldenRing");
		 }
	}
	
	static File resolveConfigDir() {
		return new File(System.getProperty("user.home"), ".eldenring-deathcounter/configs");
	}
}
