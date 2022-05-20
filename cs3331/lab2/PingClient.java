import java.io.*;
import java.net.*;
import java.util.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;

public class PingClient
{
   
      public static void main(String[] args) throws Exception 
      {
      
         if (args.length != 2) {
            System.out.println("Required arguments: host, port");
            return;
         }
         
         
         InetAddress clientHost;
         if ("localhost".equals((args[0]))) {
            clientHost = InetAddress.getByName("127.0.0.1");
         } else {
            clientHost = InetAddress.getByName(args[0]);
         }
         
         int clientPort = Integer.parseInt(args[1]);
      
         DatagramSocket socket = new DatagramSocket(1024);
         socket.setSoTimeout(600);
         
         ArrayList<Long> ping_delays = new ArrayList<Long>(); 
         
         int pingCount = 0;
         while (pingCount < 15) {
         
            // DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
         
            // InetAddress clientHost = InetAddress.getLocalHost();
            // clientPort = 8080;
            byte[] buf = new byte[4];
            buf[0] = 3;
            buf[1] = 3;
            buf[2] = 3;
            buf[3] = 1;
            DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
            int number = 3331 + pingCount;
            String keyword = "PING";
            
            Date date= new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time);
            
            String payload = number + " " + keyword + " " + ts;
            byte[] bytes = payload.getBytes();
            
            reply.setData(bytes);
            // System.out.println(reply.getData());
            socket.send(reply);
            Instant start = Instant.now();
            
            try {
               DatagramPacket replyFromServer = new DatagramPacket(new byte[1024], 1024);
               socket.receive(replyFromServer);
               Instant end = Instant.now();
               Duration timeElapsed = Duration.between(start, end);
               String s = String.format("ping to %s, seq = %d, rtt = %dms", clientHost.getHostAddress(), pingCount + 1, timeElapsed.toMillis());
               ping_delays.add(timeElapsed.toMillis());
               System.out.println(s);
            } catch (SocketTimeoutException e) {
               String s = String.format("ping to %s, seq = %d, time out", clientHost.getHostAddress(), pingCount + 1);
               System.out.println(s);
            }
            pingCount += 1;
            TimeUnit.SECONDS.sleep(1);
         }
         
         // System.out.println(ping_delays);
         System.out.println("min RTT: " + Collections.min(ping_delays));
         System.out.println("max RTT: " + Collections.max(ping_delays));
         System.out.println("average RTT: " + getAverage(ping_delays));
      }
      
      
      public static long getAverage(ArrayList<Long> ping_delays) {
         long avg = 0;
         long total = 0;
         for (int i = 0; i < ping_delays.size(); i++) {
            total += ping_delays.get(i);
         }
         avg = total / ping_delays.size();
         return avg;
      }
      
}
