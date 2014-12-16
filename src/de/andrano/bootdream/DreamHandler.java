package de.andrano.bootdream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DreamHandler {

	Activity activity;
	Context context;
	Resources resources;
	SharedPreferences sharedPreferences;
	SharedPreferences defaultPreferences;
	
	public String bootDreamDirectory = "BootDream";
	
	private String[] ownDreams = {"ice_cream_sandwich.zip", "jelly_bean.zip", "kitkat.zip", "lollipop.zip"};
	
	public DreamHandler(Context context) {
		this.context = context;
		this.resources = context.getResources();
		this.sharedPreferences = context.getSharedPreferences(
				resources.getString(R.string.preferencesFile), Context.MODE_PRIVATE);
		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/**
	 * Check if Data Directory exists
	 * 
	 * @return false if fails to check directory or it is not possible to create directory
	 * @return true if directory exists or is created
	 */
	
	public boolean checkDirectoryExistence() {
		File bootDreamData = getDirectory();
		if (!bootDreamData.exists()) {
			if (!bootDreamData.mkdir()) {
				return false;
			}
		}
		return true;
	}
	
	public List<Dream> getAvailableDreams() {
		File bootDreamData = getDirectory();
		File[] dreams = bootDreamData.listFiles();
		
		List<Dream> Dreams = new ArrayList<Dream>();
		
		Dream dream;
		for (File file : dreams) {
			if (file.isFile() && file.getName().endsWith(".zip")) {
				dream = new Dream(file);
				if (dream.isDream()) {
					Dreams.add(dream);
				}
			}
		}
		//Add own dreams
		for (int i = 0; i < ownDreams.length; i++) {
			dream = new Dream(ownDreams[i]);
			Dreams.add(dream);
		}
		return Dreams;
	}
	
	public Dream getDreamByPath(List<Dream> dreams, String path) {
		for(int i = 0; i < dreams.size(); i++) {
			if (dreams.get(i).path.equals(path)) {
				return dreams.get(i);
			}
		}
		return dreams.get(0);
	}
	
	public DreamFrame[] getFrames() {
		String defaultDream = getDefaultDream();
		List<Dream> dreams = getAvailableDreams();
		Dream dream = getDreamByPath(dreams, defaultDream);
		Log.d("de.andrano.bootdream", "getFrames");
		DreamFrame[] frames = extractDream(dream);
		return frames;
	}
	
	public String getDefaultDream() {
        return sharedPreferences.getString(resources.getString(R.string.shared_default_dream), "jelly_bean.zip");
	}
	
	/* Checks if external storage is available for read and write */
	private boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	private File getDirectory() {
		if (isExternalStorageWritable()) {
			File externalData = Environment.getExternalStorageDirectory();
			File bootDreamData = new File(externalData, bootDreamDirectory);
			return bootDreamData;
		} else {
			return null;
		}
	}
	
	private DreamFrame[] extractDream(Dream dream) {
		//Copy dream to intern data
		try {
			InputStream input;
			if (dream.intern) {
				AssetManager assets = context.getAssets();
				input = assets.open(dream.path);
				
			} else {
				 input = new FileInputStream(dream.file);
			}
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(input));
			ZipEntry ze;
		    
			BitmapFactory.Options options = new BitmapFactory.Options();
			if (defaultPreferences.getBoolean(resources.getString(R.string.key_checkbox_resolution), true)) {
				options.inSampleSize = 2;
			}
			
			String desc = "";
			HashMap<String, TreeMap<String, DreamFrame>> framesMap = new HashMap<String, TreeMap<String, DreamFrame>>();
			
			while ((ze = zis.getNextEntry()) != null) {
	        	if (!ze.isDirectory() && (ze.getName().equals("desc.txt") || ze.getName().endsWith(".png") || ze.getName().endsWith(".jpg"))) {
	        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        byte[] buffer = new byte[1024];
			        int count;
			        while ((count = zis.read(buffer)) != -1) {
			        	baos.write(buffer, 0, count);
			        }
			        String filename = ze.getName();
			        byte[] bytes = baos.toByteArray();
			        
			        if (ze.getName().equals("desc.txt")) {
			        	desc = baos.toString();
			        } else {
				        //Get dirname
				        String dirname = filename.substring(0, filename.indexOf("/"));
				        TreeMap<String, DreamFrame> dir = framesMap.get(dirname);
				        if (dir == null) {
				        	dir = new TreeMap<String, DreamFrame>();
				        }
				        //Get image
				        Bitmap frame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
				        dir.put(filename.substring(filename.indexOf("/")), new DreamFrame(frame, 0));
				        framesMap.put(dirname, dir);
			        }
	        	}
			}
			if (desc.isEmpty()) {
				Toast.makeText(context, R.string.bd_missing_description, Toast.LENGTH_LONG).show();
				Log.e("de.andrano.bootdream", "Missing description");
				return null;
			}
			Log.d("de.andrano.bootdream", "extractFrames");
			return renderDream(desc, framesMap);
		} catch (Exception e) {
			Log.e("de.andrano.bootdream", "extractFrames", e);
		}
		return null;
	}
	
	private DreamFrame[] renderDream(String description, HashMap<String, TreeMap<String, DreamFrame>> map) {
		//Parse description
		try {
			//Breite Höhe fps
			//p=Play, x-Mal spielen (0 = unendlich), fps Pause beim letzten Bild, Ordnername
			List<DreamFrame> second = new ArrayList<DreamFrame>();
			TreeMap<String, DreamFrame> dir = new TreeMap<String, DreamFrame>();
			String[] lines = description.split("\n");
			String[] current = lines[0].split(" ");
			Log.d("de.andrano.bootdream", current[2]);
			int fps = Integer.valueOf(current[2]);
			int duration =  1000/fps; //Duration has to be given in milliSeconds, 1s = 1000ms
			Log.d("de.andrano.bootdream", "duration: " + duration);
			Log.d("de.andrano.bootdream", "fps: " + fps);
			DreamFrame frame;
			for (int i = 1; i < lines.length; i++) {
				current = lines[i].split(" ");
				int length = 0;
				if (current[1].equals("0")) {
					length = 10;
					//TODO Allow user to set this value
				} else {
					length = Integer.valueOf(current[1]);
					Log.d("de.andrano.bootdream", "length: " + length);
				}
				dir = map.get(current[3]);
				for (int j = 0; j < length; j++) {
					for (Map.Entry<String, DreamFrame> entry: dir.entrySet()) {
						//frame = new DreamFrame(entry.getValue(), duration);
						frame = entry.getValue();
						frame.setDuration(duration);
						second.add(frame);
					}
				}
				if (Integer.valueOf(current[2]) > 0) {
					frame = second.get(second.size() - 1);
					frame.duration = Integer.valueOf(current[2]) * duration;
					second.set(second.size() - 1, frame);
				}
			}
			
			DreamFrame[] frames = new DreamFrame[second.size()];
			for(int i = 0; i < second.size(); i++) {
				frames[i] = second.get(i);
			}
			Log.d("de.andrano.bootdream", "renderFrames");
			return frames;
		} catch (Exception e) {
			Toast.makeText(context, R.string.bd_incorrect_description, Toast.LENGTH_LONG).show();
			return null;
		}
	}
}
