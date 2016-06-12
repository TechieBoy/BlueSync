package nas.tek.bluesync;

import java.util.ArrayList;
import java.util.List;

/*
*Storage Class which contains a stash of listItems to  dislplay
 */
public class Inventory {
    private static Inventory sInventory;
    private List<ListItem> mListItems;

    public static Inventory get(){
        if(sInventory==null){
            sInventory = new Inventory();
        }
        return sInventory;
    }

    public List<ListItem> getItems(){
        return mListItems;
    }

    private Inventory(){
        mListItems = new ArrayList<>();

        ListItem one = new ListItem();
        one.setTitle("Journal");
        one.setRfid(ConstantHelper.ONE);
        mListItems.add(one);

        ListItem two = new ListItem();
        two.setTitle("Lab Coat");
        two.setRfid(ConstantHelper.TWO);
        mListItems.add(two);

        ListItem three = new ListItem();
        three.setTitle("EVS Book");
        three.setRfid(ConstantHelper.THREE);
        mListItems.add(three);

        ListItem four = new ListItem();
        four.setTitle("Bottle");
        four.setRfid(ConstantHelper.FOUR);
        mListItems.add(four);

        ListItem five = new ListItem();
        five.setTitle("Umbrella");
        five.setRfid(ConstantHelper.FIVE);
        mListItems.add(five);

        ListItem six = new ListItem();
        six.setTitle("Bag Locked");
        six.setRfid(ConstantHelper.SIX);
        mListItems.add(six);

        for (int i = 0; i < mListItems.size(); i++) {
            mListItems.get(i).setPresent(i%2==0);
        }


    }

}
