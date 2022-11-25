package com.wgsoft.appviwifinal

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.altbeacon.beacon.*
import java.util.*


class Beacon_Detailed :  AppCompatActivity(), RangeNotifier, TextToSpeech.OnInitListener {
    lateinit var beaconManager: BeaconManager
    lateinit var beaconDistanciaTextView: TextView
    lateinit var monitoringButton: Button
    lateinit var rangingButton: Button
    lateinit var beaconReferenceApplication: BeaconReferenceApplication
    var alertDialog: AlertDialog? = null
    var neverAskAgainPermissions = ArrayList<String>()
    private var tts: TextToSpeech? = null
    var idBeacon: String =""
    var isDestino = false
    var isFinal = false
    var idBeaconObstaculo = ""
    var idBeaconDestino = ""

    lateinit var sharedPreferences: SharedPreferences

    var IDBEACON_DESTINO = ""
    var IDBEACON_OBSTACULO = ""
    var DESCRIPCION_DESTINO = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon_detailed)

        beaconReferenceApplication = application as BeaconReferenceApplication

        val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(beaconReferenceApplication.region)
        //val regionViewModel = BeaconManager.getInstanceForApplication(this).getRegionViewModel(Region("all-beacons", Identifier.parse("426c7565-4368-6172-6d42-6561636f6e92"), null, null))
        // Set up a Live Data observer for beacon data
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observe(this, monitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observe(this, rangingObserver)
       // rangingButton = findViewById<Button>(R.id.rangingButton)
        //monitoringButton = findViewById<Button>(R.id.monitoringButton)
        //beaconListView = findViewById<ListView>(R.id.beaconList)
       // beaconCountTextView = findViewById<TextView>(R.id.beaconCount)
        //beaconCountTextView.text = "No beacons detected"
        //beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        tts = TextToSpeech(this, this)

        val nameDetalle : TextView = findViewById(R.id.tvDetalle)
        val nameDestino : TextView = findViewById(R.id.tvDestino)
        val nameDistancia : TextView = findViewById(R.id.tvDistancia)

        val bundle : Bundle?= intent.extras
        val nombreBeacon = bundle!!.getString("nombreBeacon")
        val descripcionDestino = bundle!!.getString("descripcionDestino")
        val descripcionObstaculo = bundle!!.getString("descripcionObstaculo")
        var idBeaconDestino = bundle!!.getString("BeaconUUID")

        Log.d("onCreate", "BeaconUUID " + idBeaconDestino)
        nameDetalle.text = descripcionObstaculo + " " + descripcionDestino
        nameDestino.text = nombreBeacon
        IDBEACON_OBSTACULO = "426c7565-4368-6172-6d42-6561636f6e92"
        destinoObstaculo(idBeaconObstaculo, idBeaconDestino)

        if (descripcionDestino != null) {
            DESCRIPCION_DESTINO = descripcionDestino
        }

        if (idBeaconDestino != null) {
            IDBEACON_DESTINO = idBeaconDestino
        }
        sharedPreferences = getSharedPreferences(IDBEACON_DESTINO, Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences(IDBEACON_OBSTACULO, Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences(DESCRIPCION_DESTINO, Context.MODE_PRIVATE)
        speakObstaculoDestino("Hola")
    }

    fun destinoObstaculo(idBeaconObstaculo: String, idBeaconDestino: String?) {
        Log.d("destinoObstaculo", "destinoObstaculo " + idBeaconObstaculo)
        Log.d("destinoObstaculo", "idBeaconDestino " + idBeaconDestino)
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        Log.d(TAG, "destinoObstaculo " + beaconManager.rangedRegions.size.toString() )
        beaconManager.startRangingBeacons(Region("all-beacons", Identifier.parse(idBeaconObstaculo), null, null))
    }

    @SuppressLint("LongLogTag")
    override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region?) {


        var beaconDistanciaTextView: TextView = findViewById(R.id.tvDistancia)
        val beaconManager = BeaconManager.getInstanceForApplication(this)

        for (beacon in beacons) {
            var beaconScanner = beacon.id1.toString()




            Log.d("didRangeBeaconsInRegion ", beacon.distance.toString())
            Log.d("didRangeBeaconsInRegion ", beaconScanner)
            Log.d("didRangeBeaconsInRegion isDestino ", isDestino.toString())
            if(!isFinal){
            if (isDestino) {

                if(beaconScanner == IDBEACON_DESTINO){
                    Log.d("didRangeBeaconsInRegion ", "IDBEACON_DESTINO " + IDBEACON_DESTINO)
                    var distancia = (Math.round(beacon.distance * 100.0) / 100.0).toString()
                    speakOut(distancia)
                    beaconDistanciaTextView.text = distancia
                    if(beacon.distance < 0.5){
                    try {
                        beaconManager.stopMonitoring(beaconReferenceApplication.region)
                        vibratePhone()
                        speakDestino()
                        SystemClock.sleep(2000)
                        isFinal = true
                        stopTTS()
                    } catch (e: RemoteException) {
                        Log.d("FragmentActivity.TAG", e.message.toString())
                    }
                }
                }
            }else{

                if(beaconScanner == IDBEACON_OBSTACULO){
                    Log.d("didRangeBeaconsInRegion ", beacon.id1.toString())
                    Log.d("didRangeBeaconsInRegion ", "IDBEACON_obs" + IDBEACON_OBSTACULO)
                    var distancia = (Math.round(beacon.distance * 100.0) / 100.0).toString()

                    beaconDistanciaTextView.text = distancia
                    speakOutObstaculo(distancia)
                    if(beacon.distance < 0.8){
                        isDestino = true
                        speakObstaculoDestino(DESCRIPCION_DESTINO)
                        sleepThread(3000)
                    }

                }
            }


        }
        }
    }

    private fun sleepThread(sec:Long) {
        try {

            Thread.sleep(sec)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun speakObstaculoDestino(descripcionDestino: String) {
        tts!!.speak(descripcionDestino, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    private fun speakOutObstaculo(distancia: String) {
        val text = "Tenga cuidado columna a  " + distancia + "metros"
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,"")
    }

    override fun onInit(status: Int) {
        Log.d(TAG, "onInit: ")
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val locale = Locale("es", "ES")
            val result = tts!!.setLanguage(locale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS","The Language specified is not supported!")
            } else {
                //buttonSpeak!!.isEnabled = true
            }

        } else {
            Log.d("TTS", "Initilization Failed!")
        }

    }

    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    fun stopTTS() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }
    @RequiresApi(Build.VERSION_CODES.M)

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        checkPermissions()
    }

    val monitoringObserver = Observer<Int> { state ->
        var dialogTitle = "Beacons detected"
        var dialogMessage = "didEnterRegionEvent has fired"
        var stateString = "inside"


        if (state == MonitorNotifier.OUTSIDE) {
            dialogTitle = "No beacons detected"
            dialogMessage = "didExitRegionEvent has fired"
            stateString == "outside"
            //beaconCountTextView.text = "Outside of the beacon region -- no beacons detected"
            //beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("--"))
        }
        else {
            //beaconCountTextView.text = "Inside the beacon region."
        }
        Log.d("Observer", "monitoring state changed to : $stateString")
//        val builder =
//            AlertDialog.Builder(this)
//        builder.setTitle(dialogTitle)
//        builder.setMessage(dialogMessage)
//        builder.setPositiveButton(android.R.string.ok, null)
//        alertDialog?.dismiss()
//        alertDialog = builder.create()
//        alertDialog?.show()
    }

    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("rangingObserver", "Ranged Observer: ${beacons.count()} beacons")
        if (BeaconManager.getInstanceForApplication(this).rangedRegions.size > 0) {

            //beaconCountTextView.text = "Ranging enabled: ${beacons.count()} beacon(s) detected"
//            beaconListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
//                beacons
//                    .sortedBy { it.distance }
//                    .map { "${it.id1}\nid2: ${it.id2} id3:  rssi: ${it.rssi}\nest. distance: ${it.distance} m" }.toTypedArray())
        }
        didRangeBeaconsInRegion(beacons, beaconReferenceApplication.region)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in 1..permissions.size-1) {
            Log.d(TAG, "onRequestPermissionResult for "+permissions[i]+":" +grantResults[i])
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //check if user select "never ask again" when denying any permission
                if (!shouldShowRequestPermissionRationale(permissions[i])) {
                    neverAskAgainPermissions.add(permissions[i])
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions() {
        // basepermissions are for M and higher
        var permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION)
        var permissionRationale ="This app needs fine location permission to detect beacons.  Please grant this now."
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN)
            permissionRationale ="This app needs fine location permission, and bluetooth scan permission to detect beacons.  Please grant all of these now."
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION)
                permissionRationale ="This app needs fine location permission to detect beacons.  Please grant this now."
            }
            else {
                permissions = arrayOf( Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                permissionRationale ="This app needs background location permission to detect beacons in the background.  Please grant this now."
            }
        }
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            permissionRationale ="This app needs both fine location permission and background location permission to detect beacons in the background.  Please grant both now."
        }
        var allGranted = true
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) allGranted = false;
        }
        if (!allGranted) {
            if (neverAskAgainPermissions.count() == 0) {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("This app needs permissions to detect beacons")
                builder.setMessage(permissionRationale)
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(
                        permissions,
                        PERMISSION_REQUEST_FINE_LOCATION
                    )
                }
                builder.show()
            }
            else {
                val builder =
                    AlertDialog.Builder(this)
                builder.setTitle("Functionality limited")
                builder.setMessage("Since location and device permissions have not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location and device discovery permissions to this app.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { }
                builder.show()
            }
        }
        else {
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        val builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("This app needs background location access")
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                PERMISSION_REQUEST_BACKGROUND_LOCATION
                            )
                        }
                        builder.show()
                    } else {
                        val builder =
                            AlertDialog.Builder(this)
                        builder.setTitle("Functionality limited")
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { }
                        builder.show()
                    }
                }
            }
            else if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.S &&
                (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED)) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("This app needs bluetooth scan permission")
                    builder.setMessage("Please grant scan permission so this app can detect beacons.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener {
                        requestPermissions(
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                            PERMISSION_REQUEST_BLUETOOTH_SCAN
                        )
                    }
                    builder.show()
                } else {
                    val builder =
                        AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since bluetooth scan permission has not been granted, this app will not be able to discover beacons  Please go to Settings -> Applications -> Permissions and grant bluetooth scan permission to this app.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
            }
            else {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            val builder =
                                AlertDialog.Builder(this)
                            builder.setTitle("This app needs background location access")
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                            builder.setPositiveButton(android.R.string.ok, null)
                            builder.setOnDismissListener {
                                requestPermissions(
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    PERMISSION_REQUEST_BACKGROUND_LOCATION
                                )
                            }
                            builder.show()
                        } else {
                            val builder =
                                AlertDialog.Builder(this)
                            builder.setTitle("Functionality limited")
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.")
                            builder.setPositiveButton(android.R.string.ok, null)
                            builder.setOnDismissListener { }
                            builder.show()
                        }
                    }
                }
            }
        }

    }

    companion object {
        val TAG = "ActivityDetalle"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }

    fun vibratePhone() {
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(1500)
    }

    fun didEnterRegion(region: Region?) {
        Log.i("didEnterRegion", "I just saw an beacon for the first time!")
        try {
            // start ranging for beacons.  This will provide an update once per second with the estimated
            // distance to the beacon in the didRAngeBeaconsInRegion method.

            beaconManager.startRangingBeacons(Region("all-beacons", null, null, null))
            beaconManager.addRangeNotifier(this)
        } catch (e: RemoteException) {
        }
    }

    private fun speakOut(distancia: String?) {
        val text = "Su objetivo a " + distancia + "metros"
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,"")
    }

    private fun speakDestino() {
        val text = "Ha llegado a su destino"
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    private fun speakObstaculo() {
        val text = "Tenga cuidado"
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }
}