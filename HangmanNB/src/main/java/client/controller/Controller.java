package client.controller;

import client.net.ServerConnector;


public class Controller {

    private ServerConnector connector = new ServerConnector();

    public void connect() {
        connector.connectToServer();
    }

    public void disconnect() {
        connector.disconnectFromServer();
    }

    public void addUserMsgToQueue ( String msg){
        connector.addUserMsgToQueue(msg);
    }

}