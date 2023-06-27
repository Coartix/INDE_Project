# INDE_Project
Introduction Data Engineering project by Param Dave, Pierre Litoux, Hugo Deplagne


Need to have JDK 8+ installed on your machine

# Start the ZooKeeper service
sudo bin/zookeeper-server-start.sh config/zookeeper.properties

# Start the Kafka broker service
sudo bin/kafka-server-start.sh config/server.properties

# Create a topic
bin/kafka-topics.sh --create --topic drone-message --bootstrap-server localhost:9092
# Display info
bin/kafka-topics.sh --describe --topic drone-message --bootstrap-server localhost:9092

# To write events in the topic
bin/kafka-console-producer.sh --topic drone-message --bootstrap-server localhost:9092

# To read events from the topic
bin/kafka-console-consumer.sh --topic drone-message --from-beginning --bootstrap-server localhost:9092




Need to set up AWS CLI on your machine

Run:

curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

aws configure
"""
AWS Access Key ID [None]: AKIA5A7SQJHC5X4V265K
AWS Secret Access Key [None]: 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
Default region name [None]: eu-west-3
Default output format [None]: json
"""
    