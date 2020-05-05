package com.led_on_off.led.ui.main;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.led_on_off.led.Appliances;
import com.led_on_off.led.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private View view;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_room_tabs, container, false);
        View root = inflater.inflate(R.layout.fragment_configure_room, container, false);
        final TextView textView = root.findViewById(R.id.room_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        view = root;
        configureRoomLayout();
        return root;
    }
    private void configureRoomLayout() {
        RelativeLayout appLayout = (RelativeLayout) view.findViewById(R.id.configApp);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.topMargin = 100;

        for( Appliances.Types aplTypes : Appliances.Types.values()) {
//        for(int i = 0; i < Appliances.Types.values().length; i++) {
            LinearLayout ll = new LinearLayout(this.getActivity());

            TextView name = new TextView(this.getActivity());
            name.setText(aplTypes.getText());
            name.setTextAppearance( R.style.TextAppearance_AppCompat_Medium);
            RelativeLayout.LayoutParams params_text = new RelativeLayout.LayoutParams(250, 80);
            params_text.leftMargin = 30;
            params_text.topMargin = params.topMargin;
            name.setGravity(Gravity.BOTTOM);
            ll.addView(name, params_text);

            Switch onOff = new Switch(this.getActivity());
            onOff.setChecked(false);
            RelativeLayout.LayoutParams params_switch = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params_switch.addRule(RelativeLayout.RIGHT_OF, name.getId());
            params_switch.topMargin = params.topMargin;
            ll.addView(onOff, params_switch);
            onOff.setOnClickListener(this);

            appLayout.addView(ll, params);
            params.topMargin += 150;
        }
    }

    @Override
    public void onClick(View view) {
        view.getId();
    }
}