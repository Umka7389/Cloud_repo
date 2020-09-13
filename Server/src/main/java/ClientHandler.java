import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Objects;

public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private static String serverStorage = "Server/ServerStorage/";


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        if (msg instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) msg;
            ctx.writeAndFlush(new UpdateMessage(getContentsOfCloudStorage(updateMessage.getLogin())));
        } else if (msg instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) msg;
            for (int i = 0; i < fileRequest.getFilesToRequest().size(); i++) {
                File file = new File(fileRequest.getFilesToRequest().get(i).getAbsolutePath());
                Path fileToRequest = Paths.get(fileRequest.getFilesToRequest().get(i).getAbsolutePath());
                try {
                    if (file.isDirectory()) {
                        if (Objects.requireNonNull(file.listFiles()).length == 0) {
                            ctx.writeAndFlush(new FileMessage(file.getName(), true, true));
                        } else {
                            ctx.writeAndFlush(new FileMessage(file.getName(), true, false));
                        }
                    } else {
                        try {
                            ctx.writeAndFlush(new FileMessage(fileToRequest));
                        } catch (AccessDeniedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (msg instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) msg;
            Path pathToNewFile = Paths.get(serverStorage + fileMessage.getLogin() + File.separator + fileMessage.getFileName());
                if (Files.exists(pathToNewFile)) {
                    System.out.println("Файл с таким именем уже существует");
                } else {
                    Files.write(Paths.get(serverStorage + fileMessage.getLogin() + File.separator + fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
                }
            ctx.writeAndFlush(new UpdateMessage(getContentsOfCloudStorage(fileMessage.getLogin())));

        } else if (msg instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) msg;
            DBRequestHandler.getConnectionWithDB();
            if (DBRequestHandler.checkIfUserExistsForAuthorization(authMessage.getLogin())) {
                if (DBRequestHandler.checkIfPasswordIsRight(authMessage.getLogin(), authMessage.getPassword())) {
                    ctx.writeAndFlush("userIsValid±" + (authMessage.getLogin()));
                } else {
                    ctx.writeAndFlush("wrongPassword");
                }
            } else {
                ctx.writeAndFlush("userDoesNotExist");
            }
            DBRequestHandler.disconnectDB();
        } else if (msg instanceof RegistrationMessage) {
            RegistrationMessage regMessage = (RegistrationMessage) msg;
            DBRequestHandler.getConnectionWithDB();
            if (DBRequestHandler.checkIfUserExistsForAuthorization(regMessage.getLogin())) {
                ctx.writeAndFlush("userAlreadyExists");
            } else {
                DBRequestHandler.registerNewUser(regMessage.getLogin(), regMessage.getPassword());
                File newDirectory = new File(serverStorage + regMessage.getLogin());
                newDirectory.mkdir();
                ctx.writeAndFlush("userRegistered");
            }
            DBRequestHandler.disconnectDB();
        }
    }

    public static LinkedList<File> getContentsOfCloudStorage(String login) {
        LinkedList<File> listCloudStorageFiles = new LinkedList<>();
        File path = new File(serverStorage + login);
        File[] files = path.listFiles();
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            listCloudStorageFiles.add(files[i]);
        }
        return listCloudStorageFiles;
    }

}
