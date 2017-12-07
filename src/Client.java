import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;

    private String currentUsername = "";
    private String lastMessage = "";

    private ConnectRunnable connectRunnable;
    private DataSenderRunnable dataSenderRunnable;
    private DataGetterRunnable dataGetterRunnable;
    private UserInputRunnable  userInputRunnable;


    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public void run() {
        connectRunnable = new ConnectRunnable(this);
        Thread connectThread = new Thread(connectRunnable);
        connectThread.start();

        userInputRunnable = new UserInputRunnable();
        Thread userInputThread = new Thread(userInputRunnable);
        userInputThread.start();
    }

    public void connected() {
        this.socket = connectRunnable.getSocket();

        dataSenderRunnable = new DataSenderRunnable(this, socket);
        Thread senderThread = new Thread(dataSenderRunnable);
        senderThread.start();

        dataGetterRunnable = new DataGetterRunnable(this, socket);
        Thread getterThread = new Thread(dataGetterRunnable);
        getterThread.start();
    }

    public void killUserInput(){
        userInputRunnable.kill();
    }

    public String getNextMessage(){
        return userInputRunnable.getNextMessage();
    }

    public boolean hasMessages(){
        return userInputRunnable.hasMessages();
    }

    public void setWaitingForData(){
        dataGetterRunnable.setWaitingForData();
    }

    public void confirmData(String data){
        dataSenderRunnable.receivedData(data);
    }

    public void killProcesses(){
        dataGetterRunnable.kill();
        dataSenderRunnable.kill();
        connectRunnable.reconnect();
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void stopConnecting(){
        connectRunnable.kill();
    }

}
