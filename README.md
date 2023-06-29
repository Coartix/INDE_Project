# INDE_Project
Introduction Data Engineering project by Param Dave, Pierre Litoux, Hugo Deplagne

## <u>Setup for Linux distributions</u>

Need to have JDK 8+ installed on your machine

### Install Scala

`wget https://downloads.lightbend.com/scala/2.13.8/scala-2.13.8.deb`  
`sudo dpkg -i scala-2.13.8.deb`  

Verify the installation:  
`scala -version`  

Add environment variables:  
`nano ~/.bashrc`  
Add the following lines:  
`export SCALA_HOME=/usr/share/scala`  
`export PATH=$PATH:$SCALA_HOME/bin`  

`source ~/.bashrc`  

Verify that scala is working:  
`scala`

## <u>Kafka</u>

### Start the ZooKeeper service
`sudo bin/zookeeper-server-start.sh config/zookeeper.properties`

### Start the Kafka broker service
`sudo bin/kafka-server-start.sh config/server.properties`

### Create a topic
`bin/kafka-topics.sh --create --topic drone-message --bootstrap-server localhost:9092`
### Display info
`bin/kafka-topics.sh --describe --topic drone-message --bootstrap-server localhost:9092`

### To write events in the topic
`bin/kafka-console-producer.sh --topic drone-message --bootstrap-server localhost:9092`

### To read events from the topic
`bin/kafka-console-consumer.sh --topic drone-message --from-beginning --bootstrap-server localhost:9092`



## <u>Need to set up AWS CLI on your machine</u> 

Run:  

`curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"`  
`unzip awscliv2.zip`  
`sudo ./aws/install`  

`aws configure`  
"""  
AWS Access Key ID [None]: Ask administrator (Coartix)  
AWS Secret Access Key [None]: Ask administrator (Coartix)  
Default region name [None]: eu-west-3  
Default output format [None]: json  
"""  

This should display all buckets  
`aws s3 ls`  


`export AWS_ACCESS_KEY_ID=$(grep aws_access_key_id ~/.aws/credentials | awk '{print $3}')`
`export AWS_SECRET_ACCESS_KEY=$(grep aws_secret_access_key ~/.aws/credentials | awk '{print $3}')`  


Paste the previous commands in the ~/.bashrc file:  
`nano ~/.bashrc`

`source ~/.bashrc`  



## <u>Python server</u>

It is recommended to use a virtual environment to run the server.  
`python3 -m venv venv`  

`source venv/bin/activate`  

`pip install flask flask_cors`  



## <u>Web app</u>

Need nodeJS installed on your machine.  

`sudo apt update`  
`sudo apt install nodejs`  
`sudo apt install npm`  

Check the version of nodeJS and npm:

`node -v`  
`npm -v`  

Go to my-app folder and run:  

`npm install`  
`npm start`  

This will start the dev web app on http://localhost:3000/  




# <u>Step by step guide to run the entire project</u> 

## Kafka
Start the ZooKeeper service  
Start the Kafka broker service  
Create a topic 'drone-message'  

## Web app representation (Optional)
Start the python server  
Start the web app  

## Storing reports in S3
Run the scala storing_program  

## Processing reports to trigger alerts
Run the scala riot_program  

## Starting drones
Run the scala drone_program  

## Make analysis on the data in S3
Run the scala analysis_program  

(To run a scala program go into the directory and type `sbt run`)