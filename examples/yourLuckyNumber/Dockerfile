# choose base image
FROM python:2.7-slim

# run preparatory commands
RUN pip install cherrypy

# copy script to container
COPY run.py /

# expo port outside of container
EXPOSE 8080

# run the script
CMD ["python", "run.py"]
