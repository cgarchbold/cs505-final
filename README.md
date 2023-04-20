# Important Files:
**cs505-final-template/src/main/java/cs505finaltemplate/**
Launcher.java - describes the start of CEP and OrientDB
Topics/TopicConnector.java - describes the data subscription and how data to transfered to CEP and/or OrientDB
CEP/OutputSubscriber.java - describes how the output from CEP is saved
graphDB/GraphDBEngine.java - describes functions of graph database
embeddedDB/DBEngine.java - describes functions of embedded relational database
httpcontrollers/API.java - describes the API calls

# Before Running Program:
**Open ports using https://www.cs.uky.edu/docs/users/openstack.html#creating-firewall-rules-on-your-vm**
9999 - for API calls to our program
2424 - for OrientDB docker connection to our program
2480 - for OrientDB docker connection to online dashboard
		
**May also need to open on VM:**
	sudo ufw allow 9999/tcp
	sudo ufw allow 2424/tcp
	sudo ufw allow 2480/tcp

# Dependencies:
**pom.xml**
Apache Derby
OrientDB
SiddhiCEP

# Running Program:
**On VM terminal in directory cs505-final-template:**
Start OrientDB server: 
	docker run -it --name orientdb -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=rootpwd orientdb:3.0.0
If name is already used kill exited containers: docker rm $(docker ps -a -f status=exited -f status=created -q)
	
**On OrientDB dashboard http://[linkblue].cs.uky.edu:2480/studio/index.html**
Create new database:
name: test
username: root
password: rootpwd
Go to Schema and create new vertex class patient (needed?)
	
**On new VM terminal in directory cs505-final-template:**
Build application: mvn clean package
Start server: java -jar target/cs505-final-template-1.0-SNAPSHOT.jar
		
**Manual Tests:** (Port specified programatically)
	curl "http://localhost:9999/api/getteam"
	curl "http://localhost:9999/api/reset"
	curl "http://localhost:9999/api/zipalertlist"
	curl "http://localhost:9999/api/alertlist"
	curl "http://localhost:9999/api/getpatientstatus/"
		
# Before turning in:
**Same as running program except use docker containers in background**
Start OrientDB server: 
	docker run -d --name orientdb -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=rootpwd orientdb:3.0.0	
Build Docker container: 
	sudo docker build -t cs505-final .
Launch container in background: 
	sudo docker run -d --rm -p 9999:9999 cs505-final