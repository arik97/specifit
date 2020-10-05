package com.example.ivanwl.specifit;

import android.content.Intent;
import android.os.Bundle;

import com.example.ivanwl.specifit.Adapters.MainArrayAdapter;
import com.example.ivanwl.specifit.Adapters.RestaurantArrayAdapter;
import com.example.ivanwl.specifit.Interfaces.MainCallBack;
import com.example.ivanwl.specifit.Services.Firebase.Firebase;
import com.example.ivanwl.specifit.Services.Firebase.Models.Dish;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.ivanwl.specifit.Utils.Utils.print;


public class MainActivity extends AppCompatActivity implements MainCallBack {
    private Firebase firebase;
    //  This settings map will be passed down to child activities
    private HashMap<String, Object> settings;
    private BMR bmr;
    private Date dateTime;
    private ArrayList<Dish> mealsEaten;
    private ArrayList<Dish> allMeals;
    private HashMap<String, Integer> word_count;

    @Override
    protected void onResume() {
        super.onResume();
        if(settings != null) {
            bmr.update(settings);
            final TextView textViewToChange = findViewById(R.id.calorieCount);
            textViewToChange.setText(Integer.toString((int)Math.rint(((CalorieCounter) this.getApplication()).getCaloriesRemaining())));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((CalorieCounter) this.getApplication()).getCaloriesRemaining();

        Toolbar toolbar = findViewById(R.id.toolbar);
        //final TextView textView = findViewById(R.id.text_id);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Location Updated", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                 */
                goToRestaurants();
            }
        });

        this.dateTime = Calendar.getInstance().getTime();
        setupServices();
    }

    private void goToRestaurants(){
        Intent intent = new Intent(this, RestaurantsActivity.class);
        intent.putExtra("Settings", settings);
        intent.putExtra("mealsEaten", allMeals);
        intent.putExtra("word_count", word_count);
        startActivity(intent);
    }

    private void setupServices() {
        firebase = new Firebase(this, null, null);
        firebase.retrieveSettings();
        firebase.retrieveMeals();
    }

    @Override
    public void newSettings(HashMap<String, Object> settings) {
        //  Values can be null
        //  Replace old settings map with new settings map
        this.settings = new HashMap<>(settings);
        if(this.settings.get("Height") == null){
            Snackbar.make(this.findViewById(android.R.id.content), "Update your settings in the triple dots", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            this.settings.put("Height", 0);
        }
        if(this.settings.get("Weight") == null){
            this.settings.put("Weight", 0);
        }
        if(this.settings.get("Age") == null){
            this.settings.put("Age", 0.0);
        }
        if(this.settings.get("sex") == null){
            this.settings.put("sex", "female");
        }
        if(this.settings.get("Activity") == null){
            this.settings.put("Activity", 1.2);
        }
        if(this.settings.get("Goal") == null){
            this.settings.put("Goal", 0);
        }
        bmr = new BMR(this.settings);
        ((CalorieCounter) this.getApplication()).setCalories(bmr.getBMR());
        final TextView textViewToChange =
                findViewById(R.id.calorieCount);
        textViewToChange.setText(Integer.toString((int)Math.rint(((CalorieCounter) this.getApplication()).getCaloriesRemaining())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //  Build Intent and go to Settings Activity
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("Settings", settings);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void retrieveMeals(HashMap<Date, ArrayList<Dish>> meals) {
        this.mealsEaten = new ArrayList<>();
        this.allMeals = new ArrayList<>();
        this.word_count = new HashMap<>();
        ((CalorieCounter) getApplication()).setConsumed(0.0);
        for (Map.Entry<Date, ArrayList<Dish>> meal : meals.entrySet()) {
            for (Dish dish : meal.getValue()) {
                this.allMeals.add(dish);
                for(String word : dish.name.replaceAll(",", "").split("\\s+")) {
                    this.word_count.put(word, this.word_count.getOrDefault(word, 0) + 1);
                }
                if (this.dateTime != null && this.dateTime.getDate() == meal.getKey().getDate() && this.dateTime.getMonth() == meal.getKey().getMonth() && this.dateTime.getYear() == meal.getKey().getYear()){
                    // make a listview of meals
                    print("Date: " + meal.getKey().toString() + ": " + dish.name + ", " + dish.calories);
                    mealsEaten.add(dish);
                    ((CalorieCounter) getApplication()).consumeCalories(dish.calories);
                }
            }
        }
        ListView listView = findViewById(R.id.mainListview);
        MainArrayAdapter adapter = new MainArrayAdapter(this, mealsEaten);
        listView.setAdapter(adapter);
    }
}
