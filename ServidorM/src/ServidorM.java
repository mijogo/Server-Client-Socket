import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Serializable;
//import java.io.Console;
 
class ejecutarhiloS implements Runnable 
{
	private Thread hilo;
	private String threadName;
	private int contador; 
   	private int tamanioMensaje;
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
			//ESte hilo debe enviar y guardar los mensajes
			int port = 12451;
	   		InetAddress group = null;
	   		MulticastSocket socket = null;
			try {
				socket = new MulticastSocket(port);
				group = InetAddress.getByName("228.14.25.2");
				socket.joinGroup(group);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(true)
			{
				//Console console = System.console();
				//console.printf("Envie un mensaje al grupo");
				System.out.println("Envie un mensaje al grupo");
				//String dato = console.readLine();
				BufferedReader brc = new BufferedReader(new InputStreamReader(System.in));
				Calendar fecha = new GregorianCalendar();
				int ano = fecha.get(Calendar.YEAR);
				int mes = fecha.get(Calendar.MONTH);
				int dia = fecha.get(Calendar.DAY_OF_MONTH);
				int hora = fecha.get(Calendar.HOUR_OF_DAY);
				int minuto = fecha.get(Calendar.MINUTE);
				int segundo = fecha.get(Calendar.SECOND);
				String Fechaexacta = dia + "/" + (mes+1) + "/" + ano + " " + hora+ ":" +minuto+ ":" +segundo;
				String mensajeC = null;
				try {
					mensajeC = "["+contador+"] ("+Fechaexacta+"):"+brc.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				byte[] buf = mensajeC.getBytes();
		 		DatagramPacket dg = new DatagramPacket(buf, buf.length,group,port);
				try {
					socket.send(dg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FileWriter fichero = null;
				PrintWriter pw = null;
				String Nombre_arch = "Historial.txt";
				try {
					fichero = new FileWriter(Nombre_arch,true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pw = new PrintWriter(fichero);
				pw.println(mensajeC);
				try {
					fichero.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
					BufferedReader is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					System.out.println("prueba de peticiones");
					//System.out.println(is.readLine());
					PrintStream ios = new PrintStream(sock.getOutputStream());

                	//boolean enviadoUltimo=false;
                    FileInputStream fis = new FileInputStream("Historial.txt");
                    int tamanio_arch = fis.available();
                	ios.println(tamanioMensaje);
                	ios.println(tamanio_arch);
                	sock.setSoLinger(true, 10);
                    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                    byte[] datosArchivos = new byte[tamanioMensaje];
                    int leidos = fis.read(datosArchivos);
                    while (leidos > -1)
                    {
                    	oos.writeObject(datosArchivos);
                        leidos = fis.read(datosArchivos);
                    }
                    oos.close();
                    fis.close();

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

public class ServidorM
{
	public static void main(String args[]) {

      ejecutarhiloS H1 = new ejecutarhiloS("hilo1");
      H1.start();
      ejecutarhiloS H2 = new ejecutarhiloS("hilo2");
      H2.start();
   }   
}
