package com.vasquez.fernandez.AppTemplateFCM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vasquez.fernandez.AppTemplateFCM.utils.Helper;
import java.util.List;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener{

    double latitud, longitud;
    GoogleMap mapa;
    Marker marcador;
    FloatingActionButton btnMapa;
    String tituloMarcador,direccion;
    SearchView svDireccion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        //Remover la barra de titulo
        getSupportActionBar().hide();

        //Remover la barra de notificaciones
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Vincular el fragment del mapa que esta en XML
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapaCliente);

        svDireccion = findViewById(R.id.sv_direccion);
        //Configurar la busqueda de una direccion, haciendo uso del contro search view
        svDireccion.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String direccionBusqueda = svDireccion.getQuery().toString();
                List<Address> listaDireccion = null;
                if (direccionBusqueda != null || !direccionBusqueda.equals("")) {
                    Geocoder geocoder = new Geocoder(MapaActivity.this);
                    try {
                        listaDireccion = geocoder.getFromLocationName(direccionBusqueda,1);

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (listaDireccion.size()>0){
                        Address address = listaDireccion.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());

                        //Setear al marcador la latitud y la longitud
                        marcador.setPosition(latLng);

                        //Centrar el mapa
                        mapa.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                        //setear los datos que se retorna al fragment de clientes
                        latitud = address.getLatitude();
                        longitud = address.getLongitude();

                        direccion = Helper.obtenerDireccionMapa(MapaActivity.this,latitud,longitud);
                    } else {
                        Toast.makeText(MapaActivity.this, "No se encontra la direccion especificada", Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        mapFragment.getMapAsync(this);

        //Vincular el botón flotante
        btnMapa = findViewById(R.id.btnMapaCliente);
        btnMapa.setOnClickListener(this);

        //Consultar si el activity del mapa recibe algun parámetro (esto aplica cuando el usuario desea ver el lugar turistico en el mapa)
        if (this.getIntent().getExtras() != null){
//            //si hay parámetros , capturar los parametros que viene del fragment cliente
//            Bundle parametro = this.getIntent().getExtras();
//
//            //variable que se recepciona desde el fragment de cliente
//            int pos = parametro.getInt("p_posicionItemSeleccionadoRecyclerView");
//
//            //Obtener la latitud y longitud del lugar turistico que ya esta grabado en la BD
//            Cliente cliente = Cliente.listadoClientes.get(pos);
//            this.latitud = cliente.getLatitud();
//            this.longitud = cliente.getLongitud();
//
//            //Cargar como titulo del marcador el nombre del lugar turistico
//            this.tituloMarcador = cliente.getApellidosNombres();

        }else{
            //Si no hay parmátros, quiere decir que estamos agregando un nuevo cliente
            //Configurar la latitud y longitud (coordenadas) por default (CHICLAYO)
            this.latitud = -6.771144;
            this.longitud = -79.839780;
            //Configurar un titulo al marcador (marker)
            this.tituloMarcador = "Debe ubicar una dirección";
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Metodo que permite leer las coordenadas y ubicarlas en el mapa
        mapa = googleMap;

        mapa.setMapType(mapa.MAP_TYPE_NORMAL);

        //Configurar al mapa para que reconozca el movimiento del marcador (marker) click de arrastre
        googleMap.setOnMarkerDragListener(this);

        //Configurar al mapa para que reconoza el evento click
        googleMap.setOnMapClickListener(this);

        //Configurando las caracteristicas que tendrá el marcador (marker)
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);
        markerOptions.position(new LatLng(this.latitud, this.longitud));
        markerOptions.title(this.tituloMarcador);
        markerOptions.snippet("Puede arrastrar el marcador y ubicacar una dirección en el mapa");

        //Agregar al configurar al marcador (marker)
        marcador = googleMap.addMarker(markerOptions);
        marcador.showInfoWindow();

        //Centrar la camara del mapa según la ubicación del marcador
        CameraPosition camPos = new CameraPosition.Builder()
                .target(centrarCamara())
                .zoom(17)
                .build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(cameraUpdate);
    }

    private LatLng centrarCamara(){
        //Centrar la camara en el mapa según la posición del marcador
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(this.latitud, this.longitud));
        LatLngBounds bounds = builder.build();
        return bounds.getCenter();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnMapaCliente:
                this.retornarDatos();
                break;
        }
    }

    private void retornarDatos(){
        //Configurar los parametros de retorno
        Bundle parametros = new Bundle();
        parametros.putDouble("pLatitud",this.latitud);
        parametros.putDouble("pLongitud",this.longitud);
        parametros.putString("pDireccion",this.direccion);

        //Declarar un intent para realizar el retorno de datos
        Intent intent = new Intent();
        intent.putExtras(parametros);

        //Indiciarle al activity que envie un mensaje, que se retorne parametros
        this.setResult(Activity.RESULT_OK,intent);
        this.finish(); //Cerrar el activity

    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        //Se ejectura cuando se inicia el click de arrastre
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        //se ejecuta mientras se realiza el arrastre
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // Se ejecuta cuando se finaliza el click de arrastre
        //1.  Enviar la latitud y longityd a la camara para centrar el mapa
        mapa.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        //2. Capturar los datos a retornar : latitud, longitud y direccion
        this.latitud = marker.getPosition().latitude;
        this.longitud = marker.getPosition().longitude;
        this.direccion = Helper.obtenerDireccionMapa(this,this.latitud,this.longitud);
        //3. Mostrar la direccion
        Toast.makeText(this, "Direccion : "+this.direccion, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        //Se ejecuta al hacer click en el mapa
        //0. Enviar la latitud y longitud al marcador
        marcador.setPosition(latLng);
        //1.  Enviar la latitud y longityd a la camara para centrar el mapa
        mapa.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        //2. Capturar los datos
        this.latitud = latLng.latitude;
        this.longitud = latLng.longitude;
        this.direccion = Helper.obtenerDireccionMapa(this,this.latitud,this.longitud);
        //3. Mostrar la direccion
        Toast.makeText(this, "Direccion : "+this.direccion, Toast.LENGTH_SHORT).show();
    }


}