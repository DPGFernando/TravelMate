package com.example.travelmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private FirebaseFirestore db;
    private Spinner spinner1, spinner2, spinner3;
    private List<String> list1 = new ArrayList<>();
    private List<String> list2 = new ArrayList<>();
    private List<String> list3 = new ArrayList<>();
    private CardView cardResult;
    private TextView textPlace, textLocation, textContact;
    private Button btnSearch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);


        db = FirebaseFirestore.getInstance();


        spinner1 = view.findViewById(R.id.spinner1);
        spinner2 = view.findViewById(R.id.spinner2);
        spinner3 = view.findViewById(R.id.spinner3);
        cardResult = view.findViewById(R.id.card_result);
        textPlace = view.findViewById(R.id.text_place);
        textLocation = view.findViewById(R.id.text_location);
        textContact = view.findViewById(R.id.text_contact);
        btnSearch = view.findViewById(R.id.btn_search);


        spinner2.setEnabled(false);
        spinner3.setEnabled(false);


        loadSpinnerData("Collection1", list1, spinner1);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadSpinnerData("Collection2", list2, spinner2);
                    spinner2.setEnabled(true);
                } else {
                    spinner2.setEnabled(false);
                    spinner3.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed here
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    loadSpinnerData("Collection3", list3, spinner3);
                    spinner3.setEnabled(true);
                } else {
                    spinner3.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed here
            }
        });


        btnSearch.setOnClickListener(v -> onSearchButtonClicked());

        return view;
    }

    private void loadSpinnerData(String collectionName, List<String> list, Spinner spinner) {
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        list.clear();
                        list.add("Select");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String item = document.getString("fieldName"); // Replace 'fieldName' with your field
                            list.add(item);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void onSearchButtonClicked() {

        if (spinner1.getSelectedItemPosition() == 0 ||
                spinner2.getSelectedItemPosition() == 0 ||
                spinner3.getSelectedItemPosition() == 0) {


            Toast.makeText(getContext(), "Please make a selection in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }


        cardResult.setVisibility(View.VISIBLE);


        String selectedDistrict = spinner1.getSelectedItem().toString();
        String selectedCity = spinner2.getSelectedItem().toString();
        String selectedCategory = spinner3.getSelectedItem().toString();


        textPlace.setText("Place: " + selectedDistrict);
        textLocation.setText("Location: " + selectedCity);
        textContact.setText("Category: " + selectedCategory);
    }
}
