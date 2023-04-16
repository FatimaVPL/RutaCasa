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
            start = startPoint.toIntString()
            end = "20.1409401,-101.1512808"
            createRoute()
            map!!.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    start = "-100.8780778,20.1940547"
                    end = "-100.8825362,20.2138762"
                    createRoute()
                    if (event?.action == MotionEvent.ACTION_UP) {
                        //Click
                        if (start.isEmpty()) {
                            start = "${map!!.mapCenter.latitude},${map!!.mapCenter.longitude}"
                        } else if (end.isEmpty()) {
                            end = "${map!!.mapCenter.latitude},${map!!.mapCenter.longitude}"
                            createRoute()
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

    private fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute("5b3ce3597851110001cf62488d38aa048bea4519ae3177df424c06de", start, end)
            if (call.isSuccessful) {
                //Show rute
                drawRoute(call.body())
            } else {

            }
        }
    }

    private fun drawRoute(routeResponse: RouteResponse?){
        val line = Polyline()
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            line.setPoints(arrayListOf((GeoPoint(it[1],it[0]))))
        }
        map?.overlays?.add(line)
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}