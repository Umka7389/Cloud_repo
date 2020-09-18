import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class UpdateMessage extends AbstractMessage {
    private LinkedList<File> cloudStorageContents;
    private String login;

    public UpdateMessage(LinkedList<File> cloudStorageContents) {
        this.cloudStorageContents = cloudStorageContents;
    }
    public UpdateMessage(String login){
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public LinkedList<File> getCloudStorageContents() {
        return cloudStorageContents;
    }
}
