import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private String currentUsername = "";
    private ConnectRunnable connectRunnable;
    private DataSenderRunnable dataSenderRunnable;
    private DataGetterRunnable dataGetterRunnable;

    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public void run() {
        connectRunnable = new ConnectRunnable(this);
        Thread connectThread = new Thread(connectRunnable);
        connectThread.start();
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

    public void setWaitingForData(){
        dataGetterRunnable.setWaitingForData();
    }

    public void confirmData(String data){
        dataSenderRunnable.receivedData(data);
    }

    public void reconnect(){
        connectRunnable.reconnect();
    }

    public void killProcesses(){
        dataGetterRunnable.kill();
        dataSenderRunnable.kill();
    }

}
