import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class Transmitter_TCP implements Transmitter {
    Socket newTCP;
    int socketNum;
    BufferedReader buff;
    DataOutputStream data;
   
    Transmitter_TCP(int num) {
        socketNum = num;
        try {
            newTCP = new Socket("localhost", socketNum);
            buff = new BufferedReader(new InputStreamReader(newTCP.getInputStream()));
            data = new DataOutputStream(newTCP.getOutputStream());
        } catch(IOException e) {
            System.err.println("I/O Exception in connection to localhost");
        } catch(UnknownHostException e) {
            System.err.println("Unknown Host: localhost");
        }
    }
    
    @Override
    public String transmit_String(String str) {
        try {
            buff.writeBytes(str + "\r");
        } catch(IOException e) {
            System.err.println("I/O Exception in string transmission");
        }
        String input = "";
        String output = "";
        int count;
        try {
            input = data.readLine();
            count = Integer.valueOf(input);
            for(int a = 0; a < count; a++) {
                input = data.readLine();
                output += input  + "\n";
            }
        } catch(IOException) {
            System.err.println("I/O Exception in response reception");
        }
        return output;
    }
    public void close() {
        try {
            data.writeBytes("exit");
            newTCP.close();
        } catch(IOException e) {
            System.err.println("I/O Exception in socket close");
        } catch(Exception e) {
            System.err.println("Exception in socket close");
        }
    }
}