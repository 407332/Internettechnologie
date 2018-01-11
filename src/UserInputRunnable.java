import java.util.*;

public class UserInputRunnable implements Runnable {

    private Queue<String> messages = new PriorityQueue<>();
    private boolean kill;

    private Scanner userIn = new Scanner(System.in);

    @Override
    public void run() {
        while(!kill){
            String input = userIn.nextLine();
            messages.add(input);
            if(input.equals("QUIT")){
                kill = true;
            }
        }
    }

    public boolean hasMessages(){
        return messages.size() >0;
    }

    public String getNextMessage(){
        //System.out.println(messages.toString());
        return messages.poll();
    }

    public void kill(){
        userIn.close();
        kill = true;
    }
}
