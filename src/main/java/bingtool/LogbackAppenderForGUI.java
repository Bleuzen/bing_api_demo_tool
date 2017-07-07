package bingtool;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogbackAppenderForGUI extends AppenderBase<ILoggingEvent> {

	private final JTextArea textArea;
	private final PatternLayout layout;

	public LogbackAppenderForGUI(JTextArea jTextArea) {
		textArea = jTextArea;

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		layout = new PatternLayout();
		layout.setPattern("[%level] - %msg");
		layout.setContext(context);
		layout.start();

		start();

		context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(this);

		super.setContext(context);
	}

	@Override
	protected void append(ILoggingEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String log = layout.doLayout(event);
				textArea.append(System.lineSeparator() + log);
				textArea.setCaretPosition(textArea.getText().length());
			}
		});
	}

}
