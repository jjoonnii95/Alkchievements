package de.daschubbm.alkchievements;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SettingsAlktivity extends AppCompatActivity {

    private static final int UNIMPORTANT_VARIABLE = 1250;

    private SettingsAlkdapter alkdapter;
    private ListView drinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_alktivity);
        getSupportActionBar().setTitle("Einstellungen");

        ArrayList<String[]> names = new ArrayList<>(6);
        names.add(new String[]{"Spezi", "0.60"});
        names.add(new String[]{"Radler", "0.90"});
        names.add(new String[]{"Wasser", "0.30"});
        names.add(new String[]{"Bier", "0.90"});
        names.add(new String[]{"Weizen", "1.00"});

        drinks = (ListView) findViewById(R.id.settings_list);
        drinks.setAdapter(alkdapter = new SettingsAlkdapter(this, names));
    }

    public void launchPasswordCheck(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Passwort eingeben");
        dialog.setContentView(R.layout.dialog_password_checker);

        final NumberPicker p1 = (NumberPicker) dialog.findViewById(R.id.num_lock_1);
        p1.setMinValue(0);
        p1.setValue(1);
        p1.setMaxValue(9);
        p1.setWrapSelectorWheel(true);

        final NumberPicker p2 = (NumberPicker) dialog.findViewById(R.id.num_lock_2);
        p2.setMinValue(0);
        p2.setValue(1);
        p2.setMaxValue(9);
        p2.setWrapSelectorWheel(true);

        final NumberPicker p3 = (NumberPicker) dialog.findViewById(R.id.num_lock_3);
        p3.setMinValue(0);
        p3.setValue(1);
        p3.setMaxValue(9);
        p3.setWrapSelectorWheel(true);

        NumberPicker p4 = (NumberPicker) dialog.findViewById(R.id.num_lock_4);
        p4.setMinValue(0);
        p4.setValue(1);
        p4.setMaxValue(9);
        p4.setWrapSelectorWheel(true);
        p4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (((p1.getValue() * 1000)
                        + (p2.getValue() * 100)
                        + (p3.getValue() * 10)
                        + numberPicker.getValue()) == UNIMPORTANT_VARIABLE) {
                    dialog.dismiss();
                    Toast.makeText(getApplication(), "Subba Hansl!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    public void addDrink(View view) {
        String drinkName = ((EditText) findViewById(R.id.add_drink_name)).getText().toString();
        String drinkPrice = ((EditText) findViewById(R.id.add_drink_price)).getText().toString();

        if (drinkName.matches("[A-ZÄÖÜ][a-zäöüß]+") && drinkPrice.matches("[0-9]+(\\.[0-9]+)?")) {
            String[] newDrink = new String[]{drinkName, drinkPrice};

            for (int i = 0; i < alkdapter.getCount(); i++) {
                String[] item = alkdapter.getItem(0);
                if (drinkName.equals(item[0])) alkdapter.remove(item);
            }

            alkdapter.add(newDrink);
            drinks.setAdapter(alkdapter);

            DatabaseReference drinks = FirebaseDatabase.getInstance().getReference("beverages");
            drinks.child(drinkName).setValue(Float.valueOf(drinkPrice));
        }
    }
}