import processing.core.*;

public class Server {
	PApplet parent;
	// URL prefix
	String url = "http://cmu.chrisharrison.net/cgi-bin/";
	// shortcuts to get id, name, type, date, owner, projectedLocation,
	// and tentative for getFile/getAllFiles
	int id = 0;
	int name = 1;
	int type = 2;
	int date = 3;
	int owner = 4;
	int pLoc = 5;
	int tent = 6;

	Server(PApplet p) {
		parent = p;
	}

	// FILE methods ============================================================

	void getFile(int id) {
		String raw[] = parent.loadStrings(url + "getFile.py?id=" + id);
		// each raw[] is an element of the file
		for (int i = 0; i < raw.length; i++) {
			int index = ShareLight.fileList.size(); // where to insert file
			updateFileList(raw, i, index);
		}
	}

	void getAllFiles() {
		// pull file list from server (temporarily owner 1)
		String raw[] = parent.loadStrings(url + "getAllFiles.py?owner=1");
		// each raw[i] is a file; create file from each line
		for (int i = 0; i < raw.length; i++) {
			updateFileList(raw, i, i);
		}
		// debug
		parent.println("there are " + raw.length + " files");
		for (int i = 0; i < raw.length; i++) {
			parent.println(raw[i]);
		}
	}

	// helper method for getFile and getAllFiles
	// takes in String[] with file info, index of String[], and fileList index
	// to place file into
	void updateFileList(String[] s, int i, int j) {
		String[] fileData = parent.split(s[i], " , ");
		File newFile = new File(parent, fileData[name], fileData[type]);
		parent.println("Loading " + newFile.name + "." + newFile.type); // debug
		boolean fileExists = false;

		// add/mod file
		if (ShareLight.fileList.size() > 0) {
			// fileList has content. Check if file already exists
			for (int n = 0; n < ShareLight.fileList.size(); n++) {
				File current = (File) ShareLight.fileList.get(n);
				if (current.id == Integer.parseInt(fileData[id])) {
					// file exists; update file
					current.name = fileData[name];
					current.type = fileData[type];
					current.date = fileData[date];
					current.owner.id = Integer.parseInt(fileData[owner]);
					current.isProjected = Integer.parseInt(fileData[pLoc]);
					current.tentative = Integer.parseInt(fileData[tent]);
					fileExists = true;
				}
			}
		} else {
			// fileList is currently empty
			ShareLight.fileList.add(newFile);
			fileExists = true;
		}
		// file doesn't exist; add file
		if (!fileExists) {
			if (ShareLight.fileList.size() < s.length) {
				if (j >= ShareLight.fileList.size()) {
					// if server list is longer, add to list
					ShareLight.fileList.add(j, newFile);
				} else {
					// otherwise, just replace
					ShareLight.fileList.set(j, newFile);
				}
			}
		}
		// update file properties

		((File) ShareLight.fileList.get(j)).initDisplay(0, 0,
				ShareLight.iconSize, ShareLight.margin);
		parent.println("Added to fileList[" + i + "]"); // debug

	}

	void setFile(int... num) {
		// id, name, type, date, owner, projectedLocation, tentative

	}

	// STATUS methods =========================================================

	void getStatus(int id) {
		// returns isProjected value given file id
	}

	void setStatus(int id, int projectedLocation) {
		// sets isProjected value given file id
	}

	// MODE methods ===========================================================

	int getMode(int owner) {
		return 0;
	}

	void setMode(int owner, int mode) {

	}

	// TENTATIVE methods ======================================================

	void getTentativeFiles() {

	}

	void getTentativeFiles(int owner) {

	}

	// TRANSFER methods =======================================================

	void transferFile(int owner1, int owner2, int projectedLocation) {

	}
}
