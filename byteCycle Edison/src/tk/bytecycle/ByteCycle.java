package tk.bytecycle;

import java.util.Scanner;

public class ByteCycle {

	// Startup string
	// java -Djava.library.path=/usr/lib/java -cp "byteCycle.jar:/usr/lib/java/*:/lib/bluecove-2.1.0.jar:/lib/bluecove-gpl-2.1.0.jar" tk.bytecycle.ByteCycle
	
	public static ByteCycleBluetooth managerBluetooth;
	public static ByteCycleModule managerModule;

	static {
		try {
			System.loadLibrary("mraajava");
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		managerBluetooth = new ByteCycleBluetooth();
		managerModule = new ByteCycleModule();
		
		try (Scanner terminalScanner = new Scanner(System.in)) {
			while (terminalScanner.hasNext()) {
				String terminalInput = terminalScanner.next();
				if (terminalInput.equalsIgnoreCase("exit")) {
					Runtime runtime = Runtime.getRuntime();
					runtime.halt(0);
				}
			}
		}
	}
	
	public static ByteCycleBluetooth getManagerBluetooth() {
		return managerBluetooth;
	}
	
	public static ByteCycleModule getManagerModule() {
		return managerModule;
	}

}