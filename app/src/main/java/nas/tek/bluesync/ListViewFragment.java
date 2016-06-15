package nas.tek.bluesync;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ListViewFragment extends Fragment {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private List<ListItem> mListItems;
    private AlarmReceiver alarm = new AlarmReceiver();

    //Realm Database

    private Realm realm;
    private RealmConfiguration realmConfig;

    //Ensures Proper Toast creation
    private boolean shouldExecuteOnPause = false;
    SharedPreferences prefs = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        prefs = this.getActivity().getSharedPreferences("nas.tek.bluesync", Context.MODE_PRIVATE);

        realmConfig = new RealmConfiguration.Builder(getActivity()).build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ListItem one = realm.createObject(ListItem.class);
                    one.setTitle("Journal");
                    one.setRfid(ConstantHelper.ONE);

                    ListItem two = realm.createObject(ListItem.class);
                    two.setTitle("Lab Coat");
                    two.setRfid(ConstantHelper.TWO);

                    ListItem three = realm.createObject(ListItem.class);
                    three.setTitle("EVS Book");
                    three.setRfid(ConstantHelper.THREE);

                    ListItem four = realm.createObject(ListItem.class);
                    four.setTitle("Bottle");
                    four.setRfid(ConstantHelper.FOUR);

                    ListItem five = realm.createObject(ListItem.class);
                    five.setTitle("Umbrella");
                    five.setRfid(ConstantHelper.FIVE);

                    ListItem six = realm.createObject(ListItem.class);
                    six.setTitle("Bag Locked");
                    six.setRfid(ConstantHelper.SIX);

                }
            });

            prefs.edit().putBoolean("firstrun", false).apply();
        }
        mListItems = new ArrayList<>();
        //mBluetoothSerial = new BluetoothSerial(getActivity(),this);
        shouldExecuteOnPause = true;

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_list,container,false);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.fragment_list_item_recycler_view);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();



        RealmQuery<ListItem> query = realm.where(ListItem.class);
        final RealmResults<ListItem> result = query.findAll();

        if(result.size()!=0) {
            mListItems = new ArrayList<>();
            for (ListItem li : result) {
                mListItems.add(li);
            }
        }
        mAdapter = new ItemAdapter(mListItems);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                mAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        shouldExecuteOnPause = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isMyServiceRunning(ReadService.class)) {
            realm.close();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_enable_notification:
                alarm.setAlarm(getActivity());
                Toast.makeText(getActivity(), "Alarm set for 6:45 am", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_disable_notification:
                alarm.cancelAlarm(getActivity());
                Toast.makeText(getActivity(), "Reminder cancelled", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_start_service:
                if (!isMyServiceRunning(ReadService.class)) {
                    Intent i = new Intent(getActivity(), ReadService.class);
                    getActivity().startService(i);
                }
                return true;
            case R.id.menu_stop_service:
                Intent i = new Intent(getActivity(), ReadService.class);
                getActivity().stopService(i);
                return true;
        }
        return false;
    }

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
