/*
 * @author: Chloe Fan
 * @date: October 3, 2010
 * @name: ShareLight
 * @collab: Chris Harrison, Varuni Saxena
 * @notes: Mobile & Pervasive Computing Class Project
 * @last updated: November 6, 2010
 * @ When migrating over to Processing, get rid of:
 * public class Sharelight; PApplet parent; path = "", replace "this, " with ""; processing.core.*; replace "parent." with ""; remove public static main; 
 */

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;

public class ShareLight extends PApplet {

	/*--------------------------------------------------------------------------
	 S E T T I N G   U P
	 --------------------------------------------------------------------------*/
	
	// setting up timer --------------------------------------------------------
	
	float lastTime = 0;
	int updateInterval = 5000;

	// setting up images -------------------------------------------------------
	String path = "img/";
	String bgPath = path + "bg_wood_800.jpg"; // metal, paper, pattern, wood, mesh
	PImage bg;
	String shadowPath = path + "shadow.png"; // to give nice look to background
	PImage shadow;
	String dropSpacePath = path + "dropspace.png";
	PImage dropSpace;

	// declare files & icon locations ------------------------------------------
	File emptyFile = new File(this, "", "dropspace");
	static ArrayList fileList = new ArrayList(); // arbitrary fileList size
	File draggedFile;
	// List to store shared items (dropspace)
	int numSharedFiles = 3;
	ArrayList sharedFiles = new ArrayList(numSharedFiles);
	// init owner for this instance
	static Owner me = new Owner();
	// create server instance for easier communication
	Server server = new Server(this);

	// compute screen dimensions -----------------------------------------------
	static int screenWidth = 480; // 480 x 854 (Motorola Droid); 480x800 (myTouch 4G)
	int screenHeight = 800;
	static int filesPerRow = 3; // how many icons to display per row (fpr)
	static int iconSize = 128; // 128px for 3fpr, 200px for 2fpr
	// margin between icons
	static int margin = (screenWidth - (iconSize * filesPerRow)) / (filesPerRow + 1);
	// used for dragging
	int mouseDiffX;
	int mouseDiffY;
	boolean isDragging = false;
	String dragSource = ""; // = desktop | projector

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
		frameRate(30);
		size(screenWidth, screenHeight);
		bg = loadImage(bgPath);
		shadow = loadImage(shadowPath);
		println(bgPath);

		dropSpace = loadImage(dropSpacePath);

		me.id = 1; // temporarily set to 1 #todo
		me.mode = 0; // temporarily set to desktop mode #todo
		// get files from the server
		server.getAllFiles();
		// load fileList into grid
		resetFileDisplay();

		// init pulldown area
		pulldown = loadImage(path + "pulldown.png");
		// initial pulldown height will be at the top
		pdY = pdYReset;

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
		// draw base icons
		// only draw owner's files
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

		// draw pulldown menu on top of icons
		image(pulldown, 0, pdY);
		if (pdY - pdYReset >= buffer) {
			pdActivated = true;
		} else {
			pdActivated = false;
		}

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

		// draw dragged file on top of everything
		if (isDragging) {
			tint(255,255,255,200);
			draggedFile.display(mouseX - mouseDiffX, mouseY - mouseDiffY);
			tint(255,255,255,255);
		}
	}

	/*--------------------------------------------------------------------------
	 M O U S E   E V E N T S
	 --------------------------------------------------------------------------*/

	// called once
	public void mousePressed() {
		// is it pressing on an icon and not on the pulldown?
		for (int i = 0; i < fileList.size(); i++) {
			File current = (File) fileList.get(i);
			if (mouseX > current.x && mouseX < current.x + iconSize
					&& mouseY > current.y
					&& mouseY < current.y + iconSize
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
					&& mouseY > current.y 
					&& mouseY < current.y + iconSize 
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
					// draggedFile.initDisplay(mouseX - mouseDiffX, mouseY - mouseDiffY, iconSize, margin);
					
				}
			} else {
				current.isPressed = false;
			}
		}

		// is it pressing on the pulldown tab?
		if (mouseY > (pdY - pdYReset) && mouseY < (pdY + pdHeight)) {
			pdPressed = true;
		} else {
			pdPressed = false;
		}

	}

	public void mouseDragged() {
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

	public void mouseReleased() {

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
								dropSpace.initDisplay(-500, -500, iconSize, margin);
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
					// if the dragged file is not on the desktop, i.e. requesting file
					if (!hasFile) {
						// set the tentative to current owner
						server.setFile(draggedFile.id, "tentative="+me.id);
					}
				}
			}
		}

		// set all locked booleans to false
		isDragging = false;
	}
}
