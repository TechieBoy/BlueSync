package nas.tek.bluesync;

import io.realm.RealmObject;

public class ListItem  extends RealmObject{
    private String mTitle;
    private String mRfid;
    private boolean mPresent;

    public boolean isPresent() {
        return mPresent;
    }

    public void setPresent(boolean present) {
        mPresent = present;
    }

    public String getRfid() {
        return mRfid;
    }

    public void setRfid(String rfid) {
        mRfid = rfid;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
