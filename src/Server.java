import processing.core.*;
import java.util.ArrayList;

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
	// tentative files
	File currentFile;

	Server(PApplet p) {
		parent = p;
	}

	// FILE methods ============================================================

	void getFile(int id) {
		String raw[] = parent.loadStrings(url + "getFile.py?id=" + id);
		// each raw[] is an element of the file
		updateFileList((File) makeFileList(raw).get(0));
	}

	void getAllFiles() {
		// pull file list from server
		// gets both owner's files and shared files (pLoc > -1)
		String raw[] = parent.loadStrings(url + "getAllFiles.py?owner=" + ShareLight.me.id);
		//String raw[] = parent.loadStrings(url + "getAllFiles.py");
		ArrayList rawFiles = makeFileList(raw);
		for (int i = 0; i<rawFiles.size(); i++) {
			updateFileList((File) rawFiles.get(i));
		}
		
		// debug
		parent.println("there are " + raw.length + " files");
		for (int i = 0; i < raw.length; i++) {
			parent.println(raw[i]);
		}
	}

	// helper method for turning string[] of files into ArrayList of files
	ArrayList makeFileList(String[] s) {
		ArrayList fileList = new ArrayList();
		for (int i = 0; i < s.length; i++) {
			String[] fileData = parent.split(s[i], " , ");
			File newFile = new File(parent, fileData[name], fileData[type]);
			newFile.id = Integer.parseInt(fileData[id]);
			newFile.date = fileData[date];
			newFile.owner.id = Integer.parseInt(fileData[owner]);
			newFile.projectedLocation = Integer.parseInt(fileData[pLoc]);
			newFile.tentative = Integer.parseInt(fileData[tent]);
			fileList.add(newFile);
		}
		return fileList;
	}

	// helper method for getFile and getAllFiles
	void updateFileList(File f) {
		boolean fileExists = false;

		// add/mod file
		// fileList has content. Check if file already exists
		for (int n = 0; n < ShareLight.fileList.size(); n++) {
			File current = (File) ShareLight.fileList.get(n);
			if (current.id == f.id) {
				// file exists; update file
				current.name = f.name;
				current.type = f.type;
				current.date = f.date;
				current.owner.id = f.owner.id;
				current.projectedLocation = f.projectedLocation;
				current.tentative = f.tentative;
				fileExists = true;
			}
		}
		if (!fileExists) {
			ShareLight.fileList.add(f);
			// update file properties
			int index = ShareLight.fileList.size() - 1;
			((File) ShareLight.fileList.get(index)).initDisplay(0, 0,
					ShareLight.iconSize, ShareLight.margin);
			parent.println("Added to fileList[" + index + "]: "
					+ f.name); // debug
		}
	}

	void setFile(int id, String s) {
		// id, name, type, date, owner, projectedLocation, tentative
		// s should be formatted as e.g. name=Blah&type=pdf
		String set[] = parent
				.loadStrings(url + "setFile.py?id=" + id + "&" + s);
		parent.println("Setting " + s + " for file " + id); // debug
	}

	// STATUS methods =========================================================

	int getStatus(int id) {
		// returns projectedLocation value given file id
		String get[] = parent.loadStrings(url + "getStatus.py?id=" + id);
		parent.println("Status of file " + id + " is " + get[0]); // debug
		return Integer.parseInt(get[0]);
	}

	void setStatus(int id, int pLoc) {
		// sets projectedLocation value given file id
		String set[] = parent.loadStrings(url + "setStatus.py?id=" + id
				+ "&projectedLocation=" + pLoc);
		parent.println("Status of file " + id + " is set to " + pLoc); // debug
	}

	// MODE methods ===========================================================

	int getMode(int owner) {
		// gets mode for owner id
		String get[] = parent.loadStrings(url + "getMode.py?id=" + owner);
		parent.println("Owner " + owner + " mode is " + get[0]); // debug
		return Integer.parseInt(get[0]);
	}

	void setMode(int owner, int mode) {
		String set[] = parent.loadStrings(url + "setMode.py?id=" + owner + "&"
				+ "mode=" + mode);
		parent.println("Setting owner " + owner + " mode to " + mode); // debug
	}

	// TENTATIVE methods ======================================================

	void getTentativeFiles() {
		// retrieve all files marked as tentative
		String get[] = parent.loadStrings(url + "getTentativeFiles.py");
	}

	void getTentativeFiles(int owner) {
		// retrieve tentative files for specific owner
		// get tentative files directly from fileList instead of from server
		// handles tentative file one by one
		if (currentFile != null) {
			return; // if there is a file being transferred, don't get next file
		}
		for (int i = 0; i < ShareLight.fileList.size(); i++) {
			File current = (File) ShareLight.fileList.get(i);
			if (current.tentative != -1) {
				currentFile = current;
			}
		}
		
	}

	// TRANSFER methods =======================================================

	void transferFile() {
		int owner1 = currentFile.owner.id;
		int owner2 = currentFile.tentative;
		int pLoc = currentFile.projectedLocation;
		String trans[] = parent.loadStrings(url + "transferFile.py?owner="
				+ owner1 + "&secondOwner=" + owner2 + "&projLocation=" + pLoc);
		currentFile = null;
	}
}
