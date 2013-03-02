package com.example.readwritenfctest;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	TextView tv;
	Button enableWrite;
	Button enableRead;
	EditText StringToWrite;
	IntentFilter[] mWriteTagFilters;
	NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.textView3);
		enableWrite = (Button) findViewById(R.id.toggleButton1);
		enableRead = (Button) findViewById(R.id.toggleButton2);
		StringToWrite = (EditText) findViewById(R.id.editText1);
		enableWrite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enableWrite();
			}
		});
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	protected void enableWrite() {
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mWriteTagFilters, null);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			Parcelable[] rawMsgs = intent
					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				for (NdefMessage tmpMsg : msgs) {
					for (NdefRecord tmpRecord : tmpMsg.getRecords()) {
						tv.append("\n" + new String(tmpRecord.getPayload()));
					}
				}
			}

		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Tag writing mode
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NdefRecord record1 = createTextRecord(StringToWrite.getText()
					.toString());

			NdefMessage msg = new NdefMessage(new NdefRecord[] { record1 });

			if (writeTag(msg, detectedTag)) {
				Toast.makeText(this, "Success write operation!",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Failed to write!", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public NdefRecord createTextRecord(String payload) {
		byte[] textBytes = payload.getBytes();
		byte[] data = new byte[1 + textBytes.length];
		data[0] = (byte) 0;
		System.arraycopy(textBytes, 0, data, 1, textBytes.length);
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], data);
		return record;
	}

	public static boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					return false;
				}
				if (ndef.getMaxSize() < size) {
					return false;
				}
				ndef.writeNdefMessage(message);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

}
