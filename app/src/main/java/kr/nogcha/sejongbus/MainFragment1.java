/*
 * Copyright (C) 2015 Chobob City
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.nogcha.sejongbus;

import android.app.Fragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainFragment1 extends Fragment {
    private SejongBisClient mBisClient;
    private ArrayList<Spanned> mList;
    private ArrayAdapter<Spanned> mAdapter;
    private ListView mListView;
    private JSONArray mJsonArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBisClient = new SejongBisClient(getActivity());
        mList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.f_main_1, container, false);

        final EditText editText = (EditText) rootView.findViewById(R.id.editText);
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    editText.setText("");
                    return true;
                }
                return false;
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = editText.getText().toString();
                    if (!query.equals("")) onSearch(query);
                    return true;
                }
                return false;
            }
        });

        mListView = (ListView) rootView.findViewById(R.id.listView);
        mListView.setEmptyView(rootView.findViewById(R.id.textView));
        mListView.setAdapter(mAdapter);

        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = editText.getText().toString();
                if (!query.equals("")) onSearch(query);
            }
        });

        return rootView;
    }

    private void onSearch(String query) {
        MainActivity.hideSoftInput();

        if (Pattern.matches("^[0-9-]+$", query)) {
            searchBusRoute(query);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        MainActivity.startHostActivity(TrafficActivity.BUS_ROUTE_DETAIL,
                                mJsonArray.getJSONObject(position).getInt("route_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            searchBusStop(query);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        MainActivity.startHostActivity(TrafficActivity.BUS_STOP_ROUTE,
                                mJsonArray.getJSONObject(position).getInt("stop_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void searchBusRoute(String busRoute) {
        try {
            mJsonArray = mBisClient.searchBusRoute(busRoute, true).getJSONArray("busRouteList");
            mList.clear();
            for (int i = 0; i < mJsonArray.length(); i++) {
                JSONObject json = mJsonArray.getJSONObject(i);
                mList.add((Spanned) TextUtils.concat(
                        mBisClient.getRouteType(json.getInt("route_type")),
                        new SpannableString(" " + json.getString("route_name") + "\n"
                                + json.getString("st_stop_name") + "~"
                                + json.getString("ed_stop_name"))));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }

    private void searchBusStop(String busStop) {
        try {
            mJsonArray = mBisClient.searchBusStop(busStop, true).getJSONArray("busStopList");
            mList.clear();
            for (int i = 0; i < mJsonArray.length(); i++) {
                JSONObject json = mJsonArray.getJSONObject(i);
                mList.add(new SpannableString(json.getString("stop_name") + "\n("
                        + json.getString("service_id") + ")"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }
}
