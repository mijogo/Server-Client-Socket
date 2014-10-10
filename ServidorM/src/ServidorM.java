import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Serializable;
import java.util.concurrent.Semaphore;
 
class ejecutarhiloS implements Runnable 
{
	//este sera el cuerpo principal del programa, en el estan los
	//Thread que llevaran a cabo las tareas del servidor
	private Thread hilo;
	private String threadName;
	private int contador; 
	private int tamanioMensaje;//Tiene el tamaño de los mensajes que se enviaran los archivos
	private static Semaphore sema = new Semaphore(1); 
   	public ejecutarhiloS(String name)
	{
     		threadName = name;
     		tamanioMensaje = 20;
     		contador = 0;
   	}
	public void run() 
	{
		if(threadName == "hilo1")
		{
			//Este hilo que se encargara del multicast
			int port = 12451;
	   		InetAddress group = null;
	   		MulticastSocket socket = null;
	   		//Se conecta al canal del multicast
			try {
				socket = new MulticastSocket(port);
				group = InetAddress.getByName("228.14.25.2");
				socket.joinGroup(group);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Desde aca se pueden enviar mensajes al canal
			System.out.println("Listo para enviar mensajes, ingrese \"Exit\" para cerrar borrando el historial.");
			String line = null;
			byte[] buf = null;
			while(true)
			{	
				System.out.println("Envie un mensaje al grupo");
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
				try {
					line = brc.readLine();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Calendar fecha = new GregorianCalendar();
				int ano = fecha.get(Calendar.YEAR);
				int mes = fecha.get(Calendar.MONTH);
				int dia = fecha.get(Calendar.DAY_OF_MONTH);
				int hora = fecha.get(Calendar.HOUR_OF_DAY);
				int minuto = fecha.get(Calendar.MINUTE);
				int segundo = fecha.get(Calendar.SECOND);
				String Fechaexacta = dia + "/" + (mes+1) + "/" + ano + " " + hora+ ":" +minuto+ ":" +segundo;
				String mensajeC = "["+contador+"] ("+Fechaexacta+"):";
				//Se crea un mensaje con todos los datos
				if(line.equals("Exit")){
					//Aqui se borra el historial y se cierra la aplicacion
					File f = new File("Historial.txt");
					f.delete();
					socket.close();
					System.exit(0);
				}else{
					//se agrega el input al timestamp.
					mensajeC = mensajeC + line;
				}
				buf = null;
				buf = mensajeC.getBytes();
		 		DatagramPacket dg = new DatagramPacket(buf, buf.length,group,port);
				//Se envia el mensaje al multicast
		 		try {
					socket.send(dg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FileWriter fichero = null;
				PrintWriter pw = null;
				//Se guarda el mensaje en el historial
				try {
					sema.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				String Nombre_arch = "Historial.txt";
				try {
					fichero = new FileWriter(Nombre_arch,true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				pw = new PrintWriter(fichero);
				pw.println(mensajeC);
				try {
					fichero.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				sema.release();
				contador++;
			}
		}
		else
		{
			//este hilo espera a que los clientes le envien pedidos del historial de mensajes
			int uniport = 9025;
       			ServerSocket sersock = null;
        		Socket sock = null;
			try 
			{
				sersock = new ServerSocket(uniport);
            	for (;;) 
				{
            		sock = sersock.accept();
					System.out.println("Enviando archivo a cliente...");
					PrintStream ios = new PrintStream(sock.getOutputStream());
					
					//El usuario pide el historial, asi que se le envia el archivo en mensajes
					//de tamaños iguales al cliente, el los tomara y unira para volver a 
					//armar el historial
                	try 
                	{
                		sema.acquire();
                	} catch (InterruptedException e1) {
							e1.printStackTrace();
					}
                    FileInputStream fis = new FileInputStream("Historial.txt");
                    int tamanio_arch = fis.available();
                	ios.println(tamanioMensaje);
                	ios.println(tamanio_arch);
                	sock.setSoLinger(true, 10);
                    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                    Mensaje datosArchivos = new Mensaje(tamanioMensaje);
                    int leidos = fis.read(datosArchivos.contenidoMensaje);
                    //el archivo se fragmenta en mensajes de una cantidad predeterminada de bytes
                    //y se envia al cliente
                    while (leidos > -1)
                    {
                    	oos.writeObject(datosArchivos);
                    	datosArchivos = new Mensaje(tamanioMensaje);
                        leidos = fis.read(datosArchivos.contenidoMensaje);  
                    }
                    oos.close();
                    fis.close();
                    sema.release();
                    System.out.println("...Envio de historial terminado");
					sock.close();
            	}
       		} catch (SocketException se) {
       			System.out.println("Problemas con socket del servidor " + se.getMessage());
        	} catch (Exception e) {
           		System.out.println("No pudo arrancar " + e.getMessage());
        	}
        	System.out.println(" Conexion desde :  " + sock.getInetAddress());
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


public class ServidorM
{
	public static void main(String args[]) {

		//se crean los hilos y se ejecutan
      ejecutarhiloS H1 = new ejecutarhiloS("hilo1");
      H1.start();
      ejecutarhiloS H2 = new ejecutarhiloS("hilo2");
      H2.start();
   }   
}
