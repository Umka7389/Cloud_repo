import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Objects;

public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private static final String serverStorage = "Server/ServerStorage/";
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        if (msg instanceof UpdateMessage) {
            UpdateMessage updateMessage = (UpdateMessage) msg;
            ctx.writeAndFlush(new UpdateMessage(getContentsOfCloudStorage(updateMessage.getLogin())));
        } else if (msg instanceof DeletionMessage) {
            DeletionMessage deletionMessage = (DeletionMessage) msg;
            for (int i = 0; i < deletionMessage.getFilesToDelete().size(); i++) {
                File fileToDelete = new File(deletionMessage.getFilesToDelete().get(i).getAbsolutePath());
                fileToDelete.delete();
            }
            deletionMessage.getFilesToDelete().clear();
            ctx.writeAndFlush(new UpdateMessage(getContentsOfCloudStorage(deletionMessage.getLogin())));
            logger.info("File(s) deleted");
        } else if (msg instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) msg;
            for (int i = 0; i < fileRequest.getFilesToRequest().size(); i++) {
                Path fileToRequest = Paths.get(fileRequest.getFilesToRequest().get(i).getAbsolutePath());
                try {
                    ctx.writeAndFlush(new FileMessage(fileToRequest));
                    logger.info("File(s) downloaded");
                } catch (IOException e) {
                    logger.warn("Download failure");
                }
            }
        } else if (msg instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) msg;
            Path pathToNewFile = Paths.get(serverStorage + fileMessage.getLogin() + File.separator + fileMessage.getFileName());
            if (Files.exists(pathToNewFile)) {
                logger.info("File already existed");
            } else {
                Files.write(Paths.get(serverStorage + fileMessage.getLogin() + File.separator + fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
                logger.info("File(s) uploaded");
            }
            ctx.writeAndFlush(new UpdateMessage(getContentsOfCloudStorage(fileMessage.getLogin())));
        } else if (msg instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) msg;
            DBRequestHandler.getConnectionWithDB();
            if (DBRequestHandler.checkIfUserExistsForAuthorization(authMessage.getLogin())) {
                if (DBRequestHandler.checkIfPasswordIsRight(authMessage.getLogin(), authMessage.getPassword())) {
                    ctx.writeAndFlush("userIsValid±" + (authMessage.getLogin()));
                    logger.info("User authorization successful");
                } else {
                    ctx.writeAndFlush("wrongPassword");
                    logger.info("Wrong password");
                }
            } else {
                ctx.writeAndFlush("userDoesNotExist");
                logger.info("User do not exist");
            }
            DBRequestHandler.disconnectDB();
        } else if (msg instanceof RegistrationMessage) {
            RegistrationMessage regMessage = (RegistrationMessage) msg;
            DBRequestHandler.getConnectionWithDB();
            if (DBRequestHandler.checkIfUserExistsForAuthorization(regMessage.getLogin())) {
                ctx.writeAndFlush("userAlreadyExists");
                logger.info("User already exists");
            } else {
                DBRequestHandler.registerNewUser(regMessage.getLogin(), regMessage.getPassword());
                File newDirectory = new File(serverStorage + regMessage.getLogin());
                newDirectory.mkdir();
                ctx.writeAndFlush("userRegistered");
                logger.info("User successfully registered");
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
