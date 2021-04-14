package com.rlsistemas.ranchosburgers.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rlsistemas.ranchosburgers.R;
import com.rlsistemas.ranchosburgers.activities.MainActivity;

public class FirebaseServe extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0){
            String titulo = remoteMessage.getData().get("titulo");
            String mensagem = remoteMessage.getData().get("mensagem");
            String url_imagem = remoteMessage.getData().get("url_imagem");

            sendNotificationComImagem(titulo, mensagem, url_imagem);
        }
        else if(remoteMessage.getNotification() != null){
            String titulo = remoteMessage.getNotification().getTitle();
            String mensagem = remoteMessage.getNotification().getBody();

          sendNotificationSemImagem(titulo, mensagem);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationComImagem(String titulo, String mensagem, String url_imagem){

        Glide.with(this).asBitmap().load(url_imagem).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                sendNotificationSemImagem(titulo,mensagem);
                return false;
            }
            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

                Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                String canal = getString(R.string.default_notification_channel_id);
                NotificationCompat.Builder notificacao = new NotificationCompat.Builder(getBaseContext(),canal)
                        .setSmallIcon(R.drawable.logo_branca)
                        .setContentTitle(titulo)
                        .setContentText(mensagem)
                        .setLargeIcon(resource)
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource))
                        .setSound(som)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel channel = new NotificationChannel(canal,"canal", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }
                notificationManager.notify(0,notificacao.build());
                return false;
            }
        }).submit();
    }

    public void sendNotificationSemImagem(String titulo, String mensagem){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        String canal = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(getBaseContext(),canal)
                .setSmallIcon(R.drawable.logo_branca)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setSound(som)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(canal,"canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0,notificacao.build());
    }



    @Override
    public void onNewToken(@NonNull String s) {

    }
}
