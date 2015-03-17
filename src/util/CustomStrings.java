package util;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import canive.network.diagnostic.CNXSettings;


public class CustomStrings {

	public CustomStrings() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static Vector split(String phrase, char character)
	{
		Vector result = new Vector();
		char[] arr = phrase.toCharArray();
		StringBuffer collecting = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i]!=character) {
				collecting.append(arr[i]);
				if (i==arr.length-1) {
					result.addElement(collecting.toString());
				}
			}
			else
			{
				result.addElement(collecting.toString());
				collecting = new StringBuffer();
			}    		
		}
		return result;
	}
	
	//Leer archivo de configuracion de red
		public static CNXSettings readConfig(String CONFIGFOLDER,String CONFIGFILE)
		{
			InputStream is = null;
			BufferedReader inputStream = null ;
			CNXSettings config = new CNXSettings();
			try {
				FileConnection fconn = (FileConnection) Connector.open(CONFIGFOLDER+CONFIGFILE, Connector.READ);            
				if (fconn.exists()) {
					is = fconn.openInputStream();
					inputStream = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					String str = "";
					str = inputStream.readLine();
					Vector data = split(str, '|');
					//(int tries, boolean useGET, int methodPostSize, boolean useTCP, String tcpAPN, String tcpUser,String tcpPass, String url1, String url2)
					String tries = (String)data.elementAt(0);
					String useGET = (String)data.elementAt(1);
					String methodPostSize = (String)data.elementAt(2);
					String useTCP = (String)data.elementAt(3);
					String tcpAPN = (String)data.elementAt(4);
					String tcpUser = (String)data.elementAt(5);
					String tcpPass = (String)data.elementAt(6);
					String url1 = (String)data.elementAt(7);
					String url2 = (String)data.elementAt(8);
					config.setTries(Integer.parseInt(tries));
					config.setUseGET((useGET.equalsIgnoreCase("true")?true:false));
					config.setMethodPostSize(Integer.parseInt(methodPostSize));
					config.setUseTCP((useTCP.equalsIgnoreCase("true")?true:false));
					config.setTcpAPN(tcpAPN);
					config.setTcpUser(tcpUser);
					config.setTcpPass(tcpPass);
					config.setUrl1(url1);
					config.setUrl2(url2);
				} else {
					return null; //file not available
				}
				if (fconn != null) try { fconn.close(); } catch (IOException ignored) {}
				if (inputStream != null) {inputStream.close();}
				is.close();
			} catch (Exception ioe) {
				return null;
			}	
			return config;
		}
}
