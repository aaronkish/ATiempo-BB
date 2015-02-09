/*
 * DemoReasonProvider.java
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

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ReasonProvider;

/**
 * This class implements the ReasonProvider interface in order to provide
 * detailed permission request messages for the user.
 * 
 * To test ReasonProvider functionality, when prompted, set the demo's 
 * application permissions to "prompt" and then run those tests.  When the
 * pop-up asking for permission appears, click "Details from the vendor..."
 * to view your messages.  The messages will only appear when access is 
 * being requested.
 * 
 */
public final class ATiempoReasonProvider implements ReasonProvider 
{    
   /**
    * @see net.rim.device.api.applicationcontrol.ReasonProvider#getMessage(int)
    */
    public String getMessage(int permissionID) 
    {        
        // General message for other permissions
        String message = "ReasonProviderDemo recieved permissionID: " + permissionID;
        
        // Set specific messages for specific permission IDs
        
        switch(permissionID) {
        case ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION:
        	message = "Este permiso es requerido para la aplicacion para acceder a configuraciones y datos de cache."; break;
        case ApplicationPermissions.PERMISSION_FILE_API:
        	message = "Permisos de archivos son requeridos para usar carpetas de correo locales y para cache para contenido de mensajes bajados."; break;
        case ApplicationPermissions.PERMISSION_SERVER_NETWORK:
        	message = "Este permiso es requerido para conectar servidores de correo sobre las redes de dattos BES/MDS."; break;
        case ApplicationPermissions.PERMISSION_INTERNET:
        	message = "Este permiso es requerido para conectar el servidor de correo sobre la conexión de datos movil."; break;
        case ApplicationPermissions.PERMISSION_WIFI:
        	message = "Este permiso es requerido para conectar a servidores de correo sobre redes WiFi."; break;
        case ApplicationPermissions.PERMISSION_ORGANIZER_DATA:
        	message = "Este permiso es requerido para acceder a los contactos al componer un mensaje.";  break; 
        case ApplicationPermissions.PERMISSION_INPUT_SIMULATION:
            message = "Sample message for PERMISSION_INPUT_SIMULATION"; break;                
        case ApplicationPermissions.PERMISSION_PHONE:
            message = "Sample message for PERMISSION_PHONE"; break;        
        case ApplicationPermissions.PERMISSION_DEVICE_SETTINGS:
            message = "Sample message for PERMISSION_DEVICE_SETTINGS"; break;        
        case ApplicationPermissions.PERMISSION_EMAIL:
            message = "Sample message for PERMISSION_EMAIL"; break;
        default:
            return null;
        }
        return message;
    }    
    
}
