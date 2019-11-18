import sys,csv
from math import radians, cos, sin, asin, sqrt
from routing.csvMapLoader import CSVMapData, calDistance, calDistByObj
from routing.router import Router

mapData = CSVMapData()
router = Router(mapData)
#Call this function to route (result is array of nodes)
result = router.route(16.065568, 108.193644,16.055423, 108.236216)