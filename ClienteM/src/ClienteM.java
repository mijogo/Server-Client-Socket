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
			System.out.println("Que accion desea realizar");
			BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
        	String dato = null;
			try {
				dato = brc.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
					int contador_rec=0;
					String men_rec;
					do
					{
						men_rec = is.readLine();
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
			synchronized(hilo)
			{
				try {
					
					hilo.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
