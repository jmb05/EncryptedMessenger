package net.jmb19905.networking.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.crypto.EncryptedConnection;
import net.jmb19905.exception.InvalidUsernameException;
import net.jmb19905.networking.packets.*;
import net.jmb19905.util.EncryptionUtility;
import net.jmb19905.util.Logger;
import net.jmb19905.util.SerializationUtility;

import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;

/**
 * The client-side Handler for the client-server connection
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * The Encryption to thw Server
     */
    private final EncryptedConnection connection = new EncryptedConnection();

    /**
     * Executes when the Connection to the Server starts
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Connected to Server");

        KeyExchangePacket packet = new KeyExchangePacket();
        packet.key = connection.getPublicKey().getEncoded();

        final ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(packet.deconstruct());

        Logger.log("Sending buffer:" + buffer, Logger.Level.TRACE);
        ctx.writeAndFlush(buffer);
    }

    /**
     * Executes when the connection to the server drops
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ClientMain.window.appendLine("Disconnected from Server");
        ClientMain.window.dispose();
    }

    /**
     * Executes when a packet from the server is received
     * @param msg the packet as Object
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        try {
            byte[] encryptedArray = new byte[buffer.readableBytes()];
            buffer.readBytes(encryptedArray);
            byte[] array;
            if(connection.isUsable()){
                array = connection.decrypt(encryptedArray);
            }else {
                array = encryptedArray;
            }
            Packet packet = Packet.constructPacket(array);
            if(packet instanceof KeyExchangePacket){
                if(((KeyExchangePacket) packet).type.equals("Server")) {
                    activateServerEncryption((KeyExchangePacket) packet);
                    login(ctx);
                }else if(((KeyExchangePacket) packet).type.equals("Client")){
                    activatePeerEncryption((KeyExchangePacket) packet);
                }
            }else if(packet instanceof LoginPacket){
                Client.peerName = ((LoginPacket) packet).name;
                ClientMain.window.appendLine(Client.peerName + " joined");

                initiatePeerEncryption(ctx);
            }else if(packet instanceof DisconnectPacket){
                ClientMain.window.appendLine(Client.peerName + " left");
                Client.peerName = "";
                Client.peerConnection = new EncryptedConnection();
            }else if(packet instanceof MessagePacket){
                ClientMain.window.appendLine("<" + Client.peerName + "> " + new String(Client.peerConnection.decrypt(((MessagePacket) packet).message.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            }
        } catch (InvalidKeySpecException e) {
            Logger.log(e, Logger.Level.FATAL);
            System.exit(-1);
        } catch (InvalidUsernameException e) {
            Logger.log(e, "Other User has invalid username", Logger.Level.WARN);
        } finally {
            buffer.release();
        }
    }

    /**
     * Sends the PublicKey for the peer to the server to be forwarded
     */
    private void initiatePeerEncryption(ChannelHandlerContext ctx) {
        KeyExchangePacket keyExchangePacket = new KeyExchangePacket();
        keyExchangePacket.type = "Client";
        keyExchangePacket.key = Client.peerConnection.getPublicKey().getEncoded();

        ByteBuf keyPackBuffer = ctx.alloc().buffer();
        keyPackBuffer.writeBytes(connection.encrypt(keyExchangePacket.deconstruct()));
        ctx.writeAndFlush(keyPackBuffer);
    }

    /**
     * Creates the SharedSecret-Key to the Peer from the peer's PublicKey and the PrivateKey
     * @param packet the packet containing the encoded PublicKey
     * @throws InvalidKeySpecException if the PublicKey of the peer is invalid
     */
    private void activatePeerEncryption(KeyExchangePacket packet) throws InvalidKeySpecException {
        Client.peerConnection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        Logger.log("SharedSecret to Other Client: " + SerializationUtility.encodeBinary(Client.peerConnection.getSharedSecret()), Logger.Level.TRACE);
        ClientMain.window.appendLine("Connection to " + Client.peerName + " encrypted");
    }

    /**
     * Creates the SharedSecret-Key to the server from the server's PublicKey and the PrivateKey
     * @param packet the packet containing the encoded PublicKey
     * @throws InvalidKeySpecException if the PublicKey of the server is invalid
     */
    private void activateServerEncryption(KeyExchangePacket packet) throws InvalidKeySpecException {
        connection.setReceiverPublicKey(EncryptionUtility.createPublicKeyFromData(packet.key));
        Logger.log("SharedSecret to Server: " + SerializationUtility.encodeBinary(connection.getSharedSecret()), Logger.Level.TRACE);
        ClientMain.window.appendLine("Connection to Server encrypted");
    }

    /**
     * Sends a LoginPacket with the Client's name to the server
     */
    private void login(ChannelHandlerContext ctx) {
        LoginPacket loginPacket = new LoginPacket();
        loginPacket.name = ClientMain.client.name();
        ByteBuf loginBuffer = ctx.alloc().buffer();
        loginBuffer.writeBytes(connection.encrypt(loginPacket.deconstruct()));
        ctx.writeAndFlush(loginBuffer);
    }

    /**
     * Executed if an exception is caught
     * @param cause the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(cause, Logger.Level.ERROR);
        ctx.close();
    }

    public EncryptedConnection getConnection() {
        return connection;
    }
}
