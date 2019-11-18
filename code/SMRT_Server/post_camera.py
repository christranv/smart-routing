import datetime
import requests
import sys
def send_post_process(N,id):
    timenow = datetime.datetime.now().strftime('%d-%m-%Y %H:%M')
    r = requests.post("http://localhost:9999/api/update_cameras",
                      json={
                          "id": id,
                          "weight": N,
                          "time": str(timenow),
                          "image": "http://192.168.137.1:8000/media/camera01/image1.jpg",
                      }
                      )
    print(r.status_code, r.reason)

send_post_process(0,'2990909293:6509553504')
send_post_process(0,'1333394780:6040694717')
send_post_process(0,'1333394789:6530229488')
send_post_process(0,'1334198764:6334716713')
send_post_process(0,'1334198766:6530432128')
send_post_process(0,'3763846124:3763846125')
send_post_process(0,'6045271333:6045271334')
send_post_process(0,'6512003604:6555689171')
send_post_process(0,'6528618547:6530471566')


