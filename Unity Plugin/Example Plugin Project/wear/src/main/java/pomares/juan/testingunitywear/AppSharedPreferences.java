package pomares.juan.testingunitywear;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences
{
    private static final String mIdSP="UnityiWearControllerSP";
    private static final String mIdVar="OpenedApp";

    public static boolean getAppOpen(Context context)
    {
        SharedPreferences prefs =context.getSharedPreferences(mIdSP, Context.MODE_PRIVATE);
        return prefs.getBoolean(mIdVar, false);
    }

    public static void setAppOpen(Context context, boolean open)
    {
        SharedPreferences prefs =context.getSharedPreferences(mIdSP, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(mIdVar, open);
        editor.commit();
    }
}
