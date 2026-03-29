package com.cyzco.game;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.util.Objects;
import android.widget.Button;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Shader;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.graphics.RenderEffect;
import android.content.DialogInterface;
import androidx.fragment.app.DialogFragment;

public class GameOverFragment extends DialogFragment
{
    private View rootView;
    private GameOverListener listener;

    @Override
    public void onStart()
    {
        super.onStart();
        if (getDialog() == null && getDialog().getWindow() == null)
            return;

        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        if (isAdded())
        {
            rootView = requireActivity().getWindow().getDecorView().getRootView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                rootView.setRenderEffect(RenderEffect.createBlurEffect(20, 20, Shader.TileMode.MIRROR));
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
        rootView = view.getRootView();
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

        Glide.with(requireContext()).load(R.drawable.cat_laugh).into(cat_laugh);


        restart.setOnClickListener(v ->
        {
            dismiss();

            // Get MainActivity instance and call restart
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).restartGame();
            } else {
                Log.e("GameRestart", "Cannot call restartGame: Activity is not MainActivity");
            }
        });

        quit_app.setOnClickListener(v ->
        {
            dismiss();
            if (listener != null) listener.onQuitGame();
        });

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && rootView != null)
            rootView.setRenderEffect(null);
        if (listener != null) listener.onRestartGame();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof GameOverListener)
            listener = (GameOverListener) context;
        else throw new RuntimeException(
                context + " must implement GameOverListener");
    }

    public interface GameOverListener {
        void onRestartGame();
        void onQuitGame();
    }
}