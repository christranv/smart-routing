import sqlite3 as db
import json

with db.connect('db.sqlite') as conn:
	try:
		cursor  = conn.cursor().execute("SELECT * FROM Cameras")
		data = cursor.fetchall()
		result = []
		camid = 1
		f = open("mysql_cam_insert.txt", "w")
		for value in data:
			l = list(value)
			f.write('INSERT INTO camera (id, street, area, camera_name, street_width)\n')
			f.write('VALUES (\'{}\',\'{}\',\'50\',\'{}\',7);\n'.format(l[0],l[3],'camera'+(('0'+str(camid)) if camid<10 else str(camid))))
			camid+=1
		f.close()	
		conn.commit()
		# conn.close()
	except db.Error as e: 
		print("Error "+str(e))