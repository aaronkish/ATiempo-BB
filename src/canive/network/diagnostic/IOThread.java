package canive.network.diagnostic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import util.BufferedReader;


import canive.ATiempoScreen;
import canive.network.TransportDetective;
import canive.network.URLFactory;


import net.rim.device.api.crypto.tls.ssl30.SSL30Connection;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
/**
* Responsible for communicating over http via different transports.
* 
* This application is targeted for JDE 4.5 or later. If you are using earlier versions of the JDE
* to compile this application, please avoid using the classes/methods that are not present in your target JDE.
* There should not be any classes that are supported from 4.5 and is critical for this application.
*/
public class IOThread extends Thread implements RadioStatusListener{	
	/** Number of times we should retry each transport before giving up */
	private int retries;
	/** Size of data to be sent via http post */
	int postSize;
	/** Screen to show network information, the progress of each transport etc.*/
	private ATiempoDownloading downloadingScreen;	

	/** Log objects for each transport for HttpConnection based GET */
	private Log tcpHttpGetLog, wifiHttpGetLog;

	/** Log objects for each transport for HttpConnection based POST */
	private Log tcpHttpPostLog, wifiHttpPostLog;

	/** Reference to the InputScreen */
	private CNXSettings settingInput;	

	/** http headers to set */
	String headers;
	private String head1;
	private Vector response;

	/** URLFactory instance. Used to parse and construct URLs for each transport */
	URLFactory urlFactory;	
	URLFactory urlFactory2;
	/** Instance of TransportDetective that helps determine availability of transports */
	TransportDetective td;

	/** Default POST request headers. Used in case the user header string is invalid */
	String defaultPostHeaders;
	/** Default GET request headers. Used in case the user header string is invalid */
	String defaultGetHeaders;

	/** Reference to the Connection object for the current test */
	Object currentConn;

	/** Flag to indicate the test is manually stopped */
	private boolean stop = false;



	/**
	 * Basic initializations.
	 * @param inputs	Reference to InputScreen instance
	 */
	public IOThread(CNXSettings settingInput) {
		tcpHttpGetLog = new Log("TCP Celular (HTTP GET)");
		wifiHttpGetLog = new Log("WiFi (HTTP GET)");

		tcpHttpPostLog = new Log("TCP Celular (HTTP POST)");
		wifiHttpPostLog = new Log("WiFi (HTTP POST)");

		this.settingInput = settingInput;

		td = new TransportDetective();
		urlFactory = new URLFactory(settingInput.getUrl1());		
		urlFactory2 = new URLFactory(settingInput.getUrl2());

		this.retries = settingInput.getTries();
		this.postSize = settingInput.getMethodPostSize();
		this.head1 = "Content-Type: " + "application/octet-stream" + "\n" + "User-Agent: " + "Mozilla/4.0" + "\n" + "Connection: close" + "\n";
		this.response = new Vector();

		//HEADERS
		String a =  "Host: " + urlFactory.getHost().trim() + "\n" + "Content-Length: " + this.postSize;
		a = (a + "\n" + this.head1).trim();

		this.headers = a;

		downloadingScreen = new ATiempoDownloading(this);

		defaultPostHeaders = "Host: " + urlFactory.getHost() + "\n" + "Content-Length: " + postSize + "\n" + "Content-Type: " + "application/octet-stream" + "\n" + "User-Agent: " + "Mozilla/4.0" + "\n" + "Connection: close" + "\n";
		defaultGetHeaders = "Host: " + urlFactory.getHost() + "\n" + "User-Agent: " + "Mozilla/4.0" + "\n" + "Connection: close" + "\n";

		UiApplication.getUiApplication().pushScreen(downloadingScreen);
	}

	/**
	 * Triggers the selected transport tests one after the other
	 */
	public void run() {	

		/** Trigger TCP Cellular tests */
		if(settingInput.isUseTCP() && td.isCoverageAvailable(TransportDetective.TRANSPORT_TCP_CELLULAR)){
			testTcpCellular();			
		} else if(!td.isCoverageAvailable(TransportDetective.TRANSPORT_TCP_CELLULAR)){
			tcpHttpGetLog.addlog("Test Saltado: Covertura TCP Celular no disponible");
			tcpHttpPostLog.addlog("Test Saltado: Covertura TCP Celular no disponible");
			if(settingInput.isUseTCP() && settingInput.isUseHTTP() && settingInput.isUseGET())
			{
				downloadingScreen.displayResult(tcpHttpGetLog);
				if (tcpHttpGetLog.isPass()) {
					final IOThread sendIO = this;
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							sendIO.stopTest();
							UiApplication.getUiApplication().popScreen(downloadingScreen);
							ATiempoScreen atiempoScreen = new ATiempoScreen(sendIO);
							UiApplication.getUiApplication().pushScreen(atiempoScreen);
						}
					});
				}
			}
			if(settingInput.isUseTCP() && settingInput.isUseHTTP() && !settingInput.isUseGET())
			{downloadingScreen.displayResult(tcpHttpPostLog);
				if (tcpHttpPostLog.isPass()) {
					final IOThread sendIO = this;
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							sendIO.stopTest();
							UiApplication.getUiApplication().popScreen(downloadingScreen);
							ATiempoScreen in = new ATiempoScreen(sendIO);
							UiApplication.getUiApplication().pushScreen(in);
						}
					});
				}
			}
		}	

		/** HTTP GET via WiFi using HttpConnection */
		if(!settingInput.isUseTCP() && td.isCoverageAvailable(TransportDetective.TRANSPORT_TCP_WIFI)){
			testWiFi();						
		} else{
			if(!td.isTransportServiceAvailable(TransportDetective.TRANSPORT_TCP_WIFI)){
				wifiHttpGetLog.addlog("Test Saltado: No se encontraron registros de servicio WiFi.");
				wifiHttpPostLog.addlog("Test Saltado: No se encontraron registros de servicio WiFi.");				
			}
			if(!td.isCoverageAvailable(TransportDetective.TRANSPORT_TCP_WIFI)){
				wifiHttpGetLog.addlog("Test Saltado: Covertura WiFi no disponible");
				wifiHttpPostLog.addlog("Test Saltado: Covertura WiFi no disponible");
			}
			if(!settingInput.isUseTCP() && settingInput.isUseHTTP() && settingInput.isUseGET())
				downloadingScreen.displayResult(wifiHttpGetLog);
			if(!settingInput.isUseTCP() && settingInput.isUseHTTP() && !settingInput.isUseGET())
				downloadingScreen.displayResult(wifiHttpPostLog);
		}		
	}


	public Vector getResponse() {
		return response;
	}

	/**
	 * Performs selected tests via TCP Cellular
	 */
	private void testTcpCellular() {		
		String apn = settingInput.getTcpAPN();
		String username = settingInput.getTcpUser();
		String password = settingInput.getTcpPass();

		/** HTTP GET via TCP Cellular using HttpConnection */
		if(settingInput.isUseHTTP() && settingInput.isUseGET()){
			downloadingScreen.displayProgress(tcpHttpGetLog.getTransport());
			for(int i=0; i<retries; i++){
				if(tcpHttpGetLog.isPass())
					break;			
				downloadingScreen.setTrial(i, retries);
				try {sleep(2000);} catch (InterruptedException e) {}

				String httpURL = urlFactory.getHttpTcpCellularUrl(apn, username, password);	
				String httpURL2 = urlFactory2.getHttpTcpCellularUrl(apn, username, password);	
				getViaHttp(tcpHttpGetLog, httpURL);
				getViaHttp(tcpHttpGetLog, httpURL2);
			}				
			if (tcpHttpGetLog.isPass()) {
				final IOThread sendIO = this;
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						sendIO.stopTest();
						UiApplication.getUiApplication().popScreen(downloadingScreen);
						ATiempoScreen in = new ATiempoScreen(sendIO);
						UiApplication.getUiApplication().pushScreen(in);
					}
				});
			}
			
		}

		/** HTTP POST via TCP Cellular using HttpConnection */
		if(settingInput.isUseHTTP() && !settingInput.isUseGET()){
			downloadingScreen.displayProgress(tcpHttpPostLog.getTransport());
			for(int i=0; i<retries; i++){
				if(tcpHttpPostLog.isPass())
					break;			
				downloadingScreen.setTrial(i, retries);
				try {sleep(2000);} catch (InterruptedException e) {}

				String httpURL = urlFactory.getHttpTcpCellularUrl(apn, username, password);		
				postViaHttp(tcpHttpPostLog, httpURL, postSize);	
				String httpURL2 = urlFactory2.getHttpTcpCellularUrl(apn, username, password);		
				postViaHttp(tcpHttpPostLog, httpURL2, postSize);
			}				
			if (tcpHttpPostLog.isPass()) {
				final IOThread sendIO = this;
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						sendIO.stopTest();
						UiApplication.getUiApplication().popScreen(downloadingScreen);
						ATiempoScreen in = new ATiempoScreen(sendIO);
						UiApplication.getUiApplication().pushScreen(in);
					}
				});
			}
		}
	}

	/**
	 * Performs HTTP GET via WiFi using HTTPConnection
	 */
	private void testWiFi() {				
		/** HTTP GET via WiFi using HttpConnection */
		if(settingInput.isUseHTTP() && settingInput.isUseGET()){
			downloadingScreen.displayProgress(wifiHttpGetLog.getTransport());
			for(int i=0; i<retries; i++){
				if(wifiHttpGetLog.isPass())
					break;
				downloadingScreen.setTrial(i, retries);
				try {sleep(2000);} catch (InterruptedException e) {}

				String httpURL = urlFactory.getHttpTcpWiFiUrl();				
				getViaHttp(wifiHttpGetLog, httpURL);
				String httpURL2 = urlFactory2.getHttpTcpWiFiUrl();				
				getViaHttp(wifiHttpGetLog, httpURL2);
			}
			if (wifiHttpGetLog.isPass()) {
				final IOThread sendIO = this;
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						sendIO.stopTest();
						UiApplication.getUiApplication().popScreen(downloadingScreen);
						ATiempoScreen in = new ATiempoScreen(sendIO);
						UiApplication.getUiApplication().pushScreen(in);
					}
				});
			}
		}

		/** HTTP POST via WiFi using HttpConnection */
		if(settingInput.isUseHTTP() && !settingInput.isUseGET()){
			downloadingScreen.displayProgress(wifiHttpPostLog.getTransport());
			for(int i=0; i<retries; i++){
				if(wifiHttpPostLog.isPass())
					break;
				downloadingScreen.setTrial(i, retries);
				try {sleep(2000);} catch (InterruptedException e) {}

				String httpURL = urlFactory.getHttpTcpWiFiUrl();				
				postViaHttp(wifiHttpPostLog, httpURL, postSize);
				String httpURL2 = urlFactory2.getHttpTcpWiFiUrl();				
				postViaHttp(wifiHttpPostLog, httpURL2, postSize);
			}
			if (wifiHttpPostLog.isPass()) {
				final IOThread sendIO = this;
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						sendIO.stopTest();
						UiApplication.getUiApplication().popScreen(downloadingScreen);
						ATiempoScreen in = new ATiempoScreen(sendIO);
						UiApplication.getUiApplication().pushScreen(in);
					}
				});
			}
		}	
	}	

	/**
	 * Given a url, performs an HTTP GET using HttpConnection and gets the response. 
	 * Also adds logs to the provided Log instance.
	 * @param log	Log object for saving log messages.
	 * @param url	URL for connection.
	 */
	private void getViaHttp(Log log, String url){
		if(stop){
			log.addlog("Test detenido por usuario. Saltando este test.");
			return;
		}
		try {			
			HttpConnection hconn;

			if(url.indexOf("ConnectionType=mds-public")!=-1){
				log.addlog("Conectando a "+url.substring(0, url.indexOf(";"))+";***Only disclosed to ISV partners of RIM.");
				log.setUrl(url);
			} else{
				log.addlog("Conectando a "+url);
				log.setUrl(url);
			}

			log.addlog("Abriendo conexión..");

			long start = System.currentTimeMillis();			
			try {
				hconn = (HttpConnection) Connector.open(url);
			} catch (Exception e) {
				log.addlog("Error: " + e.toString());
				return;
			}
			
			currentConn = hconn;
			log.addlog("\tConexión abierta");

			log.addlog("Estableciendo propiedades de solicitud..");		
			Vector vector = getHeaders(this.headers);			
			if(vector!=null){				
				removePostHeaders(vector);				
				for(int i=0; i<vector.size(); i++){
					Header header = (Header)vector.elementAt(i);
					String key = header.getName();
					String value = header.getValue();
					hconn.setRequestProperty(key, value);
					log.addlog("\t" + key + ": " + value);
				}
			} else{
				log.addlog("¡Atención!: Los encabezado definido por usuario son inválidos. Usando por defecto..");
				vector = getHeaders(defaultGetHeaders);				
				for(int i=0; i<vector.size(); i++){
					Header header = (Header)vector.elementAt(i);
					String key = header.getName();
					String value = header.getValue();
					hconn.setRequestProperty(key, value);
					log.addlog("\t" + key + ": " + value);
				}
			}

			log.addlog("Obteniendo código de respuesta..");
			int response = hconn.getResponseCode();		
			log.setResponseCode(response);
			log.addlog("\tCódigo de Respuesta: "+response);	

			long contentLength = hconn.getLength();
			log.setContentLength(contentLength);
			log.addlog("Tamaño de contenido: "+ contentLength + " bytes");

			log.addlog("Descargando contenido..");
			String result = getURLStringFromConnection(hconn);
			this.response.addElement(result);
			//is = hconn.openInputStream();
			//String result = read(is);

			StringBuffer responseHeaders = new StringBuffer();
			responseHeaders.append(hconn.getResponseMessage() + "\n");
			int i = 0;
			while(true){
				String key = hconn.getHeaderFieldKey(i);
				if(key==null)
					break;
				String value = hconn.getHeaderField(key);
				responseHeaders.append(key + ": " + value + "\n");
				i++;
			}

			log.addlog("\tTiempo de descarga: "+(System.currentTimeMillis()-start)/1000d + " segundos");			
			log.addlog("\tDescargado: "+result.length() + " bytes");
			log.addlog("Cerrando conexión..");
			//is.close();
			hconn.close();
			log.addlog("\tConexión cerrada.");
			if(response<400 && response>0)
				log.setPass(true);
		} 
		catch (Throwable e) {
			log.addlog("Error: " + e.toString());
		}
		finally {			
			log.addlog("========FIN DE LOG========");			
		}
	}

	public void postViaHttp(Log log, String url, int postSize){
		if(stop){
			log.addlog("Test detenido por usuario. Saltando este test.");
			return;
		}
		try {				
			HttpConnection hconn;
			OutputStream os;

			if(url.indexOf("ConnectionType=mds-public")!=-1){
				log.addlog("Conectando a "+url.substring(0, url.indexOf(";"))+";***Only disclosed to ISV partners of RIM.");
				log.setUrl(url);
			} else{
				log.addlog("Conectando a "+url);
				log.setUrl(url);
			}

			log.addlog("Abriendo conexión..");

			long start = System.currentTimeMillis();

			try {
				hconn = (HttpConnection) Connector.open(url);
			} catch (Exception e) {
				log.addlog("Error: " + e.toString());
				return;
			}
			
			currentConn = hconn;
			log.addlog("\tConexión abierta");

			hconn.setRequestMethod(HttpConnection.POST);
			log.addlog("Metodo de solicitud establecido a POST");

			log.addlog("Estableciendo propiedades de solicitud..");
			Vector vector = getHeaders(this.headers);
			if(vector!=null){				
				for(int i=0; i<vector.size(); i++){
					Header header = (Header)vector.elementAt(i);
					String key = header.getName();
					String value = header.getValue();
					hconn.setRequestProperty(key, value);
					log.addlog("\t" + key + ": " + value);
				}
			} else{
				log.addlog("¡Atención!: Los encabezado definido por usuario son inválidos. Usando por defecto..");
				vector = getHeaders(defaultPostHeaders);
				for(int i=0; i<vector.size(); i++){
					Header header = (Header)vector.elementAt(i);
					String key = header.getName();
					String value = header.getValue();
					hconn.setRequestProperty(key, value);
					log.addlog("\t" + key + ": " + value);
				}
			}			

			StringBuffer contentBuffer = new StringBuffer(postSize);
			while(contentBuffer.capacity()>contentBuffer.length()){
				contentBuffer.append("B");
			}

			os = hconn.openOutputStream();
			log.addlog("Solicitando " + postSize + " bytes..");
			os.write(contentBuffer.toString().getBytes());		
			os.flush();
			os.close();
			log.addlog("\tSolicitado " + contentBuffer.length() + " bytes");			

			log.addlog("Obteniendo código de respuesta..");
			int response = hconn.getResponseCode();		
			log.setResponseCode(response);
			log.addlog("\tCódigo de respuesta: "+response);

			long contentLength = hconn.getLength();
			log.setContentLength(contentLength);
			log.addlog("Tamaño de contenido: " + contentLength + " bytes");

			log.addlog("Descargando contenido..");
			
			//is = hconn.openInputStream();
			String result = getURLStringFromConnection(hconn);//read(is);
			this.response.addElement(result);

			StringBuffer responseHeaders = new StringBuffer();
			responseHeaders.append(hconn.getResponseMessage() + "\n");
			int i = 0;
			while(true){
				String key = hconn.getHeaderFieldKey(i);
				if(key==null)
					break;
				String value = hconn.getHeaderField(key);
				responseHeaders.append(key + ": " + value + "\n");
				i++;
			}

			log.addlog("\tTiempo de descarga: "+(System.currentTimeMillis()-start)/1000d+" segundos");			
			log.addlog("\tDescargado: "+result.length()+" bytes");
			
			if(response<400 && response>0)
				log.setPass(true);

			log.addlog("Cerrando conexión");
			//is.close();
			hconn.close();
			log.addlog("Conexión cerrada");			
		} catch (Throwable e) {
			log.addlog("Error: " + e.toString());
		} finally {			
			log.addlog("========FIN DE LOG========");			
		}
	}

	private Vector getHeaders(String headers){		
		headers = headers.trim();
		Vector vector = new Vector();		
		int i = 0;
		while(i<headers.length()){
			int colon = headers.indexOf(":", i);
			int newline = headers.indexOf("\n", i);
			if(newline==-1)
				newline=headers.length();
			if(colon==-1 || newline==-1 || colon>newline)
				return null;
			String name = headers.substring(i, colon).trim();
			i = colon + 1;
			String value = headers.substring(i, newline).trim();
			i = newline + 1;
			vector.addElement(new Header(name,value));			
		}

		return vector;
	}

//	private String getHeadersCommand(Vector vector){
//		StringBuffer buffer = new StringBuffer();
//		for(int i=0; i<vector.size(); i++){
//			Header header = (Header)vector.elementAt(i);
//			buffer.append(header.getName());
//			buffer.append(": ");
//			buffer.append(header.getValue());
//			buffer.append("\r\n");
//		}	
//
//		return buffer.toString();
//	}

	private void removePostHeaders(Vector vector){
		for(int i=0; i<vector.size(); i++){
			Header header = (Header)vector.elementAt(i);

			if(header.getName().equalsIgnoreCase("Content-Length")){
				vector.removeElementAt(i);
				i--;
			} else if(header.getName().equalsIgnoreCase("Content-Type")){
				vector.removeElementAt(i);
				i--;
			}
		}		
	}

	/**
	 * Reads the content of a given InputStream and returns the content as a String
	 * @param is	InputStream to read from
	 * @return	Content as String
	 * @throws Throwable	Will be caught by the caller
	 */
	/*private String read(InputStream is) throws Throwable{
		String result = "";
		byte[] buffer = new byte[250];
		int offset = 0;

		while ((offset = is.read(buffer)) != -1) {
			result += new String(buffer, 0, offset);
		}
		is.close();

		return result;
	}*/
	
	private String getURLStringFromConnection(HttpConnection hconn) {
		InputStream in = read(hconn);
		String resultado;
		try {
			resultado = stringWriter(in);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return resultado;
	}
	
	/**
	 * read a file and converting it to String using StringWriter
	 */
	public String stringWriter(InputStream inputStream) throws IOException {
		char[] buff = new char[1024];
		StringBuffer stringWriter = new StringBuffer();
		try {
			Reader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			int n;
			while ((n = bReader.read(buff)) != -1) {
				stringWriter.append(buff, 0, n);
			}
		} finally {

		}
		String result = stringWriter.toString();
		return result;
	}
	/*DESDE OTRO CODIGO*/
	/*CODIGO EXITOSO, PARA OBTENER UNA URL HTTP O XML*/
	private InputStream read(HttpConnection httpConn) {				
		try {
			InputConnection inputConn = (InputConnection) httpConn;
			InputStream is = inputConn.openInputStream();
			return is;
			//return url.openStream();
		} catch (Exception e) {
			Dialog.alert("Error en la conexión.\nSitio inaccesible.");
			throw new RuntimeException();
		}
	}
	

	/**
	 * Stops the entire test
	 */
	public void stopTest(){
		stop = true;
		logEventLog("Test detenido por usuario. Cerrando conexiones activas..");
		if(currentConn instanceof Connection){
			try{
				((Connection) currentConn).close();
			} catch(Throwable t){
				logEventLog("!Error cerrando conexión: " + t.toString());
			}
		} else if(currentConn instanceof SSL30Connection){
			try {
				((SSL30Connection)currentConn).close();
			} catch (Throwable t) {
				logEventLog("!Error cerrando conexión: " + t.toString());
			}
		}
	}



	public void logEventLog(String msg){
		EventLogger.logEvent(0x4e697e68da459c1cL, msg.getBytes(), EventLogger.ALWAYS_LOG);
	}


	/************ RadioStatusListener Implementation**************/
	
	/********* END OF RadioStatusListener **********/

	private class Header{
		private String name;
		private String value;
		public Header(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public String getValue() {
			return value;
		}	
	}

	public void baseStationChange() {
		//  Auto-generated method stub		
	}

	public void networkScanComplete(boolean success) {
		//  Auto-generated method stub		
	}

	public void networkServiceChange(int networkId, int service) {
		//  Auto-generated method stub		
	}

	public void networkStarted(int networkId, int service) {
		//  Auto-generated method stub		
	}

	public void networkStateChange(int state) {
		//  Auto-generated method stub		
	}

	public void pdpStateChange(int apn, int state, int cause) {
		//  Auto-generated method stub		
	}

	public void radioTurnedOff() {
		//  Auto-generated method stub		
	}

	public void signalLevel(int level) {
		//  Auto-generated method stub		
	}
}
