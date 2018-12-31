
package client.net;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class ServerConnector implements Runnable {

    private Socket socket;

    private static final int portNum = 8080;
    private static InetAddress host;

    private InetSocketAddress serverAddress;
    private SocketChannel socketChannel;
    private Selector selector;
    private LinkedList<String> sendingQueue = new LinkedList<String>();
    private ByteBuffer bufferFromServer = ByteBuffer.allocateDirect(2048);
    private volatile boolean timeToSend = false;

    public void connectToServer() {
        serverAddress = new InetSocketAddress("localhost", portNum);
        System.out.println(" Thank you for playing my Hangman game!" + "\n" + " The rules are very simple. The mastermind will choose a random word and you will have to guess it.");
        System.out.println(" You can either guess it letter by letter (to do that, simply type the letter), or by typing in the whole word right away");
        System.out.println(" If you guess a letter correctly it will appear in all the places it occurs in the word, and if you don't, the amount of remaining attempts will decrease.");
        System.out.println(" Type 'Start' to start a new game session and reset the score." + "\n" + " Alternatively, Type 'Quit' to exit the program.");
        System.out.println(" You are able to get a new word at any time by typing 'Next'" + "\n" + " However if you are not done with the current word the score will decrease.");
        System.out.println(" Dear user, this program is NoT case sensitive, so do not worry about it! Please do not forget to hit 'Enter' after every guess or command");
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            initConnection();
            initSelector();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {

                selector.select();
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    } else if (key.isReadable()) {
                        recvFromServer(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
    }

    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void sendToServer(SelectionKey key) throws IOException {
        synchronized (sendingQueue) {
            while (!sendingQueue.isEmpty()) {
                ByteBuffer bufferTosend = ByteBuffer.wrap(sendingQueue.remove().getBytes());
                socketChannel.write(bufferTosend);

                if (bufferTosend.hasRemaining()) {
                    return;
                }
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void recvFromServer(SelectionKey key) throws IOException {
        bufferFromServer.clear();
        int numOfReadBytes = socketChannel.read(bufferFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException("Not able to read from server");
        }
        String recvdString = extractMessageFromBuffer();
        showOutput(recvdString);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void disconnectFromServer() {
        System.out.println("client is disconnected");
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socketChannel.keyFor(selector).cancel();
    }

    public void addUserMsgToQueue(String msg) {
        synchronized (sendingQueue) {
            sendingQueue.add(msg);
        }
        this.timeToSend = true;
        selector.wakeup();
    }

    private String extractMessageFromBuffer() {
        bufferFromServer.flip();
        byte[] bytes = new byte[bufferFromServer.remaining()];
        bufferFromServer.get(bytes);
        return new String(bytes);
    }

    private void showOutput(String recvdString) {
        String[] dataToShow = recvdString.split("/");
        System.out.println("---" + dataToShow[3] + "---");
        System.out.println("Score: " + dataToShow[0] + "     Attempts left: " + dataToShow[1]);
        System.out.println("Word to guess:   " + dataToShow[2] + "\n");

    }
}
