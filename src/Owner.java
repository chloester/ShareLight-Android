import processing.core.*;

public class Owner {
	int id;
	String name;
	String avatar;
	int mode;
	
	Owner() {
		// creates empty owner without any info
	}
	
	Owner(int i, String n, String a, int m) {
		id = i;
		name = n;
		avatar = a;
		mode = m;
	}
}
