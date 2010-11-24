/*
 * @author: Chloe Fan
 * @date: October 3, 2010
 * @name: ShareLight
 * @collab: Chris Harrison, Varuni Saxena
 * @notes: Mobile & Pervasive Computing Class Project
 * @last updated: November 6, 2010
 * @ When migrating over to Processing, get rid of:
 * public class Sharelight; PApplet parent; path = "", replace "this, " with ""; processing.core.*; replace "parent." with ""
 */

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;

public class ShareLight extends PApplet {

	/*--------------------------------------------------------------------------
	 S E T T I N G   U P
	 --------------------------------------------------------------------------*/

	// setting up images -------------------------------------------------------
	String path = "img/";
	String bgPath = path + "bg_wood_800.jpg"; // metal, paper, pattern, wood, mesh
	PImage bg;
	String shadowPath = path + "shadow.png"; // to give nice look to background
	PImage shadow;
	String dropSpacePath = path + "dropspace.png";
	PImage dropSpace;

	// declare files & icon locations ------------------------------------------
	// create server instance for easier communication
	Server server = new Server(this);
	File emptyFile = new File(this, "", "dropspace");
//	File file1 = new File(this, "Alpha.pdf", path + "pdf.png");
//	File file2 = new File(this, "Presentation.ppt", path + "ppt.png");
//	File file3 = new File(this, "Image.jpg", path + "fw.png");
//	File file4 = new File(this, "Gamma.xls", path + "excel.png");
//	File file5 = new File(this, "Beta.docx", path + "word.png");
//	File file6 = new File(this, "List.txt", path + "txt.png");
//	File[] fileList = { file1, file2, file3, file4, file5, file6 };
	static File[] fileList = new File[6];
	File draggedFile;
	// List to store shared items (dropspace)
	int numSharedFiles = 3;
	ArrayList sharedFiles = new ArrayList(numSharedFiles);

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

		// get files from the server
		server.getAllFiles();
		// load fileList into grid
		resetFileDisplay();

		// init pulldown area
		pulldown = loadImage(path + "pulldown.png");
		// initial pulldown height will be at the top
		pdY = pdYReset;

		println("Displaying " + fileList.length + " files"); // debug
	}

	/*--------------------------------------------------------------------------
	 D I S P L A Y
	 --------------------------------------------------------------------------*/

	public void draw() {
		background(bg);
		image(shadow, 0, 0);
		display();
		smooth();
	}

	/*--------------------------------------------------------------------------
	 C U S T O M   M E T H O D S
	 --------------------------------------------------------------------------*/

	void resetFileDisplay() {
		// load fileList into grid, default positions
		for (int i = 0; i < fileList.length; i++) {
			int currentX = (i % filesPerRow) * (iconSize + margin) + margin;
			int currentY = (i / filesPerRow) * (iconSize + margin) + margin
					+ buffer;
			fileList[i].x = currentX;
			fileList[i].y = currentY;
			fileList[i].initDisplay(currentX, currentY, iconSize, margin);
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
		for (int i = 0; i < fileList.length; i++) {
			int currentY = (i / filesPerRow) * (iconSize + margin) + margin
					+ buffer;
			// move along with pulldown
			fileList[i].y = currentY + (pdY - pdYReset);
			fileList[i].display(fileList[i].x, fileList[i].y);
		}

		// draw pulldown menu on top of icons
		image(pulldown, 0, pdY);
		if (pdY - pdYReset >= buffer) {
			pdActivated = true;
		} else {
			pdActivated = false;
		}

		// set sharedFiles from fileList
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isShared && fileList[i].isProjected >= 0) {
				File newFile = new File(this, fileList[i].name,
						fileList[i].type);
				newFile.isProjected = fileList[i].isProjected;
				// #todo change to prevent repeat inits (increase speed)
				// might need to compare current sharedFiles with last updated
				newFile.initDisplay(-500, -500, iconSize, margin);
				sharedFiles.set(fileList[i].isProjected, newFile);
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
		for (int i = 0; i < fileList.length; i++) {
			if (mouseX > fileList[i].x && mouseX < fileList[i].x + iconSize
					&& mouseY > fileList[i].y
					&& mouseY < fileList[i].y + iconSize
					&& mouseY > pdY + pdHeight) {
				dragSource = "desktop";
				fileList[i].isPressed = true;
				mouseDiffX = mouseX - fileList[i].x;
				mouseDiffY = mouseY - fileList[i].y;
				// initialize dragged icon
				draggedFile = new File(this, fileList[i].name, fileList[i].type);
				draggedFile.initDisplay(mouseX - mouseDiffX, mouseY
						- mouseDiffY, iconSize, margin);
			} else {
				fileList[i].isPressed = false;
			}
		}

		// is it pressing on the pulldown tab?
		if (mouseY > (pdY - pdYReset) && mouseY < (pdY + pdHeight)) {
			pdPressed = true;
		} else {
			pdPressed = false;
		}

		// is it pressing on a projected file?
		for (int i = 0; i < sharedFiles.size(); i++) {
			File tempFile = (File) sharedFiles.get(i);
			if (mouseX > tempFile.x && mouseX < tempFile.x + iconSize
					&& mouseY > tempFile.y && mouseY < tempFile.y + iconSize) {
				// don't do anything when pressing on an empty file
				if (!(tempFile.name).equals("")) {
					tempFile.isPressed = true;
					mouseDiffX = mouseX - tempFile.x;
					mouseDiffY = mouseY - tempFile.y;
					draggedFile = tempFile;
					dragSource = "projector";
				}
			} else {
				tempFile.isPressed = false;
			}
		}
	}

	public void mouseDragged() {
		// dragging icons
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isPressed) {
				isDragging = true;
			}
		}
		// dragging projected icons
		for (int i = 0; i < sharedFiles.size(); i++) {
			File tempFile = (File) sharedFiles.get(i);
			if (tempFile.isPressed) {
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
							for (int j = 0; j < fileList.length; j++) {
								// set the new file to shared & projected
								// set old file to unprojected
								if (fileList[j].name == newName) {
									fileList[j].isShared = true;
									fileList[j].isProjected = i;
								} else if (fileList[j].name == oldName) {
									fileList[j].isShared = false;
									fileList[j].isProjected = -1;
								}
							}
						}
					}
				}
			}
			if (dragSource.equals("projector")) {
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
							for (int j = 0; j < fileList.length; j++) {
								// set new file to old file's location
								int newLoc = currentFile.isProjected;
								int oldLoc = draggedFile.isProjected;
								if (fileList[j].name == newName) {
									fileList[j].isProjected = newLoc;
								} else if (fileList[j].name == oldName) {
									fileList[j].isProjected = oldLoc;
								}
							}
						}
					}
				} else {
					String fileName = draggedFile.name;
					for (int i = 0; i < fileList.length; i++) {
						if (fileList[i].name == fileName) {
							int prevLoc = fileList[i].isProjected;
							fileList[i].isProjected = -1;
							fileList[i].isShared = false;
							File dropSpace = new File(this, emptyFile.name,
									emptyFile.type);
							dropSpace.initDisplay(-500, -500, iconSize, margin);
							sharedFiles.set(prevLoc, dropSpace);
						}
					}
				}
			}
		}

		// set all locked booleans to false
		isDragging = false;
	}
}
