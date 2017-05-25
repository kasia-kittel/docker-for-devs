# Docker for developers
* [Docker 101](#docker-101)
* [More about images](#more-about-images)
* [More about containers](#more-about-containers)
* [Docker HUB](#docker-hub)
* [More about Docker-Compose](#more-about-docker-compose)
* [Examples](#how-docker-helps-developers---examples)

## Docker 101
### Containers
Container is an encapsulated application with its all dependencies. It shares resources with host OS, runs on the host kernel.
Docker containers runs on 64-bit Linux kernel.

### Image
The image is a filesystem with some sort of metadata defining the process to be started in the container.

### Docker Hub
The images are stored in local registry but also in remote repositories. One of such public registry is Docker Hub, where everyone can store private images and from where everyone can pull public images.

### Docker compose
We use to talk about Docker in context of microservices. A system or application is usually made up of many of them…
Docker compose serves to start multicontainer applications.

### Docker Daemon
The heart of the platform is Docker daemon that is wrapped in REST API.
We can communicate with Docker Daemon via command line tool and, recently, vial GUI
Kitematic.

## More about Images

__Base image__ is an image that contains the root filesystem and may also contain tools like shells, compilers or libraries. In some cases a base image has everything needed to run particular application (i.e. MongoDB)

To create our own images we have two choices:
* ```docker commit``` - first you run base image in interactive mode and install and configure whatever you container needs. Once your container is ready you call *commit* on it.
* ```docker build [options] context``` - where
  * the *context* are files in given path or url to git repository
  * these files can be later referenced in _Dockerfile_

Docker will search for *Dockerfile* at the root of the context.
Most of the time we build images from Dockerfile using *build* tool.

__Dockerfile__ is a set of instructions describing what files should be included in the image filesystem and what processes should be started with container.

The most important instructions are:
* __FROM__ 	- defines base image
* __COPY [src] [dest]__ - copy files from context to the container
* __RUN__ - run instruction inside the container
* __EXPOSE__ - inform Docker that this container listens on given port
* __CMD__ - execute a command when a container is started
* __ENTRYPOINT__ - sets executable to run when the container is starting. If there is _CMD_ instruction it will be passed as a parameter to _entrypoint_ script.

### Useful commands

* `docker images` - list images local images
* `docker rmi [img_id]` - remove image

## More about containers
Launching container means that we instantiate the image. Containers are mutable and they keep their state.
To run a container: `docker run [options] image:tag`

__Options:__
* __-d__ - detached
* __-t__ - allocate pseudo tty
* __-i__ - keep STDIN open
* __-w__ - set up working dir
* __-v__ - mount volume
* __--link [container_name:alias]__ - link container to another container
* __--p [hostport:containerport]__ - map container port to host port
* __--name [name]__ - name the container

__Examples__:

* `docker run --name mongo3-3 -d -P mongo:3.3`
* `docker exec -it 63076a62781d mongo` - execute (in interactive mode) *mongo* in container with id 63076a62781d

## Docker HUB

The Docker Hub is a public registry maintained by Docker, Inc. First step is to obtain Docker ID by creating account at [https://hub.docker.com](https://hub.docker.com).
Once we obtain the Docker ID we should call `docker login` that will create an entry with credential in `$HOME/.docker/config.json`. From now we can use commands like `docker push`.

Any Image we want to push to Docker Hub must be tagged with username. For example `docker build -t kasiak/yln .` builds and image from _Dockerfile_ in the same directory as `kasiak/yln:latest`.

`docker push kasiak/yln` pushes the Image to Docker Hub.

## More about Docker-Compose

Docker compose serves to run a set of services that makes an application in an isolated environment. All services definitions are placed in _docker-compose.yml_, where we define how all participating containers should be run and connected. (Similar like passing parameters to docker run command.)
[https://docs.docker.com/compose/overview/](More features of Docker Compose).

### docker-compose.yml
The most important entries are listed below.

```yaml
version: '2'
  services:
    <service_name1>:
      build: .            //It will be build from Dockerfile in current location.
      ports:              //define port forwarding host:container
        - "27017:27017"
      volumes:            //mount volumes host:container
        - .:/app
      depends_on:         //this affects the order in which the services are started
        - <service_name2>
      environment:        //define environmental variables
        - secret=abcd
      links:              //links service_name1 to service_name2
        - service_name2
      external_links:     //link to container started outside this docker-compose
        - image:tag
      command: sbt test   //override the default command
    <service_name2>:
      image: <image_name> //Instead of building we can also pull existing image.
      expose: 27017       //Expose ports to without publishing them to the host (but still accessible for linked service)
      ...
```
[https://docs.docker.com/compose/compose-file/](docker-compose.yml specification)

To start application defined in docker-compose.yml run `docker-compose up`. Use `docker-compose stop` to stop ale services.

[https://docs.docker.com/compose/reference/overview/#/undefined](Docker compose command reference)


## How Docker helps developers - examples

### Running MongoDB RC version

1. `docker run --name mongo3-3 -d -P mongo:3.3` - pull MongoDB image and start container
2. `docker ps` - check which host port was mapped to MongoDB container port

Now you can connect any MongoDB client to your container or `docker exec -it [container_id] mongo` to run mongo client inside the container. (Instead of using -P you can also map ports with -p 27017:27017)

### Run Python3 scrips

```docker run -it -v "$PWD":/usr/src/myapp -w /usr/src/myapp python:3 /bin/bash```

where:
* `-v "$PWD":/usr/src/myapp` - mount current directory to `/usr/src/myapp` in container
* `-w /usr/src/myapp` - set `/usr/src/myapp` as workidir
