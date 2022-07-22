package com.example.mapsactivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class SearchActivity:AppCompatActivity(){

    lateinit var sour:TextView
    lateinit var dest:TextView
    lateinit var flip:CardView

    lateinit var sour_c:LatLng
    lateinit var dest_c:LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val fields = listOf(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG)
        flip=findViewById(R.id.flip)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)

        sour=findViewById<TextView>(R.id.source)
        dest=findViewById<TextView>(R.id.destination)
        flip.setOnClickListener {
            onFlip()
        }
        sour.setOnClickListener{
            startActivityForResult(intent, 1)
        }

        dest.setOnClickListener{
            startActivityForResult(intent, 2)
        }

        findViewById<Button>(R.id.sear).setOnClickListener{
            onSearch()
        }
     //   startActivityForResult(intent, 1)
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED,null)
        finish()
    }

    fun onFlip()
    {
        if(sour.text.equals("Enter Source")||dest.text.equals("Enter Destination"))
        {
            Toast.makeText(this,"Source and Dest reqd",Toast.LENGTH_SHORT).show()
        }
        else
        {
            var temp=sour.text
            sour.text=dest.text
            dest.text=temp

            var temp1=sour_c
            sour_c=dest_c
            dest_c=sour_c
        }
    }

    fun onSearch()
    {
        if(sour.text.equals("Enter Source"))
        {
            Toast.makeText(this,"Enter a Source",Toast.LENGTH_SHORT).show()
        }
        else if(dest.text.equals("Enter Destination"))
        {
            Toast.makeText(this,"Enter a Destination",Toast.LENGTH_SHORT).show()
        }
        else
        {
            var data=Intent()
            Log.w("InSearch",sour_c.toString())
            data.putExtra("Source",sour_c)
            data.putExtra("Destination",dest_c)
            setResult(RESULT_OK,data)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        when(requestCode)
                        {
                            1-> {findViewById<TextView>(R.id.source).text=place.name
                            sour_c=place.latLng!!}
                            else -> {
                                findViewById<TextView>(R.id.destination).text = place.name
                                dest_c=place.latLng!!
                            }
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i("", status.statusMessage ?: "")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }

        super.onActivityResult(requestCode, resultCode, data)

    }
}