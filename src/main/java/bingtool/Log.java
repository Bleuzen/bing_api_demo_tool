package bingtool;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class Log {

	static Logger log = (Logger) LoggerFactory.getLogger(Log.class.getPackage().getName());

}
