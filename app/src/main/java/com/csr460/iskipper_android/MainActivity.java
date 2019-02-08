package com.csr460.iskipper_android;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.csr460.iSkipper.emulator.Emulator;
import com.csr460.iSkipper.handler.CaptureHandler;
import com.csr460.iSkipper.support.AnswerPacketHashMap;
import com.csr460.iSkipper.support.IClickerChannel;
import com.csr460.iskipper_android.device.SerialAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private volatile Emulator emulator;
    private TextView channelTextView;
    private FloatingActionButton fab;
    private static TextView output;
    private static BarChart barChart;
    private static int[] data;

    private static final String USB_PERMISSION_STRING = "com.csr460.iskipper_android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner channelSpinner = (Spinner) findViewById(R.id.channelSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.channel_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(adapter);
        channelTextView = (TextView) findViewById(R.id.channelTextView);
        output = (TextView) findViewById(R.id.output);
        barChart = (BarChart) findViewById(R.id.barChart);
        initBArChart();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> {
            if (emulator.changeChannel(IClickerChannel.valueOf(channelSpinner.getSelectedItem().toString())))
                showMessage("On channel " + channelSpinner.getSelectedItem().toString());
            (new Thread(() -> {
                emulator.startCapture(new CaptureHandlerOnUI(new AnswerPacketHashMap(), this));
            })).start();
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Auto-generated above
        App.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE), this, USB_PERMISSION_STRING);//Initialize CH34X Driver
        setOnClickListener();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initBArChart(){
        List<BarEntry> entries = new ArrayList<BarEntry>();

        //let the bar chart has white background
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        YAxis left = barChart.getAxisLeft();
        YAxis right = barChart.getAxisRight();
        left.setDrawAxisLine(false);
        left.setDrawGridLines(false);
        right.setDrawAxisLine(false);
        right.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        left.setDrawLabels(false);
        right.setDrawLabels(false);
        left.setDrawZeroLine(true);
        
        BarDataSet dataSet = new BarDataSet(entries, "statics");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }


    private void setOnClickListener() {
        findViewById(R.id.connectButton).setOnClickListener(v -> {
            v.setEnabled(false);
            SerialAdapter serial = new SerialAdapter(App.driver);
            if (!serial.usbFeatureSupported() || !serial.openDevice() || !serial.configPorts()) {
                v.setEnabled(true);
                return;
            }
            emulator = new Emulator(serial);
            (new Thread(() -> {
                emulator.initialize();
                this.runOnUiThread(() -> {
                    if (emulator.isAvailable()) {
                        showMessage("Success");
                        v.setEnabled(!emulator.isAvailable());
                        fab.setEnabled(emulator.isAvailable());
                    }
                });
            })).start();
        });
    }

    public static void showStatis(int a, int b, int c, int d, int e, int total) {
        data = new int[]{a, b, c};
        List<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(1f, a));
        entries.add(new BarEntry(2f, b));
        entries.add(new BarEntry(3f, c));
        entries.add(new BarEntry(4f, d));
        entries.add(new BarEntry(5f, e));
        BarDataSet dataSet = new BarDataSet(entries, "statics");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
        output.setText("A: " + a + "  B: " + b + "  C:  " + c + "  D: " + d + "  E: " + e);
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
