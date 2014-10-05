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
                	PrintStream ios = new PrintStream(sock.getOutputStream());
                	ios.println("aceptado");
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
							ios.println(linea);
						}
						fr.close(); 
					}
					ios.println("Fin");
                	ios.close();
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
