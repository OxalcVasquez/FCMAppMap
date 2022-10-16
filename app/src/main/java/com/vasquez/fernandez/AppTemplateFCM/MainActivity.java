package com.vasquez.fernandez.AppTemplateFCM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vasquez.fernandez.AppTemplateFCM.config.Config;
import com.vasquez.fernandez.AppTemplateFCM.modelo.Preferencia;
import com.vasquez.fernandez.AppTemplateFCM.modelo.Ubicacion;
import com.vasquez.fernandez.AppTemplateFCM.utils.NotificationUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static  final int REQUEST_MAP = 2;
    double latitud,longitud = 0;
    TextInputLayout tilDireccion;
    TextView txtTituloMensajeRemoto,txtTextoMensajeRemoto;
    TextInputEditText txtDireccion;
    EditText txtTemaInteres;
    Button btnSuscribete, btnAnularSuscripcion,  btnLogin,btnRegistrarDireccion;
    BroadcastReceiver broadcastReceiver;
    /*FIREBASE AUTH*/
    FirebaseAuth firebaseAuth;
    String authId =""; //Id del usuario que iniciar sesion, es un key
    /*FIREBASE AUTH*/

    /*FIREBASE REALTIME DATABASE*/
    FirebaseDatabase db;
    DatabaseReference nodoPreferencias,nodoUbicaciones;
    /*FIREBASE REALTIME DATABASE*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tilDireccion = findViewById(R.id.tilDireccion);
        txtDireccion = findViewById(R.id.txt_direccion);
        txtTemaInteres = findViewById(R.id.txtTemaInteres);
        txtTituloMensajeRemoto = findViewById(R.id.txtTituloMensajeRemoto);
        txtTextoMensajeRemoto = findViewById(R.id.txtTextoMensajeRemoto);
        btnSuscribete = findViewById(R.id.btnSuscribete);
        btnAnularSuscripcion = findViewById(R.id.btnAnularSuscripcion);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrarDireccion = findViewById(R.id.btnRegistrarDireccion);

        btnSuscribete.setOnClickListener(this);
        btnAnularSuscripcion.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnRegistrarDireccion.setOnClickListener(this);

        //Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        iniciarSesion();

        //Firebase RealTimeDataBase
        db = FirebaseDatabase.getInstance();
        nodoPreferencias = db.getReference("preferencias");
        nodoUbicaciones = db.getReference("ubicaciones");

        //Deshabilitar botones
        btnSuscribete.setEnabled(false);
        btnAnularSuscripcion.setEnabled(false);
        btnRegistrarDireccion.setEnabled(false);
        btnLogin.setVisibility(View.INVISIBLE);


        //OnCreateView
        tilDireccion.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapaActivity.class);
                //Espero despues que se cierre el mapa me guarda la direccion y las coordenadas
                //devolvera los siguientes datos latitud, longitud y la direccion
                startActivityForResult(intent,REQUEST_MAP);
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Cuando instalo la aplicacion
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    //Abrir un canal de comunicacion GLOBAL/GENERAL, le llegaran todas las notificaciones al ususario
                    //internamente
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)){
                    //Cuando se recibe la notificacion
                    String title = intent.getExtras().getString("firebase_title");
                    String message = intent.getExtras().getString("firebase_message");
                    String timestamp = intent.getExtras().getString("firebase_timestamp");

                    //Mostrar los datos del mensaje en el activity
                    txtTituloMensajeRemoto.setText(title);
                    txtTextoMensajeRemoto.setText(message);
                    MainActivity.this.setTitle("Fecha y hora :"+ timestamp);
                }
            }
        };

        //Registrar el MainActivity como receptor de los mensajes remotos
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter(Config.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter(Config.PUSH_NOTIFICATION));

        if (this.getIntent().getExtras() !=  null){
            String title = this.getIntent().getExtras().getString("firebase_title");
            String message = this.getIntent().getExtras().getString("firebase_message");
            String timestamp = this.getIntent().getExtras().getString("firebase_timestamp");
            Log.e("title",title);
            Log.e("message",message);

            //Mostrar los datos del mensaje en el activity
            txtTituloMensajeRemoto.setText(title);
            txtTextoMensajeRemoto.setText(message);
            MainActivity.this.setTitle("Fecha y hora : "+timestamp);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAP) {
            if (resultCode == Activity.RESULT_OK) {
                //Capturar los datos que se retornan
                Bundle parametros = data.getExtras();
                //Mostrar la direccion y almacenar la latitud y longitud en variables
                txtDireccion.setText(parametros.getString("pDireccion"));
                this.latitud = parametros.getDouble("pLatitud");
                this.longitud = parametros.getDouble("pLongitud");
            }
        }
    }

    @Override
    public void onClick(View view) {

        String tema = NotificationUtils.eliminarAcentos(txtTemaInteres.getText().toString());
        String direccion = NotificationUtils.eliminarAcentos(txtDireccion.getText().toString());
        switch (view.getId()){
            case R.id.btnSuscribete:
                if (txtTemaInteres.getText().toString().isEmpty() && !authId.isEmpty()){
                    Toast.makeText(this, "Ingrese tema de interes", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseMessaging.getInstance().subscribeToTopic(tema);
                Toast.makeText(this, "Se ha registrado la subscripcion", Toast.LENGTH_SHORT).show();
                registrarPreferencias(tema);
                break;
            case R.id.btnAnularSuscripcion:
                if (txtTemaInteres.getText().toString().isEmpty() && !authId.isEmpty()){
                    Toast.makeText(this, "Ingrese tema de interes", Toast.LENGTH_SHORT).show();
                    eliminarPreferencia(tema);
                    return;
                }
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema);
                Toast.makeText(this, "Se ha anulado la subscripcion", Toast.LENGTH_SHORT).show();
                eliminarPreferencia(tema);
                break;
            case R.id.btnLogin:
                iniciarSesion();
                break;
            case R.id.btnRegistrarDireccion:
                if (txtDireccion.getText().toString().isEmpty() && !authId.isEmpty()){
                    Toast.makeText(this, "Ingrese ubicacion", Toast.LENGTH_SHORT).show();
                    return;
                }
                registrarDireccion(direccion);
                break;
        }




    }


    private void iniciarSesion() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View prompt = layoutInflater.inflate(R.layout.login_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(prompt);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("Iniciar sesión Firebase");

        //Declarar los controles del dialog
        TextInputEditText txtEmailLogin,txtContrasenaLogin;
        Button btnIniciarSesionDialog,btnSalirDialog;
        ProgressBar progressBar;
        //Enlazar los controles
        txtEmailLogin = prompt.findViewById(R.id.txtEmailLogin);
        txtContrasenaLogin = prompt.findViewById(R.id.txtContrasenaLogin);
        btnIniciarSesionDialog = prompt.findViewById(R.id.btnIniciarSesion);
        btnSalirDialog = prompt.findViewById(R.id.btnSalir);
        progressBar = prompt.findViewById(R.id.progressBar);

        txtEmailLogin.requestFocus();

        AlertDialog alertDialog = alertDialogBuilder.show();

        //Implementar la funciondalidad de los botones
        btnIniciarSesionDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Validacion de los datos */
                progressBar.setVisibility(View.VISIBLE);
                firebaseAuth.signInWithEmailAndPassword(txtEmailLogin.getText().toString(),txtContrasenaLogin.getText().toString()).addOnCompleteListener(
                        MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()){
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    authId = user.getUid();
                                    Log.e("AUTH ID",authId);
                                    btnSuscribete.setEnabled(true);
                                    btnAnularSuscripcion.setEnabled(true);
                                    btnRegistrarDireccion.setEnabled(true);
                                    btnLogin.setVisibility(View.INVISIBLE);
                                    alertDialog.dismiss();
                                } else {
                                    Toast.makeText(MainActivity.this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });

        btnSalirDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                btnLogin.setVisibility(View.VISIBLE);
            }
        });

        alertDialog.show();

    }


    private void eliminarPreferencia(String nombrePreferencia) {
        nodoPreferencias.child(authId).orderByChild("nombre").equalTo(nombrePreferencia).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for ( DataSnapshot child : snapshot.getChildren()) {
                    Toast.makeText(MainActivity.this, child.getValue().toString(), Toast.LENGTH_SHORT).show();
                    Log.e("Preferencie value", child.getValue().toString());
                    child.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void registrarDireccion(String direccion) {
        String id = nodoUbicaciones.push().getKey();
        Ubicacion ubicacion = new Ubicacion(id,direccion,latitud,longitud);
        nodoUbicaciones.child(authId).child(id).setValue(ubicacion);
    }

    private void registrarPreferencias(String nombrePreferencia) {
        String id = nodoPreferencias.push().getKey();
        Preferencia preferencia = new Preferencia(id, nombrePreferencia);
        nodoPreferencias.child(authId).child(id).setValue(preferencia);
    }

}