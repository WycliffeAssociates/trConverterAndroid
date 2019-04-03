package org.wycliffeassociates.converter;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity {

    private ConverterFragment converterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        converterFragment = (ConverterFragment) fm.findFragmentById(R.id.fragment_container);

        if(converterFragment == null) {
            converterFragment = new ConverterFragment();
            ft.add(R.id.fragment_container, converterFragment).commit();
        }
    }
}
