import processing.core.*;

public class Server {
	PApplet parent;
	// URL prefix
	String url = "http://cmu.chrisharrison.net/cgi-bin/";

	Server(PApplet p) {
		parent = p;
	}

	void getFile(int id) {

	}

	void getAllFiles() {
		// pull file list from server
		String raw[] = parent.loadStrings(url + "getAllFiles.py?owner=1");
		// each raw[i] is a file; create file from each line
		for (int i = 0; i < raw.length; i++) {
			String[] fileData = parent.split(raw[i], " , ");
			// id, name, type, date, owner, projectedLocation, tentative
			File newFile = new File(parent, fileData[1], fileData[2]);
			parent.println("Loading " + newFile.name + "." + newFile.type);
			newFile.initDisplay(0, 0, ShareLight.iconSize, ShareLight.margin);
			parent.println("Done");
			ShareLight.fileList[i] = newFile;
			ShareLight.fileList[i].initDisplay(0, 0, ShareLight.iconSize, ShareLight.margin);
			parent.println("Added to fileList");
		}
		// debug
		parent.println("there are " + raw.length + " lines");
		for (int i = 0; i < raw.length; i++) {
			parent.println(raw[i]);
		}
	}

	void setFile() {
		// id, name, type, date, owner, projectedLocation, tentative

	}

	void getStatus(int id) {

	}

	void setStatus(int id, int projectedLocation) {

	}

	void getMode(int owner) {

	}

	void setMode(int owner, int mode) {

	}

	void getTentativeFiles() {

	}

	void getTentativeFiles(int owner) {

	}

	void transferFile(int owner1, int owner2, int projectedLocation) {

	}
}
