import processing.core.*;

public class File {
	PApplet p;
	String prePath = "img/";
	String sharedImgPath = prePath + "shared.png";
	PFont font;
	// properties: server
	int id;
	String name;
	String type;
	String date;
	Owner owner;
	int projectedLocation; // -1 = unshared >= 0 is shared
	int tentative; // previous owner
	// properties: local
	String path;
	boolean isPressed;
	boolean hasInited = false; // initDisplay can only be called once
	// icon variables
	PImage icon;
	int x;
	int y;
	int iconSize;
	int margin;
	int buffer = 3;
	// shared icon variables
	PImage sharedIcon;
	int sharedInitLoc = -500;
	int sharedIconSize;

	File(PApplet parent, String fileName, String fileType) {
		p = parent;
		name = fileName;
		type = fileType;
		path = getPath(type);
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
		icon = p.loadImage(path);
		p.image(icon, x, y, s, s);

		if (!name.equals("")) {
			// load text bg
			p.fill(0, 150);
			p.rect(x, y + s, s, m);

			// load text
			// must be vlw format, prefixed size
			font = p.loadFont(prePath + "SansSerif-18.vlw"); 
			p.textFont(font);
			p.fill(255);
			p.noStroke();
			p.text(name, x, y + s, s, m);
		}

		// init shared icon, invisible at start
		sharedIcon = p.loadImage(sharedImgPath);
		// p.image(sharedIcon, sharedInitLoc, sharedInitLoc);
		hasInited = true;
	}

	void display(int x, int y) {
		p.image(icon, x, y, iconSize, iconSize);
		this.x = x;
		this.y = y;

		// add text if it's not an empty file
		if (!name.equals("")) {
			// load text bg
			p.fill(0, 150);
			p.rect(x, y + iconSize, iconSize, margin);

			// text
			p.textFont(font);
			p.fill(255);
			p.noStroke();
			// give text a buffer
			p.text(name + "." + type, x + buffer, y + iconSize + buffer,
					iconSize, margin);
		}

		// if file is projected, add icon
		if (projectedLocation > -1) {
			p.image(sharedIcon, x + iconSize - sharedIconSize, y + iconSize 
					- sharedIconSize, sharedIconSize, sharedIconSize);
		} else {
			p.image(sharedIcon, sharedInitLoc, sharedInitLoc);
		}
	}

	void setOwner(int i, String n, String a, int m) {
		owner.id = i;
		owner.name = n;
		owner.avatar = a;
		owner.mode = m;
	}
}
