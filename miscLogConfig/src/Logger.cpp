#include<stdio.h>
#include<STDARG.H>

class Logger
{
protected:
	static Logger *instance;
	int loglevel;
public:
	const static int LOGLEVEL_ERROR = 10;
	const static int LOGLEVEL_WARNING = 5;
	const static int LOGLEVEL_DEBUG = 3;
	const static int LOGLEVEL_VERBOSE = 1;
	const static int LOGLEVEL_INFO = 0;

	Logger()
	{
		instance=this;
		loglevel = LOGLEVEL_WARNING;
	}

	void SetLogLevel(int iLogLevel)
	{
		loglevel = iLogLevel;
	}

	int GetLogLevel(void)
	{
		return loglevel;
	}

	virtual void Log(int aLogLevel, const char *tag, const char *format, ...)=0;

	static Logger *getInstance(void)
	{
		return instance;
	}
};

Logger *Logger::instance=NULL;

class FileLogger : Logger
{
	FILE *F;
public:
	FileLogger(char *filename)
	{
		F = fopen(filename,"at");
	}

	void close()
	{
		fflush(F);
		fclose(F);
	}

	virtual void Log(int aLogLevel, const char *tag, const char *format, ...)
	{
		if (aLogLevel >= loglevel)
		{
			va_list args;
			va_start (args, format);
			vfprintf (F, format, args);
			va_end (args);
		}
	}
};

class StdoutLogger : Logger
{
public:
	virtual void Log(int aLogLevel, const char *tag, const char *format, ...)
	{
		if (aLogLevel >= loglevel)
		{
			va_list args;
			va_start (args, format);
			vfprintf (stdout, format, args);
			va_end (args);
		}
	}
};


void main()
{
	int i=12;

	FileLogger loggerF("d:\\e3.txt");
	//StdoutLogger loggerStd;

	Logger::getInstance()->Log(Logger::LOGLEVEL_ERROR,"TAG","Szam:%d %d %s %d\n",1,2,"Hello",3);

	loggerF.close();
}
