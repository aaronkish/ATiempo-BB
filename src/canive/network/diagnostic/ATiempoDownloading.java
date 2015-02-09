package canive.network.diagnostic;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class ATiempoDownloading extends MainScreen {	
	/** A reference to the GaugeField that shows the progress of a single transport test */
	private Field progress;
	/** A Thread which handles updating the value of the GaugeField */
	private Thread tprogress; 
	/** A reference to IOThread instance */
	private IOThread cnxThread;
	/** Fields to show status of the network */
	EditField networkSignalLevel, wlanSignalLevel, networkName, networkType, networkServices, pin, battery, device;
	
	public ATiempoDownloading(IOThread ioThread) {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		this.cnxThread = ioThread;
		
		int width = Display.getWidth();
		int height = Display.getHeight();		
		
		//Color solido de fondo 
		int intValue = Integer.parseInt( "36bbfe",16); 
		getMainManager().setBackground(BackgroundFactory.createSolidBackground(intValue));
		//gubatel_splash-logo
		Bitmap logo = Bitmap.getBitmapResource("downloadB.png"); //OK		
		BitmapField logoIcon = new BitmapField(logo,BitmapField.FIELD_HCENTER);
		int paddingLeft = (width-logo.getWidth())/2;
		int paddingTop = (height-logo.getHeight())/2;
		logoIcon.setPadding(paddingTop, paddingLeft, 0, paddingLeft);
		getMainManager().add(logoIcon);
		
		addMenuItem(_fullExitItem);	
	}
	
	/**
	 * Displays a pass or fail image for a Transport test. A CustomButtonField titled "Details" is 
	 * also shown, clicking on which displays a very detailed report of a transport's test.
	 * @param log	The Log instance for the transport in question
	 */
	public void displayResult(final Log log) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if(tprogress!=null)					
					tprogress.interrupt();				
				if(progress!=null && progress.getScreen()!=null)
					delete(progress);			

				
				int width = Display.getWidth();
				int height = Display.getHeight();	
				Bitmap logo = Bitmap.getBitmapResource("downloadA.png"); //ERROR		
				BitmapField logoIcon = new BitmapField(logo,BitmapField.FIELD_HCENTER);
				int paddingLeft = (width-logo.getWidth())/2;
				int paddingTop = (height-logo.getHeight())/2;
				logoIcon.setPadding(paddingTop, paddingLeft, 0, paddingLeft);
				getMainManager().add(logoIcon);
				HorizontalFieldManager hfm = new HorizontalFieldManager();
				hfm.add(new CustomButtonField("Detalles", DrawStyle.ELLIPSIS,log));			
				//hfm.add(new CustomButtonField("Continuar", DrawStyle.ELLIPSIS,ioThread));
				BitmapField bf = new BitmapField(Bitmap.getBitmapResource(log.isPass() ? "pass.png" : "fail.png"));
				hfm.add(bf);
				hfm.add(new LabelField("\t"+log.getTransport()));				
				add(hfm);				
				add(new SeparatorField());
				hfm.setFocus();
			}
		});
	}

	/**
	 * Displays a GaugeField and keeps incrementing the progress at fixed interval until the test succeeds
	 * or gives up after 'retries' number of failures.
	 * @param transport	Name of the transport test this progress is for
	 */
	public void displayProgress(final String transport) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {				
				GaugeField p = new GaugeField(transport+": ",0,100,0,GaugeField.NO_TEXT); 
				add(p);
				progress = p;				
			}
		});
		tprogress = new Thread() {
			public void run() {
				while (true){
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							if(((GaugeField)progress).getValue()>99)
								((GaugeField)progress).setValue(0);
							((GaugeField)progress).setValue(((GaugeField)progress).getValue()+1);
						}
					});
					try {
						sleep(500);
					} catch (InterruptedException e) {}
				}
			}
		};
		tprogress.start();
	}
	
	/**
	 * Sets the value of the current attempt which is displayed on the Screen.
	 * @param currentAttempt	current attempt
	 * @param retries	number of allowed retries.
	 */
	public void setTrial(final int currentAttempt, final int retries){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {		
				((GaugeField)progress).setLabel(((GaugeField)progress).getLabel().substring(0,((GaugeField)progress).getLabel().indexOf(":")+2)+"Intento "+(currentAttempt+1)+"/"+retries);
			}
		});
	}

	/**
	 * Custom implementation of ButtonField which handles click events for the Details button.
	 * If the user clicks on Details, this will show all the details of a single transport
	 * test on a separate Screen created on the fly.
	 * @author Shadid Haque
	 *
	 */
	private class CustomButtonField extends ButtonField {
		private Log log;
		private IOThread io;		

		private CustomButtonField(String label, long style, IOThread io) {
			super(label, style);
			this.io = io;
		}
		private CustomButtonField(String label, long style, Log log) {
			super(label, style);
			this.log = log;
		}
		private CustomButtonField(String label, long style) {
			super(label, style);			
		}

		private MainScreen buildDetails() {
			MainScreen ms = new MainScreen();
			ms.setTitle("Reporte Detallado: "+log.getTransport());
						
			ms.add(new EditField("Transporte: ", log.getTransport(),255,EditField.READONLY));
			ms.add(new EditField("Resultado: ", log.isPass() ? "Pasó" : "Falló",255,EditField.READONLY));
			ms.add(new EditField("Respuesta: ", "" + log.getResponseCode(),255,EditField.READONLY));
			ms.add(new EditField("Tamaño: ", "" + log.getContentLength(),255,EditField.READONLY));			
			ms.add(new SeparatorField());			
			ms.add(new EditField("URL: ", log.getUrl(),255,EditField.READONLY));
			ms.add(new SeparatorField());
			ms.add(new EditField("Log: ", "\n" + log.getLog()));
			ms.add(new SeparatorField());
			ms.add(new EditField("Contenido: ", "\n" + log.getContent()));
			return ms;
		}

		protected boolean navigationClick(int status, int time) {
			final IOThread sendIO = io;
			if (this.getLabel().equalsIgnoreCase("Detalles")){
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						UiApplication.getUiApplication().pushScreen(buildDetails());
					}
				});
			} 
			else if (this.getLabel().equalsIgnoreCase("Continuar")){
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						InputScreen in = new InputScreen(sendIO);
						UiApplication.getUiApplication().pushScreen(in);
					}
				});
			} 

			return true;
		}
	}
		
	
	private MenuItem _fullExitItem = new MenuItem("Salir Completamente", 100, 200)
	{
		public void run()
		{
			System.exit(0);
			return;
		}
	};
	
	public boolean onClose() {
		cnxThread.stopTest();		
		return super.onClose();
	}
}
