import stomp
import json
import time
from log_classifier import LogClassifier

class WebSocketListener(stomp.ConnectionListener):
    def __init__(self, host, topic, classifier):
        self.host = host
        self.topic = topic
        self.classifier = classifier
        self.conn = None

    def on_message(self, frame):
        try:
            new_rules = json.loads(frame.body)
            self.classifier.update_rules(new_rules)
            print("Rules updated successfully")
        except json.JSONDecodeError as e:
            print(f"Error decoding rules: {e}")

    def on_error(self, frame):
        print(f'Error received: {frame.body}')

    def on_disconnected(self):
        print("WebSocket disconnected. Reconnecting in 60 seconds...")
        time.sleep(60)
        self.connect_and_subscribe()

    def connect_and_subscribe(self):
        while True:
            try:
                print(f"Attempting to connect to WebSocket at {self.host}...")
                # Create connection with the Spring Boot WebSocket endpoint
                self.conn = stomp.Connection([
                    (self.host, 4001)
                ])
                
                # Set connection headers for SockJS
                connect_headers = {
                    'accept-version': '1.1,1.0',
                    'heart-beat': '10000,10000'
                }
                
                # Set up the listener and connect
                self.conn.set_listener('', self)
                self.conn.connect(headers=connect_headers, wait=True)
                
                # Subscribe to the topic
                self.conn.subscribe(destination=self.topic, id=1, ack='auto')
                print(f"Connected to WebSocket and subscribed to {self.topic}")
                break
                
            except Exception as e:
                print(f"WebSocket connection failed: {e}. Retrying in 60 seconds...")
                time.sleep(60)

    def disconnect(self):
        if self.conn and self.conn.is_connected():
            self.conn.disconnect()
            print("Disconnected from WebSocket")