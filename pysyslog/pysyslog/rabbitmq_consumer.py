import pika
import json
from log_classifier import LogClassifier

class RabbitMQConsumer:
    def __init__(self, classifier, queue_name='persistent_syslog_queue'):
        self.classifier = classifier
        self.queue_name = queue_name
        self.exchange_name = 'syslog'

    def callback(self, ch, method, properties, body):
        log_message = body.decode('utf-8')
        # print("=================================================================================")
        # print(f"RAW LOG: {log_message}")
        # print("=================================================================================")
        processed_log = self.classifier.process_log(log_message)
        if processed_log:
            processed_log_dict = json.loads(processed_log)
            if processed_log_dict["level"] != "LOW":
                print("=================================================================================")
                print(f"Processed log: {processed_log}")
                print("=================================================================================")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    def start_consumer(self):
        # Connection parameters with retry
        connection_params = pika.ConnectionParameters(
            host='localhost',
            heartbeat=600,  # Heartbeat timeout
            blocked_connection_timeout=300  # Timeout for blocked connections
        )
        
        connection = pika.BlockingConnection(connection_params)
        channel = connection.channel()

        # Declare a durable exchange
        channel.exchange_declare(
            exchange=self.exchange_name,
            exchange_type='direct',
            durable=True,  # Make exchange persistent
            auto_delete=False  # Don't delete when no queues are bound
        )

        # Declare a durable queue
        channel.queue_declare(
            queue=self.queue_name,
            durable=True,  # Make queue persistent
            exclusive=False,  # Allow multiple consumers
            auto_delete=False  # Don't delete when consumers disconnect
        )

        # Bind the queue to the exchange
        channel.queue_bind(
            exchange=self.exchange_name,
            queue=self.queue_name,
            routing_key='syslog.all'
        )

        # Set QoS (Quality of Service)
        channel.basic_qos(prefetch_count=1)  # Process one message at a time

        print(f'Connected to RabbitMQ. Consuming from queue: {self.queue_name}')
        print('Waiting for messages. To exit press CTRL+C')

        # Start consuming
        channel.basic_consume(
            queue=self.queue_name,
            on_message_callback=self.callback
        )

        try:
            channel.start_consuming()
        except KeyboardInterrupt:
            print("\nShutting down consumer...")
            channel.stop_consuming()
        except Exception as e:
            print(f"Unexpected error: {e}")
        finally:
            try:
                connection.close()
                print("Connection closed")
            except Exception:
                pass