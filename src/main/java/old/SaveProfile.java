package old;

public class SaveProfile {
	public final int slot;
    public final String name;
    public final int level;
    public final long seconds;
    public final int deathCount;
    public final boolean active;
    private byte[] slotData;
    public boolean isFocused;

    public SaveProfile(int slot, String name, int level, long seconds, int deathCount, boolean active, byte[] slotData) {
        this.slot = slot;
        this.name = name;
        this.level = level;
        this.seconds = seconds;
        this.deathCount = deathCount;
        this.active = active;
        this.slotData = slotData;
    }

    public String toString() {
    	String deathStr = (deathCount >= 0) ? String.valueOf(deathCount) : "---";
        return String.format(
            "Slot %d [%s]:\n------------------------------------------------------------------------------------\n "
            + "|Level %d|\t|%.2fh gespielt|\t|%s Tode|\t\t|%s",
            slot, name, level, seconds, deathStr, active ? "AKTIV|\n" : "inaktiv|\n"
        );
    }
    
    public byte[] getSlotData() {
    	return slotData;
    }
    
    public String formatTime() {
    	long hours = seconds / 3600;
    	long minutes = (seconds % 3600) / 60;
    	return String.format("%d:%02d", hours, minutes);
    }
}
