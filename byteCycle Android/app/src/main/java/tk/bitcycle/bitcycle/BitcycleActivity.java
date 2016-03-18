package tk.bitcycle.bitcycle;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BitcycleActivity extends ActionBarActivity {

    BluetoothSocket btSocket;
    private static final UUID MY_UUID = UUID.fromString("d0c722b0-7e15-11e1-b0c4-0800200c9a66");

    TextView textLongitude;
    TextView textLatitude;
    TextView textSpeed;
    TextView textTemperature;
    TextView textBattery;
    TextView textLight;
    Button buttonConnect;

    private boolean bluetoothConnected;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setContentView(R.layout.activity_bitcycle);


        textLongitude = (TextView) findViewById(R.id.longitude);
        textLatitude = (TextView) findViewById(R.id.latitude);
        textSpeed = (TextView) findViewById(R.id.speed);
        textTemperature = (TextView) findViewById(R.id.temperature);
        textBattery = (TextView) findViewById(R.id.battery);
        textLight = (TextView) findViewById(R.id.light);
        buttonConnect = (Button) findViewById(R.id.button);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        textLongitude.setText("Longitude: " + location.getLongitude());
                        textLatitude.setText("Latitude: " + location.getLatitude());
                        textSpeed.setText("Speed: " + (location.getSpeed() / 3.6D) + " K/h");
                    }
                });
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
				
            }
        };
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Thread thread = new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                buttonConnect.setEnabled(false);
                                buttonConnect.setText("Connecting...");
                            }
                        });

                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled() == false)

                        {
                            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOn, 0);
                        }

                        bluetoothAdapter.startDiscovery();
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("98:4F:EE:06:19:2C"); // Intel Edison Bluetooth Mac
                        try

                        {
                            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                            btSocket.connect();

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    buttonConnect.setText("Connected!");
                                }
                            });


                            try (final DataInputStream bluetoothInputStream = new DataInputStream(btSocket.getInputStream())) {
                                while (!Thread.interrupted()) {
                                    Thread.sleep(50);
                                    if (bluetoothInputStream.readInt() == 170697) {
                                        final double sensorMotion = bluetoothInputStream.readDouble();
                                        final boolean sensorTemperature = bluetoothInputStream.readInt() == 1;
                                        final int sensorLight = bluetoothInputStream.readInt();

                                        if (sensorMotion < 250 || sensorMotion > 350) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            switch (which) {
                                                                case DialogInterface.BUTTON_NEGATIVE:
																	// TODO Timeout + SMS
                                                                    break;
                                                            }
                                                        }
                                                    };

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                                                    builder.setTitle("Health Check");
                                                    builder.setMessage("I have detected that you fell from your bike, are you ok?").setPositiveButton("Yes", dialogClickListener)
                                                            .setNegativeButton("No", dialogClickListener).show();
                                                }
                                            });

                                        }

                                        runOnUiThread(new Runnable() {
											public void run() {
												try {
													textTemperature.setText("Temperature: " + (!sensorTemperature ? "Low" : "High"));
													textLight.setText("Light Level: " + sensorLight + " / 10");
												} catch (Exception exception) {
													exception.printStackTrace();
												}
											  }
											}
                                        );
                                    }
                                }
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buttonConnect.setEnabled(true);
                                    buttonConnect.setText("Connect to bike!");
                                }
                            });
                        }
                    }
                };
                thread.start();
            }
        });
    }


}


