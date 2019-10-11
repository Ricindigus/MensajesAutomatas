package pe.com.ricindigus.mensajesautomatas;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.SEND_SMS;

public class MainActivity extends AppCompatActivity {
    private Button btnEnviar,pickContact;
    private EditText edtNumero, edtMensaje, edtCantidad, edtHora, edtMinutos, edtSegundos;

    private static final int REQUEST_SMS = 0;
    private static final int REQ_PICK_CONTACT = 2 ;

    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;

    private TextView txtSendStatus, txtDeliverStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnEnviar = findViewById(R.id.btnEnviar);
        edtNumero = findViewById(R.id.edtNumero);
        edtMensaje = findViewById(R.id.edtMensaje);
        pickContact = findViewById(R.id.btnContacts);
        txtSendStatus = findViewById(R.id.txtSendstatus);
        txtDeliverStatus = findViewById(R.id.txtDeliverStatus);

        edtCantidad = findViewById(R.id.edtCantidad);

        edtHora = findViewById(R.id.edtHora);
        edtSegundos = findViewById(R.id.edtSegundos);
        edtMinutos = findViewById(R.id.edtMinutos);


        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasSMSPermission = checkSelfPermission(SEND_SMS);
                    if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to Send SMS",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[] {SEND_SMS},
                                                        REQUEST_SMS);
                                            }
                                        }
                                    });
                            return;
                        }
                        requestPermissions(new String[] {SEND_SMS},
                                REQUEST_SMS);
                        return;
                    }
                    sendMySMS();
                }
            }
        });

        pickContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, REQ_PICK_CONTACT);
            }
        });
    }


    public void sendMySMS() {


        String phone = edtNumero.getText().toString();
        String message = edtMensaje.getText().toString();

        int cantidad = Integer.parseInt(edtCantidad.getText().toString());

        Constraints constraints = new Constraints.Builder()
                .build();


        Data.Builder data1 = new Data.Builder();
        data1.putString("phone",phone);
        data1.putString("mensaje",message);
        data1.putInt("cantidad",cantidad);


        Calendar calendario = Calendar.getInstance();
        int h =calendario.get(Calendar.HOUR_OF_DAY);
        int m = calendario.get(Calendar.MINUTE);
        int s = calendario.get(Calendar.SECOND);

        int hora = Integer.parseInt(edtHora.getText().toString());
        int minuto = Integer.parseInt(edtMinutos.getText().toString());
        int segundo = Integer.parseInt(edtSegundos.getText().toString());


        long totalSegundosActuales = h * 3600 + m * 60 + s;
        long totalSegundosFuturos = hora * 3600 + minuto * 60 + segundo;
        long resultadoSegundosWait = totalSegundosFuturos-totalSegundosActuales;


        OneTimeWorkRequest compressionWork1 =
                new OneTimeWorkRequest.Builder(MensajeWorker.class)
                        .setConstraints(constraints)
                        .setInputData(data1.build())
                        .setInitialDelay(resultadoSegundosWait, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(this)
                .enqueue(compressionWork1);

        txtSendStatus.setText("Tarea programada: " +
                "\nmensajes programados: " + cantidad +
                "\ntexto del mensaje: " + message);


//        //Check if the phoneNumber is empty
//        if (phone.isEmpty()) {
//            Toast.makeText(getApplicationContext(), "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
//        } else {
//
//            SmsManager sms = SmsManager.getDefault();
//            // if message length is too long messages are divided
//            List<String> messages = sms.divideMessage(message);
//            for (String msg : messages) {
//
//                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
//                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
//                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);
//
//            }
//        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access sms", Toast.LENGTH_SHORT).show();
                    sendMySMS();

                }else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and sms", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{SEND_SMS},
                                                        REQUEST_SMS);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully !!";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        break;
                    default:
                        break;
                }
                txtSendStatus.setText(s);

            }
        };

        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                txtDeliverStatus.setText(s);
                edtNumero.setText("");
                edtMensaje.setText("");
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {

                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor
                        .getString(cursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                edtNumero.setText(number);
            }

        }
    }

    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{SEND_SMS}, REQUEST_SMS);
    }

}
