package net.jmb19905.messenger.messages;

import com.esotericsoftware.kryonet.Connection;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.server.MessagingServer;
import net.jmb19905.messenger.server.ServerMain;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterMessage extends EMMessage {

    public String username;
    public String password;

    public RegisterMessage(){}

    @Override
    public void handleOnClient(Connection connection) throws UnsupportedSideException {
        throw new UnsupportedSideException("RegisterMessage received on client");
    }

    @Override
    public void handleOnServer(Connection connection) {
        Node node =  MessagingServer.clientConnectionKeys.get(connection).getNode();
        if(!MessagingServer.clientConnectionKeys.get(connection).isLoggedIn()) {
            String username = Util.decryptString(node, this.username);
            String password = Util.decryptString(node, this.password);
            SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
            if (user == null) {
                //User does not exist create a new one
                UUID uuid = SQLiteManager.createUser(username, password);
                ServerMain.messagingServer.sendRegisterSuccess(connection, username, uuid);
                MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
            } else {
                if (BCrypt.hashpw(password,user.salt).equals(user.password)) {
                    EMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                    MessagingServer.clientConnectionKeys.get(connection).setUsername(user.username);
                    MessagingServer.clientConnectionKeys.get(connection).setLoggedIn(true);
                    connection.sendTCP(new LoginSuccessMessage());
                } else {
                    connection.sendTCP(new UsernameAlreadyExistMessage());
                }
            }
        }else{
            EMLogger.warn("MessagingServer", "Already registered client tried to register");
        }
    }
}