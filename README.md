# vertx-demo
A demo for reactive programming using Vert.x

Each JVM instance becomes part of a cluster that allows it to share data using event bus.
In order to share data with Frontend using event bus, websocket server is created in DashboardBE component. 


![Application Architecture](https://github.com/oskiegarcia/vertx-demo/blob/master/archi.png)


# vertx-demo

A demo for reactive programming using Vert.x


## Deployment of Java Backend

* mvn clean package

* java -jar target/ride-hailing-app-1.0.jar  run com.rider.PassengerProducer -cluster


* java -Ddriver=Oscar -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster
* java -Ddriver=Herman -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster
* java -Ddriver=Stefan -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster
* java -Ddriver=Flo -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster
* java -Ddriver=Chloe -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster
* java -Ddriver=Tina -jar target/ride-hailing-app-1.0.jar  run com.rider.TaxiDriver -cluster


* java  -jar target/ride-hailing-app-1.0.jar  run com.rider.DashboardBE -cluster

## Deployment of React Frontend

* npm install
* npm start

Open http://localhost:9000/


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Npm](https://www.npmjs.com/) - Dependency Management


## Authors

* **Oscar Garcia** - *Initial work* - [oskiegarcia](https://github.com/oskiegarcia)




