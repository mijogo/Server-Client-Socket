import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Console;
 
class ejecutarhiloS implements Runnable 
{
	private Thread hilo;
	private String threadName;
	private int contador; 
   	private int tamanioArch;
   	public ejecutarhiloS(String name)
	{
     		threadName = name;
     		tamanioArch = 20;
     		contador = 0;
   	}
   
	public void run() 
	{
		if(threadName == "hilo1")
		{
			int port = 12451;
	   		InetAddress group = null;
	   		MulticastSocket socket = null;
			try {
				socket = new MulticastSocket(port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	// crear socket de multicast!
			// Registrarse en un grupo de Multicast 
			try {
				group = InetAddress.getByName("228.14.25.2");
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.joinGroup(group);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(true)
			{
				Console console = System.console();
				console.printf("Envie un mensaje al grupo");
				String dato = console.readLine();
				Calendar fecha = new GregorianCalendar();
				int ano = fecha.get(Calendar.YEAR);
				int mes = fecha.get(Calendar.MONTH);
				int dia = fecha.get(Calendar.DAY_OF_MONTH);
				int hora = fecha.get(Calendar.HOUR_OF_DAY);
				int minuto = fecha.get(Calendar.MINUTE);
				int segundo = fecha.get(Calendar.SECOND);
				String Fechaexacta = dia + "/" + (mes+1) + "/" + ano + " " + hora+ ":" +minuto+ ":" +segundo;
				String mensajeC = "["+contador+"] ("+Fechaexacta+"):"+dato;
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
				String Nombre_arch = "mensaje"+(contador/tamanioArch)+".txt";
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
                			
					int envio_port = 10325;
					ServerSocket envio_sersock = null;
        				Socket envio_sock = null;
					envio_sersock = new ServerSocket(envio_port);

					PrintStream ios = new PrintStream(sock.getOutputStream());
                			ios.println(envio_port);
                			ios.close();
					envio_sock = envio_sersock.accept();
					BufferedReader envio_is = new BufferedReader(new InputStreamReader(envio_sock.getInputStream()));
					PrintStream envio_ios = new PrintStream(envio_sock.getOutputStream());
                			
					File archivo = null;
					FileReader fr = null;
					BufferedReader br = null;
					for(int j=0;j<contador/tamanioArch;j++)
					{
						String Nombre_arch = "mensaje"+Integer.toString(j)+".txt";
						archivo = new File (Nombre_arch);
						fr = new FileReader (archivo);
						br = new BufferedReader(fr);
						String linea;
						while((linea=br.readLine())!=null)
						{
							envio_ios.println(linea);
						}
						fr.close(); 
					}
					envio_ios.println("Fin");
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
