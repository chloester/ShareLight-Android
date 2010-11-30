import processing.core.PApplet;

public class RunnableThread implements Runnable {

	PApplet parent;
	Server server;
	Popup popup;
	Thread runner;
	int USER = ShareLight.me.id;
	int mode = ShareLight.me.mode;

	public RunnableThread(PApplet p, Server s, Popup po) {
		parent = p;
		server = s;
		popup = po;
	}

	public RunnableThread(String threadName) {
		runner = new Thread(this, threadName); // (1) Create a new thread.
		System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.
	}

	public void run() {

		while (true) {
			server.getAllFiles();
			// gets next tentative file if we don't have one
			if (ShareLight.tentativeFile == null) {
				server.getTentativeFile(USER);
				if (ShareLight.tentativeFile != null) {
					// if we have a tentative file, tell popup
					popup.getFileRequest(ShareLight.tentativeFile,
							ShareLight.tentativeFile.owner.id);
				}
			}

			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}