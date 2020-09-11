import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            if (!Files.exists(Paths.get("Server/" + message.getName()))) {
                Files.createFile(Paths.get("Server/" + message.getName()));
                Files.write(
                        Paths.get("Server/" + message.getName()),
                        message.getData(),
                        StandardOpenOption.APPEND);
            }
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
        }else if (msg instanceof RegistrationMessage) {
            RegistrationMessage regMessage = (RegistrationMessage) msg;
            DBRequestHandler.getConnectionWithDB();
            if (DBRequestHandler.checkIfUserExistsForAuthorization(regMessage.getLogin())) {
                ctx.writeAndFlush("userAlreadyExists");
            } else {
                DBRequestHandler.registerNewUser(regMessage.getLogin(), regMessage.getPassword());
                File newDirectory = new File("Server/ServerStorage/"+regMessage.getLogin());
                newDirectory.mkdir();
                ctx.writeAndFlush("userRegistered");
            }
            DBRequestHandler.disconnectDB();
        }
    }
}
