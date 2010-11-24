import processing.core.*;

public class File {
	PApplet parent;
	String name;
	String prePath = "img/";
	String path;
	String sharedImgPath = prePath + "shared.png";
	Owner owner;
	boolean isShared;
	boolean isPressed;
	boolean hasInited = false; // initDisplay can only be called once
	int isProjected;
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

	/*
	 * to add later: int id; int owner;
	 */

	File(PApplet p, String fileName, String filePath) {
		parent = p;
		name = fileName;
		path = filePath;
		isShared = false;
		isPressed = false;
		isProjected = -1;
	}

	void initDisplay(int x, int y, int s, int m) {
		// save variables
		this.x = x;
		this.y = y;
		iconSize = s;
		sharedIconSize = s / 2;
		margin = m;

		// load image
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
			parent.text(name, x + 3, y + iconSize + 3, iconSize, margin); // +3
		}

		// if file is shared, add icon
		if (isShared) {
			parent.image(sharedIcon, x + iconSize - sharedIconSize, y
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
