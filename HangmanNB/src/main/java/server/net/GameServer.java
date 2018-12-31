package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class GameServer {

    private static final int LINGER_TIME = 5000;
    private static final int TIMEOUT_HALF_HOUR = 1800000;
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    private int portNo = 8080;
    private Selector selector;
    private ServerSocketChannel listeningSocketChannel;
    private volatile boolean timeToBroadcast = false;

    private void serve() {
        ServerSocket serverSocket;
        try {
            while (true) {
                System.out.println("Starting Server");
                initSelector();
                initListeningSocketChannel();
                System.out.println("Waiting for client");
                while (true) {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            startHandler(key);
                        } else if (key.isReadable()) {
                            recvFromClient(key);
                        } else if (key.isWritable()) {
                            sendToClient(key);
                        } else {
                            removeClient(key);
                        }

                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server failure.");
        }
    }

    private void startHandler(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        ClientHandler handler = new ClientHandler(clientChannel);
        key.attach(handler);
        System.out.println("client is connected " + clientChannel.socket());
        clientChannel.register(selector, SelectionKey.OP_READ, handler);
        clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
    }

    private void recvFromClient(SelectionKey key) throws IOException {
        ClientHandler handler = (ClientHandler) key.attachment();
        handler.recvMsg();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void sendToClient(SelectionKey key) throws IOException {
        ClientHandler clientHandler = (ClientHandler) key.attachment();
        clientHandler.sendMsg();
        key.interestOps(SelectionKey.OP_READ);
    }

    private void removeClient(SelectionKey clientKey) throws IOException {
        ClientHandler clientHandler = (ClientHandler) clientKey.attachment();
        clientHandler.disconnectClient();
        clientKey.cancel();
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
    }

    private void initListeningSocketChannel() throws IOException {
        listeningSocketChannel = ServerSocketChannel.open();
        listeningSocketChannel.configureBlocking(false);
        listeningSocketChannel.bind(new InetSocketAddress(portNo));
        listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String[] args) throws IOException {

        GameServer server = new GameServer();
        System.out.println("Entering serve");
        server.serve();
        System.out.println("Exiting serve");
    }

}
