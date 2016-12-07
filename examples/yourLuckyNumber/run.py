import cherrypy
import time
from random import randint

class YourLuckyNumber(object):
    @cherrypy.expose
    def index(self):
        return "Wellcome to Lucky Number Generator (Random Number Generator in fact..)."

    @cherrypy.expose
    def ping(self):
        return "pong"

    # accepts only date of birth i.e "30-Nov-00"
    # 127.0.0.1:8080/number?dateOfBirth=30-Nov-00
    @cherrypy.expose
    def number(self, dateOfBirth=''):
        try:
            time.strptime(dateOfBirth, "%d-%b-%y")
        except ValueError:
            raise cherrypy.HTTPError(400, "wrong format")
            # return 'Not a timestamp' #todo return 400
        else:
            return str(randint(0,100))


if __name__ == '__main__':


    def handle_error():
        cherrypy.response.status = 500
        cherrypy.response.body = "Error"

    def error_page_400(status, message, traceback, version):
        return "%s: %s" % (status, message)

    def error_page_404(status, message, traceback, version):
        return "Resource not found"

    cherrypy.config.update({'error_page.400': error_page_400})
    cherrypy.config.update({'error_page.404': error_page_404})
    cherrypy.config.update({'request.error_response': handle_error})

    # make app available on ips other than localhost
    cherrypy.config.update({'server.socket_host': '0.0.0.0'})
    cherrypy.quickstart(YourLuckyNumber())
