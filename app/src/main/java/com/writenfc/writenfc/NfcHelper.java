package com.writenfc.writenfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Locale;

public class NfcHelper {

	private Activity activity;
	private NfcAdapter nfcAdapter;

	public NfcHelper(Activity activity) {
		this.activity = activity;
		this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
	}

	public boolean isNfcEnabledDevice() {
		boolean hasFeature = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
		return hasFeature;
	}

	public boolean isNfcEnabled() {
		boolean isEnabled = (nfcAdapter != null && nfcAdapter.isEnabled());
		return isEnabled;
	}

	public boolean isNfcIntent(Intent intent) {
		return intent.hasExtra(NfcAdapter.EXTRA_TAG);
	}

	public Tag getTagFromIntent(Intent intent) {
		return ((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
	}

	public void enableForegroundDispatch() {
		Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
		IntentFilter[] intentFilter = new IntentFilter[] {};
		String[][] techList = new String[][] { { Ndef.class.getName() }, { NdefFormatable.class.getName() } };

		if (Build.DEVICE.matches(".*generic.*")) {
			// clean up the tech filter when in emulator since it doesn't work properly.
			techList = null;
		}

		nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilter, techList);
	}

	public void disableForegroundDispatch() {
		nfcAdapter.disableForegroundDispatch(activity);
	}

	public boolean writeNdefMessage(Intent intent, NdefMessage ndefMessage) {
		Tag tag = getTagFromIntent(intent);
		return writeNdefMessage(tag, ndefMessage);
	}

	private boolean writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
		boolean result = false;
		Log.e("writeTag","on method");
		try {

			if (tag != null) {
				Log.e("writeTag","tag not null");
				Ndef ndef = Ndef.get(tag);

				if (ndef == null) {
					Log.e("writeTag","Ndef null");
					result = formatTag(tag, ndefMessage);
				} else {
					Log.e("writeTag","ndef not null");
					ndef.connect();
					if (ndef.isWritable()) {
						Log.e("writeTag","tag writing");
						ndef.writeNdefMessage(ndefMessage);
						result = true;
					}
					ndef.close();
				}
			}

		} catch (Exception e) {
			Log.e("writeTag", String.valueOf(e.getMessage()));
		}
		return result;
	}

	private boolean formatTag(Tag tag, NdefMessage ndefMessage) {
		Log.e("writeTag","tag formation");
		try {
			NdefFormatable ndefFormat = NdefFormatable.get(tag);
			if (ndefFormat != null) {
				Log.e("writeTag","ndefFormate not null");
				ndefFormat.connect();
				ndefFormat.format(ndefMessage);
				ndefFormat.close();
				return true;
			}
		} catch (Exception e) {
			Log.e("writingTag", e.getMessage());
		}
		return false;
	}

	public NdefRecord createUriRecord(String uri) {

		NdefRecord rtdUriRecord = null;
		try {
			byte[] uriField;
			uriField = uri.getBytes("UTF-8");
			byte[] payload = new byte[uriField.length + 1];
			payload[0] = 0x00;
			System.arraycopy(uriField, 0, payload, 1, uriField.length);
			rtdUriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);
		} catch (UnsupportedEncodingException e) {
			Log.e("createUriRecord", e.getMessage());
		}
		return rtdUriRecord;
	}

	public NdefRecord createTextRecord(String content) {
		try {
			byte[] language;
			language = Locale.getDefault().getLanguage().getBytes("UTF-8");
			final byte[] text = content.getBytes("UTF-8");
			final int languageSize = language.length;
			final int textLength = text.length;
			final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);
			payload.write((byte) (languageSize & 0x1F));
			payload.write(language, 0, languageSize);
			payload.write(text, 0, textLength);
			return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
		} catch (UnsupportedEncodingException e) {
			Log.e("createTextRecord", e.getMessage());
		}
		return null;
	}

	public NdefRecord createVcardRecord(String uri) {
		NdefRecord vcardRecord = null;
		try {
			byte[] uriField;
			uriField = uri.getBytes("UTF-8");
			byte[] payload = new byte[uriField.length + 1];
			payload[0] = 0x00;
			System.arraycopy(uriField, 0, payload, 1, uriField.length);
			vcardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/vcard".getBytes(),
					new byte[0], payload);
		} catch (UnsupportedEncodingException e) {
			Log.e("createUriRecord", e.getMessage());
		}
		return vcardRecord;
	}

	public NdefMessage createUrlNdefMessage(String uri) {
		NdefRecord record = createUriRecord(uri);
		return new NdefMessage(new NdefRecord[] { record });
	}

	public NdefMessage createTextNdefMessage(String text) {
		NdefRecord record = createTextRecord(text);
		return new NdefMessage(new NdefRecord[] { record });
	}

	public NdefMessage createVcardNdefMessage(String vCardInfor) throws UnsupportedEncodingException {
		NdefRecord record = createVcardRecord(vCardInfor);
		return new NdefMessage(new NdefRecord[] { record });
	}

	public NdefMessage getNdefMessageFromIntent(Intent intent) {
		NdefMessage ndefMessage = null;
		Parcelable[] extra=null;
		try {
			extra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		}catch(Exception e){}
		if (extra != null && extra.length > 0) {
			ndefMessage = (NdefMessage) extra[0];
		}
		return ndefMessage;
	}

	public NdefRecord getFirstNdefRecord(NdefMessage ndefMessage) {
		NdefRecord ndefRecord = null;
		NdefRecord[] ndefRecords = ndefMessage.getRecords();
		if (ndefRecords != null && ndefRecords.length > 0) {
			ndefRecord = ndefRecords[0];
		}
		return ndefRecord;
	}

	public boolean isNdefRecordOfTnfAndRdt(NdefRecord ndefRecord, short tnf, byte[] rdt) {
		return ndefRecord.getTnf() == tnf && Arrays.equals(ndefRecord.getType(), rdt);
	}

	public String getTextFromNdefRecord(NdefRecord ndefRecord) {
		String tagContent = null;
		try {
			byte[] payload = ndefRecord.getPayload();
			String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
			int languageSize = payload[0] & 0063;
			tagContent = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
		} catch (UnsupportedEncodingException e) {
			Log.e("getTextFromNdefRecord", e.getMessage(), e);
		}
		return tagContent;
	}

}
