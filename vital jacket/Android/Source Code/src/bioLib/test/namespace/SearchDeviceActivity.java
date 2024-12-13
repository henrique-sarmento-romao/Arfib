package bioLib.test.namespace;

import java.util.ArrayList;
import java.util.Set;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class SearchDeviceActivity extends Activity 
{
	public static String SELECT_DEVICE_ADDRESS = "device_address";
	public static final int CHANGE_MACADDRESS = 100;
	private ListView mainListView ;  
	private ArrayAdapter<String> listAdapter;
	private String selectedValue = "";
	private BluetoothAdapter mBluetoothAdapter = null;
	private Button buttonOK;
	
	
	/**
	 * 
	 * @return
	 */
	public String GetMacAddress()
	{
		return selectedValue;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        buttonOK = (Button) findViewById(R.id.cmdOK);
        buttonOK.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	Intent intent = new Intent();
                intent.putExtra(SELECT_DEVICE_ADDRESS, selectedValue);

                // Set result and finish this Activity
                setResult(CHANGE_MACADDRESS, intent);
                finish();
            }
            
        });
        
        try
        {
        	mainListView = (ListView) findViewById( R.id.lstDevices);
        	
        	ArrayList<String> lstDevices = new ArrayList<String>();  
              
            // Create ArrayAdapter using the planet list.  
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, lstDevices);
            
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter != null) 
			{
				if (mBluetoothAdapter.isEnabled())
				{
					// Listing paired devices
	    		    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
	    		    for (BluetoothDevice device : devices) 
	    		    {
	    		    	listAdapter.add(device.getAddress() + "   " + device.getName());
	    		    }
				}
			}
			mainListView.setAdapter( listAdapter );
            
            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
            {  
                @Override  
                public void onItemClick( AdapterView<?> parent, View item, int position, long id) 
                {  
                	selectedValue = (String) listAdapter.getItem(position);
            		
                	String[] aux = selectedValue.split("   ");
                	selectedValue = aux[0];
                }  
            });  
        }
        catch (Exception ex)
        {
        	
        }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_search_device, menu);
        return true;
    }
    
}
