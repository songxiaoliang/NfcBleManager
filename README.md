# react-native-scy-nbc
#### A Bluetooth, NFC package library, Bluetooth module can be scanned, linked, read and other operations. NFC module can get mobile phone NFC state, and can read IOS14443A type Tag card.

[![npm version](https://img.shields.io/npm/v/react-native-scy-nbc.svg?style=flat)](https://www.npmjs.com/package/react-native-scy-nbc)

## RN版本
RN 0.40+

## Android版本
- Android (API 18+)

## 安装
```shell
npm i react-native-scy-nbc --save 
```

### 链接原生代码库

```shell
react-native link rreact-native-scy-nbc
```

`android/app/build.gradle`:
```gradle
// file: android/app/build.gradle
...

android {
    ...

    defaultConfig {
        ...
        minSdkVersion 18 // <--- make sure this is 18 or greater
        ...
    }
    ...
}
```

### 连接原生库成功后，检查如下文件是否正确

```gradle
// file: android/settings.gradle
...

include ':react-native-scy-nbc'
project(':react-native-scy-nbc').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-scy-nbc/android')
```

```gradle
// file: android/app/build.gradle
...

android {
    ...

    defaultConfig {
        ...
        minSdkVersion 18 // <--- 最低版本为18
        ...
    }
    ...
}

dependencies {
    ...
    compile project(':react-native-scy-nbc')
}
```

```java
...
import com.nfcblemanager.bluetooth.NfcBleManagerPackage; // <--- 导入包，注意查看路径是否正确

public class MainApplication extends Application implements ReactApplication {

    ...

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new NfcBleManagerPackage() // <------ 添加包
        );
    }

    ...
}
```
##### 修改 Android Manifest

```xml
// file: android/app/src/main/AndroidManifest.xml
...
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.NFC" />
...
```
```xml
...
   <activity
        android:name=".MainActivity"
        android:label="@string/app_name"
        android:launchMode="singleTop" // <--- 1.设置启动方式
        android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
        android:windowSoftInputMode="adjustResize"> 
         <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
        // 2.添加如下filter
        <intent-filter>  
            <action android:name="android.nfc.action.NDEF_DISCOVERED" />  
        </intent-filter>  
        <intent-filter>  
            <action android:name="android.nfc.action.TAG_DISCOVERED" >  
            </action>  
            <category android:name="android.intent.category.DEFAULT" >  
            </category>  
        </intent-filter>  
        <intent-filter>  
            <action android:name="android.nfc.action.TECH_DISCOVERED" />  
        </intent-filter>  
        // 3.配置扫描过滤
        <meta-data android:name="android.nfc.action.TECH_DISCOVERED"
            android:resource="@xml/nfc_tech_filter" />
      </activity>
...
```

```xml
...
   
        在app/src/res/下创建xml文件夹，并在该文件夹下创建fc_tech_filter.xml文件：
        <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
            <tech-list>
                <tech>android.nfc.tech.IsoDep</tech>  
                <tech>android.nfc.tech.NfcA</tech>    
                <tech>android.nfc.tech.NfcB</tech>    
                <tech>android.nfc.tech.NfcF</tech>    
                <tech>android.nfc.tech.NfcV</tech>    
                <tech>android.nfc.tech.Ndef</tech>    
                <tech>android.nfc.tech.NdefFormatable</tech>    
                <tech>android.nfc.tech.MifareUltralight</tech>   
                <tech>android.nfc.tech.MifareClassic</tech>  
            </tech-list>
        </resources>
...
```

## 示例代码
[示例](https://github.com/songxiaoliang/NfcBleManager/tree/master/example)

##  NFC

### 注册数据接受回调
 用于接收手机NFC状态、读取NFC数据。
 
 __示例__
```js
    componentDidMount() {
        // NFC状态
        DeviceEventEmitter.addListener('onNfcStatus', function(status) {
            ToastAndroid.show(status, ToastAndroid.SHORT);
        })
        // NFC扫描读取的Tag数据
        DeviceEventEmitter.addListener('onNfcScanResult', function(result) {
            ToastAndroid.show(result, ToastAndroid.SHORT);
        })
    }
```

### startNfc()
 检测手机NFC功能状态

__示例__
```js
  _checkNfc() {
    Manager.startNfc();
  }
```

### readNfc()
启动NFC读取功能，启动后，将Tag放入扫描区即可读取。

__示例__
```js
  _startRead() {
    Manager.readNfc();
  }
```

##  蓝牙

### 注意
- 记住在任何事情之前使用`start`方法。
- 避免在扫描期间连接/读取/写入外设。
- 读写之前，需要调用`retrieveServices`方法。

### start
在使用蓝牙前，需要调用start启动。

__示例__
```js
    componentDidMount() {
        Manager.start({showAlert: false, allowDuplicates: false});
    }
```

### enableBluetooth
检测手机蓝牙状态，例如蓝牙是否可用，是否开启，未开启状态下会申请蓝牙权限，并自动开启蓝牙功能。

__示例__
```js
     Manager.enableBluetooth()
          .then(() => {
            alert('蓝牙已开启，可以使用');
          })
          .catch((error) => {
            alert('用户拒绝授权蓝牙权限');
    });
```

### scan(serviceUUID,seconds,true)
扫描外部蓝牙设备

__参数__
- `serviceUUID`  传- `[]` - 即可
- `seconds` 扫描时长，以秒计算.

__示例__
```js
 Manager.scan([], 6, true).then((results) => {
        ToastAndroid.show("扫描中..." + results,ToastAndroid.SHORT);
      });
```

### stopScan
停止扫描外部蓝牙设备

__示例__
```js
Manager.stopScan()
  .then(() => {
     ToastAndroid.show("停止扫描..." + results,ToastAndroid.SHORT);
  });
```

### connect(peripheralId)
链接外部蓝牙设备

__参数__
- `peripheralId`  蓝牙设备Id

__示例__
```js
      Manager.connect(peripheral.id)
        .then(() => {
          ToastAndroid.show('连接外部设备'+peripheral.id, ToastAndroid.SHORT);
        })
        .catch((error) => {
          ToastAndroid.show('连接错误：'+error, ToastAndroid.SHORT);
        });

```

### disconnect(peripheralId)
断开外部蓝牙设备链接

__参数__
- `peripheralId`  蓝牙设备Id

__示例__
```js
     Manager.disconnect(peripheral.id);
```

### retrieveServices(peripheralId)
注册蓝牙设备服务,检索外设的service和characteristic。

__参数__
- `peripheralId`  蓝牙设备Id

__示例__
```js
    Manager.retrieveServices(peripheral.id).then((peripheralData) => {
          ToastAndroid.show('检索的外部服务' + JSON.stringify(peripheralData), ToastAndroid.SHORT);
    });
```

### readRSSI(peripheralId)
读取当前链接蓝牙设备的RSSI值

__参数__
- `peripheralId`  蓝牙设备Id

__示例__
```js
    Manager.readRSSI(peripheral.id).then((rssi) => {
        ToastAndroid.show('检索实际RSSI值'+rssi, ToastAndroid.SHORT);
    });
```

### read(peripheralId，serviceUUID,characteristicUUID)
读取当前链接蓝牙设备的数据

__参数__
- `peripheralId`  蓝牙设备Id
- `serviceUUID`  服务的UUID。
- `characteristicUUID`  特性的UUID。

__示例__
```js
   Manager.read('peripheralId', 'serviceUUID', 'characteristicUUID')
      .then((readData) => {
        ToastAndroid.show('读取到数据:'+readData, ToastAndroid.SHORT);
      })
      .catch((error) => {
        ToastAndroid.show('读取数据失败:'+error, ToastAndroid.SHORT);
      });
```

### write(peripheralId, serviceUUID, characteristicUUID, data, maxByteSize)
Write with response to the specified characteristic.
Returns a `Promise` object.

__参数__
- `peripheralId`  外设的id / mac地址。
- `serviceUUID`   服务的UUID。
- `characteristicUUID`  characteristic的UUID。
- `data`  要写入的数据，数组。
- `maxByteSize`   在分割消息之前指定最大字节大小

__示例__
```js
Manager.write('peripheralId', 'serviceUUID', 'characteristicUUID', data)
  .then(() => {
    ToastAndroid.show('成功写入的数据: '+data, ToastAndroid.SHORT);
  })
  .catch((error) => {
     ToastAndroid.show('写入数据失败:'+error, ToastAndroid.SHORT);
  });
```


### getConnectedPeripherals(serviceUUIDs)
获取已连接的蓝牙设备。

__参数__
- `serviceUUIDs` -寻找的服务的UUID。

__示例__
```js
Manager.getConnectedPeripherals([])
  .then((peripheralsArray) => {
    ToastAndroid.show('所有已连接的蓝牙设备 : ' + peripheralsArray.length);
  });

```

## 回调事件

### BleManagerStopScan
停止扫描回调监听。

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerStopScan',
    () => {
        // Scanning is stopped
    }
);
```

###  BleManagerDidUpdateState
蓝色状态发生改变。

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerDidUpdateState',
    (status) => {
        ToastAndroid.show('当前蓝牙状态为:'+status, ToastAndroid.SHORT);
    }
);
```

###  BleManagerDiscoverPeripheral
扫描到蓝牙设备

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerDiscoverPeripheral',
    (peripheral) => {
        ToastAndroid.show('蓝牙Id为:' + peripheral.id + '蓝牙名称为：' + peripheral.name, ToastAndroid.SHORT);
    }
);
```

###  BleManagerDidUpdateValueForCharacteristic
Characteristic通知新值。

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerDidUpdateValueForCharacteristic',
    (data) => {
        ToastAndroid.show('收到数据:' + data.value + '蓝牙设备：' + data.peripheral + '蓝牙characteristicUUID:' + data.characteristic, ToastAndroid.SHORT);
    }
);
```

###  BleManagerConnectPeripheral
外设已连接。

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerConnectPeripheral',
    (peripheralId) => {
    
    }
);
```

###  BleManagerDisconnectPeripheral
外设已断开。

__示例__
```js
bleManagerEmitter.addListener(
    'BleManagerDisconnectPeripheral',
    (peripheralId) => {
    
    }
);
```
