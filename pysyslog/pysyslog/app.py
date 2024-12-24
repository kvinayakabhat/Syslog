import json
from datetime import datetime
import spacy
from spacy.lang.en import English
from spacy.matcher import PhraseMatcher
import pika

# Load small spaCy model
nlp = spacy.load("en_core_web_sm")    # is the name of a specific language model provided by spaCy.

# Define log categories and their corresponding examples
log_categories = {
    "Unauthorized Access": {
        "keywords": ["unauthorized access", "login failure", "authentication failed", "username/password error", "failed to log in"],
        "severity": "CRITICAL"
    },
    "Network Issue": {
        "keywords": ["link down", "connection lost", "network unreachable"],
        "severity": "HIGH"
    },
    "System Resource": {
        "keywords": ["cpu high", "memory low", "disk full"],
        "severity": "MEDIUM"
    },
    "Configuration Change": {
        "keywords": ["config updated", "settings changed", "rule modified"],
        "severity": "LOW"
    },
    "Security Alert": {
        "keywords": ["firewall block", "intrusion detected", "suspicious activity"],
        "severity": "HIGH"
    },
    "Routine Operation": {
        "keywords": ["backup completed", "update successful", "user logged in"],
        "severity": "LOW"
    }
}

# Create a matcher for each category
def create_matchers():  # this creates a dictionary of phrase matchers for diff categories based on keywords
    matchers = {}       #empty dictonary is initialized
    for category, details in log_categories.items():
        matcher = PhraseMatcher(nlp.vocab, attr="LOWER")
        patterns = [nlp.make_doc(keyword) for keyword in details["keywords"]]
        matcher.add(category, patterns)
        matchers[category] = matcher
    return matchers

matchers = create_matchers()

def classify_log_message_and_severity(message):
    # Process the message with spaCy
    doc = nlp(message)
    
    # Default category and severity
    best_category = "Other"
    severity = "LOW"
    
    for category, matcher in matchers.items():
        matches = matcher(doc)
        if matches:
            best_category = category
            severity = log_categories[category]["severity"]
            break  # Stop after first match, or you can adjust logic for better matching

    return best_category, severity

def process_log(log_message):
    try:
        # Parse the JSON log message
        log_data = json.loads(log_message)

        # Extract the relevant fields from the JSON
        message = log_data.get("message", "").strip()

        # Get the current timestamp in milliseconds
        timestamp = int(datetime.now().timestamp() * 1000)
        
        # Classify the log message and get the corresponding severity
        category, severity = classify_log_message_and_severity(message)

        # Construct the JSON object to return
        log_json = {
            "timestamp": timestamp,
            "level": severity,
            "category": category,
            "message": message,
            "device": log_data.get("fromhost", "UNKNOWN"),
            "network": "NETWORK_ID"
        }

        return json.dumps(log_json)
    except Exception as e:
        print(f"Error processing log: {e}")
        return None

def callback(ch, method, properties, body):
    log_message = body.decode('utf-8')
    print("Raw log message from rabbitmq:",log_message)
    processed_log = process_log(log_message)
    if processed_log:
        processed_log_dict = json.loads(processed_log)
        # Check the severity level
        if processed_log_dict["level"] != "LOW":
            print("=================================================================================")
            print(f"Processed log: {processed_log}")
            print("=================================================================================")
    ch.basic_ack(delivery_tag=method.delivery_tag)

def start_rabbitmq_consumer():
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    channel.exchange_declare(exchange='syslog', exchange_type='direct', auto_delete=False, durable=True)
    result = channel.queue_declare(queue='', exclusive=True)
    queue_name = result.method.queue

    channel.queue_bind(exchange='syslog', queue=queue_name, routing_key='syslog.all')

    print('Waiting for messages. To exit press CTRL+C')

    channel.basic_consume(queue=queue_name, on_message_callback=callback)
    channel.start_consuming()

if __name__ == "__main__":
    start_rabbitmq_consumer()
