@file:Suppress("DEPRECATION")

package com.example.rutacasa

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var btnVerRuta: Button
    private var map: MapView? = null
    private var start: String = ""
    private var end: String = ""
    private val apiService: ApiService by lazy {
        Directions.apiService
    }
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        //Map Provider
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)

        //Current Location
        //val ubicacion = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        //ubicacion.enableMyLocation()
        //map!!.overlays.add(ubicacion)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationProvider = LocationManager.GPS_PROVIDER
       // val currentLocation = locationManager.getLastKnownLocation(locationProvider)

        //MapController
        val mapController = map!!.controller
        mapController.setZoom(15)
        val startPoint = GeoPoint(20.139398208378335, -101.15073143396242)
        mapController.setCenter(startPoint)

        //Add markers
        val markerStart = Marker(map)
        markerStart.isDraggable = true
       // markerStart.position = currentLocation?.let { GeoPoint(it.latitude, currentLocation.longitude) }
       // markerStart.title = "Punto Inicio"
      //  map!!.overlays.add(markerStart)

        val markerEnd = Marker(map)
        markerEnd.isDraggable = true
        markerEnd.position = GeoPoint(20.139398208378335, -101.15073143396242)
        markerEnd.title = "Punto Destino"
        map?.overlays?.add(markerEnd)

        markerStart.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                Log.d("MarkerStart", "Nueva posición del marcador: ${marker.position.latitude}, ${marker.position.longitude}")
            }
        })

        markerEnd.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {

            }

            override fun onMarkerDrag(marker: Marker) {

            }

            override fun onMarkerDragEnd(marker: Marker) {
                Log.d("MarkerEnd", "Nueva posición del marcador: ${marker.position.latitude}, ${marker.position.longitude}")
            }
        })


        /*
        // Click on map
        map!!.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Coordinates
                val clickedPoint = map!!.projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint

                //Log.d("MapClick", "Latitude: ${clickedPoint.latitude}, Longitude: ${clickedPoint.longitude}")

                marker = Marker(map)
                marker?.position = clickedPoint
                marker?.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
                map?.overlays?.add(marker)
            }
            true
        }*/

        // Button show rute
        btnVerRuta = findViewById(R.id.btnCalcularRuta)

        btnVerRuta.setOnClickListener{

           drawRoute()
            map!!.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_UP) {
                        //Click
                        if (start.isEmpty()) {
                            start = "${map!!.mapCenter.latitude},${map!!.mapCenter.longitude}"
                        } else if (end.isEmpty()) {
                            end = "${map!!.mapCenter.latitude},${map!!.mapCenter.longitude}"

                        }
                        return true
                    }
                    return false
                }
            })
        }


     /*   val mapController = map!!.controller
        mapController.setZoom(19)


        val startPoint = GeoPoint(20.140153689100682, -101.15067778465794)
        mapController.setCenter(startPoint)

        items.add(
            OverlayItem(
                "Title",
                "Description",
                GeoPoint(0.0, 0.0)
            )
        ) // Lat/Lon decimal degrees

        firstMarker = Marker(map)
        firstMarker?.position = startPoint
        firstMarker?.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        firstMarker?.title = "Title"
        map?.overlays?.add(firstMarker)
*/
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


    private fun drawRoute(){
        CoroutineScope(Dispatchers.IO).launch {
            val coor = apiService.getRoute("5b3ce3597851110001cf62488d38aa048bea4519ae3177df424c06de", start, end)
            val features = coor.features
            val line = Polyline()
            val startPoint = GeoPoint(20.139398208378335, -101.15073143396242)
            val endPoint = GeoPoint(20.1433984,-101.1546329)
             end = "${endPoint!!.longitude},${endPoint!!.latitude}"
            start = "${startPoint!!.longitude},${startPoint!!.latitude}"

            for (feature in features) {
                val geometry = feature.geometry
                val coordinates = geometry.coordinates
                for (coordinate in coordinates) {
                    val punto = GeoPoint(coordinate[1], coordinate[0])
                    line.addPoint(punto)
                }
                map?.overlays?.add(line)
            }
        }
    }


}