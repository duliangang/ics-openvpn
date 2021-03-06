package de.blinkt.vpndialogxposed;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.Method;

public class VpnDialogPatcher implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.vpndialogs"))
            return;


        XposedBridge.log("Got VPN Dialog");

        XposedHelpers.findAndHookMethod("com.android.vpndialogs.ConfirmDialog", lpparam.classLoader,
                "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

               // IConnectivityManager mService = IConnectivityManager.Stub.asInterface(
                //        ServiceManager.getService(Context.CONNECTIVITY_SERVICE));

                try {

                    /*Class servicemanager = Class.forName("android.os.ServiceManager");
                    Method getService = servicemanager.getMethod("getService",String.class);

                    IConnectivityManager mService = IConnectivityManager.Stub.asInterface(
                            (IBinder) getService.invoke(servicemanager, Context.CONNECTIVITY_SERVICE));

                    */
                    Object mService = XposedHelpers.getObjectField(param.thisObject, "mService");

                    String mPackage = ((Activity) param.thisObject).getCallingPackage();

                     // App is already allowed do nothing
                    /*if (mService.prepareVpn(mPackage, null)) {
                        return;
                    }*/


                    Class<?>[] prepareVPNsignature = {String.class, String.class};
                    if((Boolean) XposedHelpers.callMethod(mService,"prepareVpn",prepareVPNsignature,  mPackage,(String)null))
                        return;

                    if (mPackage.equals("de.blinkt.openvpn")) {
                        //mService.prepareVpn(null, mPackage);
                        XposedHelpers.callMethod(mService,"prepareVpn",prepareVPNsignature, (String)null,mPackage);
                        ((Activity) param.thisObject).setResult(Activity.RESULT_OK);
                        Toast.makeText((Context)param.thisObject,"Allowed de.blinkt.openvpn",Toast.LENGTH_LONG).show();
                        ((Activity) param.thisObject).finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }


        });

    }
}
