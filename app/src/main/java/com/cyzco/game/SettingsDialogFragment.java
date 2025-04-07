package com.cyzco.game;

import java.util.List;
import java.util.Arrays;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import android.widget.Button;
import android.text.Editable;
import android.widget.Spinner;
import android.view.ViewGroup;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.AdapterView;
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
    private List<String> blocks;
    private ArrayAdapter<String> adapter;

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
            tetrisGame.togglePause();
        });

        blocks = new ArrayList<>(Arrays.asList("回", "■", "□", "•"));
        adapter = new ArrayAdapter<>(mainActivity, R.layout.spinner_item, blocks);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner);
        blockSpinner.setAdapter(adapter);

        change_block.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                String newBlock = s.toString();
                if (!newBlock.isEmpty() || !blocks.contains(newBlock))
                {
                    blocks.add(newBlock);
                    adapter.notifyDataSetChanged();
                }
            }
        });

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