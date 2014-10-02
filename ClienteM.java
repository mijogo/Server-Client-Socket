import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Console;
 
class ejecutarhilo implements Runnable 
{
	private Thread hilo;
	private String threadName;
   
   	public ejecutarhilo(String name)
	{
     		threadName = name;
   	}
   
	public void run() 
	{
		if(threadName == "hilo1")
		{
			Console console = System.console();
        		console.printf("Que accion desea realizar");
        		String dato = console.readLine();
			if(dato == "Join")
			{
				notify();
				Socket sock=null; 
				DataInputStream dis=null; 
				PrintStream ps=null;
				try 
				{
					int serverPORT = 9025;
					InetAddress ip =InetAddress.getByName("localhost");
					sock= new Socket(ip, serverPORT); 
					ps= new PrintStream(sock.getOutputStream());
					ps.println("Recuperar");
					BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					
					InetAddress rec_ip =InetAddress.getByName("localhost");
					int rec_resver_port = Integer.parseInt(is.readLine());
					Socket rec_sock= new Socket(rec_ip, rec_resver_port);
					DataInputStream rec_dis=null; 
					PrintStream rec_ps=null;
					rec_ps= new PrintStream(rec_sock.getOutputStream());
					rec_ps.println("recibir");
					BufferedReader rec_is = new BufferedReader(new InputStreamReader(rec_sock.getInputStream()));
					int contador_rec=0;
					String men_rec;
					do
					{
						men_rec = rec_is.readLine();
						if(men_rec!="Fin")
						{
							FileWriter fichero = null;
							PrintWriter pw = null;
							String Nombre_arch = "mensaje"+(contador_rec/20)+".txt";
							fichero = new FileWriter(Nombre_arch,true);
							pw = new PrintWriter(fichero);
							pw.println(men_rec);
							fichero.close();
						}
					}while(men_rec!="Fin");
			  		rec_sock.close();
				}
				catch(SocketException e)
				{ 
					System.out.println("SocketException " + e); 
          			}
				catch(IOException e)
				{ 
					System.out.println("IOException " + e);
				} 
				finally
				{
			  		try
					{
				  		sock.close(); 
			 		} 
					catch(IOException ie)
					{ 
						System.out.println("Error de cerrado :" + ie.getMessage()); 
					} 
				}
			}
		}
		else
		{
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int port = 12451;
		   	InetAddress group;
		   	MulticastSocket socket;
		   	DatagramPacket datagram;
			try {
				socket = new MulticastSocket(port);
				group = InetAddress.getByName("228.14.25.2");
				socket.joinGroup(group);
				byte[] buffer = new byte[1000];
				datagram = new DatagramPacket(buffer,buffer.length);
				socket.receive(datagram);
				String message = new String(datagram.getData());
				System.out.println(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
   	}
   
   	public void start ()
   	{
   		hilo = new Thread (this, threadName);
   		hilo.start ();
   	}
}

public class ClienteM
{
	public static void main(String args[]) 
	{
		ejecutarhilo H1 = new ejecutarhilo("hilo1");
		H1.start();
		ejecutarhilo H2 = new ejecutarhilo("hilo2");
		H2.start();
	}   
}
