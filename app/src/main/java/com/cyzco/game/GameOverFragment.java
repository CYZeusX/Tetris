package com.cyzco.game;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.content.DialogInterface;

import androidx.fragment.app.DialogFragment;

public class GameOverFragment extends DialogFragment
{
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

        // resources setup
        View view = inflater.inflate(R.layout.game_over, container, false);
        RelativeLayout game_over_menu = view.findViewById(R.id.gameOver_menu);
        Button restart = view.findViewById(R.id.restart);
        Button quit_app = view.findViewById(R.id.quit_app);
        ImageView cat_laugh = view.findViewById(R.id.cat_laugh);
        TextView score_gained = view.findViewById(R.id.score_gained);
        TextView lines_cleared = view.findViewById(R.id.lines_cleared);
        TextView tetris_gained = view.findViewById(R.id.tetris_gained);

        // resources usage
        score_gained.setText(String.format("Score: %s", tetrisGame[0].getScoreGained()));
        lines_cleared.setText(String.format("Lines: %s", tetrisGame[0].getLinesCleared()));
        tetris_gained.setText(String.format("Tetris: %s", tetrisGame[0].tetrisGained));

        game_over_menu.setOnClickListener(v -> {});

        Glide.with(requireContext()).load(R.drawable.cat_laugh).into(cat_laugh);

        restart.setOnClickListener(v ->
        {
            dismiss();
            restart();
        });

        quit_app.setOnClickListener(v ->
        {
            dismiss();
            quit_app();
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        restart();
    }

    private void restart()
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        final TetrisGame[] tetrisGame = {mainActivity.tetrisGame};
        tetrisGame[0].togglePause();
        tetrisGame[0] = new TetrisGame(mainActivity);
        mainActivity.tetrisGame = tetrisGame[0];
    }

    private void quit_app()
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        final TetrisGame[] tetrisGame = {mainActivity.tetrisGame};
        mainActivity.finishAffinity();
        System.exit(0);
    }
}
