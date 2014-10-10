import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.io.Serializable;
 
class ejecutarhilo implements Runnable 
{
	private Thread hilo;
	private String threadName;
	private static Semaphore sema = new Semaphore(0);  //semaforo parte en cero para que el hilo2 no reciba nada hasta que el usuario decida que hacer.
	private static ArrayList<Integer> LostMessages = new ArrayList<Integer>();
   	public ejecutarhilo(String name)
	{
     		threadName = name;
   	}
   
	public void run() 
	{
		if(threadName == "hilo1")
		{
			//Hilo unicast, este hilo se preocupa de que le envien los mensajes anteriores o recuperar los no recibidos
			Boolean connected = false;
			while(!connected)
			{
				//al inicial se le pedira que escoja entre entrar directamente al canal sin recibir los mensajes anteriores
				//o recuperar los mensajes anteriores
				System.out.println("Ingrese \"Pull\" para recibir el historial al unirse o \"Join\" para solo unirse.");
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
	        	String dato = null;
				try {
					dato = brc.readLine();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//Se entra si se quieren recuperar los mensajes anteriroes
				if(dato.equals("Pull"))
				{
					
					Socket sock=null; 
					while(!connected)
					{
						try 
						{
							//Se conecta al server por un canal unicast
							connected = true;
							int serverPORT = 9025;
							int cant_bytes;
							int tam_archivo;
							InetAddress ip =InetAddress.getByName("localhost");
							sock= new Socket(ip, serverPORT); 
							BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
							String dato_archivo = is.readLine();
							cant_bytes = Integer.parseInt(dato_archivo);
							dato_archivo = is.readLine();
							tam_archivo = Integer.parseInt(dato_archivo);
							sema.release(); //dejamos que el otro hilo escuche y guarde momentaneamente los mensajes que puedan llegar mientras llega el historial.
							//Se crea un archivo y se rellena con los datos que envia el servidor
				            FileOutputStream fos = new FileOutputStream("Historial_temp.txt");
				            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				            Object mensajeAux;
				            Mensaje recibir_obj = new Mensaje(cant_bytes);
				            System.out.println("Recibiendo Historial...");
				            for(int i =tam_archivo;i>-1;i-=cant_bytes)
				            {
				            	mensajeAux = ois.readObject();
				            	recibir_obj = (Mensaje) mensajeAux;
				            	if(cant_bytes<i)
				            		fos.write(recibir_obj.contenidoMensaje, 0,cant_bytes);
				            	else
				            		fos.write(recibir_obj.contenidoMensaje, 0,i);
				            	//System.out.println(i);
				            }
				            System.out.println("Historial Recibido, mostrando...");
				            fos.close();
				            ois.close();
						}
						catch(SocketException e)
						{ 
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							connected = false;
		          		}
						catch(IOException e)
						{ 
							System.out.println("IOException " + e);
						} 
						catch (ClassNotFoundException e) 
						{
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
					sema.release();//release para que el otro hilo pase a mostrar los mensajes live.
				}
			}
			while(connected)
			{
				sema.release();
				//Aca ya esta funcionando todo y se le dan opciones extras mientras esta
				//recibiendo los mensajes del multicast
				System.out.println("Ingrese \"Recover\" para reparar posibles mensajes perdidos o \"Leave\" para desconectarse.");
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
				String dato = null;
				try {
					dato = brc.readLine();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(dato.equals("Recover")){
					//Si faltan mensajes se pueden pedir con un Recover
					Socket sock=null;
					connected = false;
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
							BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
							String dato_archivo = is.readLine();
							cant_bytes = Integer.parseInt(dato_archivo);
							dato_archivo = is.readLine();
							tam_archivo = Integer.parseInt(dato_archivo);
							sema.release(); //dejamos que el otro hilo escuche y guarde momentaneamente los mensajes que puedan llegar mientras llega el historial.
							//Es similar a a recuperar el historial, le pide el historial al server, para comparar los mensajes que se tienen
				            FileOutputStream fos = new FileOutputStream("Historial_temp2.txt");
				            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
				            Object mensajeAux;
				            Mensaje recibir_obj = new Mensaje(cant_bytes);
				            System.out.println("Recibiendo Historial para recuperar mensajes perdidos...");
				            for(int i =tam_archivo;i>-1;i-=cant_bytes)
				            {
				            	mensajeAux = ois.readObject();
				            	recibir_obj = (Mensaje) mensajeAux;
				            	if(cant_bytes<i)
				            		fos.write(recibir_obj.contenidoMensaje, 0,cant_bytes);
				            	else
				            		fos.write(recibir_obj.contenidoMensaje, 0,i);
				            }
				            System.out.println("Historial Recibido, Procesando...");
				            fos.close();
				            ois.close();
						}
						catch(SocketException e)
						{ 
							//darle un sleep, para meterse de nuevo al bucle y pedir nuevamente conexion
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							connected = false;
		          		}
						catch(IOException e)
						{ 
							System.out.println("IOException " + e);
						} 
						catch (ClassNotFoundException e) 
						{
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
					//Se cargan los mensajes que se pidieron al server
					System.out.println("Los mensajes recuperados son los siguientes:");
					FileInputStream fstream;
					try {
						fstream = new FileInputStream("Historial_temp2.txt");
						BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
						String line;
						while ((line = br.readLine()) != null && !LostMessages.isEmpty())   {
							if (LostMessages.get(0)>10) {
								if(LostMessages.get(0)==Integer.parseInt(line.substring(1,3))){
									System.out.println(line);
									LostMessages.remove(0);
								}
							}else if(LostMessages.get(0)>100){
								if(LostMessages.get(0)==Integer.parseInt(line.substring(1,4))){
									System.out.println(line);
									LostMessages.remove(0);
								}
							}else{
								if(LostMessages.get(0)==Integer.parseInt(line.substring(1,2))){
									System.out.println(line);
									LostMessages.remove(0);
								}
							}
						}
						br.close();
					} catch (FileNotFoundException e) {
						System.out.println("problemas de lectura de archivo al recuperar mensajes.");
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}else if (dato.equals("Leave")){
					System.exit(0);
				}
			}
		}
		else
		{	
			//Es el hilo que escucha los mensajes del multicast
			int shownMessages = 0;
			ArrayList<String> QueuedMessages = new ArrayList<String>();
			
			try {
				sema.acquire();   //Si se esta descargando el historial, empesamos a escuchar.
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			int port = 12451;
		   	InetAddress group;
		   	MulticastSocket socket;
		   	DatagramPacket datagram;
		   	//Se conecta y escucha los mensajes
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
						String message = new String(datagram.getData(),0,datagram.getLength());
						QueuedMessages.add(message);
					}else{
						//si estamos con mas de cero permisos, mostramos directamente (primero los guardados del historial recibido y luego los que llegaron miestras recibiamos el archivo).
						try {
							FileInputStream fstream = new FileInputStream("Historial_temp.txt");
							BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
							String line;
							while ((line = br.readLine()) != null)   {
								// Mostramos linea por linea los mensajes del historial recibido. Tambien nos fijamos que no falten mensajes.
								if(Character.isDigit(line.charAt(2))){
									//10 a 99
									while(shownMessages<Integer.parseInt(line.substring(1, 3)) && shownMessages<100){
										LostMessages.add(shownMessages);
										shownMessages++; //si nos faltan mensajes, sumamos a los mostrados y guardamos el numero del cual nos saltamos.
									}
								}else if(Character.isDigit(line.charAt(3))){
									//100 a 999
									while(shownMessages<Integer.parseInt(line.substring(1, 4)) && shownMessages<1000){
										LostMessages.add(shownMessages);
										shownMessages++;
									}
								}else{
									//0 a 9
									while(shownMessages<Integer.parseInt(line.substring(1, 2)) && shownMessages<10){
										LostMessages.add(shownMessages);
										shownMessages++;
									}
								}
								shownMessages++;
								System.out.println (line);
							}
							br.close();
							File f = new File("Historial_temp.txt");
							f.delete();
						} catch (Exception e) {
						}
						while(!QueuedMessages.isEmpty()){
							System.out.println(QueuedMessages.get(0));
							if(Character.isDigit(QueuedMessages.get(0).charAt(2))){
								//10 a 99
								while(shownMessages<Integer.parseInt(QueuedMessages.get(0).substring(1, 3)) && shownMessages<100){
									LostMessages.add(shownMessages);
									shownMessages++;
								}
							}else if(Character.isDigit(QueuedMessages.get(0).charAt(3))){
								//100 a 999
								while(shownMessages<Integer.parseInt(QueuedMessages.get(0).substring(1, 4)) && shownMessages<1000){
									LostMessages.add(shownMessages);
									shownMessages++;
								}
							}else{
								//0 a 9
								while(shownMessages<Integer.parseInt(QueuedMessages.get(0).substring(1, 2)) && shownMessages<10){
									LostMessages.add(shownMessages);
									shownMessages++;
								}
							}
							shownMessages++;
							QueuedMessages.remove(0);
						}
						socket.receive(datagram);
						String message = new String(datagram.getData(),0,datagram.getLength());
						if(Character.isDigit(message.charAt(2))){
							//10 a 99
							while(shownMessages<Integer.parseInt(message.substring(1, 3)) && shownMessages<100){
								LostMessages.add(shownMessages);
								shownMessages++;
							}
						}else if(Character.isDigit(message.charAt(3))){
							//100 a 999
							while(shownMessages<Integer.parseInt(message.substring(1, 4)) && shownMessages<1000){
								LostMessages.add(shownMessages);
								shownMessages++;
							}
						}else{
							//0 a 9
							while(shownMessages<Integer.parseInt(message.substring(1, 2)) && shownMessages<10){
								LostMessages.add(shownMessages);
								shownMessages++;
							}
						}
						shownMessages++;
						System.out.println(message);
					}
				}
			} catch (IOException e) {
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

//Se utiliza una clase que maneja mejor el envio de archivos tipo byte[]
@SuppressWarnings("serial")
class Mensaje implements Serializable
{
    private int Tamanio_mensaje;
	public byte[] contenidoMensaje;
	public Mensaje(int tam)
	{
		this.Tamanio_mensaje = tam;
		contenidoMensaje = new byte[Tamanio_mensaje];
	}
}

public class ClienteM
{
	public static void main(String args[]) 
	{
		//Se crean y ejecutan los hilos
		ejecutarhilo H1 = new ejecutarhilo("hilo1");
		H1.start();
		ejecutarhilo H2 = new ejecutarhilo("hilo2");
		H2.start();
	}   
}
