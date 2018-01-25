import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class DataGetterRunnable implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    private Client client;

    private boolean waitingForData;
    private boolean kill;

    private CountDownLatch latch;
    private String connectionResult;


    public DataGetterRunnable(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    public DataGetterRunnable(Socket socket, CountDownLatch latch) {
        this.socket = socket;
        this.latch = latch;
        connectionResult = "";
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (!kill) {
                String line = bufferedReader.readLine();
                if (latch != null) {
                    connectionResult = line;
                    latch.countDown();
                    kill = true;
                } else {
                    if (waitingForData && line != null) {
                        client.confirmData(line);
                        waitingForData = false;
                    } else if (line == null) {
                        client.killProcesses();
                    } else if (line.startsWith("GETNEWKEY")) {
                        String[] splits = line.split(" ");
                        if (splits.length > 2){
                            String name = splits[1];
                            stringToKey(name , splits[2]);
                            System.out.println("Key added from " + name);
                        }
                    } else if (line.startsWith("WHISPER")) {
                        String sender;
                        String message64;
                        String[] splits = line.split(" ");

                        if (splits.length > 2) {
                            sender = splits[1];
                            message64 = splits[2];
                            PublicKey senderPK = client.getkeyfromlist(sender);
                            if (senderPK != null) {
                                decryptPrivateMessage(message64, senderPK);
                            } else {
                                System.out.println("No publickey found");
                            }
                        } else {
                            System.out.println("Problem with Message");
                        }
                    } else if (line.startsWith("TRNSFR")) {
                        String[] split = line.split(" ");
                        try {
                            if (split.length > 1) {
                                System.out.println("getting a file from " + split[2]);
                                receiveFile();
                            } else {
                                System.out.println("Filetransfer had a problem");
                            }
                        } catch (IOException io) {
                            System.out.println("Filetransfer had a problem");
                        }
                    }
                    if (line.startsWith("BCST")) {
                        System.out.println(line.substring(line.split(" ")[0].length() + 1));
                    }
                }
            }
        } catch (SocketException se) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (latch == null) {
            //System.out.println("killing getter");
        }
    }

    public void setWaitingForData() {
        this.waitingForData = true;
    }

    public void kill() {
        this.kill = true;
    }

    public String getConnectionResult() {
        return connectionResult;
    }

    public void receiveFile() throws IOException {
        DataInputStream is = new DataInputStream(socket.getInputStream());
        byte[] buffer = new byte[4096];

        is.read(buffer, 0, buffer.length);
        String file = new String(buffer).trim();

        is.read(buffer, 0, buffer.length);
        int filesize = Integer.parseInt(new String(buffer).trim());

        FileOutputStream fos = new FileOutputStream(file);
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = is.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }
        if (totalRead == filesize) {
            System.out.println("file: " + file + " has been received");
            fos.close();
        } else {
            System.out.println("Filetransfer had a problem");
            fos.close();
        }
    }

    private void decryptPrivateMessage(String message64, PublicKey senderPK) {
        byte[] messageEncrypted = base64decrypt(message64);
        byte[] messagebyte = decrypt(messageEncrypted, senderPK);
        String message = new String(messagebyte);
        System.out.println(message);
    }

    private byte[] base64decrypt(String message64) {
        byte[] message = Base64.getDecoder().decode(message64);
        return message;
    }

    private static byte[] decrypt(byte[] message, PublicKey senderPK) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, senderPK);
            return cipher.doFinal(message);
        } catch (Exception e) {
            return null;
        }
    }

    private void stringToKey(String username, String message64){
        byte [] almostkey = base64decrypt(message64);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(almostkey));
            client.putKey(username, publicKey);
        } catch (GeneralSecurityException gse) {
            System.out.println("Problem reinstanceiating Publickey");
        }
    }

}
