package com.example.gotoesig.dao;

import com.example.gotoesig.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UserDAO {
    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;

    public UserDAO() {
        this.db = FirebaseFirestore.getInstance();;
    }

    public void getUserById(String userId, UserDAO.UserCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void createUser(User user, UserCallback callback) {
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("first_name", user.getFirst_name());
        userInfo.put("last_name", user.getLast_name());
        userInfo.put("email", user.getEmail());
        userInfo.put("balance", 0.0);
        userInfo.put("display_name", user.getLast_name() + " " + user.getFirst_name());

        db.collection("users").document(user.getId())
                .set(userInfo)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(callback::onFailure);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }
}
