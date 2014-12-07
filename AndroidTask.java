package org.tom.core.tasks;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import org.tom.core.database.core.DatabaseManager;
import org.tom.core.database.SettingsHandler;
import org.tom.core.database.SettingsModel;
import org.tom.core.di.InjectingObject;
import org.tom.core.factories.GSonFactory;
import org.tom.core.json.Server;

import javax.inject.Inject;

public abstract class AndroidTask extends InjectingObject {

    private static final String TAG = "LeskTask";

    @Inject
    SettingsHandler settingsHandler;

    @Inject
    GSonFactory gsonFactory;

    protected Server serverAddress;

    public LeskTask(Application application) {
        super(application);

        if(settingsHandler == null){
            Log.d(TAG, "handler is null");
        }

        String serverAddressStr = settingsHandler.getValue(DatabaseManager.getInstance().openDatabase(), SettingsModel.KEY_APP_ADDRESS);
        if(serverAddressStr == null){
            Log.d(TAG, "BAD SETTINGS - server");
            return;
        }
        serverAddress = gsonFactory.getGson().fromJson(serverAddressStr, Server.class);
    }

    public <T> T task(final Class<T> outputType, final AfterExecutor afterExecutor) throws Exception{

        new AsyncTask<Void, T, T>() {

            @Override
            protected T doInBackground(Void... voids) {
                try {
                    return perform(outputType);
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(T t) {
                super.onPostExecute(t);
                if(afterExecutor != null){
                    afterExecutor.work(t);
                }
            }
        }.execute();

        return null;
    }

    public <T> T task(final Class<T> outputType) throws Exception{
        return task(outputType, null);
    }

    public void task() throws Exception{
        task(Void.class);
    }

    public void task(final AfterExecutor afterExecutor) throws Exception{
        task(Void.class, afterExecutor);
    }

    abstract protected <T> T perform(Class<T> outputType) throws Exception;
}
