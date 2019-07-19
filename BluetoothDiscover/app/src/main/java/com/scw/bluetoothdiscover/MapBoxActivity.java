package com.scw.bluetoothdiscover;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lineProgress;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineGradient;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;


import android.os.Handler;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import timber.log.Timber;


public class MapBoxActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    // MapBox
    private static MapView mapView;
    private static MapboxMap map;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String HEATMAP_LAYER_ID = "earthquakes-heat";
    private static final String HEATMAP_LAYER_SOURCE = "earthquakes";
    private static final String CIRCLE_LAYER_ID = "earthquakes-circle";
    private static LocationComponent locationComponent;    // Mapbox Location Object
    private static HashMap<String, ArrayList<LatLng>> pathRecord;  // For drawing device path
    private static HashMap<String, Integer> pathTimer;     // For deleting time out device path
    private static HashMap<LatLng, Integer> pointResult;   // For setting icon markers
    private static int timerValue = 6;                     // For setting default timer
    private FeatureCollection lineFeatureCollection;

    private Handler updatehandler;
    private Runnable updaterunnable;

    // Bluetooth scanning
    public static final int REQUEST_ENABLE_BT = 1;

    //test
    private static Scanner_BTLE scanner_btle; //test
    private static Handler handler;//test
    private static Runnable testRunnable;//test
    private static JSONArray deviceArray = new JSONArray();//test


    // Button
    private Button startButton;
    private Button logoutButton;
    private Button locateButton;

    // HttpRequest
    HttpRequest httpRequest;
    // check mode
    String showHttp = null;
    String username = null;
    boolean login = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pathRecord = new HashMap<>();
        pathTimer = new HashMap<>();
        pointResult = new HashMap<>();


        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toast(getApplicationContext(), "BLE not supported");
            //finish();
        }
        //
        //

        // Mapbox setting
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.map_key));
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map_box);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        //

        // Button setting
        startButton = (Button) findViewById(R.id.startButton);
        findViewById(R.id.startButton).setOnClickListener(this);
        logoutButton = (Button) findViewById(R.id.logout);
        findViewById(R.id.logout).setOnClickListener(this);
        locateButton = (Button) findViewById(R.id.locate);
        findViewById(R.id.locate).setOnClickListener(this);


        // get username from previous Activity
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            login = intent.getBooleanExtra("login", false);
        }


        //test
        httpRequest = new HttpRequest(this);
        scanner_btle = new Scanner_BTLE(this, -100);

        //upload handler
        handler = new Handler();
        testRunnable = new Runnable() {
            @Override
            public void run() {
                deviceJSON(scanner_btle.result());
                scanner_btle.clean();
                Thread testThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readJSON(httpRequest.doPostData(deviceArray, (float) 35.664065, (float) 139.677224, "testname"));

                    }

                });
                testThread.start();
                try {
                    testThread.join();
                } catch (
                        InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };


    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {


                //updateFun();
                //addSourceTest(style);

                //addHeatmapLayer(style);
                //addCircleLayer(style);

                // Path setting
                addLineLayer(style);
                // Marker setting
                addPointLayer(style);


                // Location
                enableLocationComponent(style);

            }
        });
    }

    // Location
    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Get an instance of the component
        locationComponent = map.getLocationComponent();

        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    private void addSourceTest(@NonNull Style loadedMapStyle) {
        try {
            loadedMapStyle.addSource(new GeoJsonSource("SOURCE_ID_TEST", new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson")));
        } catch (MalformedURLException malformedUrlException) {
            Timber.e(malformedUrlException, "That's not an url... ");
        }
    }


    private void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(HEATMAP_LAYER_SOURCE);
        layer.setProperties(

                // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparency color
                // to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgb(103, 169, 207),
                                literal(0.4), rgb(209, 229, 240),
                                literal(0.6), rgb(253, 219, 199),
                                literal(0.8), rgb(239, 138, 98),
                                literal(1), rgb(178, 24, 43)
                        )
                ),

                // Increase the heatmap weight based on frequency and property magnitude
                heatmapWeight(
                        interpolate(
                                linear(), get("mag"),
                                stop(0, 0),
                                stop(6, 1)
                        )
                ),

                // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 3)
                        )
                ),

                // Adjust the heatmap radius by zoom level
                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 20)
                        )
                ),

                // Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 1),
                                stop(9, 0)
                        )
                )
        );

        loadedMapStyle.addLayerAbove(layer, "waterway-label");
    }

    private void addLineLayer(@NonNull Style loadedMapStyle) {
        // The layer properties for our line. This is where we make the line dotted, set the
        // color, etc.
        loadedMapStyle.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(8f),
                lineGradient(interpolate(
                        linear(), lineProgress(),
                        //stop(0f, rgb(6, 1, 255)), // blue
                        //stop(0.1f, rgb(59, 118, 227)), // royal blue
                        //stop(0.3f, rgb(7, 238, 251)), // cyan
                        //stop(0.5f, rgb(0, 255, 42)), // lime
                        //stop(0.7f, rgb(255, 252, 0)), // yellow
                        //stop(1f, rgb(255, 30, 0)) // red
                        stop(0.25f, rgb(255,0,0)),
                        stop(0.5f, rgb(35,206,250)),
                        stop(0.75f, rgb(0,191,255)),// deepsky blue
                        stop(1f, rgb(0,0,255))
                ))));
        /*loadedMapStyle.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                //PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineGradient(
                        interpolate(
                                linear(), lineProgress(),
                                stop(0f, rgb(6, 1, 255)), // blue
                                stop(0.1f, rgb(59, 118, 227)), // royal blue
                                stop(0.3f, rgb(7, 238, 251)), // cyan
                                stop(0.5f, rgb(0, 255, 42)), // lime
                                stop(0.7f, rgb(255, 252, 0)), // yellow
                                stop(1f, rgb(255, 30, 0)) // red
                        )
                        //PropertyFactory.lineColor(Color.parseColor("#e55e5e")
                )
                )
        );*/
        //GeoJsonSource sourceTest = new GeoJsonSource("line-source");
        loadedMapStyle.addSource(new GeoJsonSource("line-source", lineFeatureCollection, new GeoJsonOptions().withLineMetrics(true)));
        // Create the LineString from the list of coordinates and then make a GeoJSON
        // FeatureCollection so we can add the line to our map as a layer.

        /*
        loadedMapStyle.addSource(new GeoJsonSource("line-source",
                FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(
                        LineString.fromLngLats(routeCoordinates)
                )})));*/

    }

    private void addPointLayer(@NonNull Style loadedMapStyle) {
        // Set Marker Image
        loadedMapStyle.addImage(ICON_ID,
                BitmapFactory.decodeResource(
                        MapBoxActivity.this.getResources(),
                        R.drawable.mapbox_marker_icon_default
                ));
        GeoJsonSource sourceTest = new GeoJsonSource(SOURCE_ID);
        loadedMapStyle.addSource(sourceTest);
        SymbolLayer symbolLayer = new SymbolLayer(LAYER_ID, SOURCE_ID);
        symbolLayer.withProperties(//PropertyFactory
                iconImage(ICON_ID),
                textField("{number}"),
                textOffset(new Float[]{0f, -2.5f}),
                textColor(Color.BLUE),
                iconAllowOverlap(false),
                textIgnorePlacement(true),
                iconIgnorePlacement(true));
        loadedMapStyle.addLayer(symbolLayer);
        //Heat map
        //loadedMapStyle.addLayerBelow(symbolLayer, HEATMAP_LAYER_ID);
    }

    private void addCircleLayer(@NonNull Style loadedMapStyle) {

        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, SOURCE_ID);
        circleLayer.setProperties(

                // Size circle radius by earthquake magnitude and zoom level
                circleRadius(
                        interpolate(
                                linear(), zoom(),
                                literal(7), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 1),
                                        stop(6, 4)
                                ),
                                literal(16), interpolate(
                                        linear(), get("mag"),
                                        stop(1, 5),
                                        stop(6, 50)
                                )
                        )
                ),

                // Color circle by earthquake magnitude
                circleColor(
                        interpolate(
                                linear(), get("mag"),
                                literal(1), rgba(33, 102, 172, 0),
                                literal(2), rgb(103, 169, 207),
                                literal(3), rgb(209, 229, 240),
                                literal(4), rgb(253, 219, 199),
                                literal(5), rgb(239, 138, 98),
                                literal(6), rgb(178, 24, 43)
                        )
                ),

                // Transition from heatmap to circle layer by zoom level
                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0),
                                stop(8, 1)
                        )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
        );


        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }


    private void updateFun() {
        updatehandler = new Handler();
        updaterunnable = new Runnable() {
            @Override
            public void run() {
                Thread httpThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        readJSON(httpRequest.doPostQuery());
                    }

                });
                httpThread.start();
                try {
                    httpThread.join();
                } catch (
                        InterruptedException e) {
                    e.printStackTrace();
                }
                updateMarkerPosition();
                updateLine();
                updatehandler.postDelayed(this, 3000);
            }
        };
        updatehandler.post(updaterunnable);

    }

    private void updateMarkerPosition() {

        GeoJsonSource pointSource = map.getStyle().getSourceAs(SOURCE_ID);

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        //pointResult
        Iterator iter = pointResult.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            LatLng key = (LatLng) entry.getKey();
            Integer value = (Integer) entry.getValue();

            JsonObject json = new JsonObject();
            json.addProperty("number", value);

            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(key.getLongitude(), key.getLatitude()), json));

        }
        pointSource.setGeoJson(FeatureCollection.fromFeatures(symbolLayerIconFeatureList));

    }

    private void updateLine() {
        GeoJsonSource pathSource = map.getStyle().getSourceAs("line-source");
        // Feature List
        List<Feature> pathPointList = new ArrayList<>();
        // pathRecord
        Iterator iter = pathRecord.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ArrayList<LatLng> value = (ArrayList<LatLng>) entry.getValue();
            if (value.size() >= 2) {
                // Point List
                List<Point> coordinates = new ArrayList<>();
                String key = (String) entry.getKey();
                int size = value.size();
                if (value.size() > timerValue) {
                    for (int j = size - timerValue; j < size; j++) {
                        coordinates.add(Point.fromLngLat(value.get(j).getLongitude(), value.get(j).getLatitude()));
                    }
                } else {
                    for (int j = 0; j < size; j++) {
                        coordinates.add(Point.fromLngLat(value.get(j).getLongitude(), value.get(j).getLatitude()));
                    }
                }
                pathPointList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
            }

        }
        lineFeatureCollection = FeatureCollection.fromFeatures(pathPointList);
        pathSource.setGeoJson(lineFeatureCollection);
    }

    //test
    private void deviceJSON(HashMap<String, Integer> resultList) {
        //  delete last json
        try {
            deviceArray = new JSONArray("[]");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Iterator iter = resultList.entrySet().iterator();
        //HashMap<String, Float> resultList = new HashMap<>();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            int value = (int) entry.getValue();
            JSONObject device = new JSONObject();
            try {
                device.put("address", key);
                device.put("rssi", value);
                deviceArray.put(device);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //test
    private void readJSON(String showHttp) {
        pointResult.clear();
        try {
            JSONArray jsonArray = new JSONArray(showHttp);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //int user_to_device_id = jsonObject.getInt("user_to_device_id");
                //String date = jsonObject.getString("date");
                double lat_locate = jsonObject.getDouble("lat");
                double long_locate = jsonObject.getDouble("long");
                String mac_address = jsonObject.getString("mac_address");
                int rssi = jsonObject.getInt("rssi");
                LatLng locate = new LatLng(lat_locate, long_locate);
                // Set marker
                if (!pointResult.containsKey(locate)) {
                    pointResult.put(locate, 1);

                } else {
                    int number = pointResult.get(locate) + 1;
                    pointResult.put(locate, number);
                }
                // Set path and timer
                if (!pathTimer.containsKey(mac_address)) {
                    pathTimer.put(mac_address, timerValue);
                    ArrayList<LatLng> list = new ArrayList<>();
                    list.add(locate);
                    pathRecord.put(mac_address, list);
                } else {
                    ArrayList<LatLng> list = pathRecord.get(mac_address);
                    list.add(locate);
                    pathRecord.put(mac_address, list);
                    int timer = pathTimer.get(mac_address);
                    pathTimer.put(mac_address, timer + 1);
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Delete time out path
        Iterator entries = pathTimer.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            Integer value = (Integer) entry.getValue();
            if (value == 0) {
                String key = (String) entry.getKey();
                pathRecord.remove(key);
                entries.remove();
            } else {
                entry.setValue(value - 1);
            }

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    // Check Bluetooth device
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }


    // onClick method button
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.startButton:
                /*userLocation = mapboxMap.getLocationComponent();
                double a = userLocation.getLastKnownLocation().getLatitude();
                double b = userLocation.getLastKnownLocation().getLongitude();
                Toast.makeText(getApplicationContext(), "test1: " + a + " " + b, Toast.LENGTH_LONG).show();*/
/*
                if (!mBTLeScanner.isScanning() && !isStart) {
                    isStart = true;
                    startButton.setText("Stop");
                    Utils.toast(getApplicationContext(), "Start");
                    startScan();
                } else {
                    isStart = false;
                    startButton.setText("Start");
                    Utils.toast(getApplicationContext(), "Stop");
                    stopScan();
                }
                */
//                scanner_btle.start();
//                handler.postDelayed(testRunnable, 5000);
                updateFun();
                break;
            case R.id.logout:


//                finish();
//                Intent intent = new Intent(MapBoxActivity.this, Login.class);
//                startActivity(intent);
                updatehandler.removeCallbacks(updaterunnable);
                break;
            case R.id.locate:
                locationComponent.setCameraMode(CameraMode.TRACKING);

                break;

            default:
                break;
        }
    }


    public void writeFile(String fileName, String writestr) throws IOException {
        try {
            FileOutputStream fout = openFileOutput(fileName, MODE_APPEND);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


}





