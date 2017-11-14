package br.com.heiderlopes.helloiot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import br.com.heiderlopes.helloiot.util.MQTTConstantes;

public class MainActivity extends AppCompatActivity {


    private MqttAndroidClient client;
    public final String TAG = "HOME_AUTOMATION";

    private Button btLigar;
    private Button btDesligar;
    private TextView tvTemperatura;

    private TextToSpeech t1;

    private Button btVoz;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btLigar = (Button) findViewById(R.id.btLigar);
        btDesligar = (Button) findViewById(R.id.btDesligar);
        tvTemperatura = (TextView) findViewById(R.id.tvTemperatura);

        btVoz = (Button) findViewById(R.id.btVoz);

        btVoz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        connectMQTTClient();

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.getDefault());
                }
            }
        });

        t1.setSpeechRate(0.7f);
    }


    private void connectMQTTClient() {
        String clientId = MqttClient.generateClientId();

        client =
                new MqttAndroidClient(this.getApplicationContext(),
                        MQTTConstantes.MQTT_SERVICE_URI,
                        clientId);

        try {
            IMqttToken token = client.connect();

            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess");
                    subscribeLampada();
                    subscribeTemperatura();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onFailure");

                }
            });

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i("TAG", "TO AQUI");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i("TAG", "TO AQUI");
                    if (topic.equals("temperatura")) {
                        tvTemperatura.setText(String.valueOf(message));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("TAG", "TO AQUI");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnectMQTTClient() {
        try {
            IMqttToken disconToken = client.disconnect();
            disconToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // we are now successfully disconnected
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // something went wrong, but probably we are disconnected anyway
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeLampada() {

        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(MQTTConstantes.TOPICO_LAMPADA, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    btDesligar.setEnabled(true);
                    btLigar.setEnabled(true);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.i("TAG", exception.getMessage());
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeTemperatura() {

        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(MQTTConstantes.TOPICO_TEMPERATURA, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    btDesligar.setEnabled(true);
                    btLigar.setEnabled(true);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    Log.i("TAG", exception.getMessage());
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeLampada() {

        try {
            IMqttToken unsubToken = client.unsubscribe(MQTTConstantes.TOPICO_LAMPADA);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    disconnectMQTTClient();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeTemperatura() {

        try {
            IMqttToken unsubToken = client.unsubscribe(MQTTConstantes.TOPICO_TEMPERATURA);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                    disconnectMQTTClient();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribeLampada();
        unsubscribeTemperatura();
    }

    public void ligar(View view) {
        ligar();
    }

    public void ligar() {
        String payload = "1";
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);

            client.publish(MQTTConstantes.TOPICO_LAMPADA, message);

            falar("LED ligado");
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void desligar(View view) {
        desligar();
    }

    public void desligar() {
        String payload = "0";
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);

            client.publish(MQTTConstantes.TOPICO_LAMPADA, message);

            falar("LED desligado");
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    public void falar(String texto) {
        Toast.makeText(getApplicationContext(), texto,Toast.LENGTH_SHORT).show();
        t1.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Diga alguma coisa");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Speech nao suportado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.get(0).toUpperCase().contains("DESLIGAR")) {
                        desligar();
                    } else if (result.get(0).toUpperCase().contains("LIGAR")) {
                        ligar();
                    }
                }
                break;
            }

        }
    }
}
