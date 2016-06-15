package nas.tek.bluesync;

import android.app.Fragment;
import android.os.Bundle;


public class MainActivity extends SingleFragmentActivity {
    private AlarmReceiver alarm = new AlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

    }

    @Override
    protected Fragment createFragment() {
        return new ListViewFragment();
    }
}
