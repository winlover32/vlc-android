package org.videolan.vlc.android;

import java.util.ArrayList;
import java.util.List;

import org.videolan.vlc.android.AudioPlayer.AudioPlayerControl;
import org.videolan.vlc.android.widget.AudioMiniPlayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class AudioServiceController implements AudioPlayerControl {
	public static final String TAG = "VLC/AudioServiceContoller";
	
	private static AudioServiceController mInstance;
	private static boolean mIsBound = false;
	private Context mContext;
	private IAudioService mAudioServiceBinder;
	private ServiceConnection mAudioServiceConnection;
	private ArrayList<AudioMiniPlayer> mAudioPlayer;
	private IAudioServiceCallback mCallback = new IAudioServiceCallback.Stub() {	
		@Override
		public void update() throws RemoteException {
			updateAudioPlayer();		
		}
	};
	
	private AudioServiceController() {
		
		// Get context from MainActivity
		mContext = MainActivity.getInstance();
		
		mAudioPlayer = new ArrayList<AudioMiniPlayer>();
		
        // Setup audio service connection
        mAudioServiceConnection = new ServiceConnection() {	
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d(TAG, "Service Disconnected");
				mAudioServiceBinder = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(TAG, "Service Connected");
				mAudioServiceBinder = IAudioService.Stub.asInterface(service);
				
				// Register controller to the service
				try {
					mAudioServiceBinder.addAudioCallback(mCallback);
				} catch (RemoteException e) {
					Log.e(TAG, "remote procedure call failed: addAudioCallback()");
				}
				updateAudioPlayer();
			}
		};
	}
	
	public static AudioServiceController getInstance() {
		if (mInstance == null) {
			mInstance = new AudioServiceController();
		}
		if (!mIsBound) {
			mInstance.bindAudioService();
		}
		return mInstance;
	}

	public void load(List<String> mediaPathList, int position) {
		try {
			mAudioServiceBinder.load(mediaPathList, position);
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: load()");
		}
	}
	
	/**
	 * Bind to audio service if it is running
	 * @return true if the binding was successful.
	 */
	public void bindAudioService() {
		if (mAudioServiceBinder == null) {
	    	Intent service = new Intent(mContext, AudioService.class);
	    	mContext.startService(service);
	    	mIsBound = mContext.bindService(service, mAudioServiceConnection, Context.BIND_AUTO_CREATE);
		} else {
			// Register controller to the service
			try {
				mAudioServiceBinder.addAudioCallback(mCallback);
			} catch (RemoteException e) {
				Log.e(TAG, "remote procedure call failed: addAudioCallback()");
			}
		}
	}
	
	public void unbindAudioService() {
		if (mAudioServiceBinder != null) {
			try {
				mAudioServiceBinder.removeAudioCallback(mCallback);
				if (mIsBound) {
					mContext.unbindService(mAudioServiceConnection);	
					mIsBound = false;
				}
			} catch (RemoteException e) {
				Log.e(TAG, "remote procedure call failed: removeAudioCallback()");
			}
			
		}
	}
	
	/**
	 * Add a AudioPlayer
	 * @param ap
	 */
	public void addAudioPlayer(AudioMiniPlayer ap) {
		mAudioPlayer.add(ap);
	}
	
	/**
	 * Remove AudioPlayer from list
	 * @param ap
	 */
	public void removeAudioPlayer(AudioMiniPlayer ap) {
		if (mAudioPlayer.contains(ap)) {
			mAudioPlayer.remove(ap);
		}
	}
	
	/**
	 * Update all AudioPlayer
	 */
	private void updateAudioPlayer() {
		for (int i = 0; i < mAudioPlayer.size(); i++) 
			mAudioPlayer.get(i).update();
	}
	
	public void stop() {
		if (mAudioServiceBinder == null) 
			return;
		try {
			mAudioServiceBinder.stop();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: stop()");
		}
		updateAudioPlayer();
	}

	@Override
	public String getArtist() {
		String artist = null;
		try {
			artist = mAudioServiceBinder.getArtist();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: getArtist()");
		}
		return artist;
	}

	@Override
	public String getTitle() {
		String title = null;
		try {
			title = mAudioServiceBinder.getTitle();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: getTitle()");
		}
		return title;
	}
	
	@Override
	public boolean isPlaying() {
		boolean playing = false;
		if (mAudioServiceBinder != null) {
			try {
				playing = (hasMedia() && mAudioServiceBinder.isPlaying());
				
			} catch (RemoteException e) {
				Log.e(TAG, "remote procedure call failed: isPlaying()");
			}
		}
		return playing;
	}
	
	@Override
	public void pause() {
		try {
			mAudioServiceBinder.pause();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: pause()");
		}
		updateAudioPlayer();
	}
	
	@Override
	public void play() {
		try {
			mAudioServiceBinder.play();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: play()");
		}
		updateAudioPlayer();
	}

	@Override
	public boolean hasMedia() {
		try {
			return mAudioServiceBinder.hasMedia();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: hasMedia()");
		}
		return false;
	}

	@Override
	public int getLength() {
		try {
			return mAudioServiceBinder.getLength();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: getLength()");
		}
		return 0;
	}

	@Override
	public int getTime() {
		try {
			return mAudioServiceBinder.getTime();
		} catch (RemoteException e) {
			Log.e(TAG, "remote procedure call failed: getTime()");
		}
		return 0;
	}

	@Override
	public Bitmap getCover() {
		return null;
	}
	
	

}
