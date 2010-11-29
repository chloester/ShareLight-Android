/*
 * @author: Chloe Fan
 * @date: October 3, 2010
 * @name: ShareLight
 * @collab: Chris Harrison, Varuni Saxena
 * @notes: Mobile & Pervasive Computing Class Project
 * @last updated: November 6, 2010
 * @ When migrating over to Processing, get rid of:
 * public class Sharelight; PApplet p; path = "", replace "this, " with ""; processing.core.*; replace "p." with ""; remove public static main; 
 */

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;

public class ShareLight extends PApplet {

	/*--------------------------------------------------------------------------
	 S E T T I N G   U P
	 --------------------------------------------------------------------------*/
	static ShareLight sharedInstance;
	
	// setting up timer --------------------------------------------------------
	float lastTime = 0;
	int updateInterval = 1000;

	// declare files & icon locations ------------------------------------------
	File emptyFile = new File(this, "", "dropspace");
	File draggedFile;
	static File tentativeFile;
	static ArrayList fileList = new ArrayList(); // arbitrary fileList size
	// List to store shared items (dropspace)
	int numSharedFiles = 3;
	ArrayList sharedFiles = new ArrayList(numSharedFiles);
	// init owner for this instance
	static Owner me = new Owner();
	// create server instance for easier communication
	Server server = new Server(this);
	// create popup instance to display transfer messages
	Popup popup = new Popup(this, server);

	// compute screen dimensions -----------------------------------------------
	static int screenWidth = 480; // 480 x 854 (Motorola Droid); 480x800
									// (myTouch 4G)
	int screenHeight = 800;
	static int filesPerRow = 3; // how many icons to display per row (fpr)
	static int iconSize = 128; // 128px for 3fpr, 200px for 2fpr
	// margin between icons
	static int margin = (screenWidth - (iconSize * filesPerRow))
			/ (filesPerRow + 1);
	// used for dragging
	int mouseDiffX;
	int mouseDiffY;
	boolean isDragging = false;
	String dragSource = ""; // desktop | projector
	
	// setting up images -------------------------------------------------------
	String path = "img/";
	String bgPath = path + "bg_wood_800.jpg"; // metal, paper, pattern, wood, mesh
	String shadowPath = path + "shadow.png"; // to give nice look to background
	String dropSpacePath = path + "dropspace.png";
	String switchLPath = path + "switch-left.png";
	String switchRPath = path + "switch-right.png";
	PImage bg;
	PImage shadow;
	PImage dropSpace;
	PImage switchL;
	PImage switchR;
	int switchW = 128;
	int switchH = 144;
	int switchX = screenWidth/2-switchW/2;
	int switchY = screenHeight-switchH-margin;
	// mode font
	PFont modeFont;

	// pulldown area -----------------------------------------------------------
	PImage pulldown;
	int pdTabHeight = 50; // drag-able tab area
	int pdHeight = 750; // height of pulldown area
	int pdYReset = -pdHeight + pdTabHeight; // default location for pulldown
	// area
	int pdY; // dynamic location of pulldown area
	int buffer = 30; // buffer between pulldown and icons so it doesn't cover
	// icons
	boolean pdPressed = false;
	boolean pdActivated = false; // if pulldown has been pulled down, used for

	// -------------------------------------------------------------------------

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "ShareLight" });
	}

	public void setup() {
		sharedInstance = this;
		frameRate(30);
		size(screenWidth, screenHeight);
		bg = loadImage(bgPath);
		shadow = loadImage(shadowPath);
		dropSpace = loadImage(dropSpacePath);
		println(bgPath);

		// set owner values
		me.id = 1; // temporarily set to 1 #todo
		me.mode = 0; // initialize to desktop mode
		server.setMode(me.id, me.mode);
		// get files from the server
		server.getAllFiles();
		// load fileList into grid
		resetFileDisplay();

		// init pulldown area
		pulldown = loadImage(path + "pulldown.png");
		// initial pulldown height will be at the top
		pdY = pdYReset;
		
		// init switch
		switchL = loadImage(switchLPath);
		switchR = loadImage(switchRPath);
		
		// set up mode font
		modeFont = loadFont(path + "SansSerif-32.vlw"); 

		println("Displaying " + fileList.size() + " files"); // debug
	}

	/*--------------------------------------------------------------------------
	 D I S P L A Y
	 --------------------------------------------------------------------------*/

	public void draw() {
		background(bg);
		image(shadow, 0, 0);
		// update time; get from server every second
		if (millis() - lastTime > updateInterval) {
			server.getAllFiles();
			// gets next tentative file if we don't have one
			if (tentativeFile == null) {
				server.getTentativeFile(me.id);
				if (tentativeFile != null) {
					// if we have a tentative file, tell popup
					popup.getFileRequest(tentativeFile,
									tentativeFile.owner.id);
				}
			}
			lastTime = millis();
		}
		display();
		smooth();
	}

	/*--------------------------------------------------------------------------
	 C U S T O M   M E T H O D S
	 --------------------------------------------------------------------------*/

	void resetFileDisplay() {
		// load fileList into grid, default positions
		for (int i = 0; i < fileList.size(); i++) {
			int currentX = (i % filesPerRow) * (iconSize + margin) + margin;
			int currentY = (i / filesPerRow) * (iconSize + margin) + margin
					+ buffer;
			File current = ((File) fileList.get(i));
			current.x = currentX;
			current.y = currentY;
			current.initDisplay(currentX, currentY, iconSize, margin);
		}

		// load dropSpaces into sharedFiles
		for (int j = 0; j < numSharedFiles; j++) {
			File newFile = new File(this, emptyFile.name, emptyFile.type);
			newFile.initDisplay(-500, -500, iconSize, margin);
			sharedFiles.add(newFile);
		}

	}

	void display() {
		// draw layers
		drawFileList();
		drawPullDown();
		setSharedFiles();
		drawSharedFiles();
		drawSwitch();
		drawDraggedFile();
		drawPopUp();
	}

	void drawFileList() {
		// draw owner's files from fileList
		for (int i = 0; i < fileList.size(); i++) {
			File current = ((File) fileList.get(i));
			if (current.owner.id != me.id)
				continue;
			int currentY = (i / filesPerRow) * (iconSize + margin) + margin
					+ buffer;
			// move along with pulldown
			current.y = currentY + (pdY - pdYReset);
			current.display(current.x, current.y);
		}
	}

	void drawPullDown() {
		// draw pulldown menu on top of icons
		image(pulldown, 0, pdY);
		if (pdY - pdYReset >= buffer) {
			pdActivated = true;
		} else {
			pdActivated = false;
		}
	}

	void setSharedFiles() {
		// set sharedFiles from fileList
		// draw files that are shared whether user owns them or not
		for (int i = 0; i < fileList.size(); i++) {
			File current = ((File) fileList.get(i));
			if (current.projectedLocation >= 0) {
				File newFile = new File(this, current.name, current.type);
				newFile.projectedLocation = current.projectedLocation;
				// #todo change to prevent repeat inits (increase speed)
				// might need to compare current sharedFiles with last updated
				newFile.initDisplay(-500, -500, iconSize, margin);
				sharedFiles.set(current.projectedLocation, newFile);
			}
		}
	}

	void drawSharedFiles() {
		// draw shared icons
		// println("shared: "); // debug
		for (int i = 0; i < sharedFiles.size(); i++) {
			int currentX = (i % filesPerRow) * (iconSize + margin) + margin;
			int currentY = pdY
					- pdYReset
					- ((i / filesPerRow) * (iconSize + margin) + margin
							+ buffer + iconSize);
			File tempFile = (File) sharedFiles.get(i);
			tempFile.x = currentX;
			tempFile.y = currentY;
			tempFile.display(currentX, currentY);
			// println(tempFile.name + " " + tempFile.isProjected + " "); //
			// debug
		}
	}
	
	void drawSwitch() {
		// draw switch
		PImage img = switchL; // set to left switch by default
		if (me.mode == 0) {
			// desktop mode: draw left switch
			img = switchL;
		} else if (me.mode == 1) {
			// avatar mode: draw right switch
			img = switchR;
		}
		image(img, switchX, switchY, switchW, switchH);
		
		// draw text
		textFont(modeFont);
		fill(0);
		noStroke();
		textAlign(RIGHT);
		text("Desktop", switchX-margin, switchY+switchH/2);
		textAlign(LEFT);
		text("Avatar", switchX+switchW+margin, switchY+switchH/2);
	}

	void drawDraggedFile() {
		// draw dragged file on top of everything
		if (isDragging) {
			tint(255, 255, 255, 200);
			draggedFile.display(mouseX - mouseDiffX, mouseY - mouseDiffY);
			tint(255, 255, 255, 255);
		}
	}

	void drawPopUp() {
		popup.display();
	}

	/*--------------------------------------------------------------------------
	 M O U S E   E V E N T S
	 --------------------------------------------------------------------------*/

	// called once
	public void mousePressed() {
		// check the state of the popup. If has, then disable other presses
		if (popup.state != null) {
			return;
		}

		// is it pressing on an icon and not on the pulldown?
		for (int i = 0; i < fileList.size(); i++) {
			File current = (File) fileList.get(i);
			if (mouseX > current.x && mouseX < current.x + iconSize
					&& mouseY > current.y && mouseY < current.y + iconSize
					&& mouseY > pdY + pdHeight) {
				dragSource = "desktop";
				current.isPressed = true;
				mouseDiffX = mouseX - current.x;
				mouseDiffY = mouseY - current.y;
				// initialize dragged icon
				draggedFile = new File(this, current.name, current.type);
				draggedFile.initDisplay(mouseX - mouseDiffX, mouseY
						- mouseDiffY, iconSize, margin);
			} else {
				current.isPressed = false;
			}
		}

		// is it pressing on a shared file?
		for (int i = 0; i < sharedFiles.size(); i++) {
			File current = (File) sharedFiles.get(i);
			if (mouseX > current.x && mouseX < current.x + iconSize
					&& mouseY > current.y && mouseY < current.y + iconSize
					&& mouseY < pdY + pdHeight) {
				// don't do anything when pressing on an empty file
				if (!(current.name).equals("")) {
					dragSource = "projector";
					current.isPressed = true;
					mouseDiffX = mouseX - current.x;
					mouseDiffY = mouseY - current.y;
					// initialize dragged icon
					draggedFile = current;
					// draggedFile = new File(this, current.name, current.type);
					// draggedFile.initDisplay(mouseX - mouseDiffX, mouseY -
					// mouseDiffY, iconSize, margin);

				}
			} else {
				current.isPressed = false;
			}
		}

		// is it pressing on the pulldown tab?
		if (mouseY > (pdY - pdYReset) && mouseY < (pdY + pdHeight)) {
			if (!pdActivated)
				pdY = pdYReset + iconSize + buffer + margin;
			else
				pdY = pdYReset;
			pdPressed = true;
		} else {
			pdPressed = false;
		}

		// is it pressing on the switch?
		if (touchedRect(switchX, switchY, switchW, switchH)) {
			if (me.mode == 0) {
				me.mode = 1;
			} else if (me.mode == 1) {
				me.mode = 0;
			}
			drawSwitch();
			server.setMode(me.id, me.mode);
		}

	}

	// -------------------------------------------------------------------------

	public void mouseDragged() {
		if (popup.state != null) {
			isDragging = false;
			return;
		}
		
		// dragging icons
		for (int i = 0; i < fileList.size(); i++) {
			File current = ((File) fileList.get(i));
			if (current.isPressed) {
				isDragging = true;
			}
		}
		// dragging projected icons
		for (int i = 0; i < sharedFiles.size(); i++) {
			File current = (File) sharedFiles.get(i);
			if (current.isPressed) {
				isDragging = true;
			}
		}

		// if icon is dragged over pulldown and it's not already activated,
		// move pulldown down a little
		if (!pdActivated && isDragging && (mouseY - mouseDiffY < buffer)) {
			pdY = pdYReset + iconSize + buffer + margin;
		}

		// dragging pulldown
		if (pdPressed) {
			pdY = mouseY + pdYReset;
			// make sure pulldown stays within certain area
			if (pdY > 0) {
				pdY = 0;
			}
			if (pdY < pdYReset) {
				pdY = pdYReset;
			}
		}
	}

	// -------------------------------------------------------------------------

	public void mouseReleased() {
		// check the state of the popup. If has, then disable other presses
		if (popup.state != null) {
			popup.mouseReleased();
			return;
		}

		if (isDragging) {
			if (dragSource.equals("desktop")) {
				// dragging onto projected space
				if (mouseY < pdY - pdYReset) {
					for (int i = 0; i < sharedFiles.size(); i++) {
						// save old file to change its status later
						File currentFile = (File) sharedFiles.get(i);
						String oldName = currentFile.name;
						String newName = draggedFile.name;
						// if hovering over a dropspace
						if (mouseX > currentFile.x
								&& mouseX < currentFile.x + iconSize
								&& mouseY > currentFile.y
								&& mouseY < currentFile.y + iconSize) {
							// change old and new file statuses
							for (int j = 0; j < fileList.size(); j++) {
								File current = ((File) fileList.get(j));
								// set the new file to shared & projected
								// set old file to unprojected
								if (current.name == newName) {
									current.projectedLocation = i;
									server.setStatus(current.id,
											current.projectedLocation);
								} else if (current.name == oldName) {
									current.projectedLocation = -1;
									server.setStatus(current.id,
											current.projectedLocation);
								}
							}
						}
					}
				}
			}
			if (dragSource.equals("projector")) {
				// dragging within projector
				if (mouseY < pdY - pdYReset) {
					for (int i = 0; i < sharedFiles.size(); i++) {
						// save old file to change its status later
						File currentFile = (File) sharedFiles.get(i);
						String oldName = currentFile.name;
						String newName = draggedFile.name;
						// if hovering over a dropspace
						if (mouseX > currentFile.x
								&& mouseX < currentFile.x + iconSize
								&& mouseY > currentFile.y
								&& mouseY < currentFile.y + iconSize) {
							// change old and new file statuses
							int newLoc = currentFile.projectedLocation;
							int oldLoc = draggedFile.projectedLocation;
							for (int j = 0; j < fileList.size(); j++) {
								File current = ((File) fileList.get(j));
								// set new file to old file's location
								if (current.name == newName) {
									current.projectedLocation = i;
									server.setStatus(current.id,
											current.projectedLocation);
								} else if (current.name == oldName) {
									current.projectedLocation = oldLoc;
									server.setStatus(current.id,
											current.projectedLocation);
								}
							}
							if (oldName.equals("")) {
								// if swapping with a dropspace
								File dropSpace = new File(this, emptyFile.name,
										emptyFile.type);
								dropSpace.initDisplay(-500, -500, iconSize,
										margin);
								sharedFiles.set(oldLoc, dropSpace);
								// account for index starting at 0
							}
						}
					}
				} else {
					// dragging onto desktop
					String fileName = draggedFile.name;
					boolean hasFile = false;
					for (int i = 0; i < fileList.size(); i++) {
						File current = ((File) fileList.get(i));
						if (current.name == fileName) {
							// change the location to not projected
							int prevLoc = current.projectedLocation;
							current.projectedLocation = -1;
							server.setStatus(current.id,
									current.projectedLocation);
							// replace with dropspace
							File dropSpace = new File(this, emptyFile.name,
									emptyFile.type);
							dropSpace.initDisplay(-500, -500, iconSize, margin);
							sharedFiles.set(prevLoc, dropSpace);
							hasFile = true;
						}
					}
					// if the dragged file is not on the desktop, i.e.
					// requesting file
					if (!hasFile) {
						// set the tentative to current owner
						server.setFile(draggedFile.id, "tentative=" + me.id);
					}
				}
			}
		}
		// set all locked booleans to false
		isDragging = false;
	}
	
	// -------------------------------------------------------------------------
	
	static boolean touchedRect(int x, int y, int w, int h) {
		boolean b;
		if (sharedInstance.mouseX > x && sharedInstance.mouseX < x+w && sharedInstance.mouseY > y && sharedInstance.mouseY < y+h)
			b = true;
		else
			b = false;
		return b;
	}
}
