/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  ToastAndroid,
  DeviceEventEmitter,
  NativeModules,
  TouchableOpacity,
  Platform,
  PermissionsAndroid,
  ListView,
  ScrollView,
  NativeEventEmitter,
} from 'react-native';

/**
 * 引用库文件
 */
const bleModule = NativeModules.BleManager; 
import Manager from 'react-native-scy-nbc';
const bleManagerEmitter = new NativeEventEmitter(bleModule);


const ds = new ListView.DataSource({rowHasChanged: (r1, r2) => r1 !== r2});

export default class TestNBC extends Component {

constructor(props) {
    super(props);
    this.state = {
      scanning:false,
      peripherals: new Map()
    }

    // 蓝牙监听回调
    this.handleDiscoverPeripheral = this.handleDiscoverPeripheral.bind(this);
    this.handleStopScan = this.handleStopScan.bind(this);
    this.handleUpdateValueForCharacteristic = this.handleUpdateValueForCharacteristic.bind(this);
    this.handleDisconnectedPeripheral = this.handleDisconnectedPeripheral.bind(this);

  }

   componentDidMount() {

      // 1.启动蓝牙
      Manager.start({showAlert: false, allowDuplicates: false});
      // 2.检测手机蓝牙设备
      Manager.enableBluetooth()
          .then(() => {
            // Success code
            alert('蓝牙已开启，可以使用');
          })
          .catch((error) => {
            // Failure code
            alert('用户拒绝蓝牙授权');
      });

      // 3.注册蓝牙监听回调          
      bleManagerEmitter.addListener('BleManagerDiscoverPeripheral', this.handleDiscoverPeripheral );
      bleManagerEmitter.addListener('BleManagerStopScan', this.handleStopScan );
      bleManagerEmitter.addListener('BleManagerDisconnectPeripheral', this.handleDisconnectedPeripheral );
      bleManagerEmitter.addListener('BleManagerDidUpdateValueForCharacteristic', this.handleUpdateValueForCharacteristic );
     
      // 注册NFC监听回调
      DeviceEventEmitter.addListener('onNfcStatus', function(status) {
          ToastAndroid.show(status, ToastAndroid.SHORT);
      })

      DeviceEventEmitter.addListener('onNfcScanResult', function(result) {
          ToastAndroid.show(result, ToastAndroid.SHORT);
      })


      // android 6.0以上动态授权检查
      if (Platform.OS === 'android' && Platform.Version >= 23) {
        PermissionsAndroid.checkPermission(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
            if (result) {
              ToastAndroid.show("授权成功",ToastAndroid.SHORT);
            } else {
              PermissionsAndroid.requestPermission(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
                if (result) {
                   ToastAndroid.show("用户接受",ToastAndroid.SHORT);
                } else {
                   ToastAndroid.show("用户拒绝",ToastAndroid.SHORT);
                }
              });
            }
      });

    }
  }


  /**
   * 检测手机NFC状态
   */
  _checkNfc() {
    Manager.startNfc();
  }

  /**
   * 扫描读取Tag数据
   */
  _startRead() {
    Manager.readNfc();
  }


  /**
   * 断开蓝牙设备
   */
  handleDisconnectedPeripheral(data) {
    let peripherals = this.state.peripherals;
    let peripheral = peripherals.get(data.peripheral);
    if (peripheral) {
      peripheral.connected = false;
      peripherals.set(peripheral.id, peripheral);
      this.setState({peripherals});
    }
    ToastAndroid.show("断开"+ data.peripheral,ToastAndroid.SHORT);
  }

  /**
   * 收到更改characteristic的数据
   */
  handleUpdateValueForCharacteristic(data) {
    ToastAndroid.show('收到 '+ data.value + ' 来自 ' + data.peripheral + ' characteristic ' + data.characteristic,ToastAndroid.SHORT);
  }

  /**
   * 停止扫描
   */
  handleStopScan() {
    ToastAndroid.show("停止扫描",ToastAndroid.SHORT);
    this.setState({ scanning: false });
  }

  /**
   * 开始扫描
   */
  startScan() {
    if (!this.state.scanning) {
      Manager.scan([], 6, true).then((results) => {
        ToastAndroid.show("扫描中..." + results,ToastAndroid.SHORT);
        this.setState({scanning:true});
      });
    }
  }

  /**
   * 扫描到外部蓝牙设备
   */  
  handleDiscoverPeripheral(peripheral){
    var peripherals = this.state.peripherals;
    if (!peripherals.has(peripheral.id)){

      ToastAndroid.show('发现外部设备'+peripheral.name, ToastAndroid.SHORT);
      peripherals.set(peripheral.id, peripheral);
      this.setState({ peripherals })
    }
  }

  /**
   * 测试蓝牙设备
   */  
  test(peripheral) {
    if (peripheral) {
      if (peripheral.connected) {
        Manager.disconnect(peripheral.id);
      }else{

        // 连接蓝牙设备
        Manager.connect(peripheral.id)
        .then(() => {
         
          let peripherals = this.state.peripherals;
          let p = peripherals.get(peripheral.id);

          if (p) {
            p.connected = true;
            peripherals.set(peripheral.id, p);
            this.setState({peripherals});
          }

          ToastAndroid.show('连接外部设备'+peripheral.id, ToastAndroid.SHORT);
          
          setTimeout(() => {
              // 注册蓝牙设备服务,检索外设的service和characteristic。
              Manager.retrieveServices(peripheral.id).then((peripheralData) => {
                 
                  ToastAndroid.show('检索的外部服务' + JSON.stringify(peripheralData), ToastAndroid.SHORT);
                 
                  // Manager.read();

                  // 读取RSSI值
                  Manager.readRSSI(peripheral.id).then((rssi) => {
                      ToastAndroid.show('检索实际RSSI值'+rssi, ToastAndroid.SHORT);
                  });

              });
          }, 900);

        })
        .catch((error) => {
          ToastAndroid.show('连接错误：'+error, ToastAndroid.SHORT);
        });
      }
    }
  }


   render() {

    const list = Array.from(this.state.peripherals.values());
    const dataSource = ds.cloneWithRows(list);

    return (
      <View style={styles.container}>

        <TouchableOpacity onPress={()=>this._startNfc()}>
            <Text style={ styles.welcome }>
              检测手机NFC状态
            </Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={()=>this._startRead()}>
            <Text style={ styles.welcome }>
              点击开始读取
            </Text>
        </TouchableOpacity>  


        <TouchableOpacity style={{marginTop: 40,margin: 20, padding:20, backgroundColor:'#ccc'}} onPress={() => this.startScan() }>
            <Text>扫描蓝牙设备 ({this.state.scanning ? '开' : '关'})</Text>
        </TouchableOpacity>
        <ScrollView style={styles.scroll}>
          {(list.length == 0) &&
            <View style={{flex:1, margin: 20}}>
              <Text style={{textAlign: 'center'}}>暂无蓝牙设备</Text>
            </View>
          }
          <ListView
              enableEmptySections={true}
              dataSource={dataSource}
              renderRow={(item) => {
                const color = item.connected ? 'green' : '#fff';
                return (
                  <TouchableOpacity onPress={() => this.test(item) }>
                    <View style={[styles.row, {backgroundColor: color }]}>
                      <Text style={{fontSize: 12, textAlign: 'center', color: '#333333', padding: 10}}>
                         {item.name}
                      </Text>
                      <Text style={{fontSize: 8, textAlign: 'center', color: '#333333', padding: 10}}>
                         {item.id}
                      </Text>
                    </View>
                  </TouchableOpacity>
                );
              }}
          />
        </ScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  scroll: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    margin: 10,
  },
  row: {
    margin: 10
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('TestNBC', () => TestNBC);
