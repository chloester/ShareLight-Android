import processing.core.*;
import java.util.ArrayList;

public class Server {
	PApplet p;
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

	Server(PApplet parent) {
		p = parent;
	}

	// FILE methods ============================================================

	void getFile(int id) {
		String raw[] = p.loadStrings(url + "getFile.py?id=" + id);
		// each raw[] is an element of the file
		updateFileList((File) makeFileList(raw).get(0));
	}

	void getAllFiles() {
		// pull file list from server
		String raw[] = p.loadStrings(url + "getAllFiles.py?owner=" + ShareLight.me.id);
		ArrayList rawFiles = makeFileList(raw);
		for (int i = 0; i<rawFiles.size(); i++) {
			updateFileList((File) rawFiles.get(i));
		}
		
		// debug
//		p.println("there are " + raw.length + " files");
//		for (int i = 0; i < raw.length; i++) {
//			p.println(raw[i]);
//		}
	}

	// helper method for turning string[] of files into ArrayList of files
	ArrayList makeFileList(String[] s) {
		ArrayList fileList = new ArrayList();
		for (int i = 0; i < s.length; i++) {
			// skip over any blank lines
			if (s[i].trim().equals("")) {
				continue;
			}
			String[] fileData = p.split(s[i], " , ");
			File newFile = new File(p, fileData[name], fileData[type]);
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
			// p.println("Added to fileList[" + index + "]: " + f.name); // debug
		}
	}

	void setFile(int id, String s) {
		// id, name, type, date, owner, projectedLocation, tentative
		// s should be formatted as e.g. name=Blah&type=pdf
		String set[] = p
				.loadStrings(url + "setFile.py?id=" + id + "&" + s);
		// p.println("Setting " + s + " for file " + id); // debug
	}

	// STATUS methods =========================================================

	int getStatus(int id) {
		// returns projectedLocation value given file id
		String get[] = p.loadStrings(url + "getStatus.py?id=" + id);
		// p.println("Status of file " + id + " is " + get[0]); // debug
		return Integer.parseInt(get[0]);
	}

	void setStatus(int id, int pLoc) {
		// sets projectedLocation value given file id
		String set[] = p.loadStrings(url + "setStatus.py?id=" + id
				+ "&projectedLocation=" + pLoc);
		// p.println("Status of file " + id + " is set to " + pLoc); // debug
	}

	// MODE methods ===========================================================

	int getMode(int owner) {
		// gets mode for owner id
		String get[] = p.loadStrings(url + "getMode.py?id=" + owner);
		// p.println("Owner " + owner + " mode is " + get[0]); // debug
		return Integer.parseInt(get[0]);
	}

	void setMode(int owner, int mode) {
		String set[] = p.loadStrings(url + "setMode.py?id=" + owner + "&"
				+ "mode=" + mode);
		// p.println("Setting owner " + owner + " mode to " + mode); // debug
	}

	// TENTATIVE methods ======================================================

	void getTentativeFiles() {
		// retrieve all files marked as tentative
		// #todo unfinished; might not need all tentative files, just for owner
		String get[] = p.loadStrings(url + "getTentativeFiles.py");
	}

	void getTentativeFile(int owner) {
		// retrieve tentative files for specific owner
		// get tentative files from server
		// handles tentative file one by one
		String[] get = p.loadStrings(url + "getTentativeFiles.py?owner=" + owner);
		ArrayList list = makeFileList(get);
		if (list.size() > 0) {
			ShareLight.tentativeFile = (File) list.get(0);
		}
		// ShareLight.tentativeFile = (File) ShareLight.fileList.get(1); // debug
	}

	// TRANSFER methods =======================================================

	// not called by android app
	void transferFile() {
		int owner1 = ShareLight.tentativeFile.owner.id;
		int owner2 = ShareLight.tentativeFile.tentative;
		int pLoc = ShareLight.tentativeFile.projectedLocation;
		String trans[] = p.loadStrings(url + "transferFile.py?owner="
				+ owner1 + "&secondOwner=" + owner2 + "&projLocation=" + pLoc);
		ShareLight.tentativeFile = null;
	}
}
