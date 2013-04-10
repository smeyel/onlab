#include<stdio.h>

class ConfigReader
{
protected:
	static ConfigReader *instance;
public:

	ConfigReader()
	{
		instance=this;
	}

	static ConfigReader *getInstance(void)
	{
		return instance;
	}

	virtual bool getBoolValue(const char *section, const char *key);
	virtual int getIntValue(const char *section, const char *key);
	virtual char *getStringValue(const char *section, const char *key);
};

ConfigReader *ConfigReader::instance=NULL;