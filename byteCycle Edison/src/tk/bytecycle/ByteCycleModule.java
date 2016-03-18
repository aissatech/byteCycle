package tk.bytecycle;

import mraa.Aio;
import mraa.Dir;
import mraa.Gpio;
import mraa.Pwm;

public class ByteCycleModule {

	private Gpio gpioLedLight;
	private Gpio gpioLedStatus;
	private Gpio gpioFlame;
	
	private Aio gpioLight;
	private Aio gpioGyro;
	
	private Pwm gpioBuzzer;
	
	public ByteCycleModule() {
		gpioLedLight = new Gpio(2, false);
		gpioLedLight.dir(Dir.DIR_OUT);

		gpioLedStatus = new Gpio(8, false);
		gpioLedStatus.dir(Dir.DIR_OUT);

		gpioFlame = new Gpio(7, false);
		gpioFlame.dir(Dir.DIR_IN);

		gpioBuzzer = new Pwm(6, false);
        gpioBuzzer.enable(true);
		
		gpioLight = new Aio(0);
		gpioGyro = new Aio(1);	
	}
	
	/*
	 * Imposta il led delle luci
	 */
	public void toogleLedLight(boolean state) {
		gpioLedLight.write(state ? 0 : 1);
	}
	
	/*
	 * Imposta il di stato
	 */
	public void toggleLedStatus(boolean state) {
		gpioLedStatus.write(state ? 0 : 1);
	}
	
	
	/*
	 * Calcola se è presente un incendio
	 * impiega 5 secondi
	 */
	public boolean detectFlame() {
		try {
			int flameDelta = 0;
			for (int flameStep = 0; flameStep < 50; flameStep++) {
				flameDelta = gpioFlame.read() == 0 ? -1 : 1;
				Thread.sleep(5);
			}
			return flameDelta < 0;
		} catch (Exception exception) {
			return false;
		}
 	}
	
	/*
	 * Emetter un buz
	 */
	public boolean emitBuzz(int frequency, int delay) {
		gpioBuzzer.write(1);
		try {
			Thread.sleep(delay);
		} catch (Exception exception) {
			return false;
		} finally {
			gpioBuzzer.write(0);
		}
		return true;
	}
	public double calculateAngularMoment() {
		return gpioGyro.read();
	}
	

	/*
	 * Calcola il livello della luce,
	 * 00 = Massimo buio
	 * 10 = Massima luce
	 */
	public int calculateLight() {
		return (int) ((gpioLight.read() * 10D) / 1024D);
	}
	

}
