package bingtool;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

public class Utils extends Log {

	static String getUrlForPage(String url, int page, int listSize) {
		return "https://api.cognitive.microsoft.com/bing/v5.0/search?q=site:" + url + "&count=" + 20 + "&offset=" + listSize;
	}

	static void handleException(Exception exception) {
		if(Values.DEBUG) {
			exception.printStackTrace();
			errorExit();
		} else {
			errorExit(exception.getMessage());
		}
	}

	static void errorExit() {
		exit(1, null);
	}

	static void errorExit(String errorMessage) {
		exit(1, errorMessage);
	}

	static void exit(int exitCode, String errorMessage) {
		if(errorMessage != null) {
			log.error(errorMessage);
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(exitCode);
	}

	static void saveObjectTMP(Object object, String file) {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(System.getProperty("java.io.tmpdir"), file)));
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
		} catch (Exception e1) {
			Utils.handleException(e1);
		}
	}

	static Object loadObjectTMP(String file) {
		Object object = null;
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(System.getProperty("java.io.tmpdir"), file)));
			object = objectInputStream.readObject();
			objectInputStream.close();
		} catch (Exception e1) {
			Utils.handleException(e1);
		}
		return object;
	}

	static void setJPanelEnabled(JPanel panel, boolean enabled) {
		for(Component component : panel.getComponents()) {
			component.setEnabled(enabled);
		}
		panel.setEnabled(enabled);
	}

}
