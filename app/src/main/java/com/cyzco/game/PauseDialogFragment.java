package com.cyzco.game;

import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDelegate;
import static android.content.Context.MODE_PRIVATE;

public class PauseDialogFragment extends DialogFragment
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
        final TetrisGame[] tetrisGame = {mainActivity.tetrisGame};

        // Inflate the custom layout for the dialog
        View view = inflater.inflate(R.layout.pause_dialog, container, false);

        // Find views from the layout
        RelativeLayout pause_dialog = view.findViewById(R.id.pause_dialog);
        RelativeLayout pause_menu = view.findViewById(R.id.pause_menu);
        Button resume = view.findViewById(R.id.resume);
        Button restart = view.findViewById(R.id.restart);
        Button quit_app = view.findViewById(R.id.quit_app);

        // Set logic for the buttons
        pause_menu.setOnClickListener(v -> {});

        resume.setOnClickListener(v -> this.dismiss());

        restart.setOnClickListener(v ->
        {
            dismiss();
            tetrisGame[0].togglePause();
            tetrisGame[0] = new TetrisGame(mainActivity);
            mainActivity.tetrisGame = tetrisGame[0];
        });

        quit_app.setOnClickListener(v ->
        {
            if (getActivity() != null)
            {
                getActivity().finishAffinity();
                System.exit(0);
                dismiss();
            }
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