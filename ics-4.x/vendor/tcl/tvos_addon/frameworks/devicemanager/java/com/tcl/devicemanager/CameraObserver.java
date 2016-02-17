/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tcl.devicemanager;


import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UEventObserver;
import android.util.Slog;
import android.media.AudioManager;
import java.io.File; 
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * <p>CameraObserver monitors for a USB Webcam
 */
public class CameraObserver extends UEventObserver {
    private static final String TAG =CameraObserver.class.getSimpleName();
    private static final boolean LOG = true;
    private static final String CAMERA_UEVENT_MATCH = "DEVNAME=video";

	private boolean mSystemReady = false;
    private final Context mContext;

	private static final HashMap<String,String> mCameraList = new HashMap<String,String>();

    public CameraObserver(Context context) {
        mContext = context;
        startObserving(CAMERA_UEVENT_MATCH);

    }

    @Override
    public void onUEvent(UEventObserver.UEvent event) {
        if (LOG) Slog.i(TAG, "################### Camera UEVENT: " + event.toString());

        try {
             Slog.i(TAG, "################### Camera UEVENT: " + event.toString());
             Slog.i(TAG, "################### Camera ACTION: " + event.get("ACTION"));
		     if (mSystemReady) {
		         if(event.get("ACTION").equals("add")){
					Intent intent = new Intent(DeviceManagerEvent.Intent.ACTION_CAMERA_PLUG_IN);
					mContext.sendBroadcast(intent);

					Slog.i(TAG, "################### Camera PLUG_IN" );
					mCameraList.put(event.get("DEVNAME",""), event.get("SUBSYSTEM",""));
					/* lvh@tcl new interface */
					intent = new Intent(DeviceManagerEvent.Intent.ACTION_DEVICE_PLUG_IN);
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_TYPE, DeviceManagerEvent.DEVICE_TYPE_CAMERA  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_NAME, event.get("DEVNAME","")  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_SUBSYSTEM, event.get("SUBSYSTEM","")  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_COUNT,   mCameraList.size());
					mContext.sendBroadcast(intent);
		         }
		         else{ 
					Intent intent = new Intent(DeviceManagerEvent.Intent.ACTION_CAMERA_PLUG_OUT);
					mContext.sendBroadcast(intent);

					mCameraList.remove(event.get("DEVNAME",""));
					if(mCameraList.size() > 0) {
						intent = new Intent(DeviceManagerEvent.Intent.ACTION_CAMERA_PLUG_IN);
						mContext.sendBroadcast(intent);
					}

					/* lvh@tcl new interface */
					intent = new Intent(DeviceManagerEvent.Intent.ACTION_DEVICE_PLUG_OUT);
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_TYPE, DeviceManagerEvent.DEVICE_TYPE_CAMERA  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_NAME, event.get("DEVNAME","")  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_TYPE, event.get("SUBSYSTEM","")  );
					intent.putExtra(DeviceManagerEvent.DEVICE_EVENT_KEY_COUNT,   mCameraList.size());
					mContext.sendBroadcast(intent);

					Slog.i(TAG, "################### Camera PLUG_OUT" );
		         }
		     }
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Could not parse switch state from event " + event);
        }
    }

    public void systemReady() {
        synchronized (this) {
            mSystemReady = true;
        }
    }
}
