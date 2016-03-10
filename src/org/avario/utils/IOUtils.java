package org.avario.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class IOUtils {

	public static boolean createParentIfNotExists(File f) {
		if (f.exists()) {
			return f.canWrite();
		} else {
			return (f.getParentFile().exists()) ? true : f.getParentFile().mkdir();
		}
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public static void close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				Logger.get().log("Fail closing io ", e);
			}
		}
	}

	public static File getExternalStorageDirectory() {
		return (isExternalStorageReadable()) ? new File(Environment.getExternalStorageDirectory() + File.separator
				+ "AVario") : null;
	}

}
