package utiles;


import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

	public Conexion() { }

	public Connection getConnectionDelivery() throws Exception {
		
	    String url = "jdbc:sqlserver://192.168.245.2:1433;databaseName=Delivery"; //"+ip.getHostAddress()+"
		//String url = "jdbc:sybase:Tds:172.20.97.100:2638?ServiceName=micros";
	    String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String userName = "delivery";
		String password = "d3l1v3ry$";
		Class.forName(driver).newInstance();
		
		return DriverManager.getConnection(url, userName, password);		

	}
		public Connection conexionBksrv4() throws Exception {
		String url = "jdbc:sqlserver://172.31.1.14:1433;databaseName=micros";
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String userName ="Aplicaciones";// "mobile";
		String password =".4pl1c4c10n3s";// ".M0b1l3$.";
		Class.forName(driver).newInstance();
		
		return DriverManager.getConnection(url, userName, password);
	}
		
		
	
	
	
}

