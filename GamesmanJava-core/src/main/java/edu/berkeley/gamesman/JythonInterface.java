package edu.berkeley.gamesman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.python.core.PyObject;
import org.python.util.InteractiveConsole;
import org.python.util.JLineConsole;
import org.python.util.ReadlineConsole;

import edu.berkeley.gamesman.util.JythonUtil;

/**
 * @author Jeremy Fleischman
 * @author Steven Schlansker
 * 
 */
public final class JythonInterface extends GamesmanApplication {
	private enum Consoles {
		DUMB, READLINE, JLINE
	}

	/**
	 * No arg constructor
	 */
	public JythonInterface() {
	}

	@Override
	public int run(Properties props) {
		// Preferences prefs =
		// Preferences.userNodeForPackage(JythonInterface.class);
		Consoles console = null;
		while (console == null) {
			System.out.println("Available consoles:");
			for (int i = 0; i < Consoles.values().length; i++)
				System.out.printf("\t%d. %s\n", i,
						Consoles.values()[i].toString());
			System.out.print("What console would you like to use? ");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			int i = -1;
			try {
				i = Integer.parseInt(br.readLine());
			} catch (NumberFormatException e) {
				System.out.println("Please input a number");
				continue;
			} catch (IOException e) {
				throw new Error(e);
			}
			if (i < 0 || i >= Consoles.values().length) {
				System.out.println(i + " is out of range");
				continue;
			}
			console = Consoles.values()[i];
		}
		JythonUtil.init();
		// prefs.put("console", console.name());
		InteractiveConsole rc = null;
		switch (console) {
		case DUMB:
			rc = new InteractiveConsole() {
				@Override
				public String raw_input(PyObject prompt) {
					// eclipse sometimes interleaves the error messages
					exec("import sys; sys.stderr.flush()");
					return super.raw_input(prompt);
				}
			};
			break;
		case JLINE:
			rc = new JLineConsole();
			break;
		case READLINE:
			rc = new ReadlineConsole();
			break;
		default:
			throw new Error("Need to pick a console");
		}

		// this will let us put .py files in the junk directory, and things will
		// just work =)

		rc.exec("from Play import *");
		rc.interact();
		return 0;
	}

	/**
	 * Simple main function to run JythonInterface directly.
	 * 
	 * @param args
	 *            program args
	 */
	public static void main(String[] args) {
		String[] newArgs = new String[args.length + 1];
		newArgs[0] = "JythonInterface";
		System.arraycopy(args, 0, newArgs, 1, args.length);
		Gamesman.main(newArgs);
	}
}
