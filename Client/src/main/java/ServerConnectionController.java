import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class ServerConnectionController {

    private static Socket socket;
    private static ObjectEncoderOutputStream encOS;
    private static ObjectDecoderInputStream decIS;

    private static final int maxObjectSize = 1024*1024*1024; // 1 GB


    public static void startConnection() {
        try {
            socket = new Socket("localhost", 8888);
            encOS = new ObjectEncoderOutputStream(socket.getOutputStream());
            decIS = new ObjectDecoderInputStream(socket.getInputStream(), maxObjectSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopConnection() {
        try {
            encOS.close();
            decIS.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendUpdateMessageToServer(String login) {
        try {
            encOS.writeObject(new UpdateMessage(login));
            encOS.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendAuthMessageToServer(String login, String password) {
        try {
            encOS.writeObject(new AuthMessage(login, password));
            encOS.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendRegMessageToServer(String login, String password) {
        try {
            encOS.writeObject(new RegistrationMessage(login, password));
            encOS.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readIncomingObject() throws IOException, ClassNotFoundException {
        return decIS.readObject();
    }

    public static void transferFilesToCloudStorage(String login, LinkedList<File> filesToSendToCloud) {
        try {
            if (!filesToSendToCloud.isEmpty()) {
                for (int i = 0; i < filesToSendToCloud.size(); i++) {
                    Path path = Paths.get(filesToSendToCloud.get(i).getAbsolutePath());
                    encOS.writeObject(new FileMessage(login, path));
                    encOS.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFileRequest(LinkedList<File> filesToRequest) {
        try {
            if (!filesToRequest.isEmpty()) {
                encOS.writeObject(new FileRequest(filesToRequest));
                encOS.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendDeletionMessage(String login, LinkedList<File> filesToDelete) {
        try {
            if (!filesToDelete.isEmpty()) {
                encOS.writeObject(new DeletionMessage(login, filesToDelete));
                encOS.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
