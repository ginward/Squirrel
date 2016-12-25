'''
The script to process wav file data from java
Python version: 2.7.10
Author: Jinhua Wang 
License: MIT
'''

'''
Squirrel Protocol: 

client (JAVA) send the file to server(python) for conversion (BASE64 Encoding)
SEND\n\r 
IN SAMPLE RATE\n\r
IN NUM CHNNELS\n\r
[DATA]

server (python) send the converted file back to client (JAVA) (BASE64 Encoding)
RECV\n\r
[DATA]

close socket
END\n\r
'''

#the module to downsample wav file
import convert
#the socket for file transmission
import socket
#multithreading to handle multiple clients 
import thread
import base64
import StringIO

CONST_PORT = 8081
CONST_HOST = 'localhost'

s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.bind((CONST_HOST, CONST_PORT))
s.listen(5)
print "listenting on " + str(CONST_HOST) + ":" + str(CONST_PORT)

def on_new_client(socket):
	print "new client" + socket
	while True:
		msg = socket.recv(1024)
		cmd = msg[:msg.find['\n\r']]
		if cmd == 'END':
			#terminate the socket
			socket.close()
			print "terminated:"+socket
			break
		if cmd == "SEND":
			#start converting the file received from the socket
			data = msg.split('\n\r',4)
			inrate = data[1] if data[1] is not None else 44100
			numChannels = data[2] if data[2] is not None else 1
			#decode the base64 data
 			data_decoded = StringIO.StringIO(base64.b64decode(data[3]))
 			data_output = StringIO.StringIO()
 			#starts the conversion process
 			convert.downsampleWav(data_decoded, data_output, inrate, 16000, numChannels, 1)
 			data_output.seek(0) #reset the buffer head
 			encoded_string = base64.b64encode(data_output.read())
 			msg_send = "RECV\n\r" + encoded_string
 			socket.send(msg_send)

#accept connections from clients
while True:
	c, addr = s.accept() 
	print 'Got connections from', addr
	thread.start_new_thread(on_new_client,(c,))