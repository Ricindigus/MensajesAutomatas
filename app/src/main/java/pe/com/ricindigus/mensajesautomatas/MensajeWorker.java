package pe.com.ricindigus.mensajesautomatas;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class MensajeWorker extends Worker {

    public MensajeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        String phone = getInputData().getString("phone");
        String mensaje = getInputData().getString("mensaje");
        int cantidad = getInputData().getInt("cantidad",1);

        for (int i = 0; i <cantidad ; i++) {
            enviarSms(phone,mensaje);
        }

        return Result.success();
    }

    private void enviarSms(String phone,String mensaje) {
        //Check if the phoneNumber is empty
        if (phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(mensaje);

            for (String msg : messages) {

//                PendingIntent sentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
//                PendingIntent deliveredIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, null, null);

            }
        }
    }
}
