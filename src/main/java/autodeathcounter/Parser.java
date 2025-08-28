package autodeathcounter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	
    private static final int SLOT_START_INDEX = 0x310;
    private static final int SLOT_LENGTH = 0x280000;
    private static final int NUM_SLOTS = 10;

    private static final int SAVE_HEADER_LENGTH = 0x24C;
    private static final int SAVE_HEADER_START_INDEX = 0x1901D0E;
    private static final int CHAR_ACTIVE_STATUS_START_INDEX = 0x1901D04;

    private static final int CHAR_NAME_LENGTH = 0x22;
    private static final int CHAR_LEVEL_OFFSET = 0x22;
    private static final int CHAR_PLAYTIME_OFFSET = 0x26;
	
	public List<SaveProfile> extractProfiles(File saveFile) throws IOException {
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
	
	//Helpers -------------------------------------------------------------------------------------------------------\\
	private SaveProfile readSlot(byte[] fileData, int slotIndex) {
		int slotOffset = SLOT_START_INDEX + (slotIndex * SLOT_LENGTH);
		int headerOffset = SAVE_HEADER_START_INDEX + (slotIndex * SAVE_HEADER_LENGTH);


		if (slotOffset < 0 || headerOffset < 0) return null;
		if (slotOffset + SLOT_LENGTH > fileData.length) return null;
		if (headerOffset + SAVE_HEADER_LENGTH > fileData.length) return null;


		byte[] slotData = Arrays.copyOfRange(fileData, slotOffset, slotOffset + SLOT_LENGTH);


		boolean isActive = (CHAR_ACTIVE_STATUS_START_INDEX + slotIndex) < fileData.length
		&& fileData[CHAR_ACTIVE_STATUS_START_INDEX + slotIndex] == 1;
		if (!isActive) {
		return new SaveProfile(slotIndex, "", 0, 0, -1, false, null);
		}


		// Name (UTF-16LE)
		String name = new String(fileData, headerOffset, CHAR_NAME_LENGTH, StandardCharsets.UTF_16LE)
		.replace("\u0000", "").trim();


		// Level (2 Bytes LE)
		int level = le16(fileData, headerOffset + CHAR_LEVEL_OFFSET);


		// Playtime (4 Bytes LE) in seconds
		long time = le32u(fileData, headerOffset + CHAR_PLAYTIME_OFFSET);


		// Death Count
		int deathCount = findDeathCountCandidate(fileData, slotOffset, SLOT_LENGTH);


		return new SaveProfile(slotIndex, name, level, time, deathCount, true, slotData);
		}
	
	private int findDeathCountCandidate(byte[] data, int offset, int length) {
		if (offset < 0 || length <= 12 || offset + length > data.length) return -1;
		int end = offset + length - 12;
		for (int pos = offset; pos <= end; pos++) {
			int possibleDeathCount = le32(data, pos);
			int marker1 = le32(data, pos + 4);
			int marker2 = le32(data, pos + 8);
			if (marker1 == 0xFFFFFFFF && marker2 == 0x00000800 && possibleDeathCount >= 0) {
				return possibleDeathCount;
				}
			}
			return -1;
		}
	
	private static int le16(byte[] a, int i) {
		if (i + 1 >= a.length) return 0;
		return (a[i] & 0xFF) | ((a[i + 1] & 0xFF) << 8);
	}
	
	private static int le32(byte[] a, int i) {
		if (i + 3 >= a.length) return 0;
		return (a[i] & 0xFF) | ((a[i + 1] & 0xFF) << 8) | ((a[i + 2] & 0xFF) << 16) | ((a[i + 3] & 0xFF) << 24);
	}
	
	private static long le32u(byte[] a, int i) {
		return le32(a, i) & 0xFFFFFFFFL;
	}
}
