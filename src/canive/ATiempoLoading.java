package canive;

import java.util.Timer;
import java.util.TimerTask;

import canive.network.diagnostic.CNXSettings;
import canive.network.diagnostic.IOThread;
import canive.network.diagnostic.InputScreen;


import util.CustomStrings;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

public class ATiempoLoading extends MainScreen {
	private UiApplication application;
	private Timer timer = new Timer();
	private static String CONFIGFILE = "file:///SDCard/net/config.ini";
	private static String http = "http://127.0.0.1/atiempo/genesis.xml";
	private static String http2 = "http://127.0.0.1/atiempo/est.csv";
	private CNXSettings globalSettings;
	private IOThread cnxThread;

	public ATiempoLoading(UiApplication ui) {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		this.application = ui;
		initFields();
		Background bitmapBackground = BackgroundFactory.createBitmapBackground(Bitmap.getBitmapResource("weather_splash.png"), Background.POSITION_X_CENTER, Background.POSITION_Y_CENTER, Background.REPEAT_BOTH);
		getMainManager().setBackground(bitmapBackground);
		addMenuItem(_fullExitItem);
		//LOAD SETTINGS
		globalSettings = null;	
		add(new NullField(Field.FOCUSABLE));
		Bitmap currSettings = Bitmap.getBitmapResource("settings.png"); 
		BitmapField currSettingsIcon = new BitmapField(currSettings,BitmapField.FIELD_RIGHT|BitmapField.USE_ALL_WIDTH|BitmapField.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				timer.cancel();
				InputScreen inputScreen = new InputScreen();
				application.pushScreen(inputScreen);
				return true;
			}
		};
		HorizontalFieldManager hfm_currSettingsIcon = new HorizontalFieldManager(Manager.FIELD_LEFT);
		hfm_currSettingsIcon.setPadding(10, 0, 0, 10);
		hfm_currSettingsIcon.add(currSettingsIcon);
		add(hfm_currSettingsIcon);
		Bitmap currRefreshImage = Bitmap.getBitmapResource("refresh.png"); 
		BitmapField currRefreshIcon = new BitmapField(currRefreshImage,BitmapField.FIELD_RIGHT|BitmapField.USE_ALL_WIDTH|BitmapField.FOCUSABLE)
		{
			protected boolean navigationClick(int status, int time) 
			{
				CNXSettings leido = CustomStrings.readConfig(CONFIGFILE);
				if (leido!=null) {
					globalSettings = leido;	
					globalSettings.setUrl1(http);
					globalSettings.setUrl2(http2);

					cnxThread = new IOThread(globalSettings);
					application.addRadioListener(cnxThread);
					cnxThread.start();
				}
				else
				{
					Dialog.alert("Configuración Vacia.\nConfigurar Ahora!");
					InputScreen inputScreen = new InputScreen();
					application.pushScreen(inputScreen);
				}
				return true;
			}
		};
		HorizontalFieldManager hfm_currRefreshIcon = new HorizontalFieldManager(Manager.FIELD_LEFT);
		hfm_currRefreshIcon.setPadding(2, 0, 0, 10);
		hfm_currRefreshIcon.add(currRefreshIcon);
		add(hfm_currRefreshIcon);		
	}

	private void initFields() {

		timer.schedule(new CountDown(), 3000);        
	}

	public void dismiss() {
		timer.cancel();
		application.popScreen(this);
		CNXSettings leido = CustomStrings.readConfig(CONFIGFILE);
		if (leido!=null) {
			globalSettings = leido;	
			globalSettings.setUrl1(http);
			globalSettings.setUrl2(http2);

			cnxThread = new IOThread(globalSettings);
			application.addRadioListener(cnxThread);
			cnxThread.start();
		}
		else
		{
			Dialog.alert("Configuración Vacia.\nConfigurar Ahora!");
			InputScreen inputScreen = new InputScreen();
			application.pushScreen(inputScreen);
		}
		//ATiempoScreen atiempoScreen = new ATiempoScreen();
		//application.pushScreen(atiempoScreen);
	}
	private class CountDown extends TimerTask {
		public void run() {
			//startBackgroundLoadingProcess();
			DismissThread dThread = new DismissThread();
			application.invokeLater(dThread);
		}
	}
	private class DismissThread implements Runnable {
		public void run() {
			dismiss();
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
}
