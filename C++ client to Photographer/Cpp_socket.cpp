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

int main( int argc, char *argv[]) {
    struct sockaddr_in server;
    struct hostent *host_info;
    unsigned long addr;
	char *ip_buff = "192.168.74.103"; //ip beállítása
	int iResult;
    SOCKET sock;
    char *echo_string;
    int echo_len;

	echo_string = "p"; // p-betû -->képkészítés; ping --> pingelés
    echo_len = strlen(echo_string);


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

    if (send(sock, echo_string, echo_len, 0) != echo_len)
        error_exit("send() has sent a different number of bytes than expected !!!!");

	iResult = shutdown(sock, SD_SEND);

	if (iResult == SOCKET_ERROR) {
        printf("shutdown failed with error: %d\n", WSAGetLastError());
        closesocket(sock);
        WSACleanup();
        return 1;
    }


	//image reception
	int totalBytes = 0;
	ofstream outFile;
	char buf[1024] = "";
	int received = 0;
	char pong_buff[5];

	if(echo_string == "p")
	{
		if (outFile != NULL) 
		{
			outFile.open("D:\\test.jpg" , ofstream::binary);
			cout << "File opened!" << endl;
		} else 
		{
			cout << "Can't open file!" << endl;
		}

		while ((received = recv(sock, buf, sizeof(buf), 0)) > 0) 
		{
			cout << "R:" << received << " ";
			if (received > 0) 
			{
				totalBytes += received;
				if (outFile.is_open()) 
				{
					outFile.write(buf, received); 
					cout << " (Total: " << totalBytes << " B)" << endl;
				} else
				cout << "Error in recv() function, received bytes = " << received << endl;
			} else 
				cout << "R:" << received << " ";
		}	
		outFile.close();
	}

	if(echo_string == "ping")
		{
			recv(sock, buf, sizeof(buf),0);
			for(int i=2; i<6; i++)
			{
				pong_buff[i-2]=buf[i];
			}
			pong_buff[4]='\0';
			cout<<pong_buff<< endl;
		}
		closesocket(sock);
		WSACleanup();
		return EXIT_SUCCESS;
}