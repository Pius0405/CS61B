package gitlet;

import java.io.Serializable;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private final String contents;
    private final String filename;
    private final String ID;

    public Blob(String contents, String filename) {
        this.contents = contents;
        this.filename = filename;
        this.ID = sha1(contents, filename);
    }

    public String getID() {
        return ID;
    }

    public String getContents() {
        return contents;
    }

    public String getFilename() {
        return filename;
    }
}
