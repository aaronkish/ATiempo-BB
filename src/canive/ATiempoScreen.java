package canive;

import java.io.ByteArrayInputStream;
import java.lang.String;
import java.util.Date;
import java.util.Vector;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.util.DateTimeUtilities;
import net.rim.device.api.xml.parsers.*;

import org.w3c.dom.*;

import canive.network.diagnostic.IOThread;


import util.CustomStrings;

//IMPORTANTE NO ELIMINAR ESTA LINEA DE DATOS DE ABAJO**************************************************************************
//LONG KEY FOR STORE: ATiempo para BlackBerry 
//VALUE: 0x8d5cb8152ca80fb4L
//*****************************************************************************************************************************

/**
 * The main screen for the application.  Displays the results of parsing the XML file.
 */
public final class ATiempoScreen extends MainScreen
{
	private VerticalFieldManager vManager;

	// Statics -------------------------------------------------------------------------------------
	//private static String http = "http://www.met.inf.cu/asp/genesis.asp?TB0=RSSFEED";//NO PUBLICAR (NO SE ESTA USANDO)
	private static String http2 = "http://www.met.inf.cu/pronostico/est.csv"; //PUBLICAR SOLO ESTA
	//private static String http = "http://127.0.0.1/atiempo/genesis.xml"; //NO TESTS (NO SE ESTA USANDO)
	//private static String http2 = "http://127.0.0.1/pronostico/est.csv"; //TESTS SOLO ESTA	
	
	private static String []ciudadTitulo = new String[] {"CIENFUEGOS", "LA HABANA", "PINAR DEL RIO", "VARADERO", "CAYO COCO", "CAMAGÜEY", "HOLGUÍN", "SANTIAGO DE CUBA"};
	private static String[] estaciones = new String[] {"78344", "78325","78315","78328","78339","78355","78372","78364"};
	private static String []unidades = new String[] {"ºC","ºF"};
	private static String []meses = new String[] {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
	private static String appname = "ATiempo para BlackBerry ";
	private static String version = "v1.0.37\n";
	private static String email = "aaronkish@icloud.com\n";
	private static String copyright = "Copyright Abel Espinosa, 2015";
	private static String city;
	private static String s;	
	private static PersistentObject store;
	private static Vector ciudades = new Vector();
	private static Vector localidades = new Vector();
	private static Vector localidadesPrincipales = new Vector();
	private Document doc;
	private String fechaActual;
	private String mesActual;
	private String anhoActual;
	private String diaActual;
	private String responseA = "";
	private String responseB = "";
	static{
		store = PersistentStore.getPersistentObject( 0x8d5cb8152ca80fb4L );
	}
	private static ObjectChoiceField selectLocationField = null;
	private static ButtonField setLocationButton = null;
	private static ObjectChoiceField selectUnitField = null;
	private static ButtonField setUnitButton = null;

	// Constants -----------------------------------------------------------------------------------
	private ListField navField;
	private Vector navItems;
	private Ciudad actual = new Ciudad();

	private ATiempoDate objDate = new ATiempoDate();
	/** A reference to IOThread instance */
	private IOThread ioThread;

	/**
	 * This constructor parses the XML file into a W3C DOM document, and displays it 
	 * on the screen.
	 * 
	 * @see Document
	 * @see DocumentBuilder
	 * @see DocumentBuilderFactory
	 */
	public ATiempoScreen(IOThread ioThread) 
	{
		navItems = new Vector();
		navField = new ListField();
		s = null;
		this.ioThread = ioThread;
		ListCallback callBack = new ListCallback();
		navField.setCallback(callBack);
		//setTitle("ATiempo"); 
		fechaActual = "";
		//Color solido de fondo 
		int intValue = Integer.parseInt( "36bbfe",16); 
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(intValue));
		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);
		if (this.ioThread!=null) {
			int count = this.ioThread.getResponse().capacity();
			//Debe tener minimo 2 elementos, correspondientes a la descarga de los archivos de las 2 URL.
			if(count>=2)
			{
				String recoveredResponseA = (String)this.ioThread.getResponse().elementAt(0);
				String recoveredResponseB = (String)this.ioThread.getResponse().elementAt(1);
				this.setResponseA(recoveredResponseA);
				this.setResponseB(recoveredResponseB);
				haveFun();
			}
			else
			{
				Dialog.alert("Respuesta incorrecta.\nRevise conexión y reinicie la app.");
			}
		}
		else
		{
			Dialog.alert("Respuesta incorrecta.\nRevise conexión y reinicie la app.");
		}
	}   

	private void haveFun() {
		if (responseA.length()>0) {
			this.doc = getDocumentByString(responseA);
			/*try {
				this.extraData = stringWriter(http2);
			}
			catch (Exception e) {
				Dialog.alert("Error inesperado");
				System.exit(0);
				return;
			}*/
			//MANEJAR CVS DE HTTP2
			if (!this.responseB.equals("")) {
				localidades = extractTodayData(this.responseB);
				if (localidades==null) {
					this.responseB = HTTPSocketConnector.getHtml(http2,10000);
					if (this.responseB.equals("")||this.responseB==null) {
						System.exit(0);
						return;
					}
					else
					{
						String error = CleanERRORConnection(this.responseB);
						if (error!=null) {
							Dialog.alert(error+".\nEl archivo de datos de localidades no se ha descargado.");
						}
					}
				}
				else if (localidades.size()==1) {
					String error = (String)localidades.elementAt(0);
					Dialog.alert(error+".\nEl archivo de datos de localidades no se ha descargado.");
				}
			}
			desbaratarXMLsiDocOK();
		}		
	}	

	private void desbaratarXMLsiDocOK()
	{
		//MANEJAR XML DE HTTP
		if (this.doc!=null) {
			extractCiudad(this.doc);
			int cap = ciudades.size();
			for (int i = 0; i < cap; i++) {
				extractDataFromDescription((Ciudad)ciudades.elementAt(i));
			}
			Vector fechaAct = extractFechaActual(doc);
			int mes = 0;
			String pos = (String)fechaAct.elementAt(0);
			if (pos.equals("1")) {
				mes = 1;
			}
			else if (pos.equals("2")) {
				mes = 2;
			}
			else if (pos.equals("3")) {
				mes = 3;
			}
			else if (pos.equals("4")) {
				mes = 4;
			}
			else if (pos.equals("5")) {
				mes = 5;
			}
			else if (pos.equals("6")) {
				mes = 6;
			}
			else if (pos.equals("7")) {
				mes = 7;
			}
			else if (pos.equals("8")) {
				mes = 8;
			}
			else if (pos.equals("9")) {
				mes = 9;
			}
			else if (pos.equals("10")) {
				mes = 10;
			}
			else if (pos.equals("11")) {
				mes = 11;
			}
			else if (pos.equals("12")) {
				mes = 12;
			}
			this.mesActual = meses[mes-1];
			this.diaActual = ""+fechaAct.elementAt(1);
			this.anhoActual = ""+fechaAct.elementAt(2);
			objDate.setCurrentDay(this.diaActual);
			objDate.setCurrentMonth(this.mesActual);
			objDate.setCurrentYear(this.anhoActual);
			this.fechaActual = this.mesActual+ " " + this.diaActual + ", " +  this.anhoActual;
			displayInterface();
		}
		else
		{
			//displayInterface();
			Dialog.alert("Informacion incompleta. Reinicie la app.");
			System.exit(0);
			return;
		}
	}

	private Vector extractTodayData(String cvs) {
		cvs = this.replaceAll(cvs, "\t", "");
		cvs = this.replaceAll(cvs, "\n", "");
		cvs = this.replaceAll(cvs, "\r", "");
		cvs.trim();
		Vector cvsData = CustomStrings.split(cvs, ',');
		Vector cvsVectorLocalidad = new Vector();
		int count = 0;
		int pos = 0;
		Localidad loc = new Localidad();
		while (pos<cvsData.size()) {			
			switch (count) {
			case 0:
				loc.setEstacion((String)cvsData.elementAt(pos));
				break;
			case 1:
				loc.setTactual((String)cvsData.elementAt(pos));
				break;
			case 2:
				loc.setLluviasen3((String)cvsData.elementAt(pos));
				break;
			case 3:
				loc.setTmax((String)cvsData.elementAt(pos));
				break;
			case 4:
				loc.setTmin((String)cvsData.elementAt(pos));
				break;
			case 5:
				loc.setLluviaen24((String)cvsData.elementAt(pos));
				break;
			case 6:
				loc.setPresionMinima((String)cvsData.elementAt(pos));
				break;
			case 7:
				loc.setDireccionVientoGrados((String)cvsData.elementAt(pos));
				break;
			case 8:
				loc.setVelocidadViento((String)cvsData.elementAt(pos));
				break;
			case 9:
				loc.setUnknown5((String)cvsData.elementAt(pos));
				break;
			case 10:
				loc.setUnknown6((String)cvsData.elementAt(pos));
				break;
			case 11:
				loc.setUnknown7((String)cvsData.elementAt(pos));
				break;
			case 12:
				loc.setUnknown8((String)cvsData.elementAt(pos));
				break;
			case 13:
				loc.setUnknown9((String)cvsData.elementAt(pos));
				break;
			case 14:
				loc.setUnknown10((String)cvsData.elementAt(pos));
				break;
			case 15:
				loc.setUnknown11((String)cvsData.elementAt(pos));
				break;
			case 16:
				loc.setUnknown12((String)cvsData.elementAt(pos));
				break;
			default:
				break;
			}	
			if (count==16) {
				count=0;
				cvsVectorLocalidad.addElement(loc);
				for (int i = 0; i < estaciones.length; i++) {
					if (loc.getEstacion().equals(estaciones[i])) {
						localidadesPrincipales.addElement(loc);
						break;
					}
				}				
				loc = new Localidad();
			}
			else
			{
				count++;
			}			
			pos++;
		}
		return cvsVectorLocalidad;

	}

	private String CleanERRORConnection(String response)
	{
		int errorStartAt = response.indexOf("Error");
		if (errorStartAt>0) {
			String rest = response.substring(errorStartAt);
			int errorEndsAt = rest.indexOf("<");
			if (errorEndsAt>0) {
				String errorText = rest.substring(0, errorEndsAt);
				return errorText;
			}
		}
		return null;
	}

	public String CleanCDATA(String cdata)
	{
		cdata = this.replaceAll(cdata, "\t", "");
		cdata = this.replaceAll(cdata, "\n", "");
		cdata = this.replaceAll(cdata, "\r", "");
		cdata.trim();
		char[] cdataArray = cdata.toCharArray();
		StringBuffer a = new StringBuffer();
		for (int i = 0; i < cdataArray.length; i++) {
			int current = i;

			//ELIMINAR ESPACIOS CONTINUOS
			if (cdataArray[current]==' ' && cdataArray[current+1]==' ') {
				while(cdataArray[current+1]==' ')
				{
					cdataArray[current+1] = cdataArray[current+2];
					current++;
				}    			
			} 
			//Eliminar espacios despues de TAG <
			if (cdataArray[current]=='<' && cdataArray[current+1]==' ') {
				while(cdataArray[current+1]==' ')
				{
					cdataArray[current+1] = cdataArray[current+2];
					current++;
				}    			
			} 
			//Eliminar espacios antes de TAG >
			if (cdataArray[current]==' ' && cdataArray[current+1]=='>') {
				cdataArray[current] = cdataArray[current+1];
				current++;    						
			}
			i = current;
			a.append(cdataArray[i]);
		}
		return a.toString();

	}

	private class ListCallback implements ListFieldCallback
	{
		public void drawListRow(ListField list, Graphics g, int index, int y, int w)
		{
			String text = (String) this.get(list, index);

			g.drawText(text, 0, y, 0, w);
		}

		public Object get(ListField listField, int index) {
			return navItems.elementAt(index);
		}

		public int getPreferredWidth(ListField listField) {
			return Display.getWidth();
		}

		public int indexOfList(ListField listField, String prefix, int start) {
			return navItems.indexOf(prefix, start);
		}
	}


	/**
	 * Displays a node at a specified depth, as well as all its descendants.
	 * 
	 * @param node The node to display.
	 * @param depth The depth of this node in the document tree.
	 */

	private Vector extractCiudad(Document document)
	{		
		document.getDocumentElement().normalize();
		NodeList list = document.getElementsByTagName("item");

		for (int i=0;i<list.getLength();i++){
			actual = new Ciudad();
			Node currNode = list.item(i);

			if (currNode.getNodeType() == Node.ELEMENT_NODE) {
				Element currElement = (Element) currNode;

				NodeList titleList = currElement.getElementsByTagName("title");
				Element titleElem = (Element) titleList.item(0);
				NodeList titleNode = titleElem.getChildNodes();
				String title = ((Node) titleNode.item(0)).getNodeValue();
				//FIX para Pinar del Rio del RSS de meteorologia
				if (title.startsWith("Extendido", 11)) {
					title = "PINAR DEL RIO";
				}
				else if (title.startsWith("CAMAG", 0)) {
					title = "CAMAGÜEY";
				}
				else if (title.startsWith("HOLGU", 0)) {
					title = "HOLGUÍN";
				}
				int pos = 0;
				while (pos < ciudadTitulo.length) {
					if (title.equals(ciudadTitulo[pos])) {
						actual.setTitulo(title);
						NodeList descriptionList = currElement.getElementsByTagName("description");
						Element descriptionElem = (Element) descriptionList.item(0);
						NodeList descriptionNode = descriptionElem.getChildNodes();
						StringBuffer sb = new StringBuffer();
						for (int j = 0; j < descriptionNode.getLength(); j++) {
							sb.append(((Node) descriptionNode.item(j)).getNodeValue());
						}
						String description = sb.toString();
						actual.setDescripcion(description);
						ciudades.addElement(actual);                        
						break;
					}
					pos++;
				}
			}   
		}
		return ciudades;
	}

	private Vector extractFechaActual(Document document)
	{
		Vector fecha = new Vector();
		document.getDocumentElement().normalize();
		NodeList list = document.getElementsByTagName("lastBuildDate");
		Node currNode = list.item(0);

		if (currNode.getNodeType() == Node.ELEMENT_NODE) {
			Element currElement = (Element) currNode;
			NodeList titleNode = currElement.getChildNodes();
			String title = ((Node) titleNode.item(0)).getNodeValue(); 
			fecha = CustomStrings.split(title,'/');
		} 
		else
		{
			fecha.addElement("Indef");
		}        
		return fecha;
	}

	private void displayInterface()
	{
		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);
		add(vManager);    	
		addMenuItem(_refreshItem);
		addMenuItem(_aboutItem);
		addMenuItem(_changeCity);
		addMenuItem(_changeMetrics);
		addMenuItem(_fullExitItem);

		synchronized(store) {
			city = (String)store.getContents();
		}

		if(s == null || s == "")
		{
			s = "ºC";
		}

		if(city == null)
		{        
			changeCityEXTRA();    

		}
		else { 
			displayForecast();
		}
	}

	public boolean onClose() 
	{		
		ioThread.stopTest();		
		return super.onClose();
	};

	private MenuItem _fullExitItem = new MenuItem("Salir Completamente", 100, 200)
	{
		public void run()
		{
			System.exit(0);
			return;
		}
	};

	private MenuItem _aboutItem = new MenuItem("Acerca de ATiempo", 100, 200)
	{
		public void run()
		{
			Dialog.inform(appname+version+email+copyright);
		}
	};


	private MenuItem _refreshItem = new MenuItem("Actualizar", 100, 200)
	{
		public void run()
		{
			try {
				refresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}; 

	private MenuItem _changeCity = new MenuItem("Cambiar ciudad", 100, 200)
	{
		public void run()
		{
			changeCityEXTRA();
		}
	};

	private void changeCityEXTRA() 
	{			
		vManager.deleteAll();
		delete(vManager);
		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);        
		add(vManager);

		//TOPE
		//FUENTE
		FontFamily[] fontFamily = FontFamily.getFontFamilies();
		Font fuenteTitulo = fontFamily[0].getFont(Font.BOLD, 15);
		Font fuenteCiudades = null;
		try
		{
			FontFamily fontFamilyDatePron= FontFamily.forName("BBGlobal Sans");
			fuenteCiudades = fontFamilyDatePron.getFont(Font.ENGRAVED_EFFECT, 17);
		}
		catch (Exception e) {			
			fuenteCiudades = fontFamily[0].getFont(Font.BOLD, 17);
		}

		//BLOQUE 1
		//SEPARADOR 1
		Bitmap separatorH = Bitmap.getBitmapResource("separatorH.png");
		BitmapField separatorHIcon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorHIcon.setPadding(5, 0, 5, 0);
		//SEPARADOR 2
		BitmapField separatorH2Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH2Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 3
		BitmapField separatorH3Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH3Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 4
		BitmapField separatorH4Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH4Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 5
		BitmapField separatorH5Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH5Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 6 
		BitmapField separatorH6Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH6Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 7 
		BitmapField separatorH7Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH7Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 8 
		BitmapField separatorH8Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH8Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 9 
		BitmapField separatorH9Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH9Icon.setPadding(5, 0, 5, 0);
		//SEPARADOR 10 
		BitmapField separatorH10Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
		separatorH10Icon.setPadding(5, 0, 5, 0);

		//LABEL
		FCLabelField ciudadesTitle = new FCLabelField("Ciudades", Manager.FIELD_HCENTER|LabelField.FIELD_HCENTER);        
		ciudadesTitle.setFontColor(Color.WHITE);
		ciudadesTitle.setFont(fuenteTitulo);
		ciudadesTitle.setPadding(5, 0, 0, 0);
		vManager.add(ciudadesTitle);

		//CIUDAD 1
		FCLabelField CityOne = new FCLabelField(ciudadTitulo[0], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[0]);
				return true;
			}
		};
		FCLabelField CityTwo = new FCLabelField(ciudadTitulo[5], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[5]);
				return true;
			}
		};
		FCLabelField CityThree = new FCLabelField(ciudadTitulo[4], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[4]);
				return true;
			}
		};
		FCLabelField CityFour = new FCLabelField(ciudadTitulo[1], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[1]);
				return true;
			}
		};
		FCLabelField CityFive = new FCLabelField(ciudadTitulo[6], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[6]);
				return true;
			}
		};
		FCLabelField CitySix = new FCLabelField(ciudadTitulo[2], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[2]);
				return true;
			}
		};
		FCLabelField CitySeven = new FCLabelField(ciudadTitulo[7], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[7]);
				return true;
			}
		};
		FCLabelField CityEight = new FCLabelField(ciudadTitulo[3], LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				changeCity(ciudadTitulo[3]);
				return true;
			}
		};
		FCLabelField CityPlus = new FCLabelField("Hoy, en otras localidades", LabelField.FIELD_VCENTER|LabelField.FIELD_HCENTER|Field.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				//MOSTRAR SCREEN DE LISTA COMPLETA DE LOCALIDADES
				showAllLocalities();
				return true;
			}
		};
		CityOne.setFontColor(Color.WHITE);
		CityOne.setFont(fuenteCiudades);
		CityTwo.setFontColor(Color.WHITE);
		CityTwo.setFont(fuenteCiudades);
		CityThree.setFontColor(Color.WHITE);
		CityThree.setFont(fuenteCiudades);
		CityFour.setFontColor(Color.WHITE);
		CityFour.setFont(fuenteCiudades);
		CityFive.setFontColor(Color.WHITE);
		CityFive.setFont(fuenteCiudades);
		CitySix.setFontColor(Color.WHITE);
		CitySix.setFont(fuenteCiudades);
		CitySeven.setFontColor(Color.WHITE);
		CitySeven.setFont(fuenteCiudades);
		CityEight.setFontColor(Color.WHITE);
		CityEight.setFont(fuenteCiudades);
		CityPlus.setFontColor(Color.WHITE);
		CityPlus.setFont(fuenteCiudades);

		vManager.add(separatorHIcon);
		vManager.add(CityOne);
		vManager.add(separatorH2Icon);
		vManager.add(CityTwo);
		vManager.add(separatorH3Icon);
		vManager.add(CityThree);
		vManager.add(separatorH4Icon);
		vManager.add(CityFour);
		vManager.add(separatorH5Icon);
		vManager.add(CityFive);
		vManager.add(separatorH6Icon);
		vManager.add(CitySix);
		vManager.add(separatorH7Icon);
		vManager.add(CitySeven);
		vManager.add(separatorH8Icon);
		vManager.add(CityEight);
		vManager.add(separatorH9Icon);
		vManager.add(CityPlus);
		vManager.add(separatorH10Icon);
	}

	public void showAllLocalities() {
		vManager.deleteAll();
		delete(vManager);

		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);

		add(vManager);

		//FUENTES
		FontFamily[] fontFamily = FontFamily.getFontFamilies();
		Font fuenteTitulo = fontFamily[0].getFont(Font.BOLD, 15);
		Font fuenteLocalidades = null;
		try
		{
			FontFamily fontFamilyDatePron= FontFamily.forName("BBGlobal Sans");
			fuenteLocalidades = fontFamilyDatePron.getFont(Font.ENGRAVED_EFFECT, 15);
		}
		catch (Exception e) {			
			fuenteLocalidades = fuenteTitulo;
		}
		//TITULO DE TABLA
		FCLabelField localidadesTitle = new FCLabelField("Estaciones", Manager.FIELD_HCENTER|LabelField.FIELD_HCENTER);        
		localidadesTitle.setFontColor(Color.WHITE);
		localidadesTitle.setFont(fuenteTitulo);
		localidadesTitle.setPadding(5, 0, 0, 0);
		vManager.add(localidadesTitle);	

		if (localidades!=null && localidades.size()>1) {
			for (int i = 0; i < localidades.size(); i++) 
			{
				Localidad aux = (Localidad)localidades.elementAt(i);
				boolean isPosibleInteger = isNumberInt(aux.getEstacion());
				int val = 0;
				if (isPosibleInteger) {
					val = Integer.parseInt(aux.getEstacion());
				}		
				String estacionText = textStation(val);
				String tactual = aux.getTactual();
				String tmin = aux.getTmin();
				String tmax = aux.getTmax();
				if (!tactual.equals("")&&!tmin.equals("")&&!tmax.equals("")) {
					if (!s.equals("ºC")) {
						int auxiliar  = cambiarMetricaSingle(tactual);
						int auxiliar1  = cambiarMetricaSingle(tmin);
						int auxiliar2  = cambiarMetricaSingle(tmax);
						if (auxiliar==-1000) {
							tactual = "/";
						}
						else
						{
							tactual = String.valueOf(auxiliar);
						}
						if (auxiliar1==-1000) {
							tmin = "/";
						}
						else
						{
							tmin = String.valueOf(auxiliar1);
						}
						if (auxiliar2==-1000) {
							tmax = "/";
						}
						else
						{
							tmax = String.valueOf(auxiliar2);
						}
					}
	
					//TODO  MOSTRAR LISTA DE ESTACIONES COMPLETAS / LOCALIDADES
					//HUMEDAD RELATIVA,
					//CONDICIONES ACTUALES	
	
					//TEMPERATURA ACTUAL
					FCLabelField currentTemp = new FCLabelField(tactual+s, LabelField.FIELD_VCENTER|LabelField.FIELD_LEFT);
					currentTemp.setFont(fuenteLocalidades);
					currentTemp.setFontColor(Color.WHITE);
					currentTemp.setPadding(2, 0, 0, 5);
					//TEMPERATURA MINIMA Y MAXIMA
					FCLabelField currentMinMax = new FCLabelField(tmin+s+"/"+tmax+s, LabelField.FIELD_VCENTER|LabelField.FIELD_LEFT);
					currentMinMax.setFont(fuenteLocalidades);
					currentMinMax.setFontColor(Color.WHITE);
					currentMinMax.setPadding(2, 0, 0, 5);
					HorizontalFieldManager localityReportStation = new HorizontalFieldManager(Manager.USE_ALL_WIDTH);
					HorizontalFieldManager localityReportTemp = new HorizontalFieldManager(Manager.FIELD_RIGHT);
					localityReportTemp.add(currentTemp);
					localityReportTemp.add(currentMinMax);
					VerticalFieldManager temperaturasDerecha = new VerticalFieldManager(Manager.FIELD_RIGHT|Field.USE_ALL_WIDTH);
					temperaturasDerecha.add(localityReportTemp);
	
					//VARIABLES FINALES PARA USAR EN EL METODO INTERNO DEL CLIC
					final String estacion = estacionText;
					final String temperaturaActual = tactual;
					final String temperaturaMaxima = tmax;
					final String temperaturaMinima = tmin;
					final String displayEstadoActual = (aux.getEstado()!=null)?aux.getEstado():"Desconocido";
					final String estadoActualIcon = getIconForState("mini_icon_unknown.png");
					boolean isVelocidadPossibleFloat = isNumberFloat(aux.getVelocidadViento());
					String velocidadVientoTemp = "";
					
					//FUERZA DEL VIENTO EN TIERRA TODO
					/*Símbolo 	km/h 	Nombre 	Efectos del viento
						0.0-1.0 	Calma 	
						1.1-5.5 	Ventolina 	
						5.6-11 	Brisa muy débil 	
						12-19 	Brisa débil 	
						20-28 	Brisa moderada 
						29-38 	Brisa fresca 	
						39–49 	Brisa fuerte 	
						50–61 	Viento fuerte 	
						62-74 	Viento duro 	
						75-88 	Viento muy duro 	
						89-102 	Temporal 	
						103-117 	Temporal muy duro 	
						118- 	Huracán 	
						*/
					/*OTRA ADJETIVACION
					 * Calma: velocidad media menor o igual a 5 Km./h.   
					 * Flojos: velocidad media entre 6 y 20 Km./h.   
					 * Moderados: velocidad media entre 21 y 40 Km./h.   
					 * Fuertes: velocidad media entre 41 y 70 Km./h.   
					 * Muy fuertes: velocidad media entre 71 y 120 Km./h.   
					 * Huracanados: velocidad media mayor que 120 Km./h. 
					 * */
					if (isVelocidadPossibleFloat) {
						float velocidadVientoFloat = (float)(Integer.parseInt(aux.getVelocidadViento())*3.6);
						velocidadVientoTemp = String.valueOf(velocidadVientoFloat);
					}
					else
					{
						velocidadVientoTemp = "N/A";
					}
	
					final String velocidadVientoFinal = velocidadVientoTemp;
					String direccionViento = aux.getDireccionVientoGrados();	
					String soplaViento = "calma";
					float direccionVientoInt = -1;								//USAR ESTA VARIABLE SOLO SI SU VALOR NO ES -1;
					if (isNumberInt(direccionViento)) {
						
						/*
						 * N: dirección entre 337.5 y 22.5 °.   
						 * NE: dirección entre 22.5 y 67.5 °.   
						 * E: dirección entre 67.5 y 112.5 °.   
						 * SE: dirección entre 112.5 y 157.5 °.   
						 * S: dirección entre 157.5 y 202.5 °.   
						 * SW: dirección entre 202.5 y 247.5 °.   
						 * W: dirección entre 247.5 y 292.5 °.   
						 * NW: dirección entre 292.5 y 337.5 °.*/  
						direccionVientoInt = (float)Float.parseFloat(direccionViento)*10;					
						if (direccionVientoInt>=337.5 || direccionVientoInt<22.5) {
							soplaViento = "N";
						}
						else if (direccionVientoInt>=22.5 && direccionVientoInt<67.5) {
							soplaViento = "NE";
						}
						else if (direccionVientoInt>=67.5 && direccionVientoInt<112.5) {
							soplaViento = "E";
						}
						else if (direccionVientoInt>=112.5 && direccionVientoInt<157.5) {
							soplaViento = "SE";
						}
						else if (direccionVientoInt>=157.5 && direccionVientoInt<202.5) {
							soplaViento = "S";
						}
						else if (direccionVientoInt>=202.5 && direccionVientoInt<247.5) {
							soplaViento = "SW";
						}
						else if (direccionVientoInt>=247.5 && direccionVientoInt<292.5) {
							soplaViento = "W";
						}
						else if (direccionVientoInt>=292.5 && direccionVientoInt<337.5) {
							soplaViento = "NW";
						}
					}	
					if (direccionVientoInt!=-1) {
						direccionViento = String.valueOf(direccionVientoInt);
					}
					else
					{
						if (direccionViento.length()>0 && direccionViento.startsWith("0")) {
							direccionViento.substring(1);
						}
						else
						{
							direccionViento = "N/A";
							soplaViento = "N/A"; 
						}
					}
					final String direccionVientoLabelText = direccionViento;
					final String orientacionSoplaViento = soplaViento;
					String auxLluviasEn3 = "";
					String auxLluviasEn24 = "";
					if (!aux.getLluviasen3().equals("") && !aux.getLluviasen3().equals("/")) {
						auxLluviasEn3 = aux.getLluviasen3();
					}
					else
					{
						auxLluviasEn3 = "-";
					}
					if (!aux.getLluviaen24().equals("") && !aux.getLluviaen24().equals("/")) {
						auxLluviasEn24 = aux.getLluviaen24()+"mm";
					}
					else
					{
						auxLluviasEn24 = "-";
					}
					final String lluviasEn3 = auxLluviasEn3;
					final String lluviasEn24 = auxLluviasEn24;
					String presionMinimaTemp = aux.getPresionMinima();
					if (presionMinimaTemp.length()==0) {
						presionMinimaTemp = "N/A";
					}
					final String presionMinima = presionMinimaTemp;
					FCLabelField currentStation = new FCLabelField(estacionText, LabelField.FIELD_VCENTER|LabelField.FIELD_LEFT|LabelField.FOCUSABLE)
					{
						protected boolean navigationClick(int status, int time) {
							PopupScreen tcpScreen = new PopupScreen(new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL));				
							ButtonField bfOK = new ButtonField("OK", Field.USE_ALL_WIDTH|Field.FIELD_HCENTER){
								protected boolean navigationClick(int status, int time) {
									UiApplication.getUiApplication().popScreen(getScreen());
									return true;
								}					
							};	
							FCLabelField lbEstacion = new FCLabelField("Estación: "+estacion,LabelField.FIELD_LEFT);
							FCLabelField lbTActual = new FCLabelField("Temp. Actual: "+temperaturaActual+s,LabelField.FIELD_LEFT);
							FCLabelField lbTMax = new FCLabelField("Temp. Min/Máx: "+temperaturaMinima+s+"/"+temperaturaMaxima+s,LabelField.FIELD_LEFT);
							FCLabelField lbVelocidadViento = new FCLabelField("Viento: "+velocidadVientoFinal+" km/h "+direccionVientoLabelText+"º "+orientacionSoplaViento,LabelField.FIELD_LEFT);
							FCLabelField lbLluviasEn3 = new FCLabelField("Lluvias en 3h: "+lluviasEn3,LabelField.FIELD_LEFT);
							FCLabelField lbLluviasEn24 = new FCLabelField("Lluvias en 24h: "+lluviasEn24,LabelField.FIELD_LEFT);						
							FCLabelField lbHumedadRelativa = new FCLabelField("Humedad Relativa: ?",LabelField.FIELD_LEFT);
							FCLabelField lbPresionMinima = new FCLabelField("Presión Mínima: "+presionMinima+" hPa",LabelField.FIELD_LEFT);
							FCLabelField lbEstadoActual = new FCLabelField("Estado: "+displayEstadoActual+" ",LabelField.FIELD_LEFT);
	
							Bitmap currImage = Bitmap.getBitmapResource(estadoActualIcon); 
							BitmapField currIcon = new BitmapField(currImage,BitmapField.FIELD_RIGHT|BitmapField.USE_ALL_WIDTH);
	
							HorizontalFieldManager hfm_currIcon = new HorizontalFieldManager(Manager.FIELD_VCENTER);
							hfm_currIcon.setPadding(0, 0, 0, 0);
							hfm_currIcon.add(lbEstadoActual);
							hfm_currIcon.add(currIcon);
	
							tcpScreen.add(lbEstacion);						
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbTActual);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbTMax);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbVelocidadViento);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbLluviasEn3);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbLluviasEn24);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbHumedadRelativa);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(lbPresionMinima);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(hfm_currIcon);
							tcpScreen.add(new SeparatorField());
							tcpScreen.add(new NullField(Field.FOCUSABLE));
							tcpScreen.add(bfOK);
	
							UiApplication.getUiApplication().pushScreen(tcpScreen);
							return true;
						}
					};
					currentStation.setFont(fuenteLocalidades);
					currentStation.setFontColor(Color.WHITE);
					currentStation.setPadding(2, 0, 0, 5);
	
					localityReportStation.add(currentStation);
					localityReportStation.add(temperaturasDerecha);
					vManager.add(localityReportStation);
				}
			}			
		}
		else
		{
			Dialog.alert("No se pueden mostrar las localidades.\nLos datos no están disponibles.");
		}
	}

	private boolean isNumberInt(String value)
	{
		char[] valArray = value.toCharArray();
		if (value.length()>0) {
			for (int i = 0; i < valArray.length; i++) {
				if (valArray[i]!='0' && valArray[i]!='1' && valArray[i]!='2' && valArray[i]!='3' && valArray[i]!='4' && valArray[i]!='5' && valArray[i]!='6' && valArray[i]!='7' && valArray[i]!='8' && valArray[i]!='9' && valArray[i]!='-') {
					return false;
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean isNumberFloat(String value)
	{
		char[] valArray = value.toCharArray();
		if (value.length()>0) {
			for (int i = 0; i < valArray.length; i++) {
				if (valArray[i]!='0' && valArray[i]!='1' && valArray[i]!='2' && valArray[i]!='3' && valArray[i]!='4' && valArray[i]!='5' && valArray[i]!='6' && valArray[i]!='7' && valArray[i]!='8' && valArray[i]!='9' && valArray[i]!='-' && valArray[i]!='.') {
					return false;
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public int cambiarMetricaSingle(String temp)
	{
		if(isNumberFloat(temp))
		{
			float a = 0;
			try {
				a = Float.parseFloat(temp);
				int Far = (int) ((a/0.55)+32);
				return Far;
			} catch (Exception e) {
				return -1000;
			}
		}
		else
		{
			return -1000;
		}
	}

	public void changeCity(String ciudad) {
		if (ciudad.equals("")) {
			changeCityDO();
		}
		else
		{
			try
			{
				city = ciudad;	
				synchronized(store) {
					store.setContents(city); 
					store.commit();
				}
				displayForecast();
			}
			catch(Exception e)
			{
				System.out.println("Button pressed: ERROR EXECUTING ACTION");
			}
		}
	}

	private void changeCityDO() {
		city = null;
		vManager.deleteAll();
		delete(vManager);

		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);

		add(vManager);
		//setTitle(appname);                       

		if(city == null)
		{            
			selectLocationField = new ObjectChoiceField("Tu Ciudad:", ciudadTitulo); 
			setLocationButton = new ButtonField("¡Obtener pronóstico!", ButtonField.CONSUME_CLICK);    

			FieldChangeListener listener = new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					ButtonField buttonField = (ButtonField) field;
					if (buttonField.getLabel()=="¡Obtener pronóstico!") {
						System.out.println("Button pressed: " + buttonField.getLabel());
						try
						{
							city = ciudadTitulo[selectLocationField.getSelectedIndex()];

							synchronized(store) {
								store.setContents(city); 
								store.commit();
							}
							displayForecast(); 
						}
						catch(Exception e)
						{
							System.out.println("Button pressed: ERROR EXECUTING ACTION");
						}
					}
					else
					{
						System.out.println("Button pressed: " + buttonField.getLabel());
					}	                    
				}

			};
			setLocationButton.setChangeListener(listener);

			vManager.add(selectLocationField);
			vManager.add(setLocationButton); 
		}
		else {
			displayForecast();
		}
	}
	private MenuItem _changeMetrics = new MenuItem("Cambiar métrica", 100, 200)
	{

		public void run()
		{
			s = null;
			vManager.deleteAll();
			delete(vManager);

			vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);

			add(vManager);
			//setTitle(appname);                       

			if(s == null)
			{            
				selectUnitField = new ObjectChoiceField("Unidad de medida:", unidades); 
				setUnitButton = new ButtonField("¡Establecer!", ButtonField.CONSUME_CLICK);    

				FieldChangeListener listener = new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						ButtonField buttonField = (ButtonField) field;
						if (buttonField.getLabel()=="¡Establecer!") {
							System.out.println("Button pressed: " + buttonField.getLabel());
							try
							{
								s = unidades[selectUnitField.getSelectedIndex()];						
								displayForecast();   
							}
							catch(Exception e)
							{
								System.out.println("Button pressed: ERROR EXECUTING ACTION");
							}
						}
						else
						{
							System.out.println("Button pressed: " + buttonField.getLabel());
						}	                    
					}

				};
				setUnitButton.setChangeListener(listener);

				vManager.add(selectUnitField);
				vManager.add(setUnitButton); 
			}
			else { 
				displayForecast();
			}
		}
	};

	public boolean onSavePrompt()
	{
		return true;
	} 

	public Document getDocumentByString(String inputString)
	{
		try 
		{
			// Build a document based on the XML file.
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			String test = inputString;
			test = this.replaceAll(test, "\t", "");
			test = this.replaceAll(test, "\n", "");
			test = this.replaceAll(test, "\r", "");
			test.trim();
			//REVISAR EL this.doc a ver si es un error.
			String error = CleanERRORConnection(test);
			if (error!=null) {
				Dialog.alert(error+".\nEl pronostico no se ha descargado.");
				System.exit(0);
				return null;
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
			Document doc = builder.parse(bis);
			return doc;
		}
		catch (Exception e) {
			return null;
		}
	}    

	private void displayForecast()
	{
		vManager.deleteAll();
		delete(vManager);

		vManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL|Manager.VERTICAL_SCROLLBAR|Field.USE_ALL_WIDTH);

		add(vManager);
		// ====== start draw table ================== 
		try {
			Ciudad miciudad = null;
			for (int i = 0; i < ciudades.capacity(); i++) {
				miciudad = (Ciudad)ciudades.elementAt(i);
				if (city.equals(miciudad.getTitulo())) {
					break;
				}
			}
			if (miciudad!=null) {
				//TOMAR DATOS
				String temperaturaActual = null;
				temperaturaActual = miciudad.getTactual();
				if (temperaturaActual==null) {
					temperaturaActual = new String();
					temperaturaActual = "?"+s;
				}
				String ciudadActual = city;		            
				String fechaActual = this.fechaActual;
				//Cuarto Creciente / Luna Nueva / "moon.png"
				String estadoActualMoon = getIconForMoonState("moon.png"); //TODO OBTENER ESTADO ACTUAL(en el momento, no el pronostico) DE LA CIUDAD y ponerlo en el argumento.
				//Tormentas / "cloud.png"
				String estadoActual = getIconForCurrentState("cloud.png"); //TODO OBTENER ESTADO ACTUAL(en el momento, no el pronostico) DE LA CIUDAD y ponerlo en el argumento.
				String Dia1fecha = objDate.getNextDateString(1);
				String Dia2fecha = objDate.getNextDateString(2);
				String Dia3fecha = objDate.getNextDateString(3);
				String Dia4fecha = objDate.getNextDateString(4);
				String Dia5fecha = objDate.getNextDateString(5);
				String Dia1temperatura = "";
				String Dia2temperatura = "";
				String Dia3temperatura = "";
				String Dia4temperatura = "";
				String Dia5temperatura = "";
				if (s.equals("ºC")) {
					Dia1temperatura = miciudad.getTmin().elementAt(0)+s+"/"+miciudad.getTmax().elementAt(0)+s;
					Dia2temperatura = miciudad.getTmin().elementAt(1)+s+"/"+miciudad.getTmax().elementAt(1)+s;
					Dia3temperatura = miciudad.getTmin().elementAt(2)+s+"/"+miciudad.getTmax().elementAt(2)+s;
					Dia4temperatura = miciudad.getTmin().elementAt(3)+s+"/"+miciudad.getTmax().elementAt(3)+s;
					Dia5temperatura = miciudad.getTmin().elementAt(4)+s+"/"+miciudad.getTmax().elementAt(4)+s;
				}
				else
				{
					Dia1temperatura = miciudad.getTminF().elementAt(0)+s+"/"+miciudad.getTmaxF().elementAt(0)+s;
					Dia2temperatura = miciudad.getTminF().elementAt(1)+s+"/"+miciudad.getTmaxF().elementAt(1)+s;
					Dia3temperatura = miciudad.getTminF().elementAt(2)+s+"/"+miciudad.getTmaxF().elementAt(2)+s;
					Dia4temperatura = miciudad.getTminF().elementAt(3)+s+"/"+miciudad.getTmaxF().elementAt(3)+s;
					Dia5temperatura = miciudad.getTminF().elementAt(4)+s+"/"+miciudad.getTmaxF().elementAt(4)+s;
				}


				String Dia1estado = getIconForState(miciudad.getEstado().elementAt(0).toString());  
				String Dia2estado = getIconForState(miciudad.getEstado().elementAt(1).toString());	            
				String Dia3estado = getIconForState(miciudad.getEstado().elementAt(2).toString());	            
				String Dia4estado = getIconForState(miciudad.getEstado().elementAt(3).toString());	            
				String Dia5estado = getIconForState(miciudad.getEstado().elementAt(4).toString());
				//FIN TOMAR DATOS

				//IMAGEN DE LA FASE DE LA LUNA ACTUAL
				//TODO CALCULAR O INCLUIR LOS ECLIPSES DE LUNA
				Bitmap moonPhaseImage = Bitmap.getBitmapResource(estadoActualMoon); 
				BitmapField moonPhaseIcon = new BitmapField(moonPhaseImage,BitmapField.FIELD_HCENTER);
				
				HorizontalFieldManager hfm_moonIcon = new HorizontalFieldManager(Manager.FIELD_RIGHT);
				hfm_moonIcon.setPadding(10, 10, 0, 0);
				hfm_moonIcon.add(moonPhaseIcon);
				vManager.add(hfm_moonIcon);
				
				//IMAGEN DEL ESTADO ACTUAL (Lluvia,Sol,Luna,Nublado,ETC)
				Bitmap currImage = Bitmap.getBitmapResource(estadoActual); 
				BitmapField currIcon = new BitmapField(currImage,BitmapField.FIELD_HCENTER);
				
				
				HorizontalFieldManager hfm_currIcon = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				hfm_currIcon.setPadding(0, 0, 0, 0);
				hfm_currIcon.add(currIcon);
				//hfm.add(new LabelField("My Label"));
				vManager.add(hfm_currIcon);
				//int cloudsize = 102;
				//int sidesizes = (320-cloudsize)/2;
				//currIcon.setPadding(10, 0, 0, sidesizes);
				//vManager.add(currIcon);
				//FUENTE IZQUIERDA
				FontFamily fontFamily = FontFamily.forName("BBGlobal Sans");
				Font font = fontFamily.getFont(Font.ENGRAVED_EFFECT, 35);
				//FUENTE IZQUIERDA2
				//FontFamily fontFamilyDos = FontFamily.forName("BBGlobal Sans");
				//Font fontDos = fontFamilyDos.getFont(Font.ENGRAVED_EFFECT, 30); 
				//FUENTE DERECHA
				FontFamily fontFamilyCity = FontFamily.forName("BBGlobal Sans");
				Font fontTres = fontFamilyCity.getFont(Font.BOLD, 18);
				//FUENTE DERECHA2
				FontFamily fontFamilyDate= FontFamily.forName("BBGlobal Sans");
				Font fontQatro = fontFamilyDate.getFont(Font.ENGRAVED_EFFECT, 13);
				//FUENTE PRONOSTICO
				FontFamily fontFamilyDatePron= FontFamily.forName("BBGlobal Sans");
				Font fontCinco = fontFamilyDatePron.getFont(Font.ENGRAVED_EFFECT, 17);

				//BLOQUE 1
				if (!s.equals("ºC")) {
					temperaturaActual = String.valueOf(cambiarMetricaSingle(temperaturaActual));
				}
				FCLabelField currentTemp = new FCLabelField(temperaturaActual+s, LabelField.FIELD_LEFT);	
				Bitmap separator = Bitmap.getBitmapResource("separator.png");
				BitmapField separatorIcon = new BitmapField(separator,BitmapField.FIELD_HCENTER|Field.FOCUSABLE);		            
				//new NullField(Field.FOCUSABLE)
				Bitmap currPos = Bitmap.getBitmapResource("pin.png"); 
				BitmapField currPosIcon = new BitmapField(currPos,BitmapField.FIELD_HCENTER|Field.FOCUSABLE)
				{
					protected boolean navigationClick(int status, int time) 
					{
						//DO SOMETHING
						changeCityEXTRA();
						return true;
					}
				};
				FCLabelField currentCity = new FCLabelField(ciudadActual, LabelField.FIELD_LEFT);
				FCLabelField currentDate = new FCLabelField(fechaActual, LabelField.FIELD_LEFT);
				currentTemp.setFontColor(Color.WHITE);	            
				currentTemp.setFont(font);
				currentTemp.setPadding(5, 0, 0, 0);
				separatorIcon.setPadding(5, 5, 0, 5);
				currPosIcon.setPadding(4, 0, 0, 0);
				currentCity.setFontColor(Color.WHITE);
				currentCity.setFont(fontTres);
				currentCity.setPadding(0, 0, 0, 1);
				currentDate.setFontColor(Color.WHITE);
				currentDate.setFont(fontQatro);
				currentDate.setPadding(0, 0, 0, 5);

				//BLOQUE 2
				//SEPARADOR 1
				Bitmap separatorH = Bitmap.getBitmapResource("separatorH.png");
				BitmapField separatorHIcon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorHIcon.setPadding(5, 0, 8, 0);
				//SEPARADOR 2
				BitmapField separatorH2Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorH2Icon.setPadding(5, 0, 8, 0);
				//SEPARADOR 3
				BitmapField separatorH3Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorH3Icon.setPadding(5, 0, 8, 0);
				//SEPARADOR 4
				BitmapField separatorH4Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorH4Icon.setPadding(5, 0, 8, 0);
				//SEPARADOR 5
				BitmapField separatorH5Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorH5Icon.setPadding(5, 0, 8, 0);
				//SEPARADOR 6 LAST
				BitmapField separatorH6Icon = new BitmapField(separatorH,BitmapField.FIELD_HCENTER);
				separatorH6Icon.setPadding(5, 0, 8, 0);

				//DIAS
				int topPadding = 4;
				//PRONOSTICO DIA 1
				FCLabelField DayOne = new FCLabelField(Dia1fecha, LabelField.FIELD_LEFT);
				FCLabelField DayOneTemp = new FCLabelField(Dia1temperatura, LabelField.FIELD_LEFT|Field.FOCUSABLE);
				Bitmap DayOneEstatus =  Bitmap.getBitmapResource(Dia1estado);
				BitmapField DayOneEstatusIcon = new BitmapField(DayOneEstatus,BitmapField.FIELD_HCENTER);
				DayOne.setFontColor(Color.WHITE);
				DayOne.setPadding(topPadding, 25, 0, 5);
				DayOne.setFont(fontCinco);
				DayOneTemp.setFontColor(Color.WHITE);
				DayOneTemp.setPadding(topPadding, 5, 0, 5);
				DayOneTemp.setFont(fontCinco);
				//PRONOSTICO DIA 2
				FCLabelField DayTwo = new FCLabelField(Dia2fecha, LabelField.FIELD_LEFT);
				FCLabelField DayTwoTemp = new FCLabelField(Dia2temperatura, LabelField.FIELD_LEFT|Field.FOCUSABLE);
				Bitmap DayTwoEstatus = Bitmap.getBitmapResource(Dia2estado);
				BitmapField DayTwoEstatusIcon = new BitmapField(DayTwoEstatus,BitmapField.FIELD_HCENTER);
				DayTwo.setFontColor(Color.WHITE);
				DayTwo.setPadding(topPadding, 25, 0, 5);
				DayTwo.setFont(fontCinco);
				DayTwoTemp.setFontColor(Color.WHITE);
				DayTwoTemp.setPadding(topPadding, 5, 0, 5);
				DayTwoTemp.setFont(fontCinco);
				//PRONOSTICO DIA 3
				FCLabelField DayThree = new FCLabelField(Dia3fecha, LabelField.FIELD_LEFT);
				FCLabelField DayThreeTemp = new FCLabelField(Dia3temperatura, LabelField.FIELD_LEFT|Field.FOCUSABLE);
				Bitmap DayThreeEstatus = Bitmap.getBitmapResource(Dia3estado);
				BitmapField DayThreeEstatusIcon = new BitmapField(DayThreeEstatus,BitmapField.FIELD_HCENTER);
				DayThree.setFontColor(Color.WHITE);
				DayThree.setPadding(topPadding, 25, 0, 5);
				DayThree.setFont(fontCinco);
				DayThreeTemp.setFontColor(Color.WHITE);
				DayThreeTemp.setPadding(topPadding, 5, 0, 5);
				DayThreeTemp.setFont(fontCinco);
				//PRONOSTICO DIA 4
				FCLabelField DayFour = new FCLabelField(Dia4fecha, LabelField.FIELD_LEFT);
				FCLabelField DayFourTemp = new FCLabelField(Dia4temperatura, LabelField.FIELD_LEFT|Field.FOCUSABLE);
				Bitmap DayFourEstatus = Bitmap.getBitmapResource(Dia4estado);
				BitmapField DayFourEstatusIcon = new BitmapField(DayFourEstatus,BitmapField.FIELD_HCENTER);
				DayFour.setFontColor(Color.WHITE);
				DayFour.setPadding(topPadding, 25, 0, 5);
				DayFour.setFont(fontCinco);
				DayFourTemp.setFontColor(Color.WHITE);
				DayFourTemp.setPadding(topPadding, 5, 0, 5);
				DayFourTemp.setFont(fontCinco);
				//PRONOSTICO DIA 5
				FCLabelField DayFive = new FCLabelField(Dia5fecha, LabelField.FIELD_LEFT);
				FCLabelField DayFiveTemp = new FCLabelField(Dia5temperatura, LabelField.FIELD_LEFT|Field.FOCUSABLE);
				Bitmap DayFiveEstatus = Bitmap.getBitmapResource(Dia5estado);
				BitmapField DayFiveEstatusIcon = new BitmapField(DayFiveEstatus,BitmapField.FIELD_HCENTER);
				DayFive.setFontColor(Color.WHITE);
				DayFive.setPadding(topPadding, 25, 0, 5);
				DayFive.setFont(fontCinco);
				DayFiveTemp.setFontColor(Color.WHITE);
				DayFiveTemp.setPadding(topPadding, 5, 0, 5);
				DayFiveTemp.setFont(fontCinco);		            

				VerticalFieldManager vtcTop = new VerticalFieldManager(Manager.FIELD_VCENTER|Field.FOCUSABLE);		            
				vtcTop.add(currentCity);
				vtcTop.add(currentDate);
				HorizontalFieldManager hfm_currData = new HorizontalFieldManager(Manager.FIELD_HCENTER|Field.FOCUSABLE);
				hfm_currData.setPadding(5, 0, 5, 0);
				hfm_currData.add(currentTemp);
				hfm_currData.add(separatorIcon);
				hfm_currData.add(currPosIcon);
				hfm_currData.add(vtcTop);
				vManager.add(hfm_currData);
				vManager.add(separatorHIcon);
				HorizontalFieldManager hfm_OneDayData = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				HorizontalFieldManager hfm_TwoDaysData = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				HorizontalFieldManager hfm_ThreeDaysData = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				HorizontalFieldManager hfm_FourDaysData = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				HorizontalFieldManager hfm_FiveDaysData = new HorizontalFieldManager(Manager.FIELD_HCENTER);
				hfm_OneDayData.add(DayOne);
				hfm_OneDayData.add(DayOneTemp);
				hfm_OneDayData.add(DayOneEstatusIcon);       
				hfm_TwoDaysData.add(DayTwo);
				hfm_TwoDaysData.add(DayTwoTemp);
				hfm_TwoDaysData.add(DayTwoEstatusIcon);
				hfm_ThreeDaysData.add(DayThree);
				hfm_ThreeDaysData.add(DayThreeTemp);
				hfm_ThreeDaysData.add(DayThreeEstatusIcon);
				hfm_FourDaysData.add(DayFour);
				hfm_FourDaysData.add(DayFourTemp);
				hfm_FourDaysData.add(DayFourEstatusIcon);
				hfm_FiveDaysData.add(DayFive);
				hfm_FiveDaysData.add(DayFiveTemp);
				hfm_FiveDaysData.add(DayFiveEstatusIcon);

				vManager.add(hfm_OneDayData);
				vManager.add(separatorH2Icon);
				vManager.add(hfm_TwoDaysData);
				vManager.add(separatorH3Icon);
				vManager.add(hfm_ThreeDaysData);
				vManager.add(separatorH4Icon);
				vManager.add(hfm_FourDaysData);
				vManager.add(separatorH5Icon);
				vManager.add(hfm_FiveDaysData);
				vManager.add(separatorH6Icon);
			}
			else
			{
				System.out.println("Ciudad no encontrada. Se ha devuelto null.");
			}
		} catch (Exception e) {
			throw new RuntimeException();
		}


	}

	//ICONO PARA EL ESTADO ACTUAL (GRANDES)
	private String getIconForCurrentState(String estado) {
		String icon = "icon_unknown.png";
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		DateTimeUtilities.formatElapsedTime(now.getTime()/1000, sb, true);
		Vector splitTime = CustomStrings.split(sb.toString(), ':');
		int hora = Integer.parseInt(splitTime.elementAt(1).toString());
		System.out.println(estado);
		if (hora<6 || hora>19) {
			//ES DE NOCHE
			//icon = "icon_sunny_night.png";
			if (estado.equalsIgnoreCase("Lluvias aisladas")) {
				icon = "icon_lightrain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias dispersas")) {
				icon = "icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias Ocasionales")) {
				icon = "icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Chubascos")) {
				icon = "icon_heavyrain.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas en la Tarde")) {
				icon = "icon_thundershower_night.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias en la Tarde")) {
				icon = "icon_shower_night.png";
			}
			else if (estado.equalsIgnoreCase("Parcialmente Nublado")) {
				icon = "icon_cloudy_night.png";
			}
			else if (estado.equalsIgnoreCase("Aisladas Tormentas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas dispersas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Isolated storms")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Soleado")) {
				icon = "icon_sunny_night.png";
			}
		}
		else
		{
			//ES DE DIA
			//icon = "icon_sunny.png";
			if (estado.equalsIgnoreCase("Lluvias aisladas")) {
				icon = "icon_lightrain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias dispersas")) {
				icon = "icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias Ocasionales")) {
				icon = "icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Chubascos")) {
				icon = "icon_heavyrain.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas en la Tarde")) {
				icon = "icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias en la Tarde")) {
				icon = "icon_shower.png";
			}
			else if (estado.equalsIgnoreCase("Parcialmente Nublado")) {
				icon = "icon_cloudy.png";
			}
			else if (estado.equalsIgnoreCase("Aisladas Tormentas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas dispersas")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Isolated storms")) {
				icon = "icon_storm.png";
			}
			else if (estado.equalsIgnoreCase("Soleado")) {
				icon = "icon_sunny.png";
			}
		}   	


		/*    
		 * 
			Lluvias aisladas	 	icon_lightrain.png
			Lluvias dispersas   	icon_moderaterain.png
			Lluvias Ocasionales		icon_moderaterain.png
			Chubascos			 	icon_heavyrain.png    	 
			Tormentas			 	icon_storm.png
			Tormentas en la Tarde   icon_thundershower.png / icon_thundershower_night.png	
			Lluvias en la Tarde		icon_shower.png / icon_shower_night.png
			Parcialmente Nublado	icon_cloudy.png / icon_cloudy_night.png
			Aisladas Tormentas		icon_storm.png
			Tormentas dispersas		icon_storm.png
			Isolated storms			icon_storm.png			
		 */

		return icon;
	}

	//ICONO PARA EL PRONOSTICO (PEQUEÑOS)
	private String getIconForState(String estado) {
		String icon = "mini_icon_unknown.png";
		Date now = new Date();
		StringBuffer sb = new StringBuffer();
		DateTimeUtilities.formatElapsedTime(now.getTime()/1000, sb, true);
		Vector splitTime = CustomStrings.split(sb.toString(), ':');
		int hora = Integer.parseInt(splitTime.elementAt(1).toString());

		if (hora<6 || hora>19) {
			//ES DE NOCHE
			//icon = "mini_icon_sunny_night.png";
			if (estado.equalsIgnoreCase("Lluvias aisladas")) {
				icon = "mini_icon_lightrain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias dispersas")) {
				icon = "mini_icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias Ocasionales")) {
				icon = "mini_icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Chubascos")) {
				icon = "mini_icon_drizzle.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas en la Tarde")) {
				icon = "mini_icon_thundershower_night.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias en la Tarde")) {
				icon = "mini_icon_shower_night.png";
			}
			else if (estado.equalsIgnoreCase("Parcialmente Nublado")) {
				icon = "mini_icon_cloudy_night.png";
			}
			else if (estado.equalsIgnoreCase("Aisladas Tormentas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas dispersas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Isolated storms")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Soleado")) {
				icon = "mini_icon_sunny_night.png";
			}
		}
		else
		{
			//ES DE DIA
			//icon = "mini_icon_sunny.png";
			if (estado.equalsIgnoreCase("Lluvias aisladas")) {
				icon = "mini_icon_lightrain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias dispersas")) {
				icon = "mini_icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias Ocasionales")) {
				icon = "mini_icon_moderaterain.png";
			}
			else if (estado.equalsIgnoreCase("Chubascos")) {
				icon = "mini_icon_drizzle.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas en la Tarde")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Lluvias en la Tarde")) {
				icon = "mini_icon_shower.png";
			}
			else if (estado.equalsIgnoreCase("Parcialmente Nublado")) {
				icon = "mini_icon_cloudy.png";
			}
			else if (estado.equalsIgnoreCase("Aisladas Tormentas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Tormentas dispersas")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Isolated storms")) {
				icon = "mini_icon_thundershower.png";
			}
			else if (estado.equalsIgnoreCase("Soleado")) {
				icon = "mini_icon_sunny.png";
			}
		}  	


		/*    
		 * 
			Lluvias aisladas	 	icon_lightrain.png
			Lluvias dispersas   	icon_moderaterain.png
			Lluvias Ocasionales		icon_moderaterain.png
			Chubascos			 	icon_heavyrain.png    	 
			Tormentas			 	icon_storm.png
			Tormentas en la Tarde   icon_thundershower.png / icon_thundershower_night.png	
			Lluvias en la Tarde		icon_shower.png / icon_shower_night.png
			Parcialmente Nublado	icon_cloudy.png / icon_cloudy_night.png
			Aisladas Tormentas		icon_storm.png
			Tormentas dispersas		icon_storm.png
			Isolated storms			icon_storm.png
		 */

		return icon;
	}

	//ICONO PARA EL PRONOSTICO (PEQUEÑOS)
		private String getIconForMoonState(String fase) {
			String icon = "mini_icon_sunny_night.png";
			//TODO
			//CALCULAR CICLO LUNAR Y ECLIPSES SI ES POSIBLE
			
			/*
			 * la Tierra completa un giro cada día (la dirección de giro es también hacia el este). 
			 * Así, cada día le lleva a la Tierra alrededor de 50 minutos más para estar de frente con la Luna nuevamente 
			 * (lo cual significa que se puede ver la Luna en el cielo). El giro de la Tierra y el movimiento orbital de la Luna se combinan, 
			 * de tal forma que la salida de la Luna se retrasa del orden de 50 minutos cada día.
			 * Teniendo en cuenta que la Luna tarda aproximadamente 28 días en completar su órbita alrededor de la Tierra, 
			 * y ésta tarda 24 horas en completar una revolución alrededor de su eje, es sencillo calcular el "retraso" diario de la Luna:
			 * Mientras que en 24 horas la Tierra habrá realizado una revolución completa, la Luna sólo habrá recorrido un 1/28 de su órbita alrededor de la Tierra, 
			 * lo cual expresado en grados de arco da:
			 *  360º / 28dias = 12º51'
			 *  Si ahora se calcula el tiempo que la Tierra en su rotación tarda en recorrer este arco,
			 *  (12º51'/360º) * 24 * 60 = 50,4minutos
			 *  da los aproximadamente 51 minutos que la Luna retrasa su salida cada día.*/
			
			
			//FASE LUNAR
			//icon = "mini_icon_sunny.png";
			if (fase.equalsIgnoreCase("Luna Nueva")) {
				icon = "mini_icon_moonphases_new.png";
			}
			else if (fase.equalsIgnoreCase("Luna Nueva Visible")) {
				icon = "mini_icon_moonphases_waxing_crescent.png";
			}
			else if (fase.equalsIgnoreCase("Cuarto Creciente")) {
				icon = "mini_icon_moonphases_first_quarter.png";
			}
			else if (fase.equalsIgnoreCase("Luna Gibosa Creciente")) {
				icon = "mini_icon_moonphases_waxing_gibbous.png";
			}
			else if (fase.equalsIgnoreCase("Luna Llena")) {
				icon = "mini_icon_moonphases_full.png";
			}
			else if (fase.equalsIgnoreCase("Luna Gibosa Menguante")) {
				icon = "mini_icon_moonphases_waning_gibbous.png";
			}
			else if (fase.equalsIgnoreCase("Cuarto Menguante")) {
				icon = "mini_icon_moonphases_last_quarter.png";
			}
			else if (fase.equalsIgnoreCase("Luna Menguante")) {
				icon = "mini_icon_moonphases_waning_crescent.png";
			}

			/*    
			 * 
			 	Luna Nueva			 	mini_icon_moonphases_new.png
			 	Luna Nueva Visible		mini_icon_moonphases_waxing_crescent.png
				Cuarto Creciente	 	mini_icon_moonphases_first_quarter.png
				Luna Gibosa Creciente	mini_icon_moonphases_waxing_gibbous.png
				Luna Llena			   	mini_icon_moonphases_full.png
				Luna Gibosa Menguante	mini_icon_moonphases_waning_gibbous.png
				Cuarto Menguante	 	mini_icon_moonphases_last_quarter.png 
				Luna Menguante			mini_icon_moonphases_waning_crescent.png
				Luna/Desconocida		mini_icon_sunny_night.png
				
				Luna No. 1: Luna Nueva o Novilunio
				Luna No. 2: Luna Nueva Visible
				Luna No. 3: Cuarto Creciente. 
				Luna No. 4: Luna Gibosa Creciente
				Luna No. 5: Luna Llena o Plenilunio
				Luna No. 6: Luna Gibosa Menguante
				Luna No. 7: Cuarto Menguante
				Luna No. 8: Luna Menguante
				Luna No. 9: Luna Negra
			 */

			return icon;
		}
		
	private void refresh()
	{
		displayForecast();
	}

	public void extractDataFromDescription(Ciudad currentCity) 
	{
		//								0             1             2               3            4           5          6              7
		//estaciones = new String[] {"CIENFUEGOS", "LA HABANA", "PINAR DEL RIO", "VARADERO", "CAYO COCO", "CAMAGÜEY", "HOLGUÍN", "SANTIAGO DE CUBA"};
		//String[] estaciones = new String[] {"78344", "78325","78315","78328","78339","78355","78372","78364"};
		String description = CleanCDATA(currentCity.getDescripcion());
		Vector day = new Vector();
		Vector tmax = new Vector();
		Vector tmin = new Vector();
		Vector estado = new Vector();
		StringBuffer variable = new StringBuffer();
		char[] descriptionArray = description.toCharArray();
		int amount = 1;
		for (int i = 0; i < descriptionArray.length; i++) {    		
			variable = new StringBuffer();
			if (descriptionArray[i]=='t' && descriptionArray[i+1]=='d' && descriptionArray[i+2]=='>') {
				int current = i+3;
				while (descriptionArray[current]!='<') {
					variable.append(descriptionArray[current]);
					current++;
				}
				String value = variable.toString().trim();
				switch (amount) {
				case 1:
					if (value.length()>0 && value!=null) {                                                                                                                                                                                                                                                                                                                                         
						day.addElement(value);
						amount++;
					}
					break;
				case 2:
					if (value.length()>0 && value!=null) {                                                                                                                                                                                                                                                                                                                                         
						tmax.addElement(value);
						amount++;
					}
					break;
				case 3:
					if (value.length()>0 && value!=null) {                                                                                                                                                                                                                                                                                                                                         
						tmin.addElement(value);
						amount++;
					}
					break;
				case 4:
					if (value.length()>0 && value!=null) {                                                                                                                                                                                                                                                                                                                                         
						estado.addElement(value);
						amount=1;
					}
					break;
				default:
					break;
				}
				i = current;
			}
		}
		currentCity.setTmax(tmax);
		currentCity.setTmin(tmin);
		currentCity.setEstado(estado);
		for (int i = 0; i < ciudadTitulo.length; i++) {
			if (currentCity.getTitulo().equals(ciudadTitulo[i])) {
				int pos = 0;
				while (pos < localidadesPrincipales.size()) {
					Localidad aux = (Localidad)localidadesPrincipales.elementAt(pos);
					if (aux.getEstacion().equals(estaciones[i])) {
						currentCity.setTactual(aux.getTactual()); 	//TODO RELLENAR CON OTRAS CARACTERISTICAS CUANDO SE  DECIFREN
						break;
					}
					pos++;
				}
				break;
			}
		}		

		/*
		 * TODO Crear metodos para obtener el codigo postal de la ciudad./
		 */

	}

	private String textStation(int number)
	{
		String local = "";
		switch (number) {
		case 78308: local = "La Piedra";
		break;
		case 78309: local = "Amistad Cuba-Francia";
		break;
		case 78310: local = "Cabo de San Antonio";
		break;
		case 78312: local = "Santa Lucía";
		break;
		case 78313: local = "Isabel Rubio";
		break;
		case 78314: local = "San Juan y Martinez";
		break;
		case 78315: local = "Pinar del Río";
		break;
		case 78316: local = "La Palma";
		break;
		case 78317: local = "Paso Real de San Diego";
		break;
		case 78318: local = "Bahía Honda";
		break;
		case 78319: local = "Valle de Caujerí";
		break;
		case 78320: local = "Güira de Melena";
		break;
		case 78321: local = "Santa Fé";
		break;
		case 78322: local = "Batabanó";
		break;
		case 78323: local = "Güines";
		break;
		case 78324: local = "Punta del Este";
		break;
		case 78325: local = "Casablanca";
		break;
		case 78326: local = "Santo Domingo";
		break;
		case 78327: local = "Unión de Reyes";
		break;
		case 78328: local = "Varadero";
		break;
		case 78329: local = "Indio Hatuey";
		break;
		case 78330: local = "Jovellanos";
		break;
		case 78331: local = "Jagüey Grande";
		break;
		case 78332: local = "Colón";
		break;
		case 78333: local = "Playa Girón";
		break;
		case 78334: local = "Palenque de Yateras";
		break;
		case 78335: local = "Aguada de Pasajeros";
		break;
		case 78337: local = "Trinidad";
		break;
		case 78338: local = "Sagua la Grande";
		break;
		case 78339: local = "Cayo Coco";
		break;
		case 78340: local = "Bainoa";
		break;
		case 78341: local = "El Jíbaro";
		break;
		case 78342: local = "Topes de Collantes";
		break;
		case 78343: local = "El Yabú";
		break;
		case 78344: local = "Cienfuegos";
		break;
		case 78345: local = "Júcaro";
		break;
		case 78346: local = "Venezuela";
		break;
		case 78347: local = "Camilo Cienfuegos";
		break;
		case 78348: local = "Caibarién";
		break;
		case 78349: local = "Sancti Spíritus";
		break;
		case 78350: local = "Florida";
		break;
		case 78351: local = "Santa Cruz del Sur";
		break;
		case 78352: local = "Esmeralda";
		break;
		case 78353: local = "Nuevitas";
		break;
		case 78354: local = "Palo Seco";
		break;
		case 78355: local = "Camagüey";
		break;
		case 78356: local = "Jamal";
		break;
		case 78357: local = "Las Tunas";
		break;
		case 78358: local = "Puerto Padre";
		break;
		case 78359: local = "Manzanillo";
		break;
		case 78360: local = "Cabo Cruz";
		break;
		case 78361: local = "Jucarito";
		break;
		case 78362: local = "La Jíquima";
		break;
		case 78363: local = "Contramaestre";
		break;
		case 78364: local = "Universidad";
		break;
		case 78365: local = "Cabo Lucrecia";
		break;
		case 78366: local = "La Gran Piedra";
		break;
		case 78368: local = "Guantánamo";
		break;
		case 78369: local = "Punta de Maisí";
		break;
		case 78370: local = "Guaro";
		break;
		case 78371: local = "Pinares de Mayarí";
		break;
		case 78372: local = "Pedagógico";
		break;
		case 78373: local = "Santiago de las Vegas";
		break;
		case 78374: local = "Tapaste";
		break;
		case 78375: local = "Melena del Sur";
		break;
		case 78376: local = "Bauta";
		break;
		case 78377: local = "Veguitas";
		break;
		case 78378: local = "Velasco";
		break;
		default:
			local = "";
			break;
		}
		return local;
	}

	public String replaceAll(String source, String pattern, String replacement)
	{    

		//If source is null then Stop
		//and retutn empty String.
		if (source == null)
		{
			return "";
		}

		StringBuffer sb = new StringBuffer();
		//Intialize Index to -1
		//to check agaist it later 
		int idx = 0;
		//Search source from 0 to first occurrence of pattern
		//Set Idx equal to index at which pattern is found.

		String workingSource = source;

		//Iterate for the Pattern till idx is not be -1.
		while ((idx = workingSource.indexOf(pattern, idx)) != -1)
		{
			//append all the string in source till the pattern starts.
			sb.append(workingSource.substring(0, idx));
			//append replacement of the pattern.
			sb.append(replacement);
			//Append remaining string to the String Buffer.
			sb.append(workingSource.substring(idx + pattern.length()));

			//Store the updated String and check again.
			workingSource = sb.toString();

			//Reset the StringBuffer.
			sb.delete(0, sb.length());

			//Move the index ahead.
			idx += replacement.length();
		}

		return workingSource;
	}

	public void setResponseA(String responseA) {
		this.responseA = responseA;
	}

	public void setResponseB(String responseB) {
		this.responseB = responseB;
	}
	
	/**
     * Overrides default.  Enter key will take action on directory/file.
     * Escape key will go up one directory or close application if at top level.
     * 
     * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
     * 
     */
    public boolean keyChar(char c, int status, int time) 
    {
        switch (c) 
        {
            case Characters.ENTER:
                //return selectAction();
                
            case Characters.DELETE:
            
            case Characters.BACKSPACE:
                //deleteAction();
                return true;
                
            case Characters.ESCAPE:
            	refresh();
                    return true;
                
            default:
                return super.keyChar(c, status, time);
        }
    }
}
