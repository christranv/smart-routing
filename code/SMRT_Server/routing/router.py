import sys, heapq
from routing.csvMapLoader import calDistByObj

class Router():  
	MAX_WEIGHT = 6
	def __init__(self,mapData):
		self.mapData = mapData

	def getNearestHead(self, src, nodes):
		cur = None
		min = sys.maxsize
		for node in nodes:
			dist = calDistByObj(node,src)
			if dist < min:
				min = dist
				cur = node
		return cur

	def dijkstra(self, src, des):
		cNodes = {}
		head = src.onWay[0].nodes[0]
		tail = src.onWay[0].nodes[-1]
		src1 = self.getNearestHead(src,[head,tail])

		queue = []
		heapq.heappush(queue,(0,src1))
		minWeight2Nodes = {src1:[0,None]}

		headE = des.onWay[0].nodes[0]
		tailE = des.onWay[0].nodes[-1]

		while len(queue)>0:
			cur = heapq.heappop(queue)[1]
			if cur.id in cNodes:
				continue	
			try:
				cNodes[headE]
				cNodes[tailE]
				break
			except:
				pass	
			for node in cur.relations:
				idFormat = "{}:{}".format(*[node.id,cur.id][::(1 if node.id<cur.id else -1)])
				realWeight = self.mapData.distances[idFormat] * (self.mapData.streetWeights[idFormat] * self.getExtraParam(self.mapData.streetWeights[idFormat]) if idFormat in self.mapData.streetWeights else 3)
				dist = minWeight2Nodes[cur][0] + realWeight
				try:
					minWeight2Nodes[node] = [dist,cur] if dist < minWeight2Nodes[node][0] else minWeight2Nodes[node]
				except:
					minWeight2Nodes[node] = [dist,cur]
				heapq.heappush(queue,(minWeight2Nodes[node][0],node))	
			cNodes[cur.id] = True
		cur = headE if minWeight2Nodes[headE]<minWeight2Nodes[tailE] else tailE
		"""Need 2 FIX Distance"""
		distance = 0
		routing = [des]
		while(cur!=None):
			routing.append(cur)
			prev = minWeight2Nodes[cur][1]
			#Cal real distance
			try:
				idFormat = "{}:{}".format(*[cur.id,prev.id][::(1 if cur.id<prev.id else -1)])
				distance += self.mapData.distances[idFormat] 
			except:
				pass
			cur = prev
		routing = routing[::-1] 
		return [routing,distance]

	def getExtraParam(self,w):
		return 50 if w>=4 else (5 if w>=2 else 1) 

	def route(self,lat1,lon1,lat2,lon2):
		src = self.mapData.getNearestNode(lat1,lon1)
		des = self.mapData.getNearestNode(lat2,lon2)
		listt = self.dijkstra(src,des)
		result = []
		for node in listt[0]:
			result.append([node.lat,node.lon])
		return {'list':result,'distance':round(listt[1]/1000,2)}
