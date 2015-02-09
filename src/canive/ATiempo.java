/*
 * XMLDemo.java
 * 
 * A sample application demonstrating how to parse an XML file.
 *
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package canive;

import net.rim.device.api.ui.UiApplication;
import canive.ATiempoPermissions;

/**
 * The main class for the application.
 */
public final class ATiempo extends UiApplication
{
	private ATiempoPermissions permitCheck = new ATiempoPermissions();
	
    /**
     * This constructor simply pushes the main screen onto the display stack.
     */
    public ATiempo() 
    {
    	permitCheck.registerReasonProvider();
    	permitCheck.checkStartupPermissions(true);
    	ATiempoLoading loading = new ATiempoLoading(this);
    	pushScreen(loading);
        //pushScreen( new ATiempoCiudades(this) );
    	//pushScreen( new ATiempoConnect(this) );
    }
    
    
    /**
     * Entry point for the application.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main( String[] args ) 
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        new ATiempo().enterEventDispatcher();
   }
    
     
    
    
}
