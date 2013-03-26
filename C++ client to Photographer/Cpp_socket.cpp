#include <iostream>
#include <fstream>
using namespace std;

#include <stdio.h>
#include <conio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#include <winsock2.h>
#include <ws2tcpip.h>

#include <winsock.h>
#include <io.h>


#define PORT 6000
#define RCVBUFSIZE 8192

static void error_exit(char *errorMessage) {

    fprintf(stderr,"%s: %d\n", errorMessage, WSAGetLastError());
    exit(EXIT_FAILURE);
}

void SendPingRequest(int conn)
{
    char *echo_string;
    int echo_len;
	echo_string = "{ \"type\": \"ping\" }";
    echo_len = strlen(echo_string);

    if (send(conn, echo_string, echo_len, 0) != echo_len)
        error_exit("send() has sent a different number of bytes than expected !!!!");
}

void SendTakePhotoRequest(int conn, int DesiredTimeStamp)
{
    char buffer[100];
    int len;
	sprintf(buffer,"{ \"type\": \"takepicture\", \"desiredtimestamp\": \"%d\" }",DesiredTimeStamp);
    len = strlen(buffer);

    if (send(conn, buffer, len, 0) != len)
        error_exit("send() has sent a different number of bytes than expected !!!!");
}

void ProcessIncomingJSON(int sock,char *buffer)
{
	cout << "JSON received:" << endl << buffer << endl << "End of JSON" << endl;

	// Parse JSON
	char *posJPEG = strstr(buffer,"JPEG");
	char *posTimeStamp = strstr(buffer,"timestamp");
	char *posPong = strstr(buffer,"pong");

	if (posPong)
	{
		cout << "PONG received, nothing left to do..." << endl;
	}
	else if (posJPEG)
	{
		char tmpS[100];
		char *posTimeStampValue = posTimeStamp + 13;
		char *posTimeStampValueEnd = strstr(posTimeStampValue,"\"");
		int len = posTimeStampValueEnd-posTimeStampValue;
		strncpy(tmpS, posTimeStampValue, len );
		*(tmpS+len) = 0;
		int timestamp = atoi(tmpS);

		char *posSize = strstr(buffer,"size");
		char *posSizeValue = posSize + 8;
		char *posSizeValueEnd = strstr(posSizeValue,"\"");
		len = posSizeValueEnd-posSizeValue;
		strncpy(tmpS, posSizeValue, len );
		*(tmpS+len) = 0;
		int jpegSize = atoi(tmpS);

		cout << "Receiving JPEG. Timestamp=" << timestamp << ", size=" << jpegSize << endl;
	
		char receiveBuffer[RCVBUFSIZE];
		int received;
		int jpegBytes = 0;

		ofstream outFile;
		if (outFile != NULL) 
		{
			outFile.open("D:\\test.jpg" , ofstream::binary);
			cout << "File opened!" << endl;
		} else 
		{
			cout << "Can't open file!" << endl;
		}

		while ((received = recv(sock, receiveBuffer, RCVBUFSIZE, 0)) > 0) 
		{
			jpegBytes += received;
			if (outFile.is_open()) 
			{
				outFile.write(receiveBuffer, received); 
				cout << " (Total: " << jpegBytes << " B)" << endl;
			}
			else
			{
				cout << "Error in recv() function, received bytes = " << received << endl;
			}
		}

		outFile.flush();
		outFile.close();

		cout << "JPEG received successfully. Sent size=" << jpegSize << ", received size=" << jpegBytes << endl;
	}
}

// ----------------------------------------------------------------------------
/*
class PhoneProxy
{
public:

	void RequestPhoto(int desiredTimeStamp, char* filename);
	void RequestPing();

private:
	SOCKET sock;
	void Connect(char *ip, int port);
	void Disconnect();
}

void PhoneProxy::RequestPhoto(int desiredTimeStamp, char* filename)
{
}

void PhoneProxy::RequestPing()
{
}

void PhoneProxy::Connect(char *ip, int port)
{
	struct sockaddr_in server;
    struct hostent *host_info;
    unsigned long addr;
	char *ip_buff = "127.0.0.1"; //ip beállítása
	int iResult;

	char tmpBuff[100];
	cout << "Press enter to start connecting..." << endl;
	cin >> tmpBuff;

	cout << "Connecting..." << endl;

    WORD wVersionRequested;
    WSADATA wsaData;
    wVersionRequested = MAKEWORD (1, 1);
    if (WSAStartup (wVersionRequested, &wsaData) != 0)
        error_exit( "Initialisation of Winsock failed");
    else
        printf("Winsock Initialised\n");

    sock = socket( AF_INET, SOCK_STREAM, 0 );

    if (sock < 0)
        error_exit( "Socket error");

    memset( &server, 0, sizeof (server));
    if ((addr = inet_addr( ip_buff)) != INADDR_NONE) {
        memcpy( (char *)&server.sin_addr, &addr, sizeof(addr));
    }
    else {
        host_info = gethostbyname(ip_buff);
        if (NULL == host_info)
            error_exit("Unknown Server");
        memcpy( (char *)&server.sin_addr,
                host_info->h_addr, host_info->h_length );
    }

    server.sin_family = AF_INET;
    server.sin_port = htons( PORT );

    if(connect(sock,(struct sockaddr*)&server,sizeof(server)) <0)
        error_exit("Connection to the server failed");
}

void PhoneProxy::Disconnect()
{
}


*/

int main( int argc, char *argv[])
{
    struct sockaddr_in server;
    struct hostent *host_info;
    unsigned long addr;
	char *ip_buff = "127.0.0.1"; //ip beállítása
	int iResult;
    SOCKET sock;

	char tmpBuff[100];
	cout << "Press enter to start connecting..." << endl;
	cin >> tmpBuff;

	cout << "Connecting..." << endl;

    WORD wVersionRequested;
    WSADATA wsaData;
    wVersionRequested = MAKEWORD (1, 1);
    if (WSAStartup (wVersionRequested, &wsaData) != 0)
        error_exit( "Initialisation of Winsock failed");
    else
        printf("Winsock Initialised\n");

    sock = socket( AF_INET, SOCK_STREAM, 0 );

    if (sock < 0)
        error_exit( "Socket error");

    memset( &server, 0, sizeof (server));
    if ((addr = inet_addr( ip_buff)) != INADDR_NONE) {
        memcpy( (char *)&server.sin_addr, &addr, sizeof(addr));
    }
    else {
        host_info = gethostbyname(ip_buff);
        if (NULL == host_info)
            error_exit("Unknown Server");
        memcpy( (char *)&server.sin_addr,
                host_info->h_addr, host_info->h_length );
    }

    server.sin_family = AF_INET;
    server.sin_port = htons( PORT );

    if(connect(sock,(struct sockaddr*)&server,sizeof(server)) <0)
        error_exit("Connection to the server failed");

	SendPingRequest(sock);
	//SendTakePhotoRequest(sock, 123456);

	iResult = shutdown(sock, SD_SEND);

	if (iResult == SOCKET_ERROR) {
        printf("shutdown failed with error: %d\n", WSAGetLastError());
        closesocket(sock);
        WSACleanup();
        return 1;
    }

	// Receive response
	int totalBytes = 0;
	char buffer[RCVBUFSIZE] = "";
	int received = 0;

	// Read JSON part (1 byte at once)
	char c;
	char *bufPtr = buffer;
	*bufPtr = 0;
	bool processed = false;
	while ((received = recv(sock, &c, 1, 0)) > 0) 
	{
		if (c != '\0')	// Not at end of JSON
		{
			*bufPtr = c;
			bufPtr++;
			*bufPtr = 0;
		}
		else
		{
			// End of JSON
			ProcessIncomingJSON(sock,buffer);
			processed = true;
		}
	}
	if (!processed)
	{
		ProcessIncomingJSON(sock,buffer);
	}

	closesocket(sock);
	WSACleanup();
	return EXIT_SUCCESS;
}
