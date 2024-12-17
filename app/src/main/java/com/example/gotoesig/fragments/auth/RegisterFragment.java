package com.example.gotoesig.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.activities.HomeActivity;
import com.example.gotoesig.R;
import com.example.gotoesig.dao.UserDAO;
import com.example.gotoesig.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Button btnRegister = view.findViewById(R.id.btn_register);
        TextView linkLogin = view.findViewById(R.id.link_login);
        EditText firstNameField = view.findViewById(R.id.first_name);
        EditText lastNameField = view.findViewById(R.id.last_name);
        EditText emailField = view.findViewById(R.id.email);
        EditText passwordField = view.findViewById(R.id.password);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        // Action pour s'inscrire
        btnRegister.setOnClickListener(v -> {
            String firstName = firstNameField.getText().toString().trim();
            String lastName = lastNameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (firstName.isEmpty()) {
                firstNameField.setError("Veuillez entrer un prénom");
                firstNameField.requestFocus();
                return;
            }

            if (lastName.isEmpty()) {
                lastNameField.setError("Veuillez entrer un nom");
                lastNameField.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                emailField.setError("Veuillez entrer une adresse email");
                emailField.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.setError("Adresse email invalide");
                emailField.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordField.setError("Veuillez entrer un mot de passe");
                passwordField.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordField.setError("Le mot de passe doit contenir au moins 6 caractères");
                passwordField.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            createUser(auth, firstName, lastName, email, password);
            progressBar.setVisibility(View.GONE);
        });


        // Transition vers LoginFragment
        linkLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void createUser(FirebaseAuth auth, String firstName, String lastName, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Mettre à jour le profil utilisateur Firebase Auth
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();

                            user.updateProfile(profileChangeRequest)
                                    .addOnCompleteListener(profileUpdateTask -> {
                                        if (profileUpdateTask.isSuccessful()) {
                                            // Ajouter les informations utilisateur à Firestore via la DAO
                                            UserDAO userDAO = new UserDAO();
                                            User userDTO = new User();
                                            userDTO.setId(user.getUid());
                                            userDTO.setEmail(email);
                                            userDTO.setLast_name(lastName);
                                            userDTO.setFirst_name(firstName);
                                            userDAO.createUser(userDTO, new UserDAO.UserCallback() {
                                                @Override
                                                public void onSuccess(User user) {
                                                    // Rediriger vers HomeActivity après réussite
                                                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                    startActivity(intent);
                                                    requireActivity().finish();
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(getActivity(), "Erreur lors de l'enregistrement des données utilisateur", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getActivity(), "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void handleRegistrationError(Exception e) {
        String errorMessage;
        if (e instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "Cet email est déjà utilisé";
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Le mot de passe est trop faible";
        } else {
            errorMessage = "Erreur : " + e.getMessage();
        }
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }


}
