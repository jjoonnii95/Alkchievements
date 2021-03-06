package de.daschubbm.alkchievements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static de.daschubbm.alkchievements.NumberFormatter.formatPrice;

public class BillAlktivity extends AppCompatActivity {

    private ListView list;
    private BillAlkdapter adapter;
    private ArrayList<String[]> debtors = new ArrayList<>();

    private Map<String, Float> beverages = new HashMap<>();

    private AlkchievementsDatabase alkchievementsDatabase;

    private Context context;
    private String name = "Hansl";
    private boolean adminBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_alktivity);

        getSupportActionBar().setTitle(getString(R.string.rechnung_title));

        context = this;
        Log.d("ALKI", "Started bill");

        alkchievementsDatabase = new AlkchievementsDatabase(this);
        alkchievementsDatabase.open();
        name = getIntent().getStringExtra("NAME");

        adminBilling = getIntent().getBooleanExtra("ADMIN", false);

        final DatabaseReference all = FirebaseDatabase.getInstance().getReference();
        all.child("beverages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ALKI", "Starting beverage loading");
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    beverages.put(child.getKey(),
                            Float.parseFloat(String.valueOf(child.getValue())));
                }

                Log.d("ALKI", "Stopped beverage loading");

                all.child("people").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot person : dataSnapshot.getChildren()) {
                            String name = person.getKey();
                            float moneyToPay = 0;

                            for (DataSnapshot drink : person.getChildren()) {
                                moneyToPay += Integer.parseInt(String.valueOf(drink.getValue()))
                                        * beverages.get(drink.getKey());
                            }

                            debtors.add(new String[]{name, formatPrice(moneyToPay)});
                        }

                        Log.d("ALKI", "Done with people");

                        list = (ListView) findViewById(R.id.bill);

                        if (debtors.size() == 0)
                            debtors.add(new String[]{"Es had g'wies koana ebs gsuffa!", "-1"});

                        adapter = new BillAlkdapter(context, R.layout.bill_item, debtors);
                        list.setAdapter(adapter);

                        checkPrize();
                        checkHighestPrize();
                        if (adminBilling) addAdminTools();

                        findViewById(R.id.loading).setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater flatuleur = getMenuInflater();
        flatuleur.inflate(R.menu.menu_bill, menu);

        return true;
    }

    private void addAdminTools() {
        Button button = (Button) findViewById(R.id.admin_share_button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent hansl = new Intent();
                hansl.setAction(Intent.ACTION_SEND);

                String message = "Neue Schubbm-Rechnung:\n";

                for (int i = 0; i < debtors.size(); i++) {
                    String[] debtor = debtors.get(i);
                    message += debtor[0] + ": \t" + debtor[1] + "€\n";
                }

                hansl.putExtra(Intent.EXTRA_TEXT, message);
                hansl.setType("text/plain");
                hansl.setPackage("com.whatsapp");
                startActivity(hansl);

                final String finalMessage = message;
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Toast.makeText(context, "Lösche...", Toast.LENGTH_SHORT).show();

                                File backup = new File(getFilesDir(), Calendar.getInstance().getTime().toString().replaceAll("[: \\+A-Za-z]+", ""));

                                try {
                                    FileWriter writer = new FileWriter(backup);
                                    writer.write(finalMessage);
                                    writer.close();
                                    Toast.makeText(context, "Backup der Rechnung angelegt: \n"
                                            + backup.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                DatabaseReference people = FirebaseDatabase.getInstance().getReference("people");
                                people.removeValue();

                                Intent hansl = new Intent(context, SettingsAlktivity.class);
                                startActivity(hansl);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(context, "Dann hoid ned.", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Rechnung löschen?");
                builder.setMessage("Wenn die Rechnung in WhatsApp gepostet wurde kann sie in der App gelöscht werden.");
                builder.setPositiveButton("Weg damit!", onClickListener);
                builder.setNegativeButton("Naa, dalossn!", onClickListener);
                AlertDialog dialog = builder.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.LTGRAY);
            }
        });
    }

    private void checkPrize() {
        float prize = 0;
        for (int i = 0; i < debtors.size(); i++) {
            if (debtors.get(i)[0].equals(name)) {
                prize = Float.parseFloat(debtors.get(i)[1]);
            }
        }
        String state = alkchievementsDatabase.getItems().get(0)[2];
        if (prize > 5 && state.equals("false")) {
            alkchievementsDatabase.changeStatusForItem(0, "1");
            Toast.makeText(context, "Alkchievement erhalten!", Toast.LENGTH_SHORT).show();
        }
        if (prize > 10 && (state.equals("false") || state.equals("1"))) {
            alkchievementsDatabase.changeStatusForItem(0, "2");
            alkchievementsDatabase.changeDescriptionForItem(0, "Erhalte eine Rechnung von über 10€!");
            Toast.makeText(context, "Alkchievement erhalten!", Toast.LENGTH_SHORT).show();
        }
        if (prize > 20 && (state.equals("false") || state.equals("1") || state.equals("2"))) {
            alkchievementsDatabase.changeStatusForItem(0, "3");
            alkchievementsDatabase.changeDescriptionForItem(0, "Erhalte eine Rechnung von über 20€!");
            Toast.makeText(context, "Alkchievement erhalten!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkHighestPrize() {
        String state = alkchievementsDatabase.getItems().get(7)[2];
        if (state.equals("false")) {
            boolean highest = true;
            float prize = 0;
            for (int i = 0; i < debtors.size(); i++) {
                if (debtors.get(i)[0].equals(name)) {
                    prize = Float.parseFloat(debtors.get(i)[1]);
                }
            }
            for (int i = 0; i < debtors.size(); i++) {
                if (!debtors.get(i)[0].equals(name)) {
                    if (Float.parseFloat(debtors.get(i)[1]) >= prize) {
                        highest = false;
                    }
                }
            }
            if (highest) {
                alkchievementsDatabase.changeStatusForItem(7, "true");
                Toast.makeText(context, "Alkchievement erhalten!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void launchStock(MenuItem item) {
        Intent hansl = new Intent(context, StockAlktivity.class);
        startActivity(hansl);
    }
}
