package gold.cam.kit.plugin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.liv.goldcamera.GoldCamera;
import com.liv.goldcamera.activity.ActivityCamera;
import com.liv.goldcamera.components.Helper;
import com.liv.goldcamera.service.CheckInIntentService;
import com.liv.goldcamera.service.FileDownloadService;
import com.liv.goldcamera.util.PermissionHelper;
import com.liv.lifeofdad.qa.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class GoldCamKitPlugin extends CordovaPlugin {
    
    public static final String TAG = "GoldCamKitPlugin";
    
    public CallbackContext callbackContext;
    
    /**
     * Constructor.
     */
    public GoldCamKitPlugin() {
    }
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        this.callbackContext = callbackContext;
        
        if (action.equals("openCamera")) {
            try {
                initGoldCamera();
            } catch (Exception e) {
                callbackContext.error("Illegal Argument Exception");
                PluginResult r = new PluginResult(PluginResult.Status.ERROR);
                callbackContext.sendPluginResult(r);
                return true;
            }
            
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            
            return true;
        }
        return false;
    }
    
    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    public void initGoldCamera() {
        if (GoldCamera.getInstance() == null) {
            GoldCamera.init(cordova.getActivity(), R.raw.qasettings);
        }
        
        if (GoldCamera.getInstance().checkForPermissions(cordova.getActivity())) {
            launchGCCamera();
        } else {
            ArrayList<String> permissionList = new ArrayList<String>();
            permissionList.add(PermissionHelper.WRITE_EXTERNAL_STORAGE);
            permissionList.add(PermissionHelper.CAMERA);
            permissionList.add(PermissionHelper.ACCESS_COARSE_LOCATION);
            PermissionHelper.chcekMultiplePermissions(cordova.getActivity(), cordova.getActivity().getBaseContext(), PermissionHelper.REQUEST_CODE_READ_WRITE_EXTERNAL_STORAGE, permissionList);
        }
        
        /*cordova.getActivity().runOnUiThread(new Runnable() {
         public void run() {
         Intent intent = new Intent(cordova.getActivity(), SplashScreen.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         cordova.getActivity().getApplicationContext().startActivity(intent);
         }
         });*/
    }
    
    private void launchGCCamera() {
        // GoldCamera.getInstance().start(true);
        if (Helper.isInternetAvailable(cordova.getActivity())) {
            cordova.getActivity().startService(new Intent(cordova.getActivity(), FileDownloadService.class));
        } else {
            Toast.makeText(cordova.getActivity(), "Internet Service not available", Toast.LENGTH_SHORT).show();
        }
        cordova.getActivity().startService(new Intent(cordova.getActivity(), CheckInIntentService.class));
        Intent intent = new Intent(cordova.getActivity(), ActivityCamera.class);
        this.cordova.startActivityForResult((CordovaPlugin) this, intent, GoldCamera.REQUEST_GOLDCAMERA_CAPTURE);
        // cordova.getActivity().finish();
    }
    
    /**
     * Called when the camera view exits.
     *
     * @param requestCode The request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param intent      An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 100 && resultCode == cordova.getActivity().RESULT_OK && intent.getData() != null) {
            this.callbackContext.success(intent.getData().toString());
        } else {
            this.callbackContext.error("Operation canceled");
        }
    }
    
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionHelper.REQUEST_CODE_READ_WRITE_EXTERNAL_STORAGE: {
                
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(PermissionHelper.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(PermissionHelper.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(PermissionHelper.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (grantResults.length > 0) {
                    if (perms.get(PermissionHelper.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(PermissionHelper.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        perms.get(PermissionHelper.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // All Permissions Granted
                        launchGCCamera();
                    } else {
                        boolean showStorageRationale = ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), PermissionHelper.WRITE_EXTERNAL_STORAGE);
                        boolean showCameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), PermissionHelper.CAMERA);
                        boolean showCheckInRationale = ActivityCompat.shouldShowRequestPermissionRationale(cordova.getActivity(), PermissionHelper.ACCESS_COARSE_LOCATION);
                        if (!showStorageRationale || !showCameraRationale || !showCheckInRationale) {
                            // user denied flagging NEVER ASK AGAIN
                            PermissionHelper.showSettingDialog(cordova.getActivity(), "You need to allow access to permissions for better performance");
                        } else {
                            // cordova.getActivity().finish();
                        }
                    }
                }
            }
                break;
            default:
                break;
        }
    }
}
