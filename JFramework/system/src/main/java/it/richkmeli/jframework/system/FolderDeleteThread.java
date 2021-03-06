package it.richkmeli.jframework.system;

import it.richkmeli.jframework.util.log.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FolderDeleteThread extends Thread {
    private File folder;

    public FolderDeleteThread(File folder) {
        this.folder = folder;
    }

    @Override
    public void run() {
        super.run();
        try {
            delete(folder);
            Logger.info("folder: " + folder + " deleted");
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File c : files)
                    delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
}
