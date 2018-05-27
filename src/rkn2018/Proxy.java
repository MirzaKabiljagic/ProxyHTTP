package rkn2018;

import java.io.IOException;
import java.util.List;
import java.util.Map;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Proxy {
	protected final String dumpPath;
	protected final Map<String, String> contentReplacements, headerReplacements, redirections;
	public static String jsInjectPath;
	protected final List<String> stripDomains;
	protected final String mitmCertificatePath;
	protected final boolean sopSwitch;

	public Proxy(String dumpPath, String jsInjectPath, Map<String, String> headerReplacements, boolean sopSwitch,
			Map<String, String> contentReplacements, Map<String, String> redirections, List<String> stripDomains,
			String mitmCertificatePath) {
		this.dumpPath = dumpPath;
		this.jsInjectPath = jsInjectPath;
		this.headerReplacements = headerReplacements;
		this.sopSwitch = sopSwitch;
		this.contentReplacements = contentReplacements;
		this.redirections = redirections;
		this.stripDomains = stripDomains;
		this.mitmCertificatePath = mitmCertificatePath;
	}

	public Map<String, String > getReplacements()
	{
		return contentReplacements;
	}

	public void runProxy() throws Exception {
		// TODO: This is the starting point of your proxy implementation
		ProxyConnection proxyConnection;
		proxyConnection = new ProxyConnection(this);
		System.out.println("Starting the connection");
		try {

			proxyConnection.startServer();
		}
		catch(IOException e)
		{
			System.out.print("It is not possible to run server \n");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("help", "print this message");
		// header replacement
		options.addOption(Option.builder("header")
				.hasArg()
				.argName("new headerline")
				.desc("Replace header fields of requests and responses")
				.build());
		// SOP switch
		options.addOption(Option.builder("SOP")
				.hasArg(false)
				.desc("If set, the SOP mechanism should be circumvented")
				.build());
		// content replacement
		options.addOption(Option.builder("content")
				.hasArg()
				.argName("regex^replacement")
				.desc("Replace content strings. Also regular expressions should work")
				.build());
		// dump
		options.addOption(Option.builder("dump")
				.hasArg()
				.argName("outfile")
				.desc("Activate dumping to given outfile")
				.build());
		// SSL strip
		options.addOption(Option.builder("strip")
				.hasArg()
				.argName("domain to strip")
				.desc("SSL strip given domain. Is no domain given -> strip all domains")
				.build());
		// JS Injector
		options.addOption(Option.builder("jsinject")
				.hasArg()
				.argName("js file")
				.desc("Inject given JavaScript")
				.build());
		// Redirection
		options.addOption(Option.builder("redirect")
				.hasArg()
				.argName("domain^redirection")
				.desc("Redirect a domain to another")
				.build());
		// Certificates
		options.addOption(Option.builder("mitm")
				.hasArg()
				.argName("rootCA.pfx")
				.desc("Provide root CA certificate with private key")
				.build());
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("help"))
				throw new ParseException("help");
			
			ProxyBuilder builder = new ProxyBuilder();
			builder.dumpTo(cmd.getOptionValue("dump", ""));
			builder.injectJS(cmd.getOptionValue("jsinject", ""));
			builder.setMitmCertificate(cmd.getOptionValue("mitm", ""));
			builder.setContentReplacements(cmd.getOptionValues("content"));
			builder.setRedirections(cmd.getOptionValues("redirect"));
			builder.setHeaderReplacements(cmd.getOptionValues("header"));
			builder.setStripDomains(cmd.getOptionValues("strip"));
			builder.setSOP(cmd.hasOption("SOP"));
			
			Proxy proxy = builder.build();
			proxy.runProxy();
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("proxy", options);
		}
	}
}
