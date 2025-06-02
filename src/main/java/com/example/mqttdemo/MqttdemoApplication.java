package com.example.mqttdemo;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("!test") // 確保在測試環境中不啟動此應用程式
public class MqttdemoApplication implements CommandLineRunner{

    private static final String BROKER = "mqtt.onusflux.com";
    private static final int PORT = 18830;
    private static final String TOPIC1 = "mqttlei/2bawc";
	private static final String TOPIC2 = "mqttlei/4bawc";
    private static final String USERNAME = "mqttlei";
    private static final String PASSWORD = "leader1970";

    public static void main(String[] args) {
        // SpringApplication.run(MqttdemoApplication.class, args);
		SpringApplication app = new SpringApplication(MqttdemoApplication.class); // 正確宣告
		app.setWebApplicationType(WebApplicationType.NONE); // 設定為非 Web 應用程式
        app.run(args);
    }

	@Override
	public void run(String... args) {
		MqttClient client = null;

        try {
            client = new MqttClient(
                String.format("tcp://%s:%d", BROKER, PORT),
                MqttClient.generateClientId()
            );

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setConnectionTimeout(60);

            // Set callback for receiving messages
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Received message on " + topic + ": " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used 
                }
            });

            // Connect to broker
            System.out.println("Connecting to broker...");
            client.connect(options);
            client.subscribe(TOPIC1);
            client.subscribe(TOPIC2);

            // Add shutdown hook to gracefully disconnect
            MqttClient finalClient = client;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (finalClient.isConnected()) {
                        finalClient.disconnect();
                        System.out.println("Disconnected from broker.");
                    }
                } catch (MqttException e) {
                    System.out.println("Error during disconnection: " + e.getMessage());
                }
            }));

            // Publish message periodically
            while (true) {
                // Create JSON message
                // JSONObject data = new JSONObject();
                // data.put("sensor", "temperature");
                // data.put("value", 22.5);
                // data.put("unit", "C");
                
                // String jsonData = data.toString();
                // MqttMessage message = new MqttMessage(jsonData.getBytes());
                // client.publish(TOPIC, message);
                // System.out.println("已發送訊息: " + jsonData);

                Thread.sleep(60000); // Sleep for 5 seconds
				System.out.println("------------60秒顯示測試 per 60 secend alive---------------" );
            }

        } catch (MqttException e) {
            System.out.println("MQTT Error: " + e.getMessage());
        } 
		// set print error thread Exception
		catch (InterruptedException e) {
            System.out.println("Thread interrupted");
        } 
		finally {
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                    System.out.println("Disconnected from broker in finally block.");
                }
            } catch (MqttException e) {
                System.out.println("Error during final disconnection: " + e.getMessage());
            }
        }
	}
}