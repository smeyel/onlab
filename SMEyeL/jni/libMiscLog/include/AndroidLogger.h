#ifndef __ANDROIDLOGGER_H
#define __ANDROIDLOGGER_H

#include <android/log.h>
#include "Logger.h"

namespace Logging
{
	class AndroidLogger : public Logger
	{
	public:
		virtual void vlog(int _logLevel, const char *tag, const char *format, va_list argp);
	};
}

#endif
