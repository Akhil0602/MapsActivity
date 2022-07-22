package com.example.mapsactivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapsactivity.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlin.concurrent.thread


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    var marker:Marker?=null
    lateinit var dest:LatLng
    var canStart=false
    var isDestSet=false
    lateinit var currPos:LatLng
    lateinit var currLoc:Location
    var locationPermissionGranted=false

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val AUTOCOMPLETE_REQUEST_CODE = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map1) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        getLocationPermission()

        findViewById<CardView>(R.id.loc).setOnClickListener {
            getDeviceLocation(17f,true)
        }

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.w("Place", "Place: ${place.name}, ${place.id}")
                Log.w("LatLng",(place.latLng==null).toString())

                if(place.latLng!=null) {
                    mMap.clear()
                    var mark:MarkerOptions=MarkerOptions().position(place.latLng!!).title("Destination")
                    mMap.addMarker(mark)
                   /* val URL = getDirectionURL(currPos, place.latLng!!)
                    Log.w("GoogleMap", "URL : $URL")
                    GetDirection(URL).execute()*/
                    dest=place.latLng!!
                    isDestSet=true
                    Path(currPos,place.latLng!!,mMap)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currPos))
                    //Thread.sleep(1000)
                   /* while(path.list.size==0)
                    {

                    }*/
                /*    Log.w("Sz",path.list.size.toString())
                    for(x in path.list)
                    {
                        mMap.addPolyline(x)
                    }*/
                }

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
             //   Log.i(TAG, "An error occurred: $status")
            }
        })


        findViewById<CardView>(R.id.search_path).setOnClickListener {
            intent= Intent(this,SearchActivity::class.java)
            startActivityForResult(intent,1)
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled=true

    //    mMap.myLocation

     //   mMap.setMinZoomPreference(15f)

        dest = LatLng(26.912434, 75.787270)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(dest))

        getDeviceLocation(17f,true)

        canStart=true
        mMap.setOnMyLocationChangeListener {

            onLocationChanged(currLoc)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1) {
            if (resultCode == RESULT_OK) {
                var source: LatLng = data!!.getParcelableExtra<LatLng>("Source")!!
                var destn = data!!.getParcelableExtra<LatLng>("Destination")!!
                Log.w("Source", source.toString())
                Log.w("Destination", destn.toString())
                mMap.clear()
                mMap.addMarker(
                    MarkerOptions().position(source)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                mMap.addMarker(MarkerOptions().position(destn))
               /* val URL = getDirectionURL(source, destn)
                Log.w("GoogleMap", "URL : $URL")
                GetDirection(URL).execute()*/
                dest=destn
                isDestSet=false
                Path(source,destn,mMap)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currPos))

            } else if (resultCode == RESULT_CANCELED) {

            }
        }

//        super.onActivityResult(requestCode, resultCode, data)
    }

  /*  fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?destination=${dest.latitude},${dest.longitude}&mode=driving&key=AIzaSyCE6d2-Dvki9iPBtaP6NRO9WQFnSR4M7XI&origin=${origin.latitude},${origin.longitude}"
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }*/

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(zoom:Float,movCam:Boolean) {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                Log.w("Here","getLoc")
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.w("Here","Succ")
                        // Set the map's camera position to the current location of the device.

                        var lastKnownLocation = task.result
                        currLoc=lastKnownLocation!!

                        Log.w("Loc"," "+lastKnownLocation!!.latitude+" "+
                            lastKnownLocation!!.longitude)
                        Log.w("Here","Succ1")
                       // var currLoc:LatLng= LatLng(lastKnownLocation!!.latitude,lastKnownLocation!!.longitude)
                        if (lastKnownLocation != null) {
                            Log.w("Here","Succ2")
                       /*     mMap.addMarker(MarkerOptions().position(LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)).title("Marker in Location"))*/
                            currLoc=lastKnownLocation
                            currPos= LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)
                          //  if(zoom==17f) {
                                val cameraPosition = CameraPosition.Builder()
                                    .target(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        )
                                    ) // Sets the center of the map to Mountain View
                                    .zoom(zoom)            // Sets the zoom
                                    .bearing(0f)         // Sets the orientation of the camera to east
                                    .tilt(30f)            // Sets the tilt of the camera to 30 degrees
                                    .build()
                            if(movCam)
                                mMap.animateCamera(
                                    CameraUpdateFactory.newCameraPosition(
                                        cameraPosition
                                    )
                                )
                     //       }
                            Log.w("Here","Succ3")
                        }
                    } else {
                        Log.d("Fail", "Current location is null. Using defaults.")
                        Log.e("Fail", "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            1 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

     private fun onLocationChanged(p0: Location) {
        getDeviceLocation(15f,false)
         Log.w("Changed","YES")
         if(canStart&&isDestSet)
         Path(currPos,dest,mMap)

    }
}