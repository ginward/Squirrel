'''
The script to process wav file data from java
Python version: 2.7.10
Author: Jinhua Wang 
License: MIT
'''

'''
Squirrel Protocol: 

client (JAVA) send the file to server(python) for conversion (BASE64 Encoding)
SEND\n
LENGTH\n
IN SAMPLE RATE\n
IN NUM CHNNELS\n
[DATA]

server (python) send the converted file back to client (JAVA) (BASE64 Encoding)
RECV\n
LENGTH\n
[DATA]

close socket
END\n
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
	print "new client" + str(socket)
	while True:
		cmd = buffered_readLine(socket)
		if cmd == 'END':
			#terminate the socket
			socket.close()
			print "terminated:"+str(socket)
			break
		if cmd == "SEND":
			#start converting the file received from the socket
			length = int(buffered_readLine(socket))
			inrate = int(buffered_readLine(socket))
			if inrate is None:
				inrate = 44100
			numChannels = int(buffered_readLine(socket))
			if numChannels is None:
				numChannels = 1
			#decode the base64 data 
 			data_decoded = StringIO.StringIO(decode_base64(buffered_readLine(socket)))
 			data_decoded.seek(0)
 			data_output = StringIO.StringIO()
 			#starts the conversion process
 			convert.downsampleWav(data_decoded, data_output, int(inrate), 8000, int(numChannels), 1)
 			data_output.seek(0) #reset the buffer head
 			encoded_string = base64.b64encode(data_output.read())
 			msg_send = "RECV\n" + str(len(encoded_string))+ "\n" +encoded_string
 			socket.send(msg_send)

#read one line from the socket
def buffered_readLine(socket):
	line = ""
	while True:
		part = socket.recv(1)
		if part != "\n":
			line+=part
		elif part == "\n":
			break
	return line

def decode_base64(data):
    """Decode base64, padding being optional.

    :param data: Base64 data as an ASCII byte string
    :returns: The decoded byte string.
    """
    missing_padding = len(data) % 4
    if missing_padding != 0:
        data += b'='* (4 - missing_padding)
    return base64.b64decode(data)

#accept connections from clients
while True:
	c, addr = s.accept() 
	print 'Got connections from', addr
	thread.start_new_thread(on_new_client,(c,))