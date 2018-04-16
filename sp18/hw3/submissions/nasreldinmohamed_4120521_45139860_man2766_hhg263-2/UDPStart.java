/*
Mohamed Nasreldin man2766
Hamza Ghani hhg263
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPStart extends Thread {
    private byte[] buffer;
    private parseInventory inventory;
    private DatagramSocket datasocket;
    private DatagramPacket datapacket;
    String order;
    public UDPStart(parseInventory inventory, int udpPort) throws SocketException {
        this.inventory = inventory;
        datasocket = new DatagramSocket(udpPort);
        this.buffer = new byte[1024];
        this.inventory = inventory;
    }

    public void run() {
        while (true) {
            this.buffer = new byte[1024];
            datapacket = new DatagramPacket(buffer, buffer.length);
            try {
                datasocket.receive(datapacket);
                order = new String(buffer).trim();
                Thread t = new UDPThread(inventory,datasocket,order,datapacket);
                t.start();
                /*
                String response = inventory.Command(command);
                returnBuffer = response.getBytes();
                returnpacket = new DatagramPacket(returnBuffer,returnBuffer.length,datapacket.getAddress(),datapacket.getPort());
                datasocket.send(returnpacket);
                // made new threads instead
                */
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
