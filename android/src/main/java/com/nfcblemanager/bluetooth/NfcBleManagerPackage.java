package com.nfcblemanager.bluetooth;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.nfcblemanager.nfc.NfcReactNativeModule;

public class NfcBleManagerPackage implements ReactPackage {

	public NfcBleManagerPackage() {}

	@Override
	public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
		List<NativeModule> modules = new ArrayList<>();
		modules.add(new BleManager(reactApplicationContext));
		modules.add(new NfcReactNativeModule(reactApplicationContext));
		return  modules;
	}

	@Override
	public List<Class<? extends JavaScriptModule>> createJSModules() {
		return new ArrayList<>();
	}

	@Override
	public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
		return Collections.emptyList();
	}
}
