package nas.tek.bluesync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import nas.tek.bluesync.BThelper.BluetoothSerial;
import nas.tek.bluesync.BThelper.BluetoothSerialListener;

public class ReadService extends Service implements BluetoothSerialListener{

    private BluetoothSerial mBluetoothSerial;
    private Realm realm;
    private RealmConfiguration realmConfig;
    private List<ListItem> mListItems;
    private boolean isDestroyed = false;

    @Override
    public void onCreate() {
        mBluetoothSerial = new BluetoothSerial(this,this);
        mBluetoothSerial.setup();
        if (mBluetoothSerial.checkBluetooth() && mBluetoothSerial.isBluetoothEnabled()) {
            if (!mBluetoothSerial.isConnected()) {
                mBluetoothSerial.start();
            }
        }
        mBluetoothSerial.connect(ConstantHelper.address);

        realmConfig = new RealmConfiguration.Builder(getApplicationContext()).build();
        realm = Realm.getInstance(realmConfig);

        RealmQuery<ListItem> query = realm.where(ListItem.class);
        final RealmResults<ListItem> result = query.findAll();

        if(result.size()!=0) {
            mListItems = new ArrayList<>();
            for (ListItem li : result) {
                mListItems.add(li);
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) {
            Toast.makeText(this, "Listening in Background", Toast.LENGTH_LONG).show();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
        mBluetoothSerial.stop();
        realm.close();
        Toast.makeText(this,"stopped Listening",Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //BTSerialListener

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        if(!isDestroyed)
        Toast.makeText(this, "Connected to  " + name + " with address  " + address, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBluetoothNotSupported() {

    }

    @Override
    public void onBluetoothDisabled() {
        if(!isDestroyed) {
            Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        if(!isDestroyed) {
            Toast.makeText(this, "Device disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectingBluetoothDevice() {

    }

    @Override
    public void onBluetoothSerialRead(String message) {
        String string = "";

        //Regex to format received Data
        Pattern pattern = Pattern.compile("ID:(.*?):ID");
        Matcher matcher = pattern.matcher(message);
        if(matcher.find()) {
            string = ConstantHelper.toHex(matcher.group(1).trim());
            synchronized (mListItems) {
                for (int i = 0; i < mListItems.size(); i++) {
                    final int j=i;
                    if(mListItems.get(j).getRfid().equals(string)){
                        final boolean isPresent = mListItems.get(j).isPresent();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                mListItems.get(j).setPresent(!isPresent);
                            }
                        });
                        Toast.makeText(this,"Updated item",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }


    @Override
    public void onBluetoothSerialWrite(String message) {

    }
}
