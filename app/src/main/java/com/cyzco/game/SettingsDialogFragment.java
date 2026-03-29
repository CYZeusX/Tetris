package com.cyzco.game;

import java.util.List;
import java.util.Arrays;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.Objects;
import java.util.ArrayList;
import android.widget.Button;
import android.text.Editable;
import android.widget.Spinner;
import android.view.ViewGroup;
import android.widget.EditText;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.text.TextWatcher;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.graphics.RenderEffect;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDelegate;
import static android.content.Context.MODE_PRIVATE;

public class SettingsDialogFragment extends DialogFragment
{
    private List<String> blocks;
    private ArrayAdapter<String> adapter;
    private View rootView;

    // to remove the blank space area at the corner of the setting menu
    @Override
    public void onStart()
    {
        super.onStart();
        if (getDialog() == null && getDialog().getWindow() == null)
            return;

        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // pause the game
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        if (!mainActivity.tetrisGame.isPaused)
            mainActivity.tetrisGame.togglePause();

        Log.e("Setting onStart()", "onStart(): isPaused = " + mainActivity.tetrisGame.isPaused);

        if (isAdded())
        {
            rootView = requireActivity().getWindow().getDecorView().getRootView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                rootView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.MIRROR));
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        TetrisGame tetrisGame = mainActivity.tetrisGame;

        // Inflate the custom layout for the dialog
        View view = inflater.inflate(R.layout.setting_dialog, container, false);

        // Find views from the layout
        rootView = view.getRootView();
        EditText type_block = view.findViewById(R.id.change_block);
        Button emojiMode_button = view.findViewById(R.id.emoji_button);
        Button lightDark = view.findViewById(R.id.lightDark);
        Spinner blockSpinner = view.findViewById(R.id.choose_block);

        // Pass the EditText reference to TetrisGame
        tetrisGame.setStringShape(type_block);

        // Set logic for the buttons
        type_block.setOnClickListener(v -> tetrisGame.changeBlock());

        // Set logic for the emoji button
        emojiMode_button.setOnClickListener(v -> tetrisGame.emojiMode = !tetrisGame.emojiMode);

        lightDark.setOnClickListener(v ->
        {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE);
            int currentMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
            int newMode = currentMode == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(newMode);
            sharedPreferences.edit().putInt("theme_mode", newMode).apply();

            mainActivity.tetrisGame.togglePause();
        });

        blocks = new ArrayList<>(Arrays.asList("■", "[ ]", "屌", "\uD83D\uDE03"));
        adapter = new ArrayAdapter<>(mainActivity, R.layout.spinner, blocks);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);

        blockSpinner.setAdapter(adapter);
        blockSpinner.setDropDownVerticalOffset(350);
        blockSpinner.setSelection(blocks.indexOf(tetrisGame.getBlock()));
        blockSpinner.setOnTouchListener((v, event) ->
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                type_block.setVisibility(View.VISIBLE);
                emojiMode_button.setVisibility(View.VISIBLE);
            }
            return false;
        });

        blockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                tetrisGame.changeBlock(selectedItem);
                type_block.setVisibility(View.GONE);
                emojiMode_button.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        type_block.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                String newBlock = editable.toString();
                if (!newBlock.isEmpty() || !blocks.contains(newBlock))
                    adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    // continue the game after closing the setting menu
    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);

        MainActivity mainActivity = null;
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.tetrisGame.togglePause();
        Log.w("Setting onDismiss", "onDismiss: " + mainActivity.tetrisGame.isPaused);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && rootView != null)
            rootView.setRenderEffect(null);
        Log.d("SettingsDialog", "onDismiss finished.");
    }
}