package com.vasquez.fernandez.AppTemplateFCM;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vasquez.fernandez.AppTemplateFCM.config.Config;
import com.vasquez.fernandez.AppTemplateFCM.modelo.Mensaje;
import com.vasquez.fernandez.AppTemplateFCM.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ServicioNotificacionesFCM extends FirebaseMessagingService {

    FirebaseDatabase db;
    DatabaseReference nodoMensajes;


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Mostrar el token que ha generado la aplicaicon en la consola
        Log.e("TOKEN",token);

        //Configurar el acceso al nodo mensaje de la bd firebase
        db = FirebaseDatabase.getInstance();
        nodoMensajes = db.getReference("mensajes");

        //Configurar la notificacion para que reciba la notificacion
        Intent intent = new Intent(Config.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        if (message == null){
            return; // Mensaje no valido no ejecuta la notificacion
        }
        //Validar que el mensaje tengo datos
        if (message.getData().size()>0){
            //Capturar el mensaje que viene de la consola en formato JSON
            try {
                JSONObject json = new JSONObject(message.getData().toString());
                notificar(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void notificar(JSONObject mensaje) {
        try {
            JSONObject data = mensaje.getJSONObject("data");
            //Almacenar los datos del mensaje en variables
            String title = data.getString("title");
            String message = data.getString("message");
            String timestamp = data.getString("timestamp");
            String date = data.getString("fecha");
            String time = data.getString("hora");

            //Mostrar los datos del mensaje remoto
            Log.e("MENSAJE FCM",data.toString());
            //validar si la app se encuentra en primer plano (activa)
            Intent intent;
            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                intent = new Intent(Config.PUSH_NOTIFICATION);
            } else { //Cuando la aplicación se encuentra en segundo planop
                intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.setAction(Config.PUSH_NOTIFICATION);
            }
            intent.putExtra("firebase_title",title);
            intent.putExtra("firebase_message",message);
            intent.putExtra("firebase_timestamp",timestamp);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            new NotificationUtils(getApplicationContext()).showNotificationMessage(title,message,timestamp,intent);

            //GRABAR EL MENSAJE EN LA BASE DE DATOS FIREBASE
            registrarMensajesBD(title,message,timestamp);
        } catch (JSONException e1){
            e1.printStackTrace();
        } catch (Exception e2){
            e2.printStackTrace();
        }
    }


    private void registrarMensajesBD(String title, String message, String timestamp) {
        //Generar el id del mensaje
        String id = nodoMensajes.push().getKey();
        //Instanciar un objeto en la clase del mensaje
        Mensaje mensaje = new Mensaje(id,title,message,timestamp);
        //Regstrar el mensaje en el nodo mensajes en la bd
        nodoMensajes.child(id).setValue(mensaje);
    }
}
