# rate-limit-poc

Esempio di implementazione algoritmo `Token Bucket` per rate limiting di api rest.

L'esempio utilizza redis come cache condivisa

Fonti:
* https://medium.com/@SaiRahulAkarapu/rate-limiting-algorithms-using-redis-eb4427b47e33
* 


## Github issue https://github.com/quarkusio/quarkus/issues/32361

With redis cluster mode it seems that there is issue with the `withTransaction`
To reproduce:
* run the redis cluster `docker compose up`
* hit the `hello` endpoint: `curl -v http://localhost:8080/hello` 

For the sake of simplicity there is no authentication and the `IdentityAugmentor` provide a fixed principal `test-rate-limit`

