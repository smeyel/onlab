#include <iostream>
#include <assert.h>

#include "../include/ConfigManagerBase.h"

using namespace MiscTimeAndConfig;

bool ConfigManagerBase::init(char *filename)
{
    // load from a data file
//    SI_Error rc = ini.LoadFile(filename);
//	assert(rc >= 0);

//	return this->readConfiguration(&ini);
	return this->readConfiguration();
}

//bool ConfigManagerBase::readConfiguration(CSimpleIniA *ini)
bool ConfigManagerBase::readConfiguration()
{
	assert(false);	// This method should not be called in the base class.
	return false;
}