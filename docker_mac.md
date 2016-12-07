## Docker on mac
Docker daemon needs 64-bit Linux kernel. In the old times you needed to install _boot2docker_ Linux image on Virtual Box and manage it using _Docker Machine_. In this solution to point the docker client to correct host you need to execute:

`eval "$(docker-machine env default)"`  

Currently a native application is available.
[https://docs.docker.com/docker-for-mac/](https://docs.docker.com/docker-for-mac/)
