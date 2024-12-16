package com.cyzco.game;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.EditText;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDelegate;
import static android.content.Context.MODE_PRIVATE;

public class SettingsDialogFragment extends DialogFragment
{
    // to remove the blank space area at the corner of the setting menu
    @Override
    public void onStart()
    {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null)
        {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        TetrisGame tetrisGame = mainActivity.tetrisGame;
        Shapes shapes = new Shapes();

        // Inflate the custom layout for the dialog
        View view = inflater.inflate(R.layout.setting_dialog, container, false);

        // Find views from the layout
        RelativeLayout setting_menu = view.findViewById(R.id.setting_menu);
        EditText change_block = view.findViewById(R.id.change_block);
        Button lightDark = view.findViewById(R.id.lightDark);
        Spinner blockSpinner = view.findViewById(R.id.choose_block);

        // Pass the EditText reference to TetrisGame
        tetrisGame.setStringShape(change_block);

        // Set logic for the buttons
        setting_menu.setOnClickListener(v -> {});

        change_block.setOnClickListener(v -> tetrisGame.changeBlock());

        lightDark.setOnClickListener(v ->
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE);
            int currentMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
            int newMode = currentMode == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(newMode);
            sharedPreferences.edit().putInt("theme_mode", newMode).apply();
        });

        String[] blocks = {"回", "■", "□", String.valueOf(change_block)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, R.layout.spinner_item, blocks);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner);
        blockSpinner.setAdapter(adapter);
        blockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                tetrisGame.changeBlock(selectedItem);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Return the view
        return view;
    }

    // continue the game after closing the setting menu
    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.tetrisGame.togglePause();
    }

}