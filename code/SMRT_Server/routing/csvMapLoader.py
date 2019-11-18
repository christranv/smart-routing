import sys, csv
from math import radians, cos, sin, asin, sqrt,atan2,pi

DECIMAL_LIMIT = 2


def calDistance(lat1,lon1,lat2,lon2):
    R = 6371
    dLat = deg2rad(lat2-lat1)  
    dLon = deg2rad(lon2-lon1) 
    a = sin(dLat/2) ** 2 + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * sin(dLon/2) **2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    d = R * c; 
    return d

def deg2rad(deg):
  return deg * (pi/180)


def calDistByObj(src, des):
    return calDistance(src.lat,src.lon,des.lat,des.lon)

class Node:
    def __init__(self,id,lat,lon):
        self.id = id
        self.lat = lat
        self.lon = lon
        self.onWay = []
        self.relations = []
    def __lt__(self, value):
        return 10

class Way:
    def __init__(self,id,nodes):
        self.id = id
        self.weight = -1
        self.nodes = nodes

class CSVMapData:
    def __init__(self):
        self.nodes = {}
        self.ways = {}
        self.latNodes = {}
        self.lonNodes = {}
        self.distances = {}
        self.streetWeights = {}

        with open('routing/nodes.csv') as csvfile1:
            readCSV1 = csv.reader(csvfile1, delimiter=',')
            for row in readCSV1:
                id = int(row[0])
                self.nodes[id] = Node(id,float(row[1]),float(row[2]))
                lat=round(float(row[1]),DECIMAL_LIMIT)

                try:
                    self.latNodes[lat].append(id) 
                except:
                    self.latNodes[lat] = [id]

                lon=round(float(row[2]),DECIMAL_LIMIT)
                try:
                    self.lonNodes[lon].append(id)
                except:
                    self.lonNodes[lon] = [id]  

        with open('routing/ways.csv') as csvfile2:
            readCSV2 = csv.reader(csvfile2, delimiter=',')
            for row in readCSV2:
                id = int(row[0])
                childNodes = list(map(int, row[1].split(',')))
                self.ways[id] = Way(id,[])
                for i in childNodes:
                    try:
                        #Append reference to child node
                        self.ways[id].nodes.append(self.nodes[i])
                        self.nodes[i].onWay.append(self.ways[id])
                    except:
                        pass    

        with open('routing/relation.csv') as csvfile3:
            readCSV3 = csv.reader(csvfile3, delimiter=',')
            for row in readCSV3:
                try:
                    self.nodes[int(row[0])].relations = list(map(lambda x:self.nodes[int(x)], row[1].split(',')))
                except:
                    pass
                    
        with open('routing/distance.csv') as csvfile4:
            readCSV4 = csv.reader(csvfile4, delimiter=',')
            for row in readCSV4:
                try:
                    self.distances[row[0]] = float(row[1])
                except:
                    pass            

    def getNearestNode(self,lat,lon):
        lats = self.latNodes[round(lat,DECIMAL_LIMIT)]
        lons = self.lonNodes[round(lon,DECIMAL_LIMIT)]
        result = set(lats).intersection(set(lons))
        if len(result) >= 2:
            id = -1
            min = sys.maxsize
            for i in result:
                dis = calDistance(self.nodes[i].lat,self.nodes[i].lon,lat,lon)
                if dis < min:
                    min = dis
                    id = i
            return self.nodes[id]        
        elif len(result) == 1:
            return self.nodes[result.pop()]
        else: 
            return None