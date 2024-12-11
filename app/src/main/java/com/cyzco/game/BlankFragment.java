package com.cyzco.game;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.app.ActivityCompat.recreate;
import static androidx.core.graphics.BitmapKt.set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Objects;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        RelativeLayout setting_menu = view.findViewById(R.id.setting_menu);
        Button quit = view.findViewById(R.id.quit);
        EditText change_block = view.findViewById(R.id.change_block);
        Button lightDark = view.findViewById(R.id.lightDark);

        mainActivity.tetrisGame.setStringShape(change_block);

        quit.setOnClickListener(v ->
        {
            if (mainActivity.tetrisGame.isBoardHidden())
            {
                mainActivity.tetrisGame.togglePause();
                mainActivity.tetrisGame.toggleBoardVisible();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        change_block.setOnClickListener(v -> mainActivity.tetrisGame.changeBlock());

        lightDark.setOnClickListener(v -> {
            // Access shared preferences and toggle theme
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE);
            int currentMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO);
            int newMode = currentMode == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(newMode);
            sharedPreferences.edit().putInt("theme_mode", newMode).apply();

            // Delay toggleBoardVisible until after recreation
            SurfaceView surfaceView = mainActivity.monitor; // Ensure this is the correct SurfaceView reference
            surfaceView.post(() -> {
                if (surfaceView.getHolder().getSurface().isValid()) {
                    mainActivity.tetrisGame.toggleBoardVisible(); // Toggle visibility
                } else {
                    Log.e("SurfaceView", "Surface is not valid yet!");
                }
            });
        });

        setting_menu.setOnClickListener(v -> {});
        return view;
    }
}