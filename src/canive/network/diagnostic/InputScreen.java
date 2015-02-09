package canive.network.diagnostic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import util.CustomStrings;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * This is the Screen that is displayed to the user when the application
 * launches. The user can input the url to connecct to, TCP APN parameters and
 * other parameters related to the test. 
 * 
 * @author Shadid Haque
 * 
 */
public class InputScreen extends MainScreen {
	/**
	 * Fields for entering url, port and number of allowed retries in case of a
	 * failure
	 */
	private static String http = "http://127.0.0.1/atiempo/genesis.xml";
	private static String http2 = "http://127.0.0.1/atiempo/est.csv";
	private final String []unidades = new String[] {"1","2","3","4","5","6","7","8","9","10"};
	private static String CONFIGFILE = "file:///SDCard/net/config.ini";
	private ObjectChoiceField efRetriesCombo;
	private EditField efPostSize;
	private int postSize;
	/** Action buttons to run the tests and to show/hide advanced options */
	private CustomButtonField bfGuardar, bfTcpOptions;
	/** Fields to enter WAP1.0 parameters */
	private EditField efTcpApn, efTcpApnUser, efTcpApnPassword;	
	private RadioButtonField rfTestHttpGet;
	private RadioButtonField rfTestHttpPost;
	private RadioButtonField rfWiFiTransport;
	private RadioButtonField rfTCPTransport;
	private RadioButtonGroup cnxMethod;
	private RadioButtonGroup cnxTransports;
	/** An instance of this */
	private InputScreen iscreen;
	private CNXSettings globalSettings;

	/**
	 * Constructor. Initializes all the UI Fields and adds them to this.
	 */
	public InputScreen() {
		this.iscreen = this;
		setTitle("ATiempo 2014");

		postSize = 1500;
		cnxMethod = new RadioButtonGroup();
		cnxTransports = new RadioButtonGroup();
		bfGuardar = new CustomButtonField("Guardar", Field.USE_ALL_WIDTH | Field.FIELD_HCENTER,this.iscreen);
		//LOAD SETTINGS
		globalSettings = new CNXSettings();		
		globalSettings = CustomStrings.readConfig(CONFIGFILE);
		int tries = 1;
		boolean useGET = true;
		postSize = 1500;
		
		boolean useTCP = true;
		String tcpAPN = "nauta";
		String tcpUser = "";
		String tcpPass = "";

		if (globalSettings!=null) {	
			//CNXSettings(String tcpAPN, String tcpUser, String tcpPass)
			tries = globalSettings.getTries();
			useGET = globalSettings.isUseGET();
			postSize = globalSettings.getMethodPostSize();
			useTCP = globalSettings.isUseTCP();
			tcpAPN = globalSettings.getTcpAPN();
			tcpUser = globalSettings.getTcpUser();
			tcpPass = globalSettings.getTcpPass();
		}	

		//INTENTOS
		efRetriesCombo = new ObjectChoiceField("Intentos:", unidades);
		efRetriesCombo.setSelectedIndex(tries-1);			
		//METODO DE CONEXION (GET/POST)
		rfTestHttpGet = new RadioButtonField("Método HTTP GET",cnxMethod, useGET);
		rfTestHttpPost = new RadioButtonField("Método HTTP POST",cnxMethod, !useGET);
		//TAMAÑO DEL BUFFER DEL POST
		efPostSize = new EditField("Tamaño de POST [bytes]: ", String.valueOf(postSize), 100, EditField.FILTER_INTEGER);
		//RED DE CONEXION
		rfWiFiTransport = new RadioButtonField("WiFi",cnxTransports, !useTCP);
		rfTCPTransport = new RadioButtonField("TCP Celular",cnxTransports, useTCP);

		//[Note: Host and Content-Length are determined dynamically from URL and POST size. Content-Type and Content-Length are used for POST only.];

		rfWiFiTransport.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (rfTCPTransport.isSelected()) {
					bfTcpOptions.setEditable(true);
				}
				else
				{
					bfTcpOptions.setEditable(false);
				}
			}
		});
		rfTCPTransport.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (rfTCPTransport.isSelected()) {
					bfTcpOptions.setEditable(true);
				}
				else
				{
					bfTcpOptions.setEditable(false);
				}
			}
		});

		bfTcpOptions = new CustomButtonField("Opciones TCP", Field.USE_ALL_WIDTH | Field.FIELD_HCENTER);
		if (rfTCPTransport.isSelected()) {
			bfTcpOptions.setEditable(true);
		}
		else
		{
			bfTcpOptions.setEditable(false);
		}

		efTcpApn = new EditField("  APN: ", tcpAPN);
		efTcpApnUser = new EditField("  Usuario: ", tcpUser);
		efTcpApnPassword = new EditField("  Contraseña: ", tcpPass);

		add(efRetriesCombo);
		add(new SeparatorField());
		add(rfTestHttpGet);
		add(rfTestHttpPost);		
		add(efPostSize);
		add(new SeparatorField());

		add(rfWiFiTransport);
		add(rfTCPTransport);;
		add(bfTcpOptions);
		add(new SeparatorField());
		add(bfGuardar);		
		add(new SeparatorField());
	}

	public InputScreen(IOThread io) {
		setTitle("Moving ON!");
		Vector response = new Vector();
		response = io.getResponse();
		String a = (String)response.elementAt(0);
		String b = (String)response.elementAt(1);
		Dialog.alert(a);
		Dialog.alert(b);
	}


	//Leer archivo de configuracion de red
	public boolean writeConfig(String FILE){
		OutputStream out = null;
		FileConnection fconn = null;
		boolean exito = false;
		try {
			fconn = (FileConnection) Connector.open(FILE,Connector.READ_WRITE);
			if (!fconn.exists()) {
				fconn.create();
			}
			else if (fconn.exists())
			{
				fconn.delete();
				fconn.create();
				exito = true;
			}
			else 
			{
				Dialog.alert("Error al guardar configuración.No hay dónde guardar.");	
			}
			out = fconn.openOutputStream();
			// it might be advisable to specify an encoding on the next line.
			//ENCADENAR ESTOS DATOS
			//(int tries, boolean useGET, int methodPostSize, boolean useTCP, String tcpAPN, String tcpUser,String tcpPass, String url1, String url2)
			String keys = globalSettings.getTries()+"|"+globalSettings.isUseGET()+"|"+globalSettings.getMethodPostSize()
					+"|"+globalSettings.isUseTCP()+"|"+globalSettings.getTcpAPN()+"|"+globalSettings.getTcpUser()+"|"+globalSettings.getTcpPass()+"|";
			out.write(keys.getBytes());	
			//SINO CERRAR APP O DAR ERROR
		}
		catch(Exception ex) {}
		finally{if (out != null) try { out.close(); } catch (IOException ignored) {}}
		return exito;
	}

	/**
	 * Custom implementation of ButtonField which handles click events for the
	 * Advanced and Run buttons.
	 * 
	 * @author Shadid Haque
	 * 
	 */
	private class CustomButtonField extends ButtonField {

		private InputScreen in;
		private CustomButtonField(String label, long style) {
			super(label, style);
		}

		public CustomButtonField(String label, long style, InputScreen iscreen) {
			super(label, style);
			in = iscreen;
		}

		protected boolean navigationClick(int status, int time) {			
			if(getLabel().equals("Opciones TCP")){
				if (bfTcpOptions.isEditable()) {
					PopupScreen tcpScreen = new PopupScreen(new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL));				
					ButtonField bfOK = new ButtonField("OK", Field.USE_ALL_WIDTH|Field.FIELD_HCENTER){
						protected boolean navigationClick(int status, int time) {
							getScreen().deleteRange(0, getScreen().getFieldCount()-1);
							UiApplication.getUiApplication().popScreen(getScreen());
							return true;
						}					
					};		

					tcpScreen.add(efTcpApn);
					tcpScreen.add(new SeparatorField());
					tcpScreen.add(efTcpApnUser);
					tcpScreen.add(new SeparatorField());
					tcpScreen.add(efTcpApnPassword);
					tcpScreen.add(new SeparatorField());
					tcpScreen.add(bfOK);
					UiApplication.getUiApplication().pushScreen(tcpScreen);
				}
			}
			else if (getLabel().equals("Guardar")) {
				int retry = getRetries();
				boolean get = isTestGet();
				int size = getPostSize();
				boolean tcp = isDoTcp();
				CNXSettings settings = new CNXSettings(retry, get, size, tcp , efTcpApn.getText(), efTcpApnUser.getText(), efTcpApnPassword.getText(), http, http2);
				globalSettings = settings;
				boolean escrito = writeConfig(CONFIGFILE);
				if (!escrito) {
					Dialog.alert("Debe poner una SD o corregir su sistema de archivos interno.");
				}
				in.close();
			}	
			return true;
		}
	}

	public boolean onClose() {
		return super.onClose();
	}


	public boolean isDoTcp() {
		return rfTCPTransport.isSelected();
	}

	public int getRetries() {
		try {
			int num = Integer.parseInt(unidades[efRetriesCombo.getSelectedIndex()]);
			if (num < 1)
				return 1;
			else
				return num;
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	public boolean isTestGet(){
		return rfTestHttpGet.isSelected();
	}

	public int getPostSize(){
		int aux = Integer.parseInt(efPostSize.getText());
		if (aux !=0) {
			return aux;
		}		
		return postSize; 		
	}

	/** END OF Getters and Setters */

	protected boolean onSave() {
		return true;
	}

	protected boolean onSavePrompt() {
		return true;
	}

}