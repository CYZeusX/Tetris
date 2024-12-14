package com.cyzco.game;

import coil.Coil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.ImageView;
import coil.request.ImageRequest;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

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

        View view = inflater.inflate(R.layout.game_over, container, false);

        RelativeLayout game_over_menu = view.findViewById(R.id.gameOver_menu);
        Button restart = view.findViewById(R.id.restart);
        ImageView cat_laugh = view.findViewById(R.id.cat_laugh);

        game_over_menu.setOnClickListener(v -> {});

        Glide.with(requireContext()).load(R.drawable.cat_laugh).into(cat_laugh);

        restart.setOnClickListener(v ->
        {
            dismiss();
            restart();
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
}
