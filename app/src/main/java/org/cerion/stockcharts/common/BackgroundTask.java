package org.cerion.stockcharts.common;

import android.os.AsyncTask;

public abstract class BackgroundTask {

    public static void run(final BackgroundTask worker) {

        AsyncTask task = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    worker.doInBackground();
                } catch(Exception e) {
                    worker.onError(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                worker.onFinish();
            }
        };

        task.execute();
    }

    public abstract void doInBackground() throws Exception;

    public void onFinish() {
        // Override with what to do on finish
    }

    public void onError(Exception e) {
        // Override to handle exception on background work
    }
}
