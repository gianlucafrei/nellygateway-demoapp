# Demo Application for NellGateway
This is a demo application built with [NellyGateway](https://github.com/gianlucafrei/nellygateway) used for integration testing and demonstration purposes. It is based on the Angular frontend and Spring Boot backend [RealWorld Example Apps](https://github.com/gothinkster/realworld) that are slightly adapted to integrate with Nelly.

**Link:** https://nellygateway.azurewebsites.net
The application as well as Nellygateway is currently hosted very rudimentary on a Azure App Service. You can check out the demo application by yourself, please just be aware that currently only an in-memory database is used and the application has around 30s for a cold startup. (Because currently only a free plan is used the application is shut-down after some time of inactivity)

## Structure
```
/frontend/                   (Frontend built with Angular)
/backend/                    (Backend built with Spring Boot)
/nelly-config-azure.yaml     (Configuration file for Nellygateway)
```
