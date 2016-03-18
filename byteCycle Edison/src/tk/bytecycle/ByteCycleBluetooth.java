package tk.bytecycle;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class ByteCycleBluetooth implements Runnable {
	
	private final UUID bluetoothUID = new UUID("d0c722b07e1511e1b0c40800200c9a66", false);
	private final String bluetoothScheme = "btspp://localhost:" + bluetoothUID + ";authenticate=false;encrypt=false;name=IEServer";

	private Boolean bluetoothConnected;
	private Thread bluetoothThread;

	public ByteCycleBluetooth() {
		bluetoothThread = new Thread(this);
		bluetoothThread.setDaemon(true);
		bluetoothThread.start();

		bluetoothConnected = false;
	}

	@Override
	public void run() {
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();

			System.out.println("BT Nome: " + localDevice.getFriendlyName());
			System.out.println("BT Mac: " + localDevice.getBluetoothAddress());

			while (!Thread.interrupted()) {

				try {
					// Ottengo il manager dei moduli
					ByteCycleModule managerModule = ByteCycle.getManagerModule();
					managerModule.toggleLedStatus(true);
					
					// Apertura del connection-notifier
					StreamConnectionNotifier bluetoothConnectionManager = (StreamConnectionNotifier) Connector.open(bluetoothScheme);
					StreamConnection bluetoothConnection = bluetoothConnectionManager.acceptAndOpen();
	
					managerModule.toggleLedStatus(false);
					managerModule.emitBuzz(0, 500);

					// Indico che qualcuno è connesso al bluetooth
					bluetoothConnected = true;
					
					// Ottengo il device che si è connesso
					RemoteDevice bluetoothDevice = RemoteDevice.getRemoteDevice(bluetoothConnection);
					System.out.println("BT Remote device name: " + bluetoothDevice.getFriendlyName(true));
					System.out.println("BT Remote device mac: " + bluetoothDevice.getBluetoothAddress());
									
					DataInputStream bluetoothConnectionInput = bluetoothConnection.openDataInputStream();
					DataOutputStream bluetoothConnectionOutput = bluetoothConnection.openDataOutputStream();
					
					Thread bluetoothConnectionThreadTX = new Thread() {
						public void run() {
							try {
								while (bluetoothConnected) {									
									bluetoothConnectionOutput.writeInt(170697);
								    bluetoothConnectionOutput.writeDouble(managerModule.calculateAngularMoment());
									bluetoothConnectionOutput.writeInt(managerModule.detectFlame() ? 1 : 0);
									bluetoothConnectionOutput.writeInt(managerModule.calculateLight());
									bluetoothConnectionOutput.flush();
								}
							} catch (Exception exception) {
								bluetoothConnected = false;
							}
						}
					};
					
					Thread bluetoothConnectionThreadRX = new Thread() {
						public void run() {
							try {
								while (bluetoothConnected) {									
									switch (bluetoothConnectionInput.readInt()) {
										case 0: managerModule.toogleLedLight(bluetoothConnectionInput.readBoolean()); break;
									}
								}
							} catch (Exception exception) {
								bluetoothConnected = false;
							}
						}
					};
					
					bluetoothConnectionThreadTX.setDaemon(true);
					bluetoothConnectionThreadRX.setDaemon(true);
					bluetoothConnectionThreadTX.start();
					bluetoothConnectionThreadRX.start();
						
					while (bluetoothConnectionThreadTX.isAlive() 
						&& bluetoothConnectionThreadRX.isAlive()) {
						Thread.sleep(100);
						managerModule.toogleLedLight(
							managerModule.calculateLight() > 5
						);
					}
					
					managerModule.emitBuzz(0, 500);					
					
					bluetoothConnectionManager.close();
					bluetoothConnection.close();
					
					System.out.println("BT Disconnesso");
					bluetoothConnected = false;
					
				} catch (Exception exception) {
					
					System.out.println("BT Disconnesso (Errore)");
					bluetoothConnected = false;
					
				}
			}
			
		} catch (Exception exception) {
			
			System.out.println("Impossibile avviare l'interfaccia bluetooth");
			exception.printStackTrace();
			
		}
	}

	
	public boolean isBluetoothConnected() {
		return bluetoothConnected.booleanValue();
	}
	
}
