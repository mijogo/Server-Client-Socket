import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.io.Serializable;
//import java.io.Console;
 
class ejecutarhilo implements Runnable 
{
	private Thread hilo;
	private String threadName;
	private static Semaphore sema = new Semaphore(0);  //semaforo parte en cero para que el hilo2 no reciba nada hasta que el usuario decida que hacer.
   
   	public ejecutarhilo(String name)
	{
     		threadName = name;
   	}
   
	public void run() 
	{
		if(threadName == "hilo1")
		{
			//Hilo unicast, este hilo se preocupa de que le envien los mensajes anteriores
			Boolean connected = false;
			while(!connected)
			{
				System.out.println("Ingrese \"Pull\" para recibir el historial al unirse o \"Join\" para solo unirse.");
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
	        	String dato = null;
				try {
					dato = brc.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(dato.equals("Pull"))
				{
					//System.out.println(dato);
					Socket sock=null; 
					DataInputStream dis=null; 
					PrintStream ps=null;
					
					while(!connected)
					{
						try 
						{
							connected = true;
							int serverPORT = 9025;
							int cant_bytes;
							int tam_archivo;
							InetAddress ip =InetAddress.getByName("localhost");
							sock= new Socket(ip, serverPORT); 
							//ps= new PrintStream(sock.getOutputStream());
							//ps.println("Recuperar");
							BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
							String dato_archivo = is.readLine();
							cant_bytes = Integer.parseInt(dato_archivo);
							System.out.println(cant_bytes);
							is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
							dato_archivo = is.readLine();
							tam_archivo = Integer.parseInt(dato_archivo);
							System.out.println(tam_archivo);
							sema.release(); //dejamos que el otro hilo escuche y guarde momentaneamente los mensajes que puedan llegar mientras llega el historial.
							 
				            FileOutputStream fos = new FileOutputStream("Historial_temp.txt");
				            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				            Object mensajeAux;
				            for(int i =tam_archivo;i>-1;i-=cant_bytes)
				            {
				            	mensajeAux = ois.readObject();
				            	if(cant_bytes<i)
				            		fos.write(serialize(mensajeAux), 0,cant_bytes);
				            	else
				            		fos.write(serialize(mensajeAux), 0,i);
				            	
				            		
				            }
				            fos.close();
				            ois.close();
							
							sema.release();
						}
						catch(SocketException e)
						{ 
							//darle un sleep, para meterse de nuevo al bucle y pedir nuevamente conexion
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							connected = false;
							System.out.println("SocketException " + e); 
		          		}
						catch(IOException e)
						{ 
							System.out.println("IOException " + e);
						} 
						catch (ClassNotFoundException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
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
				else if(dato.equals("Join")){
					connected = true;
					sema.release();//doble release para que el otro hilo pase a mostrar los mensajes live.
					sema.release();
				}
				//{}
			}
			while(connected)
			{
				System.out.println("Ingrese \"Recover\" para reparar posibles mensajes perdidos o \"Leave\" para desconectarse.");
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
				String dato = null;
				try {
					dato = brc.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(dato.equals("Recover")){
					//recuperacion de mensajes perdidos. --> pedir historial denuevo y comparar
				}else if (dato.equals("Leave")){
					//Desconectar
				}
			}
		}
		else
		{	
			try {
				sema.acquire();   //Si se esta descargando el historial, empesamos a escuchar.
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int port = 12451;
		   	InetAddress group;
		   	MulticastSocket socket;
		   	DatagramPacket datagram;
			try 
			{
				socket = new MulticastSocket(port);
				group = InetAddress.getByName("228.14.25.2");
				socket.joinGroup(group);
				byte[] buffer = new byte[1000];
				datagram = new DatagramPacket(buffer,buffer.length);
				while(true)
				{	
					if(sema.availablePermits()== 0){ //estamos aun descargando el historial, aqui el los mensajes deben ser guardados y mostrados cuando lleguen los permisos.
						socket.receive(datagram);
						String message = new String(datagram.getData());
						System.out.println("mensaje pendiente" + message); //a la lista, no se deberia mostrar.
					}else{
						//si estamos con mas de cero permisos, mostramos directamente (primero los guardados).
						//if(lista o archivo temporal de mensajes tiene mensajes pendientes)
							//while(quedan mensajes)
								//mostrar mensajes.
						socket.receive(datagram);
						String message = new String(datagram.getData());
						System.out.println(message);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
   	}
	
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
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
