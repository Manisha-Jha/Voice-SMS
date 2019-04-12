package com.android.learning.speechsms;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    List<String> nameList = new ArrayList<String>();
    List<String> phoneList = new ArrayList<String>();
    ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getAllContacts(this.getContentResolver());
        ListView lv= (ListView) findViewById(R.id.lvContacts);
        adapter = new ContactsAdapter();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        lv.setItemsCanFocus(false);

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        adapter.toggle(arg2);
    }

    /**
     * Method to get all the contacts available in the Contacts
     * @param cr
     */
    public  void getAllContacts(ContentResolver cr) {

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            nameList.add(name);
            phoneList.add(phoneNumber);
        }

        phones.close();
    }

    public void cancelSelection(View view) {
        onBackPressed();
    }

    /**
     * Method to identify the selected contacts,
     * get the numbers for the selected contacts,
     * create the string
     * and send it back to MainActivity
     * @param view
     */
    public void getSelectedContacts(View view) {
        StringBuilder checkedcontacts= new StringBuilder();

        boolean isSelected = false;
        for(int i = 0; i < nameList.size(); i++) {
            if(adapter.mCheckStates.get(i))
            {
                isSelected = true;
                checkedcontacts.append(phoneList.get(i));
                checkedcontacts.append(", ");

            }

        }
        if(isSelected) {
            String numbers = checkedcontacts.subSequence(0, checkedcontacts.length() - 2).toString();
            Intent data = new Intent();
            data.putExtra(MainActivity.SELECTED_CONTACT_NUMBERS, numbers);
            setResult(RESULT_OK, data);
            finish();
        }else{
            Toast.makeText(this, getString(R.string.select_a_contact_text), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Adapter class to display the contacts with the details such as name and number,
     * along with a checkbox to identify whether the contact is selected or not
     */
    class ContactsAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener
    {
        private SparseBooleanArray mCheckStates;
        LayoutInflater mInflater;
        TextView tvName, tvNumber;
        CheckBox cbSelection;
        ContactsAdapter() {
            mCheckStates = new SparseBooleanArray(nameList.size());
            mInflater = (LayoutInflater)ContactListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return nameList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub

            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view = convertView;
            if(convertView==null)
                view = mInflater.inflate(R.layout.contact_item, null);
            tvName = (TextView) view.findViewById(R.id.tvContactNum);
            tvNumber = (TextView) view.findViewById(R.id.tvContactName);
            cbSelection = (CheckBox) view.findViewById(R.id.cbSelection);

            tvNumber.setText(nameList.get(position));
            tvName.setText(phoneList.get(position));

            cbSelection.setTag(position);
            cbSelection.setChecked(mCheckStates.get(position, false));
            cbSelection.setOnCheckedChangeListener(this);

            return view;
        }
        public boolean isChecked(int position) {
            return mCheckStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
            mCheckStates.put(position, isChecked);
        }

        public void toggle(int position) {
            setChecked(position, !isChecked(position));
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // TODO Auto-generated method stub

            mCheckStates.put((Integer) buttonView.getTag(), isChecked);
        }
    }
}
