import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDP_Handler implements Runnable {

    private DatagramPacket inPacket;

    public UDP_Handler(DatagramPacket inPacket) {
        this.inPacket = inPacket;
    }


    @Override
    public void run() {

        String cmd = new String(inPacket.getData(), 0,
                inPacket.getLength());
        //System.out.println("Server received UDP cmd: " + cmd);

//        if (BookServer.debug) {
//            BookServer.writeCommand(cmd);
//        }

        String[] tokens = cmd.split(" ");

        String[] response = BookServer.makeTransaction(tokens[0], cmd);
        //System.out.println(Arrays.toString(response));

        if (response != null) {

            StringBuilder stringData = new StringBuilder();
            for (String s : response) {
                stringData.append(String.format("%s\n", s));
            }
            stringData.append(":::end:::");

            //System.out.println("Sending:");
            //System.out.println(stringData.toString());

            byte[] responseBuffer = stringData.toString().getBytes();
            DatagramPacket responsePacket =  new DatagramPacket
                    (responseBuffer, responseBuffer.length, inPacket.getAddress(), inPacket.getPort());
            BookServer.sendUDPResponse(responsePacket);

        }

    }


}
