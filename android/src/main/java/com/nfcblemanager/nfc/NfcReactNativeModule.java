package com.nfcblemanager.nfc;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.nfc.FormatException;
import java.io.IOException;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.Ndef;


public class NfcReactNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    private ReactApplicationContext reactContext;
    private NfcAdapter mNfcAdapter;
    private boolean isFirst = true;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private int bIndex;  
    private int bCount;
    private String status;
    private boolean isRead;
    private Tag tagFromIntent;

    public NfcReactNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addActivityEventListener(this);
        this.reactContext.addLifecycleEventListener(this);
    }

    @Override
    public void onHostResume() {
        if (mNfcAdapter != null) {
             if(mNfcAdapter.isEnabled()) {
                setupForegroundDispatch(getCurrentActivity(), mNfcAdapter);
                if (isFirst) {
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getCurrentActivity().getIntent().getAction())// NDEF类型  
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(getCurrentActivity().getIntent().getAction())// 其他类型  
                    || NfcAdapter.ACTION_TAG_DISCOVERED.equals(getCurrentActivity().getIntent().getAction())) {
                        String result = processIntent(getCurrentActivity().getIntent());
                        if(isRead) {
                            reactContext
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("onNfcScanResult", result);
                        }
                    }
                    isFirst = false;
                }
            }
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this.reactContext);
            if (mNfcAdapter == null) {
                // 不存在NFC功能
                status = "手机不存在NFC功能";
            } else if (!mNfcAdapter.isEnabled()) {
                // 未打开NFC功能
                status = "手机未打开NFC功能";
            } else {
                // 可以使用NFC功能
                status = "NFC功能已启动";
            }
            return;
        }
    }

    @ReactMethod
    public void start() {
         reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onNfcStatus", status);
    }

    @ReactMethod
    public void read() {
         this.isRead = true;
    }

    @ReactMethod
    public void readOver() {
         this.isRead = false;
    }

    @Override
    public void onHostPause() {
        if (mNfcAdapter != null)
            stopForegroundDispatch(getCurrentActivity(), mNfcAdapter);
    }

    public void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
           try {
            // 过滤器
            mFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED,"*/*")};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        // 允许扫描的标签类型
        mTechLists = new String[][] {
                {MifareClassic.class.getName()},
                {NfcA.class.getName()},
                {NfcB.class.getName()}
        };

        pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
        adapter.enableForegroundDispatch(activity, pendingIntent, mFilters, mTechLists);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    public void onNewIntent(Intent intent) {
         String intentActionStr = intent.getAction();// 获取到本次启动的action 
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentActionStr)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentActionStr) 
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentActionStr)) {
            String result = processIntent(intent);
            //发送到rn
            if(isRead) {
                reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("onNfcScanResult", result);
            }
        }
    }

    @Override
    public void onActivityResult(
            final Activity activity,
            final int requestCode,
            final int resultCode,
            final Intent intent) {
    }

    @Override
    public void onHostDestroy() {
    }
    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from javascript.
     */
    @Override
    public String getName() {
        return "NfcReactNative";
    }

    /**
     * 获取tab标签中的内容
     * @param intent
     * @return
     */
    private String processIntent(Intent intent) {
        if(intent != null ) {
            tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG); 
            String tagType = tagFromIntent.getTechList()[0];
            String result = readTag(tagFromIntent);
            if( result != null ) {
                 return result;
            } else {
                return "读取数据失败";
            }
        } else {
            return "intent为空";
        }
    }

    /**
    * 十六进制数据
    */
    public String readTag(Tag tag) {  
        MifareClassic mfc = MifareClassic.get(tag);  
        boolean auth = false;  
        // 读取TAG  
        try {  
            StringBuilder metaInfo = new StringBuilder();  
            mfc.connect();  
            int type = mfc.getType(); // 获取TAG的类型  
            int sectorCount = mfc.getSectorCount(); // 获取TAG中包含的扇区数  
            String typeS = "";  
            switch (type) {  
            case MifareClassic.TYPE_CLASSIC:  
                typeS = "TYPE_CLASSIC";  
                break;  
            case MifareClassic.TYPE_PLUS:  
                typeS = "TYPE_PLUS";  
                break;  
            case MifareClassic.TYPE_PRO:  
                typeS = "TYPE_PRO";  
                break;  
            case MifareClassic.TYPE_UNKNOWN:  
                typeS = "TYPE_UNKNOWN";  
                break;  
            }  
            metaInfo.append("  卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"  
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()  
                    + "B\n");  
            for (int j = 0; j < sectorCount; j++) {  
                // Authenticate a sector with key A.  
                auth = mfc.authenticateSectorWithKeyA(j,  
                        MifareClassic.KEY_DEFAULT); 
                /*  
                 * byte[]  
                 * codeByte_Default=MifareClassic.KEY_DEFAULT;//FFFFFFFFFFFF  
                 * byte[]  
                 * codeByte_Directory=MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY  
                 * ;//A0A1A2A3A4A5 byte[]  
                 * codeByte_Forum=MifareClassic.KEY_NFC_FORUM;//D3F7D3F7D3F7  
                 */if (auth) {  
                    metaInfo.append("Sector " + j + ":验证成功\n");  
                    // 读取扇区中的块  
                    bCount = mfc.getBlockCountInSector(j);  
                    bIndex = mfc.sectorToBlock(j);  
                    for (int i = 0; i < bCount; i++) {  
                        byte[] data = mfc.readBlock(bIndex);  
                        metaInfo.append("Block " + bIndex + " : "  
                                + ByteArrayToHexString(data)  
                                + "\n");  
                        bIndex++;  
                    }  
                } else {  
                    metaInfo.append("Sector " + j + ":验证失败\n");  
                }  
            }  
            return metaInfo.toString();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (mfc != null) {  
                try {  
                    mfc.close();  
                } catch (IOException e) {  
                }  
            }  
        }  
        return null;  
    }

    private  String ByteArrayToHexString(byte[] bytesId) {   //Byte数组转换为16进制字符串  
            // TODO 自动生成的方法存根  
            int i, j, in;  
            String[] hex = {  
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"  
            };  
            String output = "";  

            for (j = 0; j < bytesId.length; ++j) {  
                in = bytesId[j] & 0xff;  
                i = (in >> 4) & 0x0f;  
                output += hex[i];  
                i = in & 0x0f;  
                output += hex[i];  
            }  
            return output;  
    } 

}
