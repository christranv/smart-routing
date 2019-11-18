import sqlite3 as db
import json
def fetchall():
	with db.connect('db.sqlite') as conn:
		try:
			cursor  = conn.cursor().execute("SELECT * FROM Cameras")
			data = cursor.fetchall()
			result = []
			print("CAM: ",len(data))
			for value in data:
				l = list(value)
				result.append({"id":l[0],"lat":l[1],"lon":l[2],"streetname":l[3],"status":round((float(l[4])/5.3)*100),"can_go":str(l[5]),"last_image":str(l[6]),"last_update":str(l[7])})
			return json.dumps(result)
			conn.commit()
			conn.close()
		except db.Error as e: 
			print("Error "+str(e))
			return False
def create(data):
	with db.connect('db.sqlite') as conn:
		try:
			conn.cursor().execute("INSERT INTO Cameras (latitude,longtitude,streetname,status,last_image,last_update) VALUES (?,?,?,?,?,strftime('%Y-%m-%d %H:%M:%f','now'))",data)
			conn.commit()
			return True
		except db.Error as e: 
			print("Error "+str(e))
			return False
# def updateAll(data):
# 	with db.connect('db.sqlite') as conn:
# 		try:
# 			prepare = []
# 			query = "UPDATE Cameras SET "
# 			for key,val in data.items():
# 				if key == "streetname":
# 					query +=str(key)+"='"+str(val)+"',"
# 				else:
# 					query +=str(key)+"="+str(val)+","
# 			query = query.strip().strip(",")
# 			conn.cursor().execute(query)
# 			conn.commit()
# 			return True
# 		except db.Error as e: 
# 			print("Error "+str(e))
# 			return False
def updateCamera(mapData,data):
	print(str(data))
	jObj = dict(data)
	#Update to mapData
	mapData.streetWeights[data['id']]=float(data['weight'])
	with db.connect('db.sqlite') as conn:
		try: 
			prepare = []
			query = "UPDATE Cameras SET "
			query +="status='"+str(jObj['weight'])+"',last_image='"+str(jObj['image'])+"',last_update='"+str(jObj['time'])+"',can_go='"+("yes" if str(jObj['can_go'])=="True" else "no")+"'"
			query +=" WHERE id='"+str(jObj['id'])+"'"
			print(query)
			query = query.strip()
			conn.cursor().execute(query)
			conn.commit()
			return "Ok"
		except db.Error as e: 
			print("Error "+str(e))
			return "Fail"
# def deleteAll():
# 	with db.connect("db.sqlite") as conn:
# 		try:
# 			conn.cursor().execute("DELETE FROM Cameras")
# 			conn.commit()
# 			return True
# 		except db.Error as e:
# 			print("Error "+str(e))
# 			return False
def updateOne(data):
	with db.connect("db.sqlite") as conn:
		try:
			id = data["id"]
			del data["id"]
			query = "UPDATE Cameras SET "
			for key,val in data.items():
				if key == "streetname":
					query +=str(key)+"='"+str(val)+"',"
				else:
					query +=str(key)+"="+str(val)+","
			query = query.strip().strip(",")
			query = query +" WHERE id='"+str(id)+"'"
			print(query)
			conn.cursor().execute(query)
			conn.commit()
			return True
		except db.Error as e:
			print("Error "+str(e))
			return False
def fetchOne(id):
	with db.connect("db.sqlite") as conn:
		with db.connect('db.sqlite') as conn:
			try:
				cursor  = conn.cursor().execute("SELECT * FROM Cameras WHERE id='"+str(id)+"'")
				data = cursor.fetchone()
				l = list(data)
				result = {"id":l[0],"lat":l[1],"lon":l[2],"streetname":l[3],"vote":l[4],"traffic":json.loads(l[5]),"last_image":str(l[7]),"last_update":str(l[8])}
				return json.dumps(result)
			except db.Error as e:
				print("Error "+str(e))
				return False
def removeOne(id):
	with db.connect("db.sqlite") as conn:
		try:
			cursor  = conn.cursor().execute("DELETE FROM Cameras WHERE id='"+str(id)+"'")
			conn.commit()
			return True
		except db.Error  as e:
			print("Error "+str(e))
			return False