package bioLib.test.namespace;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Bio.Library.namespace.BioLib;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// SDK v1.0.07 @MAR15
public class BioLibTestActivity extends Activity 
{	
	private BioLib lib = null;
	
	private String address = "";
	private String macaddress = "";
	private String mConnectedDeviceName = "";
	private BluetoothDevice deviceToConnect;
	
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	private TextView text;
	private TextView textRTC;
	private TextView textPUSH;
	private TextView textPULSE;
	private TextView textBAT;
	private TextView textDataReceived;
	private TextView textSDCARD;
	private TextView textACC;
	private TextView textHR;
	private TextView textECG;
	private TextView textDeviceId;
	private TextView textRadioEvent;
	private TextView textTimeSpan;
	
	private Button buttonConnect;
	private Button buttonDisconnect;
	private Button buttonGetRTC;
	private Button buttonSetRTC;
	private Button buttonRequest;
	private Button buttonSearch;
	private Button buttonSetLabel;
	private Button buttonGetDeviceId;
	private Button buttonGetAcc;
	
	private int BATTERY_LEVEL = 0;
	private int PULSE = 0;
	private Date DATETIME_PUSH_BUTTON = null;
	private Date DATETIME_RTC = null;
	private Date DATETIME_TIMESPAN = null;
	private int SDCARD_STATE = 0;
	private int numOfPushButton = 0;
	private BioLib.DataACC dataACC = null;
	private String deviceId = "";
	private String firmwareVersion = "";
	private byte accSensibility = 1;	// NOTE: 2G= 0, 4G= 1
	private byte typeRadioEvent = 0;
	private byte[] infoRadioEvent = null;
	private short countEvent = 0;
	
	private boolean isConn = false;
	
	private byte[][] ecg = null;
	private int nBytes = 0;
	
	private String accConf = "";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		
        // ###################################################
        // MACADDRESS:
        address = "00:23:FE:00:0B:59";
        // ###################################################
        
        
        text = (TextView) findViewById(R.id.lblStatus);
        text.setText("");
        
        textRTC = (TextView) findViewById(R.id.lblRTC);
    	textPUSH = (TextView) findViewById(R.id.lblButton);
    	textPULSE = (TextView) findViewById(R.id.lblPulse);
    	textBAT = (TextView) findViewById(R.id.lblBAT);
    	textDataReceived = (TextView) findViewById(R.id.lblData);
    	textSDCARD = (TextView) findViewById(R.id.lblSDCARD);
    	textACC = (TextView) findViewById(R.id.lblACC);
    	textHR = (TextView) findViewById(R.id.lblHR);
    	textECG = (TextView) findViewById(R.id.lblECG);
    	textDeviceId = (TextView) findViewById(R.id.lblDeviceId);
    	textRadioEvent = (TextView) findViewById(R.id.textRadioEvent);
    	textTimeSpan  = (TextView) findViewById(R.id.lblTimeSpan);
    	
    	try 
    	{
			lib = new BioLib(this, mHandler);
			text.append("Init BioLib \n");
		} 
    	catch (Exception e) 
		{
			text.append("Error to init BioLib \n");
			e.printStackTrace();
		}
    	
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	Connect();
            }

            /***
             * Connect to device.
             */
			private void Connect() 
			{	
				try 
				{
					deviceToConnect =  lib.mBluetoothAdapter.getRemoteDevice(address);
					
					Reset();
					
					text.setText("");
					lib.Connect(address, 5);
				} catch (Exception e) 
				{
					text.setText("Error to connect device: " + address);
					e.printStackTrace();
				}
			}

        });
        
        buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	Disconnect();
            }
        });
        
        buttonSetRTC = (Button) findViewById(R.id.buttonSetRTC);
        buttonSetRTC.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	try 
            	{
            		Date date = new Date();
					lib.SetRTC(date);
				} 
            	catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
        });
        
        buttonGetRTC = (Button) findViewById(R.id.buttonGetRTC);
        buttonGetRTC.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	try 
            	{
					lib.GetRTC();
				} 
            	catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
        });
        
        buttonRequest = (Button) findViewById(R.id.buttonRequestData);
        buttonRequest.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	RequestData();
            }

			private void RequestData() 
			{
				try
				{
					deviceToConnect =  lib.mBluetoothAdapter.getRemoteDevice(address);
				    
					Reset();
					text.setText("");
					lib.Request(address, 30);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();	
				}
			}
        });
        
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	Search(view);
            }

            /*
             * Search for bluetooth devices.
             */
			private void Search(View view) 
			{
				try
				{
					Intent myIntent = new Intent(view.getContext(), SearchDeviceActivity.class);
	                startActivityForResult(myIntent, 0);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();	
				}
			}
        });
        
        buttonSetLabel = (Button) findViewById(R.id.buttonSetLabel);
        buttonSetLabel.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	try 
            	{
            		/*
            		// SAMPLE 1: Sample of radio event: send array of bytes (10Bytes maximum)
            		byte type = 1;
            		// Maximum 10 bytes to send device [Optional]
            		byte[] info = new byte[4];
            		info[0] = 0x31; // 1 ascii table
            		info[1] = 0x32; // 2 ascii table
            		info[2] = 0x33; // 3 ascii table
            		info[3] = 0x34; // 4 ascii table
            		
            		textRadioEvent.setText("Start send");
					if (lib.SetBytesToRadioEvent(type, info))
					{
						countEvent++;
						textRadioEvent.setText("REvent: " + countEvent);
					}
					else
						textRadioEvent.setText("Error");
					*/
					
            		// SAMPLE 2: Sample of radio event: send string (10 char maximum)
					byte type = 2;
					String info = "5678";
					textRadioEvent.setText("Start send");
					if (lib.SetStringToRadioEvent(type, info))
					{
						countEvent++;
						textRadioEvent.setText("REvent: " + countEvent);
					}
					else
						textRadioEvent.setText("Error");
					
				} 
            	catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
        });
        
        buttonGetDeviceId = (Button) findViewById(R.id.buttonGetDeviceId);
        buttonGetDeviceId.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	try 
            	{
					lib.GetDeviceId();
				} 
            	catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
        });
        
        buttonGetAcc = (Button) findViewById(R.id.buttonGetAcc);
        buttonGetAcc.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	try 
            	{
					lib.GetAccSensibility();
				} 
            	catch (Exception e) 
				{
					e.printStackTrace();
				}
            }
        });
        
        buttonConnect.setEnabled(false);
        buttonRequest.setEnabled(false);
        buttonDisconnect.setEnabled(false);
        buttonGetRTC.setEnabled(false);
        buttonSetRTC.setEnabled(false);
        buttonSetLabel.setEnabled(false);
        buttonGetDeviceId.setEnabled(false);
        buttonGetAcc.setEnabled(false);
    }
    
    public void OnDestroy()
    {
    	if (isConn)
    	{
    		Disconnect();
    	}
    }
    
    protected void onDestroy() 
	{
        super.onDestroy();
        
        if (lib.mBluetoothAdapter != null) 
        {	
        	lib.mBluetoothAdapter.cancelDiscovery();
        }
        
        lib = null;
    }
    
    
    /***
     * Disconnect from device.
     */
    private void Disconnect()
	{
		try 
		{
			lib.Disconnect();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			Reset();
		}
	}
    
    /***
     * Reset variables and UI.
     */
    private void Reset() 
    {
		try
		{
			textBAT.setText("BAT: - - %");
	    	textPULSE.setText("PULSE: - - bpm");
	    	textPUSH.setText("PUSH-BUTTON: - - - ");
        	textRTC.setText("RTC: - - - ");
        	textDataReceived.setText("RECEIVED: - - - ");
        	textACC.setText("ACC:  X: - -  Y: - -  Z: - -");
        	textSDCARD.setText("SD CARD STATUS: - - ");
        	textECG.setText("Ecg stream: -- ");
        	textHR.setText("PEAK: --  BPMi: -- bpm  BPM: -- bpm  R-R: -- ms");
        	textBAT.setText("BAT: -- %");
        	textPULSE.setText("HR: -- bpm     Nb. Leads: -- ");
        	textDeviceId.setText("Device Id: - - - - - - - - - -");
        	textRadioEvent.setText(".");
        	textTimeSpan.setText("SPAN: - - - ");
        	
        	SDCARD_STATE = 0;
			BATTERY_LEVEL = 0;
			PULSE = 0;
			DATETIME_PUSH_BUTTON = null;
			DATETIME_RTC = null;
			DATETIME_TIMESPAN = null;
			numOfPushButton = 0;
			countEvent = 0;
			accConf = "";
			firmwareVersion = "";
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
    
    /**
     * The Handler that gets information back from the BioLib
     */
    private final Handler mHandler = new Handler() 
    {
        @Override
        public void handleMessage(Message msg) 
        {	
            switch (msg.what) 
            {	    
	            case BioLib.MESSAGE_READ:
	            	textDataReceived.setText("RECEIVED: " + msg.arg1);
	                break;
	                
	            case BioLib.MESSAGE_DEVICE_NAME:
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
	                text.append("Connected to " + mConnectedDeviceName + " \n");
	                break;
	                
	            case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
	            	Toast.makeText(getApplicationContext(), "Bluetooth NOT supported. Aborting! ", Toast.LENGTH_SHORT).show();
	            	text.append("Bluetooth NOT supported. Aborting! \n");
	            	isConn = false;
	            	break;
	            
	            case BioLib.MESSAGE_BLUETOOTH_ENABLED:
	            	Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
	            	text.append("Bluetooth is now enabled \n");
	            	text.append("Macaddress selected: " + address + " \n");
	            	buttonConnect.setEnabled(true);
	            	buttonRequest.setEnabled(true);
	            	break;
	            	
	            case BioLib.MESSAGE_BLUETOOTH_NOT_ENABLED:
	            	Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
	            	text.append("Bluetooth not enabled \n");
	            	isConn = false;
	            	break;
	            	
	            case BioLib.REQUEST_ENABLE_BT:
	            	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	                startActivityForResult(enableIntent, BioLib.REQUEST_ENABLE_BT);
	                text.append("Request bluetooth enable \n");
	                break;
	            	
	            case BioLib.STATE_CONNECTING:
                	text.append("   Connecting to device ... \n");
	            	break;
	                
	            case BioLib.STATE_CONNECTED:
	            	Toast.makeText(getApplicationContext(), "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show();
                	text.append("   Connect to " + deviceToConnect.getName() + " \n");
                	isConn = true;
                	
                	buttonConnect.setEnabled(false);
                	buttonRequest.setEnabled(false);
	                buttonDisconnect.setEnabled(true);
	                buttonGetRTC.setEnabled(true);
	                buttonSetRTC.setEnabled(true);
	                buttonSetLabel.setEnabled(true);
	                buttonGetDeviceId.setEnabled(true);
	                buttonGetAcc.setEnabled(true);
	                
	            	break;
	            	
	            case BioLib.UNABLE_TO_CONNECT_DEVICE:
	            	Toast.makeText(getApplicationContext(), "Unable to connect device! ", Toast.LENGTH_SHORT).show();
	            	text.append("   Unable to connect device \n");
	            	isConn = false;
	            	
	            	buttonConnect.setEnabled(true);
	            	buttonRequest.setEnabled(true);
	                buttonDisconnect.setEnabled(false);
	                buttonGetRTC.setEnabled(false);
	                buttonSetRTC.setEnabled(false);
	                buttonSetLabel.setEnabled(false);
	                buttonGetDeviceId.setEnabled(false);
	                buttonGetAcc.setEnabled(false);
	                
	            	break;
	            	
	            case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
	            	Toast.makeText(getApplicationContext(), "Device connection was lost", Toast.LENGTH_SHORT).show();
                	text.append("   Disconnected from " + deviceToConnect.getName() + " \n");
                	isConn = false;
                	
                	buttonConnect.setEnabled(true);
                	buttonRequest.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                    buttonGetRTC.setEnabled(false);
                    buttonSetRTC.setEnabled(false);
                    buttonSetLabel.setEnabled(false);
                    buttonGetDeviceId.setEnabled(false);
                    buttonGetAcc.setEnabled(false);
                    
	            	break;
	            	
	            case BioLib.MESSAGE_PUSH_BUTTON:
	            	DATETIME_PUSH_BUTTON = (Date)msg.obj;
	            	numOfPushButton = msg.arg1;
	            	textPUSH.setText("PUSH-BUTTON: [#" + numOfPushButton + "]" + DATETIME_PUSH_BUTTON.toString());
	            	break;
	            	
	            case BioLib.MESSAGE_RTC:
	            	DATETIME_RTC = (Date)msg.obj;
	            	textRTC.setText("RTC: " + DATETIME_RTC.toString());
	            	break;
	            	
	            case BioLib.MESSAGE_TIMESPAN:
	            	DATETIME_TIMESPAN = (Date)msg.obj;
	            	textTimeSpan.setText("SPAN: " + DATETIME_TIMESPAN.toString());
	            	break;
	            	
	            case BioLib.MESSAGE_DATA_UPDATED:
	            	BioLib.Output out = (BioLib.Output)msg.obj;
	            	BATTERY_LEVEL = out.battery;
	            	textBAT.setText("BAT: " + BATTERY_LEVEL + " %");
	            	PULSE = out.pulse;
	            	textPULSE.setText("HR: " + PULSE + " bpm     Nb. Leads: " + lib.GetNumberOfChannels());
	            	break;
	            	
	            case BioLib.MESSAGE_SDCARD_STATE:
	            	SDCARD_STATE = (int)msg.arg1;
	            	if (SDCARD_STATE == 1)
	            		textSDCARD.setText("SD CARD STATE: ON");
	            	else
	            		textSDCARD.setText("SD CARD STATE: OFF");
	            	break;
	            	
	            case BioLib.MESSAGE_RADIO_EVENT:
	            	textRadioEvent.setText("Radio-event: received ... ");
	            	
	            	typeRadioEvent = (byte)msg.arg1;
	            	infoRadioEvent = (byte[]) msg.obj;
	            	
	            	String str = "";
					try {
						str = new String(infoRadioEvent, "UTF8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	            	textRadioEvent.setText("Radio-event: " + typeRadioEvent + "[" + str + "]");
	            	break;
	            	
	            case BioLib.MESSAGE_FIRMWARE_VERSION:
	            	// Show firmware version in device VitalJacket ...
	            	firmwareVersion = (String)msg.obj;
	            	break;
	            	
	            case BioLib.MESSAGE_DEVICE_ID:
	            	deviceId = (String)msg.obj;
	            	textDeviceId.setText("Device Id: " + deviceId);
	            	break;
	            
	            case BioLib.MESSAGE_ACC_SENSIBILITY:
	            	accSensibility = (byte)msg.arg1;
	            	accConf = "4G";
	            	switch (accSensibility)
	            	{
	            		case 0:
	            			accConf = "2G";
	            			break;
	            			
	            		case 1:
	            			accConf = "4G";
	            			break;
	            	}
	            	
	            	textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
	            	break;
	            	
	            case BioLib.MESSAGE_PEAK_DETECTION:
	            	BioLib.QRS qrs = (BioLib.QRS)msg.obj;
	            	textHR.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");
	            	break;
	            	
	            case BioLib.MESSAGE_ACC_UPDATED:
	            	dataACC = (BioLib.DataACC)msg.obj;
	            	
	            	if (accConf == "")
	            		textACC.setText("ACC:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
	            	else
	            		textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
	            	
	            	break;
	            	
	            case BioLib.MESSAGE_ECG_STREAM:
	            	try
	            	{
						//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Bio.Library.namespace;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

						public class BioLib {
							private static final String TAG = "BioLib";
							private static final boolean D = false;
							public static final int MESSAGE_READ = 2;
							public static final int MESSAGE_WRITE = 3;
							public static final int MESSAGE_DEVICE_NAME = 4;
							public static final int MESSAGE_TOAST = 5;
							public static final int MESSAGE_BLUETOOTH_NOT_SUPPORTED = 6;
							public static final int MESSAGE_BLUETOOTH_ENABLED = 7;
							public static final int MESSAGE_BLUETOOTH_NOT_ENABLED = 8;
							public static final int REQUEST_ENABLE_BT = 9;
							public static final String TOAST = "toast";
							public static final int STATE_CONNECTING = 12;
							public static final int STATE_CONNECTED = 13;
							public static final int UNABLE_TO_CONNECT_DEVICE = 14;
							public static final int MESSAGE_DISCONNECT_TO_DEVICE = 15;
							public static final int MESSAGE_DATA_UPDATED = 16;
							public static final int MESSAGE_ACC_UPDATED = 17;
							public static final int MESSAGE_PUSH_BUTTON = 19;
							public static final int MESSAGE_RTC = 20;
							public static final int MESSAGE_TIMESPAN = 21;
							public static final int MESSAGE_SDCARD_STATE = 22;
							public static final int MESSAGE_PEAK_DETECTION = 23;
							public static final int MESSAGE_ECG_STREAM = 24;
							public static final int MESSAGE_RADIO_EVENT = 25;
							public static final int MESSAGE_DEVICE_ID = 26;
							public static final int MESSAGE_ACC_SENSIBILITY = 27;
							public static final int MESSAGE_FIRMWARE_VERSION = 28;
							private Context context;
							public BluetoothAdapter mBluetoothAdapter;
							private BluetoothService mRfcommService = null;
							private StringBuffer mOutStringBuffer;
							private BluetoothDevice deviceToConnect;
							private final Handler mHandlerUI;
							private static final int REQUEST_CONNECTION_TYPE = 1;
							private static final int ONLINE_CONNECTION_TYPE = 2;
							private QrsDetector qrs = null;
							private int[] table;
							private boolean init;
							private int indexLead;
							private int leadToAnalyse;
							private int state;
							private int countECG;
							private int stateACC;
							private int nBytesACC;
							private int countBytesACC;
							private DataACC dataACC;
							private int stateCommand;
							private int nBytesCommand;
							private int typeCommand;
							private int countBytesCommand;
							private int[] dataComnand;
							private String deviceId;
							private String firmwareVersion;
							private byte typeRadioEvent;
							private byte accSensibility;
							private byte[] infoRadioEvent;
							private int BATTERY_LEVEL;
							private int PULSE;
							private int SAMPLEFREQUENCY;
							private int NCHANNELS;
							private Date DATETIME_PUSH_BUTTON;
							private Date DATETIME_RTC;
							private int SDCARD_STATE;
							private Date DATETIME_TIMESPAN;
							private int numOfPushButton;
							private final int kBytes;
							private byte[][] ecg;
							private int nBytesECG;
							private int nQRSs;
							private int connection;
							private int time;
							private Thread worker;
							private final Handler mHandler;
							private byte[][] ecg2Send;
							private List<Integer> pulseQueue;
							private int sampleCount;
							private short windowCounter;
							private long oldPeak;
							private QRS peak;

							public BioLib(Context context, Handler handlerUI) throws Exception {
								this.table = Include.decode_table;
								this.init = true;
								this.indexLead = 0;
								this.leadToAnalyse = 1;
								this.state = 0;
								this.countECG = 0;
								this.stateACC = 0;
								this.nBytesACC = 0;
								this.countBytesACC = 0;
								this.dataACC = new DataACC();
								this.stateCommand = 0;
								this.nBytesCommand = 0;
								this.typeCommand = 0;
								this.countBytesCommand = 0;
								this.dataComnand = null;
								this.deviceId = "";
								this.firmwareVersion = "";
								this.typeRadioEvent = 0;
								this.accSensibility = 0;
								this.infoRadioEvent = null;
								this.BATTERY_LEVEL = 0;
								this.PULSE = 0;
								this.SAMPLEFREQUENCY = 500;
								this.NCHANNELS = 1;
								this.DATETIME_PUSH_BUTTON = null;
								this.DATETIME_RTC = null;
								this.SDCARD_STATE = 0;
								this.DATETIME_TIMESPAN = null;
								this.numOfPushButton = 0;
								this.kBytes = 500;
								this.ecg = null;
								this.nBytesECG = 0;
								this.nQRSs = 5;
								this.connection = 0;
								this.time = 30;
								this.worker = null;
								this.mHandler = new Handler() {
									public void handleMessage(Message msg) {
										try {
											switch (msg.what) {
												case 1:
													switch (msg.arg1) {
														case 0:
														case 1:
														default:
															return;
														case 2:
															BioLib.this.mHandlerUI.obtainMessage(12, 0, -1).sendToTarget();
															return;
														case 3:
															BioLib.this.mHandlerUI.obtainMessage(13, 0, -1).sendToTarget();

															try {
																BioLib.this.worker = new Thread((ThreadGroup)null, BioLib.this.new TimerCommandWorker((TimerCommandWorker)null));
																BioLib.this.worker.start();
															} catch (Exception var3) {
															}

															return;
													}
												case 2:
													BioLib.this.mHandlerUI.obtainMessage(2, msg.arg1, -1).sendToTarget();
													BioLib.this.TreatData();
													break;
												case 3:
													BioLib.this.mHandlerUI.obtainMessage(3, 1, -1).sendToTarget();
												case 4:
												default:
													break;
												case 5:
													if (msg.getData().toString().toLowerCase().contains("unable to connect device")) {
														BioLib.this.mHandlerUI.obtainMessage(14, 0, -1).sendToTarget();
														BioLib.this.mRfcommService = null;
													} else if (msg.getData().toString().toLowerCase().contains("device connection was lost")) {
														BioLib.this.mHandlerUI.obtainMessage(15, 0, -1).sendToTarget();
														BioLib.this.mRfcommService = null;
													}
											}
										} catch (Exception var4) {
										}

									}
								};
								this.ecg2Send = null;
								this.pulseQueue = new ArrayList();
								this.sampleCount = 0;
								this.windowCounter = 0;
								this.oldPeak = 0L;
								this.context = context;
								this.mHandlerUI = handlerUI;

								try {
									this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
									if (this.mBluetoothAdapter == null) {
										this.mHandlerUI.obtainMessage(6, 0, -1).sendToTarget();
									} else if (!this.mBluetoothAdapter.isEnabled()) {
										this.mHandlerUI.obtainMessage(9, 0, -1).sendToTarget();
									} else {
										this.mHandlerUI.obtainMessage(7, 0, -1).sendToTarget();
									}

								} catch (Exception var4) {
									Exception ex = var4;
									throw ex;
								}
							}

							public boolean Request(String address, int time) throws Exception {
								boolean result = false;

								try {
									this.connection = 1;
									if (time >= 30) {
										this.time = time;
									} else {
										this.time = 30;
									}

									if (this.mRfcommService == null) {
										this.Reset();
										this.mRfcommService = new BluetoothService(this.context, this.mHandler);
										this.mOutStringBuffer = new StringBuffer("");
										this.deviceToConnect = this.mBluetoothAdapter.getRemoteDevice(address);
										this.mRfcommService.connect(this.deviceToConnect, false);
									}

									result = true;
									return result;
								} catch (Exception var5) {
									Exception ex = var5;
									result = false;
									throw ex;
								}
							}

							public boolean Connect(String address, int nQRS) throws Exception {
								boolean result = false;

								try {
									this.connection = 2;
									this.nQRSs = nQRS;
									if (this.mRfcommService == null) {
										this.Reset();
										this.mRfcommService = new BluetoothService(this.context, this.mHandler);
										this.mOutStringBuffer = new StringBuffer("");
										this.deviceToConnect = this.mBluetoothAdapter.getRemoteDevice(address);
										this.mRfcommService.connect(this.deviceToConnect, false);
									}

									result = true;
									return result;
								} catch (Exception var5) {
									Exception ex = var5;
									result = false;
									throw ex;
								}
							}

							public boolean Disconnect() throws Exception {
								boolean result = true;

								try {
									if (this.mRfcommService.getState() == 3) {
										this.mRfcommService.stop();
										this.mRfcommService = null;
										this.worker.interrupt();
										this.worker = null;
									}

									result = true;
									return result;
								} catch (Exception var3) {
									Exception ex = var3;
									result = false;
									throw ex;
								}
							}

							private void Reset() {
								this.qrs = new QrsDetector((short)this.SAMPLEFREQUENCY, 3.0F, 0.3125F, (byte)8);
								this.init = true;
								this.leadToAnalyse = 1;
								this.indexLead = 0;
								this.state = 0;
								this.countECG = 0;
								this.dataACC = new DataACC();
								this.stateACC = 0;
								this.nBytesACC = 0;
								this.countBytesACC = 0;
								this.stateCommand = 0;
								this.nBytesCommand = 0;
								this.typeCommand = 0;
								this.countBytesCommand = 0;
								this.dataComnand = null;
								this.BATTERY_LEVEL = 0;
								this.PULSE = 0;
								this.dataACC = null;
							}

							private boolean TreatData() {
								try {
									for(int nBytes = this.mRfcommService.dataReceived.nBytes; nBytes > 0; --nBytes) {
										int databyte = this.mRfcommService.dataReceived.RdBuf();
										if (databyte <= 255) {
											databyte = this.table[databyte];
											int i;
											switch (this.state) {
												case 240:
													this.indexLead = 0;
													this.state = 0;
													break;
												case 241:
												case 243:
												case 244:
												case 246:
												case 247:
												case 248:
												case 249:
												default:
													if (databyte >= 240) {
														this.indexLead = 0;
														this.state = databyte;
													}
													break;
												case 242:
													if (databyte < 240) {
														if (this.init) {
															this.init = false;
															this.NCHANNELS = 1;
															this.ecg = new byte[1][500];
															this.ecg2Send = new byte[1][500];
															this.nBytesECG = 0;
														}

														this.Detector(databyte);
														this.ecg[0][this.nBytesECG] = (byte)databyte;
														++this.nBytesECG;
														if (this.nBytesECG >= 500) {
															for(i = 0; i < 500; ++i) {
																this.ecg2Send[0][i] = this.ecg[0][i];
															}

															this.mHandlerUI.obtainMessage(24, 0, -1, this.ecg2Send).sendToTarget();
															this.nBytesECG = 0;
														}

														++this.countECG;
													} else {
														this.state = databyte;
													}
													break;
												case 245:
													if (databyte >= 240) {
														this.state = databyte;
														this.indexLead = 0;
													} else {
														if (this.init) {
															this.init = false;
															this.NCHANNELS = 5;
															this.ecg = new byte[4][500];
															this.ecg2Send = new byte[4][500];
															this.nBytesECG = 0;
														}

														this.ecg[this.indexLead][this.nBytesECG] = (byte)databyte;
														++this.indexLead;
														if (this.leadToAnalyse == this.indexLead) {
															this.Detector(databyte);
															++this.countECG;
														}

														if (this.indexLead < 4) {
															continue;
														}

														++this.nBytesECG;
														if (this.nBytesECG >= 500) {
															for(i = 0; i < 500; ++i) {
																for(int j = 0; j < 4; ++j) {
																	this.ecg2Send[j][i] = this.ecg[j][i];
																}
															}

															this.mHandlerUI.obtainMessage(24, 0, -1, this.ecg2Send).sendToTarget();
															this.nBytesECG = 0;
														}

														this.indexLead = 0;
													}
													break;
												case 250:
													this.BATTERY_LEVEL = databyte;
													this.indexLead = 0;
													this.state = 0;
													break;
												case 251:
													switch (this.stateACC) {
														case 0:
															this.nBytesACC = databyte;
															this.countBytesACC = 0;
															this.dataACC = new DataACC();
															++this.stateACC;
															continue;
														case 1:
															switch (this.countBytesACC) {
																case 0:
																	this.dataACC.X = (byte)databyte;
																	break;
																case 1:
																	this.dataACC.Y = (byte)databyte;
																	break;
																case 2:
																	this.dataACC.Z = (byte)databyte;
															}

															++this.countBytesACC;
															if (this.countBytesACC >= this.nBytesACC) {
																this.mHandlerUI.obtainMessage(17, 0, -1, this.dataACC).sendToTarget();
																this.countBytesACC = 0;
																this.nBytesACC = 0;
																this.stateACC = 0;
																this.indexLead = 0;
																this.state = 0;
															}
														default:
															continue;
													}
												case 252:
													switch (this.stateCommand) {
														case 0:
															this.nBytesCommand = databyte;
															this.typeCommand = 0;
															this.countBytesCommand = 0;
															++this.stateCommand;
															break;
														case 1:
															this.typeCommand = databyte;
															this.dataComnand = new int[this.nBytesCommand - 1];
															++this.countBytesCommand;
															++this.stateCommand;
															break;
														case 2:
															this.dataComnand[this.countBytesCommand - 1] = databyte;
															++this.countBytesCommand;
															if (this.countBytesCommand >= this.nBytesCommand) {
																this.ParserCommand(this.typeCommand, this.dataComnand);
																this.stateCommand = 0;
																this.typeCommand = 0;
																this.countBytesCommand = 0;
																this.dataComnand = null;
																this.indexLead = 0;
																this.state = 0;
															}
													}
											}
										}
									}

									Output out;
									if (this.connection == 1 && this.countECG >= this.time * this.SAMPLEFREQUENCY) {
										out = this.GetData();
										this.mHandlerUI.obtainMessage(16, 0, -1, out).sendToTarget();
										this.Disconnect();
									} else if (this.connection == 2 && this.countECG >= 10 * this.SAMPLEFREQUENCY) {
										out = this.GetData();
										this.mHandlerUI.obtainMessage(16, 0, -1, out).sendToTarget();
									}

									return true;
								} catch (Exception var5) {
									this.indexLead = 0;
									this.state = 0;
									return false;
								}
							}

							private Output GetData() {
								Output out = new Output();
								out.battery = this.BATTERY_LEVEL;
								out.pulse = this.PULSE;
								out.accValues = this.dataACC;
								return out;
							}

							private boolean ParserCommand(int type, int[] data) throws Exception {
								boolean result = false;

								try {
									int i;
									int nHours;
									int day;
									int month;
									int year;
									Exception ex1;
									SimpleDateFormat format;
									byte[] info;
									int i;
									String date;
									switch (type) {
										case 4:
											i = Utils.BCDtoInteger(data[0]);
											i = Utils.BCDtoInteger(data[1]);
											nHours = Utils.BCDtoInteger(data[2]);
											day = Utils.BCDtoInteger(data[3]);
											month = Utils.BCDtoInteger(data[4]);
											year = Utils.BCDtoInteger(data[5]) + 2000;

											try {
												date = year + "-" + month + "-" + day + " " + nHours + ":" + i + ":" + i;
												format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
												this.DATETIME_RTC = ((DateFormat)format).parse(date);
												this.mHandlerUI.obtainMessage(20, 0, -1, this.DATETIME_RTC).sendToTarget();
												result = true;
											} catch (Exception var13) {
												ex1 = var13;
												this.DATETIME_RTC = null;
												result = false;
												throw ex1;
											}
										case 5:
										case 6:
										case 8:
										case 9:
										case 12:
										case 13:
										case 14:
										case 15:
										case 18:
										case 20:
										default:
											break;
										case 7:
											this.numOfPushButton = data[0];
											i = Utils.BCDtoInteger(data[1]);
											i = Utils.BCDtoInteger(data[2]);
											nHours = Utils.BCDtoInteger(data[3]);
											day = Utils.BCDtoInteger(data[4]);
											month = Utils.BCDtoInteger(data[5]);
											year = Utils.BCDtoInteger(data[6]) + 2000;

											try {
												date = year + "-" + month + "-" + day + " " + nHours + ":" + i + ":" + i;
												format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
												this.DATETIME_PUSH_BUTTON = ((DateFormat)format).parse(date);
												this.mHandlerUI.obtainMessage(19, this.numOfPushButton, -1, this.DATETIME_PUSH_BUTTON).sendToTarget();
												result = true;
												break;
											} catch (Exception var14) {
												ex1 = var14;
												this.DATETIME_PUSH_BUTTON = null;
												result = false;
												throw ex1;
											}
										case 10:
											i = Utils.BCDtoInteger(data[0]);
											i = Utils.BCDtoInteger(data[1]);
											nHours = Utils.BCDtoInteger(data[2]);
											day = Utils.BCDtoInteger(data[3]);
											month = Utils.BCDtoInteger(data[4]);
											year = Utils.BCDtoInteger(data[5]) + 2000;

											try {
												date = year + "-" + month + "-" + day + " " + nHours + ":" + i + ":" + i;
												format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
												this.DATETIME_TIMESPAN = ((DateFormat)format).parse(date);
												this.mHandlerUI.obtainMessage(21, 0, -1, this.DATETIME_TIMESPAN).sendToTarget();
												result = true;
												break;
											} catch (Exception var12) {
												ex1 = var12;
												this.DATETIME_TIMESPAN = null;
												result = false;
												throw ex1;
											}
										case 11:
											this.SDCARD_STATE = data[0];
											this.mHandlerUI.obtainMessage(22, this.SDCARD_STATE, -1, -1).sendToTarget();
											result = true;
											break;
										case 16:
											info = new byte[data.length];

											for(i = 0; i < data.length - 1; ++i) {
												info[i] = (byte)data[i];
											}

											this.deviceId = new String(info, "UTF8");
											this.mHandlerUI.obtainMessage(26, 0, -1, this.deviceId).sendToTarget();
											result = true;
											break;
										case 17:
											this.typeRadioEvent = (byte)data[0];
											if (data.length > 2) {
												this.infoRadioEvent = new byte[data.length - 2];

												for(i = 1; i < data.length - 1; ++i) {
													this.infoRadioEvent[i - 1] = (byte)data[i];
												}
											} else {
												this.infoRadioEvent = null;
											}

											this.mHandlerUI.obtainMessage(25, this.typeRadioEvent, -1, this.infoRadioEvent).sendToTarget();
											result = true;
											break;
										case 19:
											this.accSensibility = (byte)data[0];
											this.mHandlerUI.obtainMessage(27, this.accSensibility, -1, -1).sendToTarget();
											result = true;
											break;
										case 21:
											info = new byte[data.length];

											for(i = 0; i < data.length - 1; ++i) {
												info[i] = (byte)data[i];
											}

											this.firmwareVersion = new String(info, "UTF8");
											this.mHandlerUI.obtainMessage(28, 0, -1, this.firmwareVersion).sendToTarget();
											result = true;
									}

									return result;
								} catch (Exception var15) {
									Exception ex = var15;
									result = false;
									throw ex;
								}
							}

							public boolean SetRTC(Date date) throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 8, 6, Utils.DECtoBCD(date.getSeconds()), Utils.DECtoBCD(date.getMinutes()), Utils.DECtoBCD(date.getHours()), Utils.DECtoBCD(date.getDate()), Utils.DECtoBCD(date.getMonth() + 1), 0, 0};
									int year = date.getYear() - 100;
									data[8] = Utils.DECtoBCD(year);
									data[9] = Utils.Get_CRC(data, 9);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var5) {
									Exception ex = var5;
									result = false;
									throw ex;
								}
							}

							public boolean GetRTC() throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 2, 4, 0};
									data[3] = Utils.Get_CRC(data, 3);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var3) {
									Exception ex = var3;
									result = false;
									throw ex;
								}
							}

							public boolean SetStringToRadioEvent(byte type, String info) throws Exception {
								boolean result = false;

								try {
									int nBytes = info.length();
									byte[] data = new byte[5 + nBytes];
									int index = false;
									char[] buffer = info.toCharArray();
									data[0] = -4;
									data[1] = (byte)(3 + nBytes);
									data[2] = 17;
									data[3] = type;
									int index = 4;
									if (nBytes > 0) {
										for(int i = 0; i < nBytes; ++i) {
											data[index] = (byte)buffer[i];
											++index;
										}
									}

									data[index] = Utils.Get_CRC(data, index);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var9) {
									Exception ex = var9;
									result = false;
									throw ex;
								}
							}

							public boolean SetBytesToRadioEvent(byte type, byte[] info) throws Exception {
								boolean result = false;

								try {
									int nBytes = info.length;
									byte[] data = new byte[5 + nBytes];
									int index = false;
									data[0] = -4;
									data[1] = (byte)(3 + nBytes);
									data[2] = 17;
									data[3] = type;
									int index = 4;
									if (nBytes > 0) {
										for(int i = 0; i < nBytes; ++i) {
											data[index] = info[i];
											++index;
										}
									}

									data[index] = Utils.Get_CRC(data, index);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var8) {
									Exception ex = var8;
									result = false;
									throw ex;
								}
							}

							public boolean GetDeviceId() throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 2, 16, 0};
									data[3] = Utils.Get_CRC(data, 3);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var3) {
									Exception ex = var3;
									result = false;
									throw ex;
								}
							}

							public boolean SetAccSensibility(AccSensibility sensibility) throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 3, 18, (byte)sensibility.getValue(), 0};
									data[4] = Utils.Get_CRC(data, 4);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var4) {
									Exception ex = var4;
									result = false;
									throw ex;
								}
							}

							public boolean GetAccSensibility() throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 2, 19, 0};
									data[3] = Utils.Get_CRC(data, 3);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var3) {
									Exception ex = var3;
									result = false;
									throw ex;
								}
							}

							public boolean GetFirmwareVersion() throws Exception {
								boolean result = false;

								try {
									byte[] data = new byte[]{-4, 2, 21, 0};
									data[3] = Utils.Get_CRC(data, 3);
									result = this.SendDataToBluetooth(data);
									return result;
								} catch (Exception var3) {
									Exception ex = var3;
									result = false;
									throw ex;
								}
							}

							public int GetNumberOfChannels() {
								return this.NCHANNELS;
							}

							public int GetLeadToAnalyse() {
								return this.leadToAnalyse;
							}

							private boolean Detector(int databyte) throws Exception {
								boolean result = false;

								try {
									++this.sampleCount;
									if (this.windowCounter <= this.SAMPLEFREQUENCY * 2) {
										++this.windowCounter;
									} else {
										while(this.pulseQueue.size() > this.nQRSs) {
											this.pulseQueue.remove(0);
										}

										int[] data = new int[this.pulseQueue.size()];

										for(int i = 0; i < this.pulseQueue.size(); ++i) {
											data[i] = (Integer)this.pulseQueue.get(i);
										}

										this.PULSE = (int)((double)Utils.mean(data, data.length) + 0.5);
										this.windowCounter = 0;
									}

									int delay = this.qrs.QRSDet(databyte * 10, 0, (long)this.sampleCount);
									if (delay != 0) {
										long DetectionTime = (long)(this.sampleCount - delay);
										long rr = DetectionTime - this.oldPeak;
										float r_r = Utils.GetRR((int)rr, this.SAMPLEFREQUENCY);
										int pulse = (int)Utils.GetPulse(r_r);
										if (pulse >= 30 && pulse <= 250) {
											this.pulseQueue.add(pulse);
											this.peak = new QRS();
											this.peak.position = DetectionTime;
											this.peak.rr = (int)r_r;
											this.peak.bpmi = pulse;
											this.peak.bpm = this.PULSE;
											if (this.connection == 2) {
												this.mHandlerUI.obtainMessage(23, 0, -1, this.peak).sendToTarget();
											}
										}

										this.oldPeak = DetectionTime;
									}

									result = true;
									return result;
								} catch (Exception var10) {
									Exception ex = var10;
									result = false;
									throw ex;
								}
							}

							private boolean SendDataToBluetooth(byte[] data) throws Exception {
								boolean result = false;

								try {
									if (this.mRfcommService.getState() != 3) {
										return false;
									} else {
										if (data.length > 0 && this.mRfcommService != null) {
											this.mRfcommService.write(data);
											this.mOutStringBuffer.setLength(0);
										}

										result = true;
										return result;
									}
								} catch (Exception var4) {
									Exception ex = var4;
									result = false;
									throw ex;
								}
							}

							public static enum AccSensibility {
								acc_2G(0),
								acc_4G(1);

								private int value;

								private AccSensibility(int val) {
									this.value = val;
								}

								public int getValue() {
									return this.value;
								}
							}

							public class DataACC {
								public byte X = 0;
								public byte Y = 0;
								public byte Z = 0;

								public DataACC() {
								}
							}

							public class Output {
								public int pulse = 0;
								public DataACC accValues = BioLib.this.new DataACC();
								public int battery = 0;

								public Output() {
								}
							}

							public class QRS {
								public long position = 0L;
								public int rr = 0;
								public int bpmi = 0;
								public int bpm = 0;

								public QRS() {
								}
							}

							private class TimerCommandWorker implements Runnable {
								private final int SLEEP_TIME;

								private TimerCommandWorker() {
									this.SLEEP_TIME = 2000;
								}

								public void run() {
									while(true) {
										try {
											Thread.sleep(2000L);
										} catch (InterruptedException var3) {
											break;
										}

										try {
											if (BioLib.this.mRfcommService.getState() == 3) {
												this.GetECG();
											}
										} catch (Exception var2) {
											break;
										}
									}

								}

								private void GetECG() throws Exception {
									try {
										byte[] data = new byte[]{-1};
										BioLib.this.SendDataToBluetooth(data);
									} catch (Exception var2) {
										Exception ex = var2;
										throw ex;
									}
								}
							}
						}

						int nLeads = ecg.length;
	            		nBytes = ecg[0].length;
	            		textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);
	            	}
	            	catch (Exception ex)
	            	{
	            		textECG.setText("ERROR in ecg stream");
	            	}
	            	break;
	            	
	            case BioLib.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
	                break;
            }
        }
    };
    
    /*
     * 
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {	
        switch (requestCode) 
        {	
	        case BioLib.REQUEST_ENABLE_BT:
	            if (resultCode == Activity.RESULT_OK) 
	            {
	            	Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
	            	text.append("Bluetooth is now enabled \n");
	            	
	            	buttonConnect.setEnabled(true);
	            	buttonRequest.setEnabled(true);
	                buttonDisconnect.setEnabled(false);
	                buttonGetRTC.setEnabled(false);
	                buttonSetRTC.setEnabled(false);
	                buttonSetLabel.setEnabled(false);
	                buttonGetDeviceId.setEnabled(false);
	                buttonGetAcc.setEnabled(false);
	                
	                text.append("Macaddress selected: " + address + " \n");
	            } 
	            else 
	            {
	            	Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
	            	text.append("Bluetooth not enabled \n");
	            	isConn = false;
	            	
	            	buttonConnect.setEnabled(false);
	            	buttonRequest.setEnabled(false);
	                buttonDisconnect.setEnabled(false);
	                buttonGetRTC.setEnabled(false);
	                buttonSetRTC.setEnabled(false);
	                buttonSetLabel.setEnabled(false);
	                buttonGetDeviceId.setEnabled(false);
	                buttonGetAcc.setEnabled(false);
	            }
	            break;
	            
	        case 0:
	        	switch (resultCode)
	        	{
	        		case SearchDeviceActivity.CHANGE_MACADDRESS:
	        			try
	        			{
	        				text.append("\nSelect new macaddress: ");
	        				macaddress = data.getExtras().getString(SearchDeviceActivity.SELECT_DEVICE_ADDRESS);
	        				Toast.makeText(getApplicationContext(), macaddress, Toast.LENGTH_SHORT).show();
	        				
	        				text.append(macaddress);
	        				
	        				address = macaddress;
	        			}
	        			catch (Exception ex)
	        			{
	        				Toast.makeText(getApplicationContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
	        			}
	        			break;
	        	}
    			break;
        }
    }
    
}