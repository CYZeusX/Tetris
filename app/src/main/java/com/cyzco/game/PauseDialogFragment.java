package com.cyzco.game;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.graphics.Shader;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.graphics.RenderEffect;
import android.content.DialogInterface;
import androidx.fragment.app.DialogFragment;

public class PauseDialogFragment extends DialogFragment
{
    private View rootView;

    // to remove the blank space area at the corner of the setting menu
    @Override
    public void onStart()
    {
        super.onStart();

        // --- Window Styling ---
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Cannot proceed without dialog/window
        else return;

        // pause the game
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        if (!mainActivity.tetrisGame.isPaused)
            mainActivity.tetrisGame.togglePause();

        Log.e("Pause onStart()", "onStart(): isPaused = " + mainActivity.tetrisGame.isPaused);

        // --- Apply Blur Effect ---
        mainActivity = null;
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();

        // Check if Activity is valid before accessing its views/state
        if (mainActivity != null && !mainActivity.isFinishing() && !mainActivity.isDestroyed() && isAdded())
        {
            try {
                rootView = mainActivity.getWindow().getDecorView().getRootView();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && rootView != null && rootView.isAttachedToWindow())
                {
                    // Apply blur only if API level supports it and view is attached
                    rootView.setRenderEffect(RenderEffect.createBlurEffect(25, 25, Shader.TileMode.MIRROR));
                }
            }

            catch (Exception e) {
                Log.e("PauseDialog", "Error applying blur effect in onStart", e);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //get hosting activity instance
        MainActivity mainActivity = null;
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();

        // Handle case where activity might not be MainActivity or is null
        if (mainActivity == null) {
            Log.e("PauseDialog", "onCreateView: Could not get MainActivity reference.");
            // Return an empty view or handle error appropriately
            return new View(getContext()); // Avoid null pointer
        }

        // Inflate the layout
        View view = inflater.inflate(R.layout.pause_dialog, container, false);

        // Find views
        Button resume = view.findViewById(R.id.resume);
        Button restart = view.findViewById(R.id.restart);
        Button quit_app = view.findViewById(R.id.quit_app);

        //AI
        final MainActivity finalMainActivity = mainActivity;

        // Resume button just dismisses the dialog. MainActivity's logic handles resuming.
        resume.setOnClickListener(v -> this.dismiss());

        // Restart button dismisses and calls MainActivity's restart logic
        restart.setOnClickListener(v -> {
            dismiss();
            finalMainActivity.restartGame();
        });

        // Quit button finishes the activity stack
        quit_app.setOnClickListener(v -> {
            finalMainActivity.finishAffinity(); // Close all activities in the task
            System.exit(0); // Force exit (use with caution)
            // dismiss(); // Not strictly needed after finishAffinity/exit
        });

        return view;
    }

    // continue the game after closing the setting menu
    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Log.d("PauseDialog", "onDismiss called.");

        MainActivity mainActivity = null;
        if (getActivity() instanceof MainActivity)
            mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.tetrisGame.togglePause();

        // Remove blur effect only if view is still valid
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && rootView != null && rootView.isAttachedToWindow()) {
            try {
                rootView.setRenderEffect(null);
            }

            catch (Exception e) {
                Log.e("PauseDialog", "Error removing blur effect in onDismiss", e);
            }
        }
        rootView = null;
    }
}