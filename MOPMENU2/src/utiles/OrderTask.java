package utiles;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class OrderTask{ 
	
	
	public static void main(String[] args) {
				
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				
				String f  = new File("").getAbsolutePath();
				System.out.println(f);
				String rutaLog = "/home/sistemas/jSenderDelivery/Log/LogMenuSender%g.log";
				
				while (true) 
				{
					try 
					{	 
						Thread.sleep(30000);

						String OrderIsSend = "";							
						String Order_id = "";							
						String token = "token";
										
						Conexion c = new Conexion();
						Connection conn = c.getConnectionDelivery();
						PreparedStatement pst = conn.prepareStatement("SELECT top 20 numpedidoprov, tienda, fechamensaje FROM Pedidos where proveedor in ('MAPP-AUTO',\r\n"
									+ "'MAPP-CURB',\r\n"
									+ "'MENUAPP',\r\n"
									+ "'MOP-DELI') and procesada = 1  and anulada != 1 and fechapos < DATEADD(mi,-10,GETDATE()) and fechapos > DATEADD(mi,-120,GETDATE()) order by fechapos desc");
						ResultSet rs = pst.executeQuery();
								
						while(rs.next())//
						{												
							Order_id = rs.getString("numpedidoprov");
							String[]  id =   Order_id.split("-");
							String sOrder_id = id[0];																
							String tienda_id = rs.getString("tienda");												
							OrderIsSend = rs.getString("fechamensaje");
							
							if (OrderIsSend == null ) 
							{		    
								System.out.println(sOrder_id);										
								Conexion conex = new Conexion();
								Connection connec = conex.conexionBksrv4();
								PreparedStatement pstam = connec.prepareStatement("select * from micros.dbo.bkmop where ip = '172.20."+tienda_id+".100'");		
								ResultSet rset = pstam.executeQuery();
								
								while (rset.next())
								{			
									token = rset.getString("token");	
									System.out.println("saca token a la api" +token+""); 
								}		
																																					
								URL urlForPutRequest = new URL("https://api-lac.menu.app/api/tablet/orders/"+sOrder_id+"/ready");  
								//URL urlForPutRequest = new URL("https://api-demo.menu.app/api/tablet/orders/\"+sOrder_id+\"/ready");
								HttpURLConnection conection = (HttpURLConnection) urlForPutRequest.openConnection();
								conection.setRequestMethod("PUT");
								conection.setRequestProperty("tablet-token", token);
								//conection.setRequestProperty("tablet-token", "e2dd2388d45d9006b78bfa167e344852530e83ae");
								conection.setRequestProperty("Content-type", "application/json");
								conection.setRequestProperty("Accept", "application/json");
								conection.setRequestProperty("Application", "tablet-application");
								int responseCode = conection.getResponseCode();
								System.out.println(responseCode);
								
								if (responseCode == HttpURLConnection.HTTP_OK) 
								{	
									Conexion conect = new Conexion();
									Connection connection = conect.getConnectionDelivery();
									Statement pstat = connection.createStatement();		
									pstat.executeUpdate("UPDATE pedidos SET fechamensaje = GETDATE() where numpedidoprov = '"+Order_id+"'");
									System.out.println("Envio la orden");
										    		
									escribirLog(rutaLog, "Envio la orden "+Order_id+" para tienda n°"+tienda_id);
								}
								else 
								{
									Conexion conect2 = new Conexion();
									Connection connection2 = conect2.getConnectionDelivery();
									Statement pstat2 = connection2.createStatement();		
									pstat2.executeUpdate("UPDATE pedidos SET fechamensaje = '3000-01-01' where numpedidoprov = '"+Order_id+"'");
									
									System.out.println("La orden no se envio a la api Menu corrctamente.");
									escribirLog(rutaLog, "La orden "+Order_id+" para tienda n°"+tienda_id+" no se envio a la api Menu correctamente. (Error Api: "+responseCode+")");
								}
							}													
						}	

					} 	
					catch (Exception e){
						e.printStackTrace();
						escribirLog(rutaLog,e.toString());
					}
				}
			}
		};
		
		Thread hilo = new Thread(runnable);
		hilo.start();
			
	}

	public static void escribirLog(String rutaArchivo, String mensaje) {

	    Logger logger = Logger.getLogger("Error_log");
	    FileHandler fh;

	    try 
	    {
	    	int limit = 5000000; // 1 Mb 1000000
	        int numLogFiles = 3;
	        fh = new FileHandler(rutaArchivo, limit, numLogFiles,true);
	        
	        //fh = new FileHandler(rutaArchivo, true);
	        logger.addHandler(fh);

	        SimpleFormatter formatter = new SimpleFormatter();

	        fh.setFormatter(formatter);

	        Long datetime = System.currentTimeMillis();
	        Timestamp timestamp = new Timestamp(datetime);
	        
	        String mensajeLog = timestamp+": "+ mensaje;
	        
	        logger.info(mensajeLog);

	        fh.close();

	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}	
}

// 18-01-2022 Streitenberger Sebastian