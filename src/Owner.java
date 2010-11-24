import processing.core.*;

public class Owner {
	PApplet parent;
	int id;
	String name;
	String avatar;
	int mode;
	
	Owner() {
		
	}
	
	Owner(int i, String n, String a, int m) {
		id = i;
		name = n;
		avatar = a;
		mode = m;
	}
}
