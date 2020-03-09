# lambda-daily-accounts-api
Application to create AWS Lambda based on Scala with API Gateway Trigger and AWS Dynamo DB

This application provides API  for the frontend 'scalajs-daily-acccounts' 

#Set up 

Download and install from Serverless framework from Serverless.com. This will ease creation of configuration scripts for AWS Lambda

In order to quick start, we use project seed template "aws-scala-sbt" 

> serverless create --template aws-scala-sbt

Note : Since we are using aws-lambda-scala library to wrap Request and Response objects, we  only keep the Handler.scala file. Rest of the files are deleted. 

Update the build.sbt file with the following library dependency
> libraryDependencies += "io.github.mkotsur" %% "aws-lambda-scala" % "0.2.0"

#Building
Build the project 
> sbt assembly

#Deploying
Update the severless.yml file
```
functions:
    app:
     handler: app.ApiGatewayScalaHandler::handle
   events:
      - http:
          path: "lambda-event-app-api"
          method: post
          async: true
```

#Deploy the project 

> serverless deploy

#Running

Example: As API Gateway GET/PUT/POST Async request
> set Integration Request "Use Lambda Proxy Integration" to false

> set Http Header "X-Amz-Invocation-Type" to 'Event'

> set Request Body passthrough to When no template matches the request Content-Type header  with no templates available.

Use the URL provided by AWS in the front end app
