Javers with a basic Jersey/Spring/Hibernate App
===============================================

Adapted from https://github.com/amacoder/demo-restWS-spring-jersey-jpa2-hibernate

Run the example
--
- Download/clone the project
- Run 'mvn jetty:run -Djetty.port=9090' from the project directory (assumes maven is installed locally)

Endpoints
--

GET - http://localhost:9090/javers-hibernate-proxy-0.0.1-SNAPSHOT/podcasts
PUT - http://localhost:9090/javers-hibernate-proxy-0.0.1-SNAPSHOT/podcasts

Sample payload:
```
{
  "id": 1,
  "title": "New Title - Quarks & Co - zum Mitnehmen",
  "linkOnPodcastpedia": "http:\/\/www.podcastpedia.org\/podcasts\/1\/Quarks-Co-zum-Mitnehmen",
  "feed": "http:\/\/podcast.wdr.de\/quarks.xml",
  "description": "Quarks & Co: Das Wissenschaftsmagazin",
  "insertionDate": "09-01-2014 20:21",
  "soundEffectList": [
    {
      "id": 1,
      "path": "\/dummy-dir\/dummy-file-path-1",
      "desc": "drumroll"
    },
    {
      "id": 3,
      "path": "\/dummy-dir\/dummy-file-path-3",
      "desc": "club intro"
    }
  ],
  "edition": {
    "id": 3,
    "name": "Weekend",
    "desc": "Weedend"
  }
}
```
