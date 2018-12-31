
package server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.LinkedList;

import server.controller.Controller;

public class ClientHandler {

    private static final String JOIN_MESSAGE = " joined conversation.";
    private static final String LEAVE_MESSAGE = " left conversation.";
    private static final String USERNAME_DELIMETER = ": ";
    private Controller controller = new Controller();
    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(2048);
    private LinkedList<String> sendingQueue = new LinkedList<String>();
    private LinkedList<String> receivingQueue = new LinkedList<String>();


    /*
      Creates a new instance, which will handle communication with one specific
      client connected to the specified channel.     */
    ClientHandler(SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
    }

    
     // Sends the specified message to the connected client.
     
    void sendMsg() throws IOException {
        synchronized (sendingQueue) {
            while (!sendingQueue.isEmpty()) {
                ByteBuffer message = ByteBuffer.wrap(sendingQueue.remove().getBytes());
                try {
                    clientChannel.write(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
     // Reads a message from the connected client, and adds it to a queue for processing
     
    void recvMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        receivingQueue.add(extractMessageFromBuffer());
        gameProcessing();
    }

    public void gameProcessing() {
        // get the strings from the queue and pass them to the GameSetup
        while (!receivingQueue.isEmpty()) {
            String msg = receivingQueue.remove();
            controller.setInput(msg);
            addToSendingQueue(controller.getOutput());
        }
    }

    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }

    void disconnectClient() throws IOException {
        clientChannel.close();
    }

    private void addToSendingQueue(String msg) {
        synchronized (sendingQueue) {
            sendingQueue.add(msg);
        }
    }

}