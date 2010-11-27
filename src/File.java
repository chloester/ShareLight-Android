import processing.core.*;

public class File {
	PApplet parent;
	String prePath = "img/";
	String sharedImgPath = prePath + "shared.png";
	// properties: server
	int id;
	String name;
	String type;
	String date;
	Owner owner;
	int projectedLocation; // projectedLocation
	int tentative; // previous owner
	// properties: local
	String path;
	boolean isShared;
	boolean isPressed;
	boolean hasInited = false; // initDisplay can only be called once
	// icon variables
	PImage icon;
	int x;
	int y;
	int iconSize;
	int margin;
	// shared icon variables
	PImage sharedIcon;
	int sharedInitLoc = -500;
	int sharedIconSize;

	File(PApplet p, String fileName, String fileType) {
		parent = p;
		name = fileName;
		type = fileType;
		path = getPath(type);
		isShared = false;
		isPressed = false;
		projectedLocation = -1;
		owner = new Owner();
	}

	String getPath(String fileType) {
		return prePath + fileType + ".png";
	}

	void initDisplay(int x, int y, int s, int m) {
		// save variables
		this.x = x;
		this.y = y;
		iconSize = s;
		sharedIconSize = s / 2;
		margin = m;

		// load image
		// lines cannot be separated
		icon = parent.loadImage(path);
		parent.image(icon, x, y, s, s);

		if (!name.equals("")) {
			// load text bg
			parent.fill(0, 150);
			parent.rect(x, y + s, s, m);

			// load text
			PFont font;
			font = parent.loadFont(prePath + "SansSerif-18.vlw"); // must be vlw
			// format,
			// prefixed
			// size
			parent.textFont(font);
			parent.fill(255);
			parent.noStroke();
			parent.text(name, x, y + s, s, m);
		}

		// init shared icon, invisible at start
		sharedIcon = parent.loadImage(sharedImgPath);
		// parent.image(sharedIcon, sharedInitLoc, sharedInitLoc);
		hasInited = true;
	}

	void display(int x, int y) {
		parent.image(icon, x, y, iconSize, iconSize);
		this.x = x;
		this.y = y;

		// add text if it's not an empty file
		if (!name.equals("")) {
			// load text bg
			parent.fill(0, 150);
			parent.rect(x, y + iconSize, iconSize, margin);

			// text
			parent.fill(255);
			parent.noStroke();
			// give text a buffer
			parent.text(name + "." + type, x + 3, y + iconSize + 3, iconSize,
					margin); // +3
		}

		// if file is shared, add icon
		if (isShared) {
			parent
					.image(sharedIcon, x + iconSize - sharedIconSize, y
							+ iconSize - sharedIconSize, sharedIconSize,
							sharedIconSize);
		} else {
			parent.image(sharedIcon, sharedInitLoc, sharedInitLoc);
		}
	}

	void setOwner(int i, String n, String a, int m) {
		owner.id = i;
		owner.name = n;
		owner.avatar = a;
		owner.mode = m;
	}
}
