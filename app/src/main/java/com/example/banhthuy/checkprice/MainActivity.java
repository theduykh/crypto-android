package com.example.banhthuy.checkprice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.banhthuy.checkprice.util.BittrexMarket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    String url_summaries = "https://bittrex.com/api/v1.1/public/getmarketsummaries";
    static String requestMarket = "";
    static String dataShow = "";
    EditText editText;
    TextView textView;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMarket = editText.getText().toString();
                new GetUrlContentTask().execute(url_summaries);

                editor.putString("input", requestMarket);
                editor.apply();
            }
        });
        editText = (EditText) findViewById(R.id.textInput);
        textView = (TextView) findViewById(R.id.textOutput);
        String input = sharedPref.getString("input", "");
        if (input.equals("") || input == null) {
            editText.setText(R.string.action_input);
        } else editText.setText(input);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            requestMarket = editText.getText().toString();
                            new GetUrlContentTask().execute(url_summaries);

                            editor.putString("input", requestMarket);
                            editor.apply();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String formatChange(double change) {
        String rString = "";
        if (change >= 10) {
            rString = " " + new DecimalFormat("0.00").format(change).concat("%");
        } else if (change >= 0) {
            rString = "  " + new DecimalFormat("0.00").format(change).concat("%");
        } else if (change <= -10) {
            rString = "" + new DecimalFormat("0.00").format(change).concat("%");
        } else rString = " " + new DecimalFormat("0.00").format(change).concat("%");

        return rString;
    }

    public String formatBTC(double price) {
        String rString = "";
        rString = new DecimalFormat("0.000000000").format(price);
        char[] chars = rString.toCharArray();
        rString = "";

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '0' || chars[i] == '.') {
                continue;
            } else {
                rString = rString + chars[i] + chars[i + 1] + chars[i + 2] + chars[i + 3];
                break;
            }
        }
        return rString;
    }

    public String formatUSDT(double data) {
        String rString = "";
        double temp = data;
        // NumberFormat formatter = new DecimalFormat("#0.0000000");

        if (temp > 10000) {
            rString = new DecimalFormat("00000").format(data);
            return rString;
        }
        if (temp > 1000) {
            rString = new DecimalFormat("0000").format(data);
            return rString.concat(" ");
        }
        if (temp > 100) {
            rString = new DecimalFormat("000").format(data);
            return rString.concat("  ");
        }
        if (temp > 10) {
            rString = new DecimalFormat("00.0").format(data);
            return rString.concat(" ");
        }
        if (temp > 0) {
            rString = new DecimalFormat("0.000").format(data);
            return rString.concat("");
        }

        rString = new DecimalFormat("0.000").format(data);
        return rString.concat("");
    }

    private class GetUrlContentTask extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            URL url = null;
            String content = "", line;
            StringBuilder rs = new StringBuilder();
            try {
                url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while ((line = rd.readLine()) != null) {
                    content += line + "\n";
                }

                JSONObject dataJson = new JSONObject(content);
                JSONArray array = dataJson.getJSONArray("result");
                ArrayList<BittrexMarket> markets = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    BittrexMarket market = null;
                    try {
                        market = new BittrexMarket(array.getJSONObject(i));
                    } catch (JSONException e) {
                        continue;
                    }
                    markets.add(market);
                }

                String[] requests = requestMarket.split(";");
                for (String request : requests) {
                    for (BittrexMarket market : markets) {
                        if (request.toUpperCase().equals(market.getMarket())) {
                            if (market.getType() == 1) {
                                rs.append(market.getMarket() + " : " + formatBTC(market.getLastPrice()) + "  " + formatChange(market.getChange()) + " " + formatBTC(market.gethighPrice()) + "  " + formatBTC(market.getlowPrice()));
                            }
                            if (market.getType() == 0) {
                                rs.append(market.getMarket() + ": " + formatUSDT(market.getLastPrice()) + " " + formatChange(market.getChange()) + " " + formatUSDT(market.gethighPrice()) + " " + formatUSDT(market.getlowPrice()));
                            }
                            rs.append(System.lineSeparator());
                        }
                    }
                }
                dataShow = rs.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return rs.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(String result) {
            // this is executed on the main thread after the process is over
            // update your UI here
//            displayMessage(result);
            textView.setText(result);
            if (result.isEmpty() || result == null) {
                Toast.makeText(MainActivity.this, "The internet sucks", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(MainActivity.this, "bla bla", Toast.LENGTH_SHORT).show();
        }
    }
}
