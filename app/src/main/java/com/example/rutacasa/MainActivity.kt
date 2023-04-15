package com.example.rutacasa

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private var firstMarker: Marker? = null
    private lateinit var btnVerRuta: Button
    private var map: MapView? = null
    private var start: String = ""
    private var end: String = ""


    //your items
    var items = ArrayList<OverlayItem>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        setContentView(R.layout.activity_main)

        //Proveedor de mapas
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.MAPNIK)

        //Mi ubicación
        var ubicacion = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), map)
        ubicacion.enableMyLocation()
        val mapController = map!!.controller
        mapController.setZoom(9)
        val startPoint = GeoPoint(20.4791291,-100.7582735)
        mapController.setCenter(startPoint)
        map!!.getOverlays().add(ubicacion)





        // Botón para mostrar ruta
        btnVerRuta = findViewById(R.id.btnCalcularRuta)

        btnVerRuta.setOnClickListener{
            map!!.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    start = ""
                    end = ""
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
        firstMarker?.title = "Bello ITSUR"
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
                //Aquí se pintará la ruta
                drawRoute(call.body())
            } else {

            }
        }
    }

    private fun drawRoute(routeResponse: RouteResponse?){
        val line = Polyline()
        //line.setPoints()
        map?.overlays?.add(line)
    }
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}