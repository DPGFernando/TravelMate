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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private FirebaseFirestore db;
    private Spinner spinner1, spinner2, spinner3;
    private String document;
    private List<String> list1 = new ArrayList<>();
    private List<String> list2 = new ArrayList<>();
    private List<String> list3 = new ArrayList<>();
    private CardView cardResult;
    private TextView textPlace, textLocation, textCategory, textContact;
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
        textCategory = view.findViewById(R.id.text_category);
        btnSearch = view.findViewById(R.id.btn_search);


        spinner2.setEnabled(false);
        spinner3.setEnabled(false);


        loadSpinnerData("Locations", list1, spinner1);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    document = spinner1.getSelectedItem().toString();
                    loadSpinnerData1(document, list2, spinner2);
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
                    loadSpinnerData2(document, list3, spinner3);
                    spinner3.setEnabled(true);
                    btnSearch.setEnabled(true);
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

    private void loadSpinnerData2(String documentName, List<String> list, Spinner spinner) {
        db.collection("Locations").document(documentName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<String> categoriesList = (List<String>) document.get("categories"); // Get array field

                        if (categoriesList != null) {
                            categoriesList.add(0, "Select"); // Optional: Add a default "Select" option

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categoriesList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter); // Set adapter to spinner
                        } else {
                            Log.w("Firestore", "Array field is null or doesn't exist.");
                        }
                    } else {
                        Log.w("Firestore", "Error getting document.", task.getException());
                    }

                });
    }

    private void loadSpinnerData1(String documentName, List<String> list, Spinner spinner) {
        db.collection("Locations").document(documentName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        List<String> citiesList = (List<String>) document.get("cities"); // Get array field

                        if (citiesList != null) {
                            citiesList.add(0, "Select"); // Optional: Add a default "Select" option

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, citiesList);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter); // Set adapter to spinner
                        } else {
                            Log.w("Firestore", "Array field is null or doesn't exist.");
                        }
                    } else {
                        Log.w("Firestore", "Error getting document.", task.getException());
                    }

                });
    }

    private void loadSpinnerData(String collectionName, List<String> list, Spinner spinner) {
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        list.clear();
                        list.add("Select");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId(); // Get document ID
                            list.add(documentId);
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

        CollectionReference colRef = db.collection("Locations").document(selectedDistrict).collection(selectedCity);

        colRef
                .limit(1) // Limit to the first document
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot firstDoc = task.getResult().getDocuments().get(0);
                        String contact = firstDoc.getString(selectedCategory);
                        textPlace.setText("District: " + selectedDistrict);
                        textLocation.setText("Location: " + selectedCity);
                        textCategory.setText("Category: " + selectedCategory);
                        textContact.setText("Contact: " + contact);

                    } else {
                        Log.d("Firestore", "Failed to retrieve documents or collection is empty.", task.getException());
                    }
                });

    }
}
