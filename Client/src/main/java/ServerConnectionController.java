import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class ServerConnectionController {

    private static Socket socket;
    private static ObjectEncoderOutputStream encOS;
    private static ObjectDecoderInputStream decIS;
    private static final int maxObjectSize = 1024 * 1024 * 20; // 20 MB


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
        Object object = decIS.readObject();
        return object;
    }
}
