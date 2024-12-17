package com.example.gotoesig.dao;

import com.example.gotoesig.models.Profile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {
    private static final String COLLECTION_NAME = "profils";
    private final FirebaseFirestore db;

    public ProfileDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void getProfiles(ProfilesCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Profile> profiles = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Profile profile = documentSnapshot.toObject(Profile.class);
                        profiles.add(profile);
                    }
                    callback.onSuccess(profiles);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface ProfilesCallback {
        void onSuccess(List<Profile> profiles);
        void onFailure(Exception e);
    }
}
