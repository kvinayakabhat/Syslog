import json
import requests
import datetime
from datetime import datetime
import time
import numpy as np
from sentence_transformers import SentenceTransformer

class LogClassifier:
    def __init__(self, api_url):
        self.api_url = api_url
        # Load the lightweight sentence transformer model
        print("Loading sentence transformer model...")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')
        self.device_categories = self.fetch_rules_with_retry()
        self.category_embeddings = self.create_embeddings()

    def fetch_rules_with_retry(self):
        while True:
            try:
                response = requests.get(self.api_url)
                response.raise_for_status()
                print("Successfully fetched rules from API.")
                
                rules = response.json()
                formatted_rules = {}
                
                for device_type in rules:
                    type_name = device_type['type']
                    for category in device_type['categories']:
                        category_key = f"{type_name}_{category['title']}"
                        formatted_rules[category_key] = {
                            'device_type': type_name,
                            'title': category['title'],
                            'keywords': category['keywords'],
                            'severity': category['severity']
                        }
                
                print("Formatted rules -",formatted_rules)

                return formatted_rules
                    
            except requests.RequestException as e:
                print(f"Error fetching rules from API: {e}. Retrying in 60 seconds...")
                time.sleep(60)

    def create_embeddings(self):
        """Create embeddings for all keywords in each category"""
        category_embeddings = {}
        
        for category_key, details in self.device_categories.items():
            # Create embeddings for each keyword in the category
            keyword_embeddings = self.model.encode(details['keywords'])
            category_embeddings[category_key] = keyword_embeddings
            
        return category_embeddings

    def update_rules(self, new_rules):
        """Update rules and recreate embeddings"""
        formatted_rules = {}
        for device_type in new_rules:
            type_name = device_type['type']
            for category in device_type['categories']:
                category_key = f"{type_name}_{category['title']}"
                formatted_rules[category_key] = {
                    'device_type': type_name,
                    'title': category['title'],
                    'keywords': category['keywords'],
                    'severity': category['severity']
                }
        self.device_categories = formatted_rules
        self.category_embeddings = self.create_embeddings()

    def compute_similarity(self, message_embedding, category_embeddings):
        """Compute cosine similarity between message and category keywords"""
        similarities = np.max([
            np.dot(message_embedding, keyword_emb) / 
            (np.linalg.norm(message_embedding) * np.linalg.norm(keyword_emb))
            for keyword_emb in category_embeddings
        ])
        return similarities

    def classify_log_message_and_severity(self, message, device_type=None, threshold=0.6):
        """Classify message using semantic similarity"""
        message_embedding = self.model.encode(message)
        
        best_category = "Other"
        severity = "LOW"
        device_category = None
        best_similarity = threshold  # Only match if similarity is above threshold
        
        for category_key, embeddings in self.category_embeddings.items():
            # If device_type is specified, only check relevant categories
            if device_type and not category_key.startswith(f"{device_type}_"):
                continue
                
            similarity = self.compute_similarity(message_embedding, embeddings)
            
            if similarity > best_similarity:
                best_similarity = similarity
                category_details = self.device_categories[category_key]
                best_category = category_details['title']
                severity = category_details['severity']
                device_category = category_details['device_type']
        
        return best_category, severity, device_category

    def process_log(self, log_message):
        try:
            log_data = json.loads(log_message)
            message = log_data.get("message", "").strip()
            device_type = log_data.get("device_type", None)
            timestamp = int(datetime.now().timestamp() * 1000)
            
            category, severity, detected_device_type = self.classify_log_message_and_severity(
                message, 
                device_type
            )
            
            log_json = {
                "timestamp": timestamp,
                "level": severity,
                "category": category,
                "message": message,
                "device": log_data.get("fromhost", "UNKNOWN"),
                "device_type": detected_device_type or device_type or "UNKNOWN",
                "network": "NETWORK_ID"
            }
            
            return json.dumps(log_json)
        except Exception as e:
            print(f"Error processing log: {e}")
            return None

    def get_available_categories(self, device_type=None):
        """Return all available categories, optionally filtered by device type"""
        categories = []
        for category_key, details in self.device_categories.items():
            if not device_type or details['device_type'] == device_type:
                categories.append({
                    'device_type': details['device_type'],
                    'title': details['title'],
                    'severity': details['severity']
                })
        return categories