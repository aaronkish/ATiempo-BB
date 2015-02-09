/**
 * 
 */
package canive;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ControlledAccessException;

/**
 * @author Abel
 *
 */
public class ATiempoPermissions {

	private static ATiempoReasonProvider reasonProvider = new ATiempoReasonProvider();
	public void registerReasonProvider() {
        try {
            ApplicationPermissionsManager.getInstance().addReasonProvider(
                    ApplicationDescriptor.currentApplicationDescriptor(),
                    reasonProvider);
        } catch (ControlledAccessException e) {
            // Apparently lack of IPC permissions makes this call non-functional
        }
    }
    
    /*public static void unregisterReasonProvider() {
        try {
            ApplicationPermissionsManager.getInstance().removeReasonProvider(reasonProvider);
        } catch (ControlledAccessException e) {
            // Apparently lack of IPC permissions makes this call non-functional
        }
    }*/
    
    /**
     * Check application startup permissions, prompting if necessary.
     *
     * @param checkExtended true, if extended permissions should be verified
     * @return true, if the application has sufficient permissions to start
     */
    public boolean checkStartupPermissions(boolean checkExtended) {
        ApplicationPermissionsManager permissionsManager = ApplicationPermissionsManager.getInstance();
        ApplicationPermissions originalPermissions = permissionsManager.getApplicationPermissions();
        
        boolean permissionsUsable;
        if(checkExtended) {
            permissionsUsable = hasMinimumPermissions(originalPermissions) && hasExtendedPermissions(originalPermissions);
        }
        else {
            permissionsUsable = hasMinimumPermissions(originalPermissions);
        }
        
        if(permissionsUsable) { return true; }
        
        
        
        // Create a permissions request containing a generous set of things we can use
        ApplicationPermissions permRequest = new ApplicationPermissions();
        permRequest.addPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_FILE_API);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_INTERNET);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_WIFI);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_DISPLAY_LOCKED);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_USB);
        permRequest.addPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION);  
        permRequest.addPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS);        
        permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);
        
        // Request that the user change permissions
        boolean acceptance = permissionsManager.invokePermissionsRequest(permRequest);
        if(!acceptance) {
            // If the complete request was not accepted, make sure we at least
            // got the minimum required permissions before starting.
            return hasMinimumPermissions(permissionsManager.getApplicationPermissions());
        }
        else {
            return true;
        }
    }

    private static boolean hasMinimumPermissions(ApplicationPermissions permissions) {
    	if(permissions.getPermission(ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION) == ApplicationPermissions.VALUE_ALLOW
    			&& permissions.getPermission(ApplicationPermissions.PERMISSION_INTERNET) == ApplicationPermissions.VALUE_ALLOW)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    private static boolean hasExtendedPermissions(ApplicationPermissions permissions) {
        return permissions.getPermission(ApplicationPermissions.PERMISSION_FILE_API) == ApplicationPermissions.VALUE_ALLOW
            && permissions.getPermission(ApplicationPermissions.PERMISSION_SERVER_NETWORK) == ApplicationPermissions.VALUE_ALLOW
            && permissions.getPermission(ApplicationPermissions.PERMISSION_INTERNET) == ApplicationPermissions.VALUE_ALLOW
            && permissions.getPermission(ApplicationPermissions.PERMISSION_WIFI) == ApplicationPermissions.VALUE_ALLOW
            && permissions.getPermission(ApplicationPermissions.PERMISSION_ORGANIZER_DATA) == ApplicationPermissions.VALUE_ALLOW
            && permissions.getPermission(ApplicationPermissions.PERMISSION_SECURITY_DATA) == ApplicationPermissions.VALUE_ALLOW
        	&& permissions.getPermission(ApplicationPermissions.PERMISSION_INPUT_SIMULATION) == ApplicationPermissions.VALUE_ALLOW  
        	&& permissions.getPermission(ApplicationPermissions.PERMISSION_DEVICE_SETTINGS) == ApplicationPermissions.VALUE_ALLOW        
        	&& permissions.getPermission(ApplicationPermissions.PERMISSION_EMAIL) == ApplicationPermissions.VALUE_ALLOW;
    }
}
