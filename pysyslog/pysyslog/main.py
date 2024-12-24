from log_classifier import LogClassifier
from rabbitmq_consumer import RabbitMQConsumer
from websocket_listener import WebSocketListener

API_URL = "http://localhost:4001/api/assets/all"
WS_URL = "localhost"
WS_TOPIC = "/topic/syslog"

def main():
    # Step 1: Initialize the log classifier by fetching rules from the API
    classifier = LogClassifier(API_URL)

    # Step 2: Start the WebSocket listener to monitor for rule updates
    # ws_listener = WebSocketListener(WS_URL, WS_TOPIC, classifier)
    # ws_listener.connect_and_subscribe()

    # Step 3: Start the RabbitMQ consumer
    consumer = RabbitMQConsumer(classifier)
    consumer.start_consumer()

if __name__ == "__main__":
    main()
