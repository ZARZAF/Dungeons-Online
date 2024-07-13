package server.communication;

import logger.Logger;
import server.managers.CommandManager;
import server.managers.UserManager;
import server.managers.message.Message;
import server.managers.user.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class ClientCommunication {

    public static final int SERVER_PORT = 3333;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private static final String SEND_PREFIX = "send: ";
    private static final String RECEIVE_PREFIX = "receive: ";
    private static ConcurrentLinkedQueue<Message> messagesToSend = new ConcurrentLinkedQueue<>();

    public static void sendMessage(Message message) {
        messagesToSend.add(message);
    }

    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                handleKeyIterator(keyIterator, buffer, selector);

                sendPendingMessages();
            }
        } catch (IOException e) {
            System.out.println("There is a problem with the server socket");
            Logger.logError(Logger.error(), Level.SEVERE, "There is a problem with the server socket");
            System.exit(-1);
        }
    }

    private static void handleKeyIterator(Iterator<SelectionKey> keyIterator, ByteBuffer buffer, Selector selector)
            throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                handleReadableKey(key, buffer);
            } else if (key.isAcceptable()) {
                handleAcceptableKey(key, selector);
            }
            keyIterator.remove();
        }
    }

    private static void sendMessageToPlayer(String message, SocketChannel userSocketChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        while (buffer.hasRemaining()) {
            if (userSocketChannel.isOpen()) {
                userSocketChannel.write(buffer);
            }
        }
        buffer.clear();
    }

    private static void sendPendingMessages() throws IOException {
        while (!messagesToSend.isEmpty()) {
            Message message = messagesToSend.poll();
            message.endMessage();

            for (User u : message.getRecipient()) {
                if (u.getSocketChannel().isOpen()) {
                    sendMessageToPlayer(message.toString(), u.getSocketChannel());
                }
            }

            Logger.logError(Logger.game(), Level.FINE, SEND_PREFIX + message.toString());
            System.out.println("Sending message to clients: " + message.toString());
        }
    }

    private static void handleReadableKey(SelectionKey key, ByteBuffer buffer) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        if (sc.socket() == null) {
            return;
        }

        buffer.clear();
        int r = 0;
        try {
            r = sc.read(buffer);
        } catch (IOException e) {
            Logger.logError(Logger.error(), Level.WARNING
                    , "User " + UserManager.getUser(sc).get().getUsername() + " left the game.");
            sc.close();
            key.cancel();
            key.channel().close();

            return;
        }
        if (r < 0) {
            System.out.println("Client has closed the connection");
            sc.close();
            key.cancel();
            key.channel().close();
        } else {
            handleReading(sc, buffer);
        }
    }

    private static void handleReading(SocketChannel sc,  ByteBuffer buffer) throws IOException {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String message = new String(bytes);
        Logger.logError(Logger.game(), Level.FINE, RECEIVE_PREFIX + message);

        User player = UserManager.getUser(sc).get();
        CommandManager.executeCommand(message, player);
        sc.write(buffer);
    }

    private static void handleAcceptableKey(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);

        System.out.println("Client connected.");

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = accept.read(buffer);
        if (bytesRead > 0) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String username = new String(bytes);

            System.out.println("Client username " + username);
            if (UserManager.getUser(username).isPresent()) {
                accept.close();
                Logger.logError(Logger.error(), Level.WARNING, username + " already logged in");
            }

            UserManager.addUser(User.create(username, accept));
        } else {
            accept.close();
            Logger.logError(Logger.error(), Level.WARNING, "User tries to log with empty username");
        }
    }

}


