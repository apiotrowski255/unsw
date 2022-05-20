"""
    Sample code for Multi-Threaded Server
    Python 3
    Usage: python3 TCPserver3.py 12000
    coding: utf-8
    
    Author: Alex Piotrowski
"""
from socket import *
from threading import Thread
import sys, select
import time
from datetime import datetime
import os.path
from os import path


# acquire server host and port from command line parameter
if len(sys.argv) != 2:
    print("\n===== Error usage, python3 TCPServer3.py SERVER_PORT ======\n")
    exit(0)
serverHost = "127.0.0.1"
serverPort = int(sys.argv[1])
serverAddress = (serverHost, serverPort)

# define socket for the server side and bind address
serverSocket = socket(AF_INET, SOCK_STREAM)
serverSocket.bind(serverAddress)

logged_in_users = []

"""
    Define multi-thread class for client
    This class would be used to define the instance for each connection from each client
    For example, client-1 makes a connection request to the server, the server will call
    class (ClientThread) to define a thread for client-1, and when client-2 make a connection
    request to the server, the server will call class (ClientThread) again and create a thread
    for client-2. Each client will be runing in a separate therad, which is the multi-threading
"""
class ClientThread(Thread):
    def __init__(self, clientAddress, clientSocket):
        Thread.__init__(self)
        self.clientAddress = clientAddress
        self.clientSocket = clientSocket
        self.clientAlive = False
        self.clientUsername = ""
        
        print("===== New connection created for: ", clientAddress)
        self.clientAlive = True
        
    def run(self):
        message = ''
        
        while self.clientAlive:
            # use recv() to receive message from the client
            data = self.clientSocket.recv(1024)
            message = data.decode()
            
            # if the message from client is empty, the client would be off-line then set the client as offline (alive=Flase)
            if message == '':
                self.clientAlive = False
                print("===== the user disconnected - ", clientAddress)
                break
            
            # handle message from the client
            if message.startswith("login"):
                print("[recv] New login request")
                data = message.split()
                username = data[1]
    
                if username in logged_in_users:
                    self.clientSocket.send("User has already logged in".encode())
                elif (self.does_user_exist_in_credentials_txt(username) == False):
                    print("User does not exist - creating new user")
                    self.clientSocket.send("user does not exist - creating new user account".encode())
                    password_data = self.clientSocket.recv(1024)
                    password = password_data.decode()

                    fp = open("credentials.txt", 'a')
                    fp.write(username + " " + password + "\n")
                    fp.close()

                    self.clientSocket.send("user created".encode())
                else: 
                    print("User exists - requesting password")
                    self.clientSocket.send("user confirmed - please provide password".encode())
                    password_data = self.clientSocket.recv(1024)
                    password = password_data.decode()

                    if (self.process_authenication(username, password) == False):
                        self.clientSocket.send("incorrect password".encode())
                    else:
                        print("Authenication successful")
                        logged_in_users.append(username)
                        print(logged_in_users)
                        self.clientSocket.send("user authenication successful".encode())
                
            elif message == 'download':
                print("[recv] Download request")
                message = 'download filename'
                print("[send] " + message)
                self.clientSocket.send(message.encode())
            elif message.startswith("MSG"):
                print("[recv] Post message request")
                data = message.split()
                if len(data) < 4:
                    # incorrect number of arguments. - return error
                    self.clientSocket.send("Error with posting message - Incorrect number of arguments".encode())
                elif self.does_thread_exist(data[1]) == False: 
                    self.clientSocket.send("Error with posting message - Thread does not exist".encode())
                else: 
                    line_number = self.get_number_of_lines_from_file(data[1])
                    fp = open(data[1], 'a')
                    fp.write(str(line_number) + " " + str(data[-1]) + ": " + " ".join(str(x) for x in data[2:-1]) + "\n")
                    fp.close()

                    self.clientSocket.send("Message posted successfully!".encode())
            elif message.startswith("CRT"):
                print("[recv] create new thread request")
                data = message.split()
                if len(data) != 3:
                    self.clientSocket.send("Error with creating thread - Incorrect number of arguments".encode())
                else:
                    print(data[0])
                    print(data[1])
                    username = data[2]
                    does_thread_exist = path.exists(data[1])
                    if does_thread_exist == True:
                        self.clientSocket.send("Error with creating thread - Threadtitle already exists".encode())
                    else:
                        fp = open(data[1], 'w')
                        if username == "":
                            fp.close()
                            os.remove(data[1])
                            self.clientSocket.send("Error with creating thread - User is not logged in.".encode())
                        else: 
                            fp.write(username)
                            fp.write("\n")
                            fp.close()
                            self.clientSocket.send("Thread created".encode())
            elif message.startswith("DLT"):
                print("[recv] delete message in thread request")
                data = message.split()
                if len(data) != 4:
                    self.clientSocket.send("Error deleting message from thread - Incorrect number of arguments".encode())
                else: 
                    thread = data[1]
                    message_number = data[2]
                    user = data[3]

                    # check if the thread exists
                    if (self.does_thread_exist(thread) == False):
                        self.clientSocket.send("Error deleting message from thread - Thread does not exist".encode())
                    
                    # check if the message number exists in the thread
                    elif (self.check_message_number_is_valid_in_thread(thread, message_number) == False):
                        self.clientSocket.send("Error deleting message from thread - Message number does not exist".encode())

                    # check if the user made this message number
                    elif (self.check_message_number_belongs_to_user(thread, message_number, user) == False):
                        self.clientSocket.send("Error deleting message from thread - You are not the owner of the message".encode())
                    else: 
                        self.delete_message_number_from_thread(thread, message_number)
                        self.clientSocket.send("Successfully deleted message from thread".encode())
            elif message.startswith("EDT"):
                print("[recv] edit message in thread request")
                data = message.split()
                if len(data) < 5:
                    self.clientSocket.send("Error with editing message from thread - Incorrect number of arguments".encode())
                else: 
                    thread = data[1]
                    message_number = data[2]
                    message = data[3:-1]
                    user = data[-1]

                    if (self.does_thread_exist(thread) == False):
                        self.clientSocket.send("Error with editing message from thread - Thread does not exist".encode())
                    elif (self.check_message_number_is_valid_in_thread(thread, message_number) == False):
                        self.clientSocket.send("Error with editing message from thread - Message number does not exist".encode())
                    elif (self.check_message_number_belongs_to_user(thread, message_number, user) == False):
                        self.clientSocket.send("Error with editing message from thread - You are not the owner of the message".encode())
                    else:
                        self.edit_message_number_from_thread(thread, message_number, message) 
                        self.clientSocket.send("Successfully edited message from thread".encode())
            elif message.startswith("LST"):
                print("[recv] List threads request")
                list_of_threads = []
                for root, dirs, files in os.walk("."):
                    for file in files:
                        if (str(file).endswith(".py") == False and str(file).endswith(".txt") == False):
                            list_of_threads.append(str(file))
                self.clientSocket.send(" ".join(list_of_threads).encode())
            elif message.startswith("RDT"):
                print("[recv] read thread request")
                data = message.split()
                if len(data) != 2:
                    self.clientSocket.send("Error with reading thread - Incorrect number of arguments".encode())    
                else: 
                    thread = data[1]
                    if (self.does_thread_exist(thread) == False):
                        self.clientSocket.send("Error with editing message from thread - Thread does not exist".encode())
                    else: 
                        fp = open(thread, 'r')
                        lines = fp.readlines()
                        fp.close()
                        lines[0] = "RDT"
                        self.clientSocket.send("".join(lines).encode())
            elif message.startswith("RMV"):
                print("[recv] remove thread request")
                data = message.split()
                if len(data) != 3:
                    self.clientSocket.send("Error with removing thread - Incorrect number of arguments".encode())
                else: 
                    thread = data[1]
                    user = data[2]
                    if (self.does_thread_exist(thread) == False):
                        self.clientSocket.send("Error with removing thread - Thread does not exist".encode())
                    elif (self.check_if_thread_belongs_to_user(thread, user) == False):
                        self.clientSocket.send("Error with removing thread - Thread does belong to user".encode())
                    else:
                        self.remove_thread(thread)
                        self.clientSocket.send("Thread removed successfully".encode())
            elif message.startswith("XIT"):
                data = message.split()
                user = data[1]
                logged_in_users.remove(user)
            else:
                print("[recv] " + message)
                print("[send] Cannot understand this message")
                message = 'Cannot understand this message'
                self.clientSocket.send(message.encode())
    
    """
        You can create more customized APIs here, e.g., logic for processing user authentication
        Each api can be used to handle one specific function, for example:
        def process_login(self):
            message = 'user credentials request'
            self.clientSocket.send(message.encode())
    """
    def remove_thread(self, thread):
        os.remove(thread)

    def check_if_thread_belongs_to_user(self, thread, user):
        fp = open(thread, 'r')
        line = fp.readline()
        fp.close()

        if (line.startswith(user) == True):
            return True
        else:
            return False

    def edit_message_number_from_thread(self, thread, message_number, message):
        fp = open(thread, 'r')
        lines = fp.readlines()
        fp.close()

        fp = open(thread, 'w')
        for number, line in enumerate(lines):
            if number != int(message_number):
                fp.write(line)
            else: 
                line_data = line.split()
                newline = " ".join(line_data[0:2]) + " " + " ".join(message)
                print(newline)
                fp.write(newline + "\n")
        fp.close()

    def delete_message_number_from_thread(self, thread, message_number):
        fp = open(thread, 'r')
        lines = fp.readlines()
        fp.close()

        fp = open(thread, 'w')
        for number, line in enumerate(lines):
            if number > int(message_number):
                line_data = line.split()
                line_data[0] = str(int(line_data[0]) - 1)
                newline = " ".join(line_data)
                print(newline)
                fp.write(newline + "\n")
            elif number != int(message_number):
                fp.write(line)
        fp.close()


    def check_message_number_belongs_to_user(self, thread, message_number, user):
        FILE = open(thread, 'r')
        lines = FILE.readlines()
        for line in lines:
            line_data = line.split()
            if (len(line_data) != 1): 
                # the line number is the first element in the array
                if str(message_number) == line_data[0]:
                    FILE.close()
                    if str(user) + ":" == line_data[1]:
                        return True
                    else:
                        return False
        FILE.close()
        return False

    def check_message_number_is_valid_in_thread(self, thread, message_number):
        FILE = open(thread, 'r')
        lines = FILE.readlines()
        for line in lines:
            line_data = line.split()
            if (len(line_data) != 1): 
                # the line number is the first element in the array
                if str(message_number) == line_data[0]:
                    FILE.close()
                    return True
        FILE.close()
        return False


    def get_number_of_lines_from_file(self, file_name):
        with open(file_name, 'r') as fp:
            number_of_lines = len(fp.readlines())
            return number_of_lines

    def does_thread_exist(self, thread_name):
        return path.exists(thread_name)



    def process_login(self, username):
        message = 'user credentials request'
        print('[send] ' + message)
        self.clientSocket.send(message.encode())

        result = ""

        print("[recv] recieved username: " + username)

        does_user_exist = self.does_user_exist_in_credentials_txt(username)

        print(does_user_exist)

        if does_user_exist == True:
            # we move onto the next phase - password authenication

            print('[send] ' + "Confirmation - User Exist")
            self.clientSocket.send("user exists".encode())

            # we request the client to send us a password
            password_data = self.clientSocket.recv(1024)
            password = password_data.decode()
            print(password)
            password_auth_result = self.process_authenication(username, password)
            print(password_auth_result)

            if password_auth_result == True:
                result = "Login Successful"
            else: 
                result = "Error - Incorrect password"

        else:
            self.clientSocket.send("Error - User does not exist".encode())

            # awaiting a new password from the client
            password_data = self.clientSocket.recv(1024)
            password = password_data.decode()
            print("new password is: " + password)

            self.add_user_to_credentials_txt(username, password)

            result = "User created"



        
        return result
        

    def process_authenication(self, username, password):
        FILE = open('credentials.txt', 'r')
        status = False
        for one_profile in FILE:
            data = one_profile.split()
            if data[0] == username and data[1] == password:
                status = True
        return status 
        

    def does_user_exist_in_credentials_txt(self, username):
        FILE = open('credentials.txt', 'r')
        status = False
        for one_profile in FILE:
            data = one_profile.split()
            if data[0] == username:
                status = True
        return status

    def add_user_to_credentials_txt(self, username, password):
        FILE = open('credentials.txt', 'a')
        FILE.write(username + ' ' + password + '\n')




print("\n===== Server is running =====")
print("===== Waiting for connection request from clients...=====")


while True:
    serverSocket.listen()
    clientSockt, clientAddress = serverSocket.accept()
    clientThread = ClientThread(clientAddress, clientSockt)
    clientThread.start()
