@file:Suppress("DEPRECATION")

package com.example.rutacasa

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*

class MainActivity : AppCompatActivity() {
    private lateinit var btnVerRuta: Button
    private var map: MapView? = null
    private var line = Polyline()
    private var start: String = ""
    private var end: String = ""
    private val apiService: ApiService by lazy {
        Directions.apiService
    }
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility", "MissingPermission",
        "SetTextI18n"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        //Map Provider
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)

        //Current Location
        //val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //val locationProvider = LocationManager.GPS_PROVIDER
        //val currentLocation = locationManager.getLastKnownLocation(locationProvider)

        //MapController
        val mapController = map!!.controller
        mapController.setZoom(15)
        val startPoint = GeoPoint(20.139398208378335, -101.15073143396242)
        mapController.setCenter(startPoint)

        //Add markers
        val markerStart = Marker(map)
        markerStart.isDraggable = true
        //markerStart.position = currentLocation?.let { GeoPoint(it.latitude, currentLocation.longitude) }
        markerStart.position = GeoPoint(20.139398208378335, -101.15073143396242)
        markerStart.title = "Punto Inicio"
        map!!.overlays.add(markerStart)

        val markerEnd = Marker(map)
        markerEnd.isDraggable = true
        markerEnd.position = GeoPoint(20.118886642184798, -101.17034500679607)
        markerEnd.title = "Punto Destino"
        map?.overlays?.add(markerEnd)

        markerStart.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                //Log.d("MarkerStart", "${marker.position.latitude}, ${marker.position.longitude}")
            }
        })

        markerEnd.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                //Log.d("MarkerEnd", "${marker.position.latitude}, ${marker.position.longitude}")
            }
        })

        btnVerRuta = findViewById(R.id.btnCalcularRuta)
        var aux = 0

        btnVerRuta.setOnClickListener{
            if (aux == 0) {
                line = Polyline()
                drawRoute(markerStart.position, markerEnd.position)
                btnVerRuta.text = "Limpiar"
                aux++
            } else {
                btnVerRuta.text = "Ver ruta"
                map?.overlays?.remove(line)
                aux = 0
            }
        }

        map?.invalidate()
    }

    override fun onResume() {
        super.onResume()
        //
        map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //
        map!!.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }


    private fun drawRoute(startPoint: GeoPoint, endPoint: GeoPoint){
        CoroutineScope(Dispatchers.IO).launch {
            //val startPoint = GeoPoint(20.139398208378335, -101.15073143396242)
            //val endPoint = GeoPoint(20.118886642184798, -101.17034500679607)
            end = "${endPoint.longitude},${endPoint.latitude}"
            start = "${startPoint.longitude},${startPoint.latitude}"

            val points = apiService.getRoute("5b3ce3597851110001cf62488d38aa048bea4519ae3177df424c06de", start, end)
            val features = points.features
            //val line = Polyline()

            for (feature in features) {
                val geometry = feature.geometry
                val coordinates = geometry.coordinates
                for (coordinate in coordinates) {
                    val point = GeoPoint(coordinate[1], coordinate[0])
                    line.addPoint(point)
                }
                map?.overlays?.add(line)
            }
        }
    }


}