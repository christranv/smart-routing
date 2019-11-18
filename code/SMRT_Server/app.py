from flask import Flask,request,render_template,abort,Response,session,jsonify,redirect,url_for,render_template
import camera as db
from flask_cors import CORS,cross_origin
import json
from routing.router import Router
from routing.csvMapLoader import CSVMapData
app = Flask(__name__)
cors = CORS(app, resources={r"/api/*": {"origins": "*"}})
'''
AIOT RestfullApi
'''
@app.route('/static/',methods=["GET"])
def red():
		if "logined" in session.keys():
				return redirect('http://0.0.0.0:9999/static/index.html',code=302)
		else:
				return redirect('http://0.0.0.0:9999/static/login.html',code=302)
PREFIX = '/api'

@app.route('/static/logout')
def logout():
		del session["logined"]
		return redirect('/static/login.html')
@app.route(PREFIX+'/auth',methods=['OPTIONS','POST','GET'])
def auth(data=False):
		if request.method == 'POST':
				username = 'admin'
				pwd = '12345'
				data = request.get_json()
				if("logined" in session.keys()):
						reptext = {"success":True}
						resp = Response(json.dumps(reptext))
						resp.headers["content-type"] = "application/json"
						return resp
				reptext = {"success":False}
				if (str(data['username']) == str(username) and str(data['password']) == str(pwd)):
						reptext = {"success":True}
						session["logined"] = True
				
				resp = Response(json.dumps(reptext))
				resp.headers["content-type"] = "application/json"
				return resp
		elif request.method =='GET':
				reptext = {"success":False}
				if("logined" in session.keys()):
						reptext = {"success":True}
				resp = Response(json.dumps(reptext))
				resp.headers["content-type"] = "application/json"
				return resp
@app.route(PREFIX+'/routing/<float:lat1>,<float:lon1>/<float:lat2>,<float:lon2>',methods=['GET'])
def routing(lat1,lon1,lat2,lon2):
	return str(app.router.route(lat1,lon1,lat2,lon2))
@app.route(PREFIX+'/update_cameras',methods=['POST'])
def updateCamera():
	return db.updateCamera(app.mapData,request.json)
@app.route(PREFIX+'/get_point/<int:id1>/<int:id2>',methods=['get'])
def getPoint(id1,id2):
	return str([[app.mapData.nodes[id1].lat,app.mapData.nodes[id1].lon],[app.mapData.nodes[id2].lat,app.mapData.nodes[id2].lon]])	
@app.route(PREFIX+'/cameras',methods=['POST','GET','PUT','DELETE'])
def cameras(data=False):
	if request.method == 'GET':
		resp = Response(db.fetchall())
		resp.headers["content-type"] = "application/json"
		return resp
	elif request.method == 'POST':
		try:
			if request.get_json():
				camera = dict(request.get_json())
				if "traffic" not in camera.keys():
					camera["traffic"] = "{}"
				else:
					camera["traffic"] = json.dumps(camera["traffic"])
				if "last_image" not in camera.keys():
					camera["last_image"] = ""
				if "vote" not in camera.keys():
					camera["vote"] = 5
				ordered = []
				ordered.append(camera["latitude"])
				ordered.append(camera["longtitude"])
				ordered.append(camera["streetname"])
				ordered.append(camera["status"])
				ordered.append(camera["traffic"])
				ordered.append(camera["vote"])
				ordered.append(camera["last_image"])
				resp = Response(json.dumps({"success":db.create(tuple(ordered))}))
				resp.headers["content-type"] = "application/json"
				return resp
			else:
				resp = Response(json.dumps({"success":False}))
				resp.headers["content-type"] = "application/json"
				return resp
		except:
			resp = Response(json.dumps({"success":False}))
			resp.headers["content-type"] = "application/json"
			return resp
	elif request.method == 'PUT':
		if request.get_json():
			camera = dict(request.get_json())
			if "traffic" in camera.keys():
				camera["traffic"] = json.dumps(camera["traffic"])
			resp = Response(json.dumps({"success":db.updateAll(camera)}))
			resp.headers["content-type"] = "application/json"
			return resp
		else:
			resp = Response(json.dumps({"success":False}))
			resp.headers["content-type"] = "application/json"
			return resp
	elif request.method =='DELETE':
		#db.deleteAll()
		resp = Response(json.dumps({"success":True}))
		resp.headers["content-type"] = "application/json"
		return resp
@app.route(PREFIX+'/cameras/<string:id>',methods=['GET','PUT','DELETE'])
def single_camera(id):
	if request.method == 'GET':
		resp = Response(db.fetchOne(id))
		resp.headers["content-type"] = "application/json"
		return resp
	elif request.method == 'PUT':
		camera = dict(request.get_json())
		camera["id"] = id
		resp = Response(json.dumps({"success":db.updateOne(camera)}))
		resp.headers["content-type"] = "application/json"
		return resp
	elif request.method =='DELETE':
		resp = Response(json.dumps({"success":db.removeOne(id)}))
		resp.headers["content-type"] = "application/json"
		return resp


if __name__ == '__main__':
		app.mapData = CSVMapData()
		app.router  = Router(app.mapData)
		
		app.secret_key = 'super secret key'
		app.config['SESSION_TYPE'] = 'filesystem'
		app.run(debug=True,host='0.0.0.0',port="9999")