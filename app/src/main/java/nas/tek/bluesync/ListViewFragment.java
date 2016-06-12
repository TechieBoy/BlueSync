package nas.tek.bluesync;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
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

public class ListViewFragment extends Fragment implements BluetoothSerialListener{

    private BluetoothSerial mBluetoothSerial;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private List<ListItem> listitems;

    //Realm Database
    private Realm realm;
    private RealmConfiguration realmConfig;

    //Ensures Proper Toast creation
    private boolean shouldExecuteOnPause = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        realmConfig = new RealmConfiguration.Builder(getActivity()).build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        RealmQuery<ListItem> query = realm.where(ListItem.class);
        RealmResults<ListItem> result = query.findAll();

        if(result.size()!=0){
            List<ListItem> managedListItems = new ArrayList<>();
            for(ListItem li: result){
                managedListItems.add(li);
            }
            listitems = new ArrayList<>();
            listitems.addAll(managedListItems);
        }else if(result.size()==0) {
            listitems = Inventory.get().getItems();
        }


        mAdapter = new ItemAdapter(listitems);
        mBluetoothSerial = new BluetoothSerial(getActivity(),this);
        shouldExecuteOnPause = true;



    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_list,container,false);

        mRecyclerView = (RecyclerView)v.findViewById(R.id.fragment_list_item_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBluetoothSerial.setup();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothSerial.checkBluetooth() && mBluetoothSerial.isBluetoothEnabled()) {
            if (!mBluetoothSerial.isConnected()) {
                mBluetoothSerial.start();
            }
        }
        mBluetoothSerial.connect(ConstantHelper.address);



    }

    @Override
    public void onPause() {
        super.onPause();
        shouldExecuteOnPause = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mBluetoothSerial.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for(ListItem li:listitems){
                    ListItem managedListItem = realm.copyToRealm(li);
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BLUETOOTH:
                if(resultCode== Activity.RESULT_OK){
                    mBluetoothSerial.setup();
                }
                break;
        }
    }

    //Implemented methods of BluetoothSerialListener
    //========================================================================================


    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        Toast.makeText(getActivity(), "Connected to  " + name + " with address  " + address, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(getActivity())
                .setMessage("No Bluetooth")
                .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        if(shouldExecuteOnPause) {
            Toast.makeText(getActivity(), "Device disconnected. Please ensure device is in range and paired", Toast.LENGTH_SHORT).show();
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
            synchronized (listitems) {
                for (int i = 0; i < listitems.size(); i++) {
                    final int j=i;
                    if(listitems.get(j).getRfid().equals(string)){
                        final boolean isPresent = listitems.get(j).isPresent();
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                listitems.get(j).setPresent(!isPresent);
                            }
                        });
                        mAdapter.update(j);
                    }
                }
            }
        }

    }

    @Override
    public void onBluetoothSerialWrite(String message) {

    }

    //=============================================================================================


    //ViewHolder for RecyclerView
    private class ItemHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView;
        private CheckBox mPresentCheckBox;
        private ListItem mItem;

        public ItemHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView)itemView.findViewById(R.id.single_item_textView);
            mPresentCheckBox = (CheckBox)itemView.findViewById(R.id.single_item_checkBox);
        }

        public void bindItem(ListItem item){
            mItem = item;
            mTitleTextView.setText(mItem.getTitle());
            mPresentCheckBox.setChecked(mItem.isPresent());
        }
    }

    //Adapter for RecyclerView
    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{

        private List<ListItem> mListItems;
        public ItemAdapter(List<ListItem> listItems) {
            mListItems = listItems;
        }

        public void update(int i) {
            notifyItemChanged(i);
        }


        @Override
        public int getItemCount() {
            return mListItems.size();
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.single_item,parent,false);
            return new ItemHolder(v);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            final ListItem listItem = mListItems.get(position);
            holder.bindItem(listItem);
            holder.mPresentCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isPresent = listItem.isPresent();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            listItem.setPresent(!isPresent);
                        }
                    });
                }
            });
        }
    }
}
