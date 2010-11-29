import processing.core.*;

public class Popup {
	PApplet p;
	Server server;
	String state;
	File file;
	// message box dimensions
	int boxX = ShareLight.margin;
	int boxY = 200;
	int boxW = ShareLight.screenWidth - ShareLight.margin*2;
	int boxH = 300;
	int boxR = 20;
	int buffer = 3;
	// icons
	String prePath = "img/";
	String yesPath = "yes.png";
	String noPath = "no.png";
	PImage yesIcon;
	PImage noIcon;
	int yesnoIconSize = 128;
	int yesIconX = ShareLight.screenWidth / 2 - yesnoIconSize
			- ShareLight.margin;
	int yesIconY = boxY + boxH - buffer - yesnoIconSize;
	int noIconX = ShareLight.screenWidth / 2 + ShareLight.margin;
	int noIconY = boxY + boxH - buffer - yesnoIconSize;
	Owner requester = new Owner();

	Popup(PApplet parent, Server server) {
		p = parent;
		this.server = server;

		yesIcon = p.loadImage(prePath + yesPath);
		noIcon = p.loadImage(prePath + noPath);
	}
	
	void display() {
		if (state != null) {
			messageBox();
			loadFont();
			if (state.equals("getRequest")) {
				String s = "Owner " + requester.id + " is requesting "
						+ file.name + "." + file.type + ".\nConfirm request?";
				p.text(s, boxX + buffer, boxY + buffer, boxW - buffer, boxH
						- buffer);
				p.image(yesIcon, yesIconX, yesIconY, yesnoIconSize,
						yesnoIconSize);
				p.image(noIcon, noIconX, noIconY, yesnoIconSize, yesnoIconSize);
			}
			if (state.equals("transferring")) {
				String s = file.name + "." + file.type
						+ "is transferring to owner " + requester.id;
				p.text(s, boxX + buffer, boxY + buffer, boxW - buffer, boxH
						- buffer);
			}
		}
	}
	
	void messageBox() {
		p.fill(0, 0, 0, 220);
		roundrect(boxX, boxY, boxW, boxH, boxR);
		p.noFill();
	}
	
	void loadFont() {
		PFont font;
		font = p.loadFont(prePath + "SansSerif-32.vlw"); 
		p.textFont(font);
		p.fill(255);
		p.noStroke();
		p.textAlign(p.CENTER);
	}
	
	void getFileRequest(File f, int r) {
		// takes File and Requester ID
		state = "getRequest";
		file = f;
		requester.id = r;
	}

	void roundrect(int x, int y, int w, int h, int r) {
		p.noStroke();
		p.rectMode(p.CORNER);

		int ax, ay, hr;

		ax = x + w - 1;
		ay = y + h - 1;
		hr = r / 2;

		p.rect(x, y, w, h);
		p.arc(x, y, r, r, p.radians((float) 180.0), p.radians((float) 270.0));
		p.arc(ax, y, r, r, p.radians((float) 270.0), p.radians((float) 360.0));
		p.arc(x, ay, r, r, p.radians((float) 90.0), p.radians((float) 180.0));
		p.arc(ax, ay, r, r, p.radians((float) 0.0), p.radians((float) 90.0));
		p.rect(x, y - hr, w, hr);
		p.rect(x - hr, y, hr, h);
		p.rect(x, y + h, w, hr);
		p.rect(x + w, y, hr, h);
	}
	
	void mouseReleased() {
		if (state.equals("getRequest")) {
			// if is pressing on yes or no, do appropriate action
			// yes
			if (ShareLight.touchedRect(yesIconX, yesIconY, yesnoIconSize, yesnoIconSize)) {
				server.setFile(ShareLight.tentativeFile.id, "tentative=-1");
				p.println("Accepted owner " + requester.id + "'s request for " + ShareLight.tentativeFile.name + "." + ShareLight.tentativeFile.type);
				state = null;
				ShareLight.tentativeFile = null;
			}
			
			// no
			if (ShareLight.touchedRect(noIconX, noIconY, yesnoIconSize, yesnoIconSize)) {
				server.setFile(ShareLight.tentativeFile.id, "tentative=999");
				p.println("Declined owner " + requester.id + "'s request for " + ShareLight.tentativeFile.name + "." + ShareLight.tentativeFile.type);
				state = null;
				ShareLight.tentativeFile = null;
			}
		}
	}
}
