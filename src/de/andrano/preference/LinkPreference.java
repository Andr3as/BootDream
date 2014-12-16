package de.andrano.preference;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;

public class LinkPreference extends Preference {

	String namespace = "http://schemas.android.com/apk/gen/de.andrano";
	String tagURI = "uri";
	String defaultValue = "http://andrano.de/error/listPreference.html";
	
	String value;
	String title;
	
	Context context;
	AttributeSet attributes;
	
	public LinkPreference(Context nContext, AttributeSet attrs) {
		super(nContext, attrs);
		context = nContext;
		attributes = attrs;
		//Get uri
		value = attributes.getAttributeValue(namespace, tagURI);
		if (value == null) {
			value = defaultValue;
		}
	}
	
	@Override
	protected void onClick() {
		Intent i = new Intent(Intent.ACTION_VIEW);  
        i.setData(Uri.parse(value));  
        context.startActivity(i);
		super.onClick();
	}

}
