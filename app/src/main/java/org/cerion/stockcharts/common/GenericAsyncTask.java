package org.cerion.stockcharts.common;

import android.os.AsyncTask;

public class GenericAsyncTask extends AsyncTask<Void, Void, Void> {

    //private static final String TAG = GenericAsyncTask.class.getSimpleName();
    private TaskHandler mHandler;

    public interface TaskHandler {
        void run();
        void onFinish();
    }

    public GenericAsyncTask(TaskHandler handler) {
        mHandler = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        mHandler.run();
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        mHandler.onFinish();
    }
}
