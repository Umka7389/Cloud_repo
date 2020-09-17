import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String fileName;
    private String login;
    private byte[] data;

    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
    public FileMessage(String login, Path path) throws IOException, AccessDeniedException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.login = login;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getLogin() {
        return login;
    }
}
