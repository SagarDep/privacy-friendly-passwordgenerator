package org.secuso.privacyfriendlypasswordgenerator.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlypasswordgenerator.R;
import org.secuso.privacyfriendlypasswordgenerator.database.MetaData;
import org.secuso.privacyfriendlypasswordgenerator.database.MetaDataSQLiteHelper;
import org.secuso.privacyfriendlypasswordgenerator.generator.PasswordGenerator;
import org.secuso.privacyfriendlypasswordgenerator.generator.UTF8;

import static android.content.Context.*;

/**
 * Created by karo on 13.11.16.
 */

public class GeneratePasswordDialog extends DialogFragment {

    Activity activity;
    View rootView;
    MetaDataSQLiteHelper database;
    int position;
    MetaData metaData;
    Boolean bindToDevice_enabled;
    Boolean clipboard_enabled;
    String hashAlgorithm;
    int number_iterations;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        rootView = inflater.inflate(R.layout.dialog_generate_password, null);

        Bundle bundle = getArguments();

        position = bundle.getInt("position");
        clipboard_enabled = bundle.getBoolean("clipboard_enabled");
        bindToDevice_enabled = bundle.getBoolean("bindToDevice_enabled");
        hashAlgorithm = bundle.getString("hash_algorithm");
        number_iterations = bundle.getInt("number_iterations");


        this.database = new MetaDataSQLiteHelper(getActivity());
        metaData = database.getMetaData(position);

        TextView domain = (TextView) rootView.findViewById(R.id.domainHeadingTextView);
        domain.setText(metaData.getDOMAIN());

        TextView username = (TextView) rootView.findViewById(R.id.domainUsernameTextView);

        username.setText(metaData.getUSERNAME());

        TextView iteration = (TextView) rootView.findViewById(R.id.textViewIteration);
        iteration.setText(String.valueOf(metaData.getITERATION()));

        builder.setView(rootView);
        builder.setIcon(R.mipmap.ic_drawer);
        builder.setTitle(getActivity().getString(R.string.generate_heading));
        builder.setPositiveButton(getActivity().getString(R.string.done), null);

        Button generateButton = (Button) rootView.findViewById(R.id.generatorButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager inputManager = (InputMethodManager)
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);

                generatePassword();

            }
        });

        return builder.create();
    }

    public void generatePassword() {

        EditText editTextMasterpassword = (EditText) rootView.findViewById(R.id.editTextMasterpassword);

        if (editTextMasterpassword.getText().toString().length() == 0) {
            Toast toast = Toast.makeText(activity.getBaseContext(), getString(R.string.enter_masterpassword), Toast.LENGTH_SHORT);
            toast.show();
        } else {
            metaData = database.getMetaData(position);

            Log.d("BINDING", Boolean.toString(bindToDevice_enabled));

            String deviceID;
            if (bindToDevice_enabled) {
                deviceID = Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Log.d("DEVICE ID", Settings.Secure.getString(getContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID));
            } else {
                deviceID = "SECUSO";
            }

            PasswordGenerator generator = new PasswordGenerator(metaData.getDOMAIN(),
                    metaData.getUSERNAME(),
                    editTextMasterpassword.getText().toString(),
                    deviceID,
                    UTF8.encode(metaData.getDOMAIN()),
                    metaData.getITERATION(),
                    number_iterations,
                    hashAlgorithm);

            Log.d("GENERATOR Hash", hashAlgorithm);

            String password = generator.getPassword(metaData.getHAS_SYMBOLS(), metaData.getHAS_LETTERS_LOW(), metaData.getHAS_LETTERS_UP(), metaData.getHAS_NUMBERS(), metaData.getLENGTH());
//                Log.d("Generator", "Length: " + Integer.toString(metaData.getLENGTH()));
//                Log.d("Generator", "Domain: " + metaData.getDOMAIN());
//
//                Log.d("Generator", "Symbols: " + Integer.toString(metaData.getHAS_SYMBOLS()));
//                //Log.d("Generator", "Letters: " + Integer.toString(metaData.getHAS_LETTERS()));
//                Log.d("Generator", "Numbers: " + Integer.toString(metaData.getHAS_NUMBERS()));
//                Log.d("Generator", "Iterations: " + Integer.toString(metaData.getITERATION()));

            //Copy password to clipboard
            if (clipboard_enabled) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Password", password);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, activity.getString(R.string.password_copied), Toast.LENGTH_SHORT).show();
            }
            TextView textViewPassword = (TextView) rootView.findViewById(R.id.textViewPassword);
            textViewPassword.setText(password);
            Log.d("Generator", password);

        }

    }

}
