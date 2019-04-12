package com.android.learning.speechsms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static final String SELECTED_CONTACT_NUMBERS = "contact_numbers";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1001;
    private static final int PERMISSION_REQUEST_CALL_PHONE = 2001;
    private static final int PERMISSION_REQUEST_SEND_SMS = 3001;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 4001;
    private static final int REQ_CONTACT_LIST = 1005;
    SpeechRecognizer speech;
    Intent recognizerIntent;
    String message;
    EditText etMessage;
    AutoCompleteTextView actvPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = findViewById(R.id.etMessage);
        actvPhoneNumber = findViewById(R.id.actvPhoneNumber);

        checkPermissions();
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    /**
     * Overridden method of RecognitionListener
     *
     * @param bundle
     */
    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    /**
     * Overridden method of RecognitionListener
     */
    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * Overridden method of RecognitionListener
     *
     * @param v
     */

    @Override
    public void onRmsChanged(float v) {

    }

    /**
     * Overridden method of RecognitionListener
     *
     * @param bytes
     */
    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    /**
     * Overridden method of RecognitionListener
     */
    @Override
    public void onEndOfSpeech() {

    }

    /**
     * Overridden method of RecognitionListener
     *
     * @param i
     */

    @Override
    public void onError(int i) {
        switch (i) {

            case SpeechRecognizer.ERROR_AUDIO:
                message = getString(R.string.error_audio_error);
                break;

            case SpeechRecognizer.ERROR_CLIENT:
                message = getString(R.string.error_client);
                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = getString(R.string.error_permission);
                break;

            case SpeechRecognizer.ERROR_NETWORK:
                message = getString(R.string.error_network);
                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = getString(R.string.error_timeout);
                break;

            case SpeechRecognizer.ERROR_NO_MATCH:
                message = getString(R.string.error_no_match);
                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = getString(R.string.error_busy);
                break;

            case SpeechRecognizer.ERROR_SERVER:
                message = getString(R.string.error_server);
                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = getString(R.string.error_speech_timeout);
                break;

            default:
                message = getString(R.string.error_understand);
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Overridden method of RecognitionListener
     *
     * @param bundle
     */
    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String speech = matches.get(0);
        // key phrase - to field
        String key_phrase = getString(R.string.key_phrase);
        if (speech.startsWith(key_phrase)) {
            // Get the command
            String command = speech.substring(key_phrase.length()).trim();

            // If command to find a contact from contacts
            if (command.startsWith(getString(R.string.find_text))) {
                String contact = command.substring(getString(R.string.find_text).length()).trim();
                List<String> options = findContact(contact);
                // If only single contact found, then display details
                if (options.size() == 1) {
                    int beginIndex = options.get(0).indexOf("(")+1;
                    int endIndex = options.get(0).indexOf(")");
                    String numbers = options.get(0).substring(beginIndex, endIndex);
                    if (numbers == null) {
                        Toast.makeText(this, getString(R.string.contact_not_found_text, contact), Toast.LENGTH_SHORT).show();
                    } else {
                        actvPhoneNumber.setText(numbers);
                    }
                }
                // else show the contacts in the dropdown for the user to select
                else {
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, options.toArray(new String[options.size()]));
                    actvPhoneNumber.setAdapter(adapter);
                    actvPhoneNumber.setText(contact);
                    actvPhoneNumber.showDropDown();
                    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            // Once the user selects a contact, get the numbers for that contact
                            String contact = adapter.getItem(i);
                            // removing the name and display the numbers
                            int beginIndex = contact.indexOf("(") + 1;
                            int endIndex = contact.indexOf(")");
                            String number = contact.substring(beginIndex, endIndex);
                            actvPhoneNumber.setText(number);
                            actvPhoneNumber.setOnItemClickListener(null);
                        }
                    };
                    actvPhoneNumber.setOnItemClickListener(listener);
                }
            }
            // If command to call a contact from Contacts
            else if (command.startsWith(getString(R.string.call_text))) {
                String contact = command.substring(getString(R.string.call_text).length()).trim();

                List<String> options = findContact(contact);
                // If only one contact found, call first number for the contact
                if (options.size() == 1) {
                    String numbers = options.get(0).substring(options.get(0).indexOf("(") + 1, options.get(0).indexOf(")"));
                    if (numbers == null) {
                        Toast.makeText(this, getString(R.string.contact_not_found_text, contact), Toast.LENGTH_SHORT).show();
                    } else {
                        if (numbers.contains(", ")) {
                            numbers = numbers.substring(0, numbers.indexOf(", "));
                        }
                        actvPhoneNumber.setText(numbers);
                        callNumber(numbers);
                    }
                }
                // if multiple contacts found, provide a dropdown for the user to select
                else {
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, options.toArray(new String[options.size()]));
                    actvPhoneNumber.setAdapter(adapter);
                    actvPhoneNumber.setText(contact);
                    actvPhoneNumber.showDropDown();
                    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Once the user selects a contact, call the first number for that contact
                            String contact = adapter.getItem(i);
                            // calling the very first number for the contact
                            int beginIndex = contact.indexOf("(") + 1;
                            int endIndex = contact.contains(", ") ? contact.indexOf(", ") : contact.indexOf(")");
                            String number = contact.substring(beginIndex, endIndex);
                            callNumber(number);
                            actvPhoneNumber.setOnItemClickListener(null);
                        }
                    };
                    actvPhoneNumber.setOnItemClickListener(listener);
                }

            }
            // If user is providing the number
            else if (command.matches("[0-9 ]+")) {

                actvPhoneNumber.setText(command.replace(" ",""));
            }
        }
        // message
        else {
            etMessage.setText(speech);
        }


        stopListening();
    }

    /**
     * Method to call the number
     *
     * @param number
     */
    private void callNumber(String number) {
        // call the number
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    /**
     * Method to stop listening the speech input
     */
    protected void stopListening() {
        super.onStop();
        speech.stopListening();
        speech.cancel();
        speech.destroy();
        speech = null;
    }

    /**
     * Method to start listening the speech input
     *
     * @param view - Button
     */
    public void startListening(View view) {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        speech.startListening(recognizerIntent);
    }

    /**
     * Method to check the required permissions -
     * - to record audio
     * - to read contacts
     * - to place a call
     * - to send a sms
     */
    private void checkPermissions() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
        }
        Log.d(getString(R.string.app_name), "Received the permissions");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == PERMISSION_REQUEST_RECORD_AUDIO)
                ||(requestCode == PERMISSIONS_REQUEST_READ_CONTACTS)
                || (requestCode == PERMISSION_REQUEST_CALL_PHONE)
                || (requestCode == PERMISSION_REQUEST_SEND_SMS)){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                checkPermissions();
            } else {
                Toast.makeText(this, getString(R.string.need_permission_text), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Method to get all the contacts from the Contacts
     *
     * @param view
     */
    public void getContacts(View view) {

        Intent intent = new Intent(this, ContactListActivity.class);
        startActivityForResult(intent, REQ_CONTACT_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CONTACT_LIST && resultCode == RESULT_OK) {
            actvPhoneNumber.setText(data.getStringExtra(SELECTED_CONTACT_NUMBERS));
        }

    }

    /**
     * Method to find a contact based on the search
     *
     * @param search - search criteria - part of contact name
     * @return
     */
    private List findContact(String search) {
        // find contact
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
        ContentResolver cr = this.getContentResolver();
        String selection = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE '%" + search + "%'";

        Cursor contacts = cr.query(uri, projection, selection, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
        List<String> options = new ArrayList<>();
        String numbers = "";
        String name = "";
        if (contacts!= null && contacts.moveToFirst()) {
            numbers = "";
            do {
                String number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                // Same contact with multiple numbers
                if (name.equals(contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))) {
                    number = ", " + number.replace("-", "");
                    numbers += number;
                }
                // New contact
                else {
                    // If not the First time
                    if (!name.equals("")&&!numbers.equals("")) {

                        String details = name + " (" + numbers + ")";
                        options.add(details);
                    }
                    name = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    numbers = number;

                }
            } while (contacts.moveToNext());
            contacts.close();
            if (!name.equals("")) {

                String details = name + " (" + numbers + ")";
                options.add(details);
            }
        }
        return options;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_help:
                startActivity(new Intent(this, HelpActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to send a SMS with the help of SmsManager
     *
     * @param view
     */
    public void sendSMS(View view) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();

        String phoneNumber = actvPhoneNumber.getText().toString();
        String message = etMessage.getText().toString();
        if (TextUtils.isEmpty(phoneNumber.trim()) || phoneNumber.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, getString(R.string.enter_valid_phone_text), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(message.trim())) {
            Toast.makeText(this, getString(R.string.enter_valid_msg_text), Toast.LENGTH_SHORT).show();
        } else {
            // SMS to be sent to multiple numbers
            if(phoneNumber.contains(",")){
                String number;
                int beginIndex = 0;
                int endIndex;
                while(phoneNumber.contains(",")){
                    endIndex = phoneNumber.indexOf(",");
                    number = phoneNumber.substring(beginIndex, endIndex);
                    number = number.replace(" ","");
                    boolean isValid = number.startsWith("+")?number.length()<=13:number.length()<=10;
                    if(isValid) {
                        sms.sendTextMessage(number, null, message, pi, null);
                        Toast.makeText(this, getString(R.string.sms_sent_text, number), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, getString(R.string.invalid_phone_num_text, number), Toast.LENGTH_SHORT).show();
                    }
                    phoneNumber = phoneNumber.substring(endIndex + 1);
                }
            }
            phoneNumber = phoneNumber.replace(" ","");
            boolean isValid = phoneNumber.startsWith("+")?phoneNumber.length()<=13:phoneNumber.length()<=10;
            if(isValid) {
                sms.sendTextMessage(phoneNumber, null, message, pi, null);

                Toast.makeText(this, getString(R.string.sms_sent_text, phoneNumber), Toast.LENGTH_SHORT).show();
                actvPhoneNumber.setText("");
                etMessage.setText("");
            }else{
                Toast.makeText(this, getString(R.string.invalid_phone_num_text, phoneNumber), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
