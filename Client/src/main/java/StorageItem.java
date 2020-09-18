import java.io.File;

public class StorageItem {

    private String name;
    private long size;
    private String lastModificationDate;
    private File pathToFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public File getPathToFile() {
        return pathToFile;
    }

    public StorageItem(String name, long size, boolean isChosen, String lastModificationDate, File pathToFile) {
        this.name = name;
        this.size = size;
        this.lastModificationDate = lastModificationDate;
        this.pathToFile = pathToFile;
    }
}