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
			updateFileList(raw[0]);
	}

	void getAllFiles() {
		// pull file list from server
		// gets both owner's files and shared files (pLoc > -1)
		//String raw[] = parent.loadStrings(url + "getAllFiles.py?owner=" + ShareLight.me.id);
		String raw[] = parent.loadStrings(url + "getAllFiles.py");
		// only select files that owner owns or is shared
		ArrayList unmatchedFiles = new ArrayList();  // keep track of all the files that shouldn't match
		for (int i = 0; i < ShareLight.fileList.size(); i++) {
			// track all the files we have right now
			File current = (File) ShareLight.fileList.get(i);
			unmatchedFiles.add(new Integer(current.id));
		}
		
		for (int i = 0; i < raw.length; i++) {
			// update the files from the server and note that they are matched now and should still exist
			Integer matchedFileID = new Integer(updateFileList(raw[i]));
			int index = unmatchedFiles.indexOf(matchedFileID);
			if(index != -1)
				unmatchedFiles.remove(index);
		}
		for(int i = 0; i < unmatchedFiles.size(); i++) {
			// get rid of all the leftover local files that shouldn't be there anymore
			for(int j = 0; j < ShareLight.fileList.size(); j++) {
				File current = (File) ShareLight.fileList.get(j);
				if(new Integer(current.id) == unmatchedFiles.get(i)) {
					//ShareLight.fileList.remove(j);
					break;
				}
			}
		}
		
		// debug
		parent.println("there are " + raw.length + " files");
		for (int i = 0; i < raw.length; i++) {
			parent.println(raw[i]);
		}
	}

	// helper method for getFile and getAllFiles
	// takes in String with file info and fileList index
	// to place file into
	int updateFileList(String s) {
		String[] fileData = parent.split(s, " , ");
		File newFile = new File(parent, fileData[name], fileData[type]);
		parent.println("Loading " + newFile.name + "." + newFile.type); // debug
		newFile.id = Integer.parseInt(fileData[id]);
		newFile.date = fileData[date];
		newFile.owner.id = Integer.parseInt(fileData[owner]);
		newFile.projectedLocation = Integer.parseInt(fileData[pLoc]);
		newFile.tentative = Integer.parseInt(fileData[tent]);
		boolean fileExists = false;
		int matchedFileID = -1;

		// add/mod file
		// fileList has content. Check if file already exists
		for (int n = 0; n < ShareLight.fileList.size(); n++) {
			File current = (File) ShareLight.fileList.get(n);
			parent.println("Current.id: " + current.id + " - current.name: "
					+ current.name + " - fileData.id: " + fileData[id]
					+ " - fileData.name: " + fileData[name] + " - matches: "
					+ (current.id == Integer.parseInt(fileData[id])));
			if (current.id == Integer.parseInt(fileData[id])) {
				// file exists; update file
				current.name = newFile.name;
				current.type = newFile.type;
				current.date = newFile.date;
				current.owner.id = newFile.owner.id;
				current.projectedLocation = newFile.projectedLocation;
				current.tentative = newFile.tentative;
				fileExists = true;
				matchedFileID = current.id;
			}
		}
		if (!fileExists) {
			parent.println("My id is: " + ShareLight.me.id);
			if (newFile.owner.id != ShareLight.me.id
					&& Integer.parseInt(fileData[pLoc]) == -1)
				// we can't even see this file; don't add it to our list
				return -1;
			ShareLight.fileList.add(newFile);
			// update file properties
			int index = ShareLight.fileList.size() - 1;
			((File) ShareLight.fileList.get(index)).initDisplay(0, 0,
					ShareLight.iconSize, ShareLight.margin);
			parent.println("Added to fileList[" + index + "]: " + fileData[name]); // debug
		}
		return matchedFileID;
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
