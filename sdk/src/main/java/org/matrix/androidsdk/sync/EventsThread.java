/*
 * Copyright 2014 OpenMarket Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrix.androidsdk.sync;

import android.util.Log;

import org.matrix.androidsdk.MXApiClient;
import org.matrix.androidsdk.MXApiClient.InitialSyncCallback;
import org.matrix.androidsdk.api.response.Event;
import org.matrix.androidsdk.api.response.InitialSyncResponse;
import org.matrix.androidsdk.api.response.TokensChunkResponse;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Thread that continually watches the event stream and sends events to its listener.
 */
public class EventsThread extends Thread {
    private static final String LOG_TAG = "EventsThread";

    private MXApiClient mApiClient;
    private EventsThreadListener mListener;
    private String mCurrentToken;


    public EventsThread(MXApiClient apiClient, EventsThreadListener listener) {
        super("Events thread");
        mApiClient = apiClient;
        mListener = listener;
    }

    public interface EventsThreadListener {
        public void onInitialSyncComplete(InitialSyncResponse response);
        public void onEventsReceived(List<Event> events);
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "Requesting initial sync...");

        // Start with initial sync
        final CountDownLatch latch = new CountDownLatch(1);
        mApiClient.initialSync(new InitialSyncCallback() {
            @Override
            public void onSynced(InitialSyncResponse initialSync) {
                Log.i(LOG_TAG, "Received initial sync response.");
                mListener.onInitialSyncComplete(initialSync);
                mCurrentToken = initialSync.end;
                // unblock the events thread
                latch.countDown();
            }
        });

        // block until the initial sync callback is invoked.
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted whilst performing initial sync.");
        }

        Log.d(LOG_TAG, "Starting event stream from token " + mCurrentToken);

        // Then work from there
        while (true) {
            TokensChunkResponse<Event> eventsResponse = mApiClient.events(mCurrentToken);
            mListener.onEventsReceived(eventsResponse.chunk);
            mCurrentToken = eventsResponse.end;
        }
    }
}
