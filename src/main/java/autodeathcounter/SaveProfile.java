package autodeathcounter;

public class SaveProfile {
	public int slot;
	public String name;
	public int level;
	public long time;
	public int deaths;
	public boolean active;
	private byte[] data;
	private boolean isFocused;

	public SaveProfile(int slot, String name, int level, long time, int deaths, boolean active, byte[] data) {
		this.slot = slot;
		this.name = name;
		this.level = level;
		this.time = time;
		this.deaths = deaths;
		this.active = active;
		this.data = data;
		this.isFocused = false;
	}
	
	public String formatTime() {
		long hours = time / 3600;
		long minutes = (time % 3600) / 60;
		return String.format("%d:%02d", hours, minutes);
	}
	
	public int getSlot() {
		return slot;
	}
	
	public boolean isFocused() {
		return isFocused;
	}

	public void setFocused(boolean isFocused) {
		this.isFocused = isFocused;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public String toString() {
    	String deathStr = (deaths >= 0) ? String.valueOf(deaths) : "---";
        return String.format(
            "Slot %d [%s]:\n------------------------------------------------------------------------------------\n "
            + "|Level %d|\t|%.2fh gespielt|\t|%s Tode|\t\t|%s",
            slot, name, level, time, deathStr, active ? "AKTIV|\n" : "inaktiv|\n"
        );
    }
	
	public byte[] getData() {
		return data;
	}
}
