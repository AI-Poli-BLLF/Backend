# AI Virtual Labs Backend

## Come effettuare la push dell'immagine sul repository

1. (Eseguire solo la prima volta)  Creare un nuovo token github seguendo le istruzioni [qui](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token), con il permesso di scrivere packages e salvalo in un file `~/GH_TOKEN.txt`.
1. (Eseguire solo la prima volta) `cat ~/GH_TOKEN.txt | sudo docker login docker.pkg.github.com -u username_github --password-stdin`
1.  Cambiare l'url del db in application.properties da `spring.datasource.url=jdbc:mysql://localhost:3306/teams` a `spring.datasource.url=jdbc:mysql://vl_db:3306/teams`
1. `mvn package`
1. `sudo docker build -t docker.pkg.github.com/ai-poli-bllf/backend/vl_backend:VERSION .`
1. `sudo docker push docker.pkg.github.com/ai-poli-bllf/backend/vl_backend:VERSION`
