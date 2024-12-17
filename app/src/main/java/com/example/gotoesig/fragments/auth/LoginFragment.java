package com.example.gotoesig.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.activities.HomeActivity;
import com.example.gotoesig.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView linkRegister = view.findViewById(R.id.link_register);
        EditText emailField = view.findViewById(R.id.email);
        EditText passwordField = view.findViewById(R.id.password);

        // Action pour se connecter
        btnLogin.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            if (email.isEmpty()) {
                emailField.setError("Veuillez entrer un email");
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

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Rediriger vers HomeActivity après succès
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            // Gérer l'échec (afficher un message d'erreur)
                            Toast.makeText(getActivity(), "Connexion échouée", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Transition vers RegisterFragment
        linkRegister.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
