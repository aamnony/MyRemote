package com.gmail.aamnony.myremote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
    private static final String IR_FREQ = "ir_freq";
    private static final String TV_CHANNELS = "tv_channels";

    private ConsumerIrManager irManager;
    private IrRemote irRemote;

    private ArrayList<TvChannel> tvChannels;
    private ArrayAdapter<TvChannel> tvChannelAdapter;

    private AlertDialog dialogIrFrequency;
    private AlertDialog dialogAddChannel;

    private EditText editChannel;
    private GridView gridChannels;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        irManager = (ConsumerIrManager) getSystemService(CONSUMER_IR_SERVICE);
        irRemote = new YesSilverRemote();

        String json = getDefaultSharedPreferences(this).getString(TV_CHANNELS, "");
        TvChannel[] savedTvChannels = new Gson().fromJson(json, TvChannel[].class);

        tvChannels = (savedTvChannels == null) ?
                new ArrayList<TvChannel>() :
                new ArrayList<>(Arrays.asList(savedTvChannels));

        tvChannelAdapter = new ArrayAdapter<>(this, R.layout.grid_text, android.R.id.text1, tvChannels);
        tvChannelAdapter.setNotifyOnChange(true);
        gridChannels = (GridView) findViewById(R.id.gridChannels);
        gridChannels.setAdapter(tvChannelAdapter);
        gridChannels.setOnItemClickListener(this);
        gridChannels.setOnItemLongClickListener(this);

        editChannel = (EditText) findViewById(R.id.editChannel);
        editChannel.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    if (!TextUtils.isEmpty(v.getText()))
                    {
                        int channelNumber = Integer.parseInt(v.getText().toString());
                        int[] digits = TvChannel.getDigits(channelNumber);
                        transmitDigits(digits);
                        v.setText("");
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null)
                        {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long id)
    {
        transmitDigits(tvChannels.get(position).digits);
    }

    @Override
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id)
    {
        removeChannel(tvChannels.get(position));
        return true;
    }

    public void onButtonClick (View view)
    {
        if (view instanceof Button)
        {
            final int freq = getFrequency();
            int[] pattern = irRemote.getPattern(((Button) view).getText().toString());
            irManager.transmit(freq, pattern);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_ir_frequency:
                showIrFrequencyDialog();
                return true;
            case R.id.action_add_channel:
                showAddChannelDialog();
                return true;
            case R.id.action_debug_ir:
                irManager.transmit(getFrequency(), new int[]{500000, 500000, 500000, 500000});
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void transmitDigits (int[] digits)
    {
        final int freq = getFrequency();
        for (int i = digits.length - 1; i >= 0; i--)
        {
            irManager.transmit(freq, irRemote.getPattern(String.valueOf(digits[i])));
            SystemClock.sleep(300);
        }
    }

    private int getFrequency ()
    {
        return getDefaultSharedPreferences(MainActivity.this).getInt(IR_FREQ, 38000);
    }

    private void showIrFrequencyDialog ()
    {
        if (dialogIrFrequency == null)
        {
            final ConsumerIrManager.CarrierFrequencyRange[] frequencies = irManager.getCarrierFrequencies();
            CharSequence[] items = new CharSequence[frequencies.length];
            final int savedFreq = getFrequency();
            final int[] checked = {-1};
            for (int i = 0; i < frequencies.length; i++)
            {
                int frequency = frequencies[i].getMinFrequency();
                if (frequency == savedFreq)
                {
                    checked[0] = i;
                }
                items[i] = String.valueOf(frequency);
            }

            dialogIrFrequency = new AlertDialog.Builder(this)
                    .setTitle("Set IR Frequency")
                    .setSingleChoiceItems(items, checked[0], new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            checked[0] = which;
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Apply", new DialogInterface.OnClickListener()
                    {
                        @SuppressLint("ApplySharedPref")
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            getDefaultSharedPreferences(MainActivity.this)
                                    .edit().putInt(IR_FREQ, frequencies[checked[0]].getMinFrequency())
                                    .commit();
                            dialog.dismiss();
                        }
                    })
                    .create();
        }
        dialogIrFrequency.show();
    }

    private void showAddChannelDialog ()
    {

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_channel, null);

        final EditText editNumber = ((EditText) view.findViewById(R.id.editNumber));
        final EditText editName = ((EditText) view.findViewById(R.id.editName));


        if (dialogAddChannel == null)
        {
            dialogAddChannel = new AlertDialog.Builder(this)
                    .setTitle("Add Channel")
                    .setView(view)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Add", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            addChannel(new TvChannel(editName.getText().toString(), Integer.parseInt(editNumber.getText().toString())));

                            dialog.dismiss();
                        }
                    }).create();
        }
        dialogAddChannel.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogAddChannel.show();
    }

    @SuppressLint("ApplySharedPref")
    private void addChannel (TvChannel tvChannel)
    {
        tvChannelAdapter.add(tvChannel);

        getDefaultSharedPreferences(MainActivity.this)
                .edit().putString(TV_CHANNELS, new Gson().toJson(tvChannels.toArray(new TvChannel[tvChannels.size()])))
                .commit();
    }

    @SuppressLint("ApplySharedPref")
    private void removeChannel (TvChannel tvChannel)
    {
        tvChannelAdapter.remove(tvChannel);

        getDefaultSharedPreferences(MainActivity.this)
                .edit().putString(TV_CHANNELS, new Gson().toJson(tvChannels.toArray(new TvChannel[tvChannels.size()])))
                .commit();
    }
}
