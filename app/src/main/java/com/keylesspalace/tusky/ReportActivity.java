/* Copyright 2017 Andrew Dawson
 *
 * This file is part of Tusky.
 *
 * Tusky is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky. If not, see
 * <http://www.gnu.org/licenses/>. */

package com.keylesspalace.tusky;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends BaseActivity {
    private static final String TAG = "ReportActivity"; // Volley request tag

    private String domain;
    private String accessToken;
    private View anyView; // what Snackbar will use to find the root view

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Intent intent = getIntent();
        final String accountId = intent.getStringExtra("account_id");
        String accountUsername = intent.getStringExtra("account_username");
        final String statusId = intent.getStringExtra("status_id");
        String statusContent = intent.getStringExtra("status_content");

        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        domain = preferences.getString("domain", null);
        accessToken = preferences.getString("accessToken", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            String title = String.format(getString(R.string.report_username_format),
                    accountUsername);
            bar.setTitle(title);
        }
        anyView = toolbar;

        TextView content = (TextView) findViewById(R.id.report_status_content);
        content.setText(HtmlUtils.fromHtml(statusContent));

        final EditText comment = (EditText) findViewById(R.id.report_comment);
        Button send = (Button) findViewById(R.id.report_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] statusIds = new String[] { statusId };
                sendReport(accountId, statusIds, comment.getText().toString());
            }
        });
    }

    /* JSONArray has a constructor to take primitive arrays but it's restricted to API level 19 and
     * above, so this is an alternative. */
    private static JSONArray makeStringArrayCompat(String[] stringArray) throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < stringArray.length; i++) {
            result.put(i, stringArray[i]);
        }
        return result;
    }

    private void sendReport(final String accountId, final String[] statusIds,
            final String comment) {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("account_id", accountId);
            parameters.put("status_ids", makeStringArrayCompat(statusIds));
            parameters.put("comment", comment);
        } catch (JSONException e) {
            Log.e(TAG, "Not all the report parameters have been properly set. "
                    + e.getMessage());
            onSendFailure(accountId, statusIds, comment);
            return;
        }
        String endpoint = getString(R.string.endpoint_reports);
        String url = "https://" + domain + endpoint;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onSendSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onSendFailure(accountId, statusIds, comment);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
        request.setTag(TAG);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void onSendSuccess() {
        Toast.makeText(this, getString(R.string.confirmation_reported), Toast.LENGTH_SHORT)
                .show();
        finish();
    }

    private void onSendFailure(final String accountId, final String[] statusIds,
            final String comment) {
        Snackbar.make(anyView, R.string.error_report_unsent, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendReport(accountId, statusIds, comment);
                    }
                })
                .show();
    }
}
