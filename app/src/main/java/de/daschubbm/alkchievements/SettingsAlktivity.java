package de.daschubbm.alkchievements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static de.daschubbm.alkchievements.NumberFormatter.formatPrice;

public class SettingsAlktivity extends AppCompatActivity {

    //private static final int UNIMPORTANT_VARIABLE = 1;

    private SettingsAlkdapter alkdapter;
    private ListView drinks;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_alktivity);
        getSupportActionBar().setTitle("Einstellungen");

        context = this;

        DatabaseReference beverages = FirebaseDatabase.getInstance().getReference("beverages");
        beverages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String[]> names = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                for (DataSnapshot drink : dataSnapshot.getChildren()) {
                    names.add(new String[]{drink.getKey(), formatPrice(String.valueOf(drink.getValue()))});
                }

                drinks = (ListView) findViewById(R.id.settings_list);
                drinks.setAdapter(alkdapter = new SettingsAlkdapter(context, names, drinks));

                findViewById(R.id.loading).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent hansl = new Intent(context, MainAlktivity.class);
        startActivity(hansl);
        finish();
    }

    public void launchPasswordCheck(final View view) {
        switch ((String) view.getTag()) {
            case "billing":
                launchBilling();
                break;
            case "add_drink":
                addDrink();
                break;
            case "change_pin":
                changePin();
                break;
        }
        /*final Dialog dialog = new Dialog(this);
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

                    switch ((String) view.getTag()) {
                        case "billing":
                            launchBilling();
                            break;
                        case "add_drink":
                            addDrink();
                            break;
                    }
                }
            }
        });

        dialog.show();*/
    }

    private void changePin() {
        EditText pinField = (EditText) findViewById(R.id.pinText);
        String newPin = pinField.getText().toString();
        if (!newPin.matches("[0-9]{4}")) {
            Toast.makeText(context, "Die PIN muss genau 4 Stellen haben!", Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("adminPassword");
            reference.setValue(Integer.parseInt(newPin));

            Toast.makeText(context, "Die PIN wurde zu \"" + newPin + "\" geändert. Gut merken!",
                    Toast.LENGTH_SHORT).show();
        }

        pinField.setText("");
    }

    public void launchBilling() {
        Intent hansl = new Intent(this, BillAlktivity.class);
        hansl.putExtra("ADMIN", true);
        startActivity(hansl);
    }

    public void addDrink() {
        EditText drinkNameField = (EditText) findViewById(R.id.add_drink_name);
        EditText drinkPriceField = (EditText) findViewById(R.id.add_drink_price);

        String drinkName = drinkNameField.getText().toString();
        String drinkPrice = drinkPriceField.getText().toString();

        if (drinkName.matches("[A-ZÄÖÜ][a-zäöüß]+") && drinkPrice.matches("[0-9]+(\\.[0-9]+)?")) {
            String[] newDrink = new String[]{drinkName, formatPrice(drinkPrice)};

            for (int i = 0; i < alkdapter.getCount(); i++) {
                String[] item = alkdapter.getItem(0);
                if (drinkName.equals(item[0])) alkdapter.remove(item);
            }

            alkdapter.add(newDrink);
            drinks.setAdapter(alkdapter);

            drinkNameField.setText("");
            drinkPriceField.setText("");

            DatabaseReference drinks = FirebaseDatabase.getInstance().getReference("beverages");
            drinks.child(drinkName).setValue(Float.valueOf(drinkPrice));
        }
    }
}
