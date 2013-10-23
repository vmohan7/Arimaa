import urllib2 
import sys
from HTMLParser import HTMLParser
from zipfile import ZipFile
from StringIO import StringIO

extension = '.zip'

class MyHTMLParser(HTMLParser):
	def __init__(self):
		HTMLParser.__init__(self)
		self.links = []

  	def handle_starttag(self, tag, attrs):
		if tag == 'a':
			for attr in attrs:
				if attr[0] == 'href' and extension in attr[1]:
					self.links.append(attr[1])


link = 'http://arimaa.com/arimaa/download/gameData/'
f = urllib2.urlopen(link)
html = f.read()

p = MyHTMLParser()
p.feed(html)
store = 'TextData/'

numLinks = len(p.links)
count = 1
print "Extracting data from " + str(numLinks) + " links..."

for l in p.links:
	ziploc = link + l
	sys.stdout.write("(" + str(count) + "/" + str(numLinks) +")" + " Pulling data from " + link + l + " ... ")
	zipfile = urllib2.urlopen(ziploc).read()
	result = ZipFile(StringIO(zipfile))
	result.extractall(store)
	print("done!")
	count += 1

p.close()
