/*
 * Copyright (c) Christopher A Longo
 * Modified By YaSH069 23-04-2014
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yash069.hls;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class Main {
    private static final String ARG_KEY = "key";
    private static final String ARG_OUT_FILE = "file";
    private static final String ARG_PLAYLIST = "playlist";
    private static final String CLI_SYNTAX = "download-hls [options...] <url>";
    private static final String OPT_HELP = "h";
    private static final String OPT_KEY = "k";
    private static final String OPT_KEY_LONG = "force-key";
    private static final String OPT_OUT_FILE = "o";
    private static final String OPT_OUT_FILE_LONG = "output";
    private static final String OPT_SILENT = "s";
    private static final String OPT_OVERWRITE = "y";
	private static final String OPT_PLAYLIST = "p";
	
    public static void main(String[] args) {
        CommandLine commandLine = parseCommandLine(args);
        String[] commandLineArgs = commandLine.getArgs();

        try {
            String playlistUrl = commandLineArgs[0];
            String outFile = null;
            String key = null;
			String lplaylist = null;
			
            if (commandLine.hasOption(OPT_OUT_FILE)) {
                outFile = commandLine.getOptionValue(OPT_OUT_FILE);

                File file = new File(outFile);

                if (file.exists()) {
                    if (!commandLine.hasOption(OPT_OVERWRITE)) {
                        System.out.printf("File '%s' already exists. Overwrite? [y/N] ", outFile);

                        int ch = System.in.read();

                        if (!(ch == 'y' || ch == 'Y')) {
                            System.exit(0);
                        }
                    }

                    file.delete();
                }
            }

            if (commandLine.hasOption(OPT_KEY))
                key = commandLine.getOptionValue(OPT_KEY);

			if (commandLine.hasOption(OPT_PLAYLIST))
				lplaylist = commandLine.getOptionValue(OPT_PLAYLIST);
				
            PlaylistDownloader downloader =
                    new PlaylistDownloader(playlistUrl, lplaylist);

            if (commandLine.hasOption(OPT_SILENT)) {
                System.setOut(new PrintStream(new OutputStream() {
                    public void close() {}
                    public void flush() {}
                    public void write(byte[] b) {}
                    public void write(byte[] b, int off, int len) {}
                    public void write(int b) {}
                }));
            }

            downloader.download(outFile, key);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static CommandLine parseCommandLine(String[] args) {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = null;

        Option help = new Option(OPT_HELP, "help", false, "print this message.");
        Option silent = new Option(OPT_SILENT, "silent", false, "silent mode.");
        Option overwrite = new Option(OPT_OVERWRITE, false, "overwrite output files.");
		
		Option lPlaylist = OptionBuilder.withArgName(ARG_PLAYLIST)
				.hasArg()
				.withDescription("force local playlist")
				.create(OPT_PLAYLIST);
		
        Option key = OptionBuilder.withArgName(ARG_KEY)
                .withLongOpt(OPT_KEY_LONG)
                .hasArg()
                .withDescription("force use of the supplied AES-128 key.")
                .create(OPT_KEY);

        Option outFile = OptionBuilder.withArgName(ARG_OUT_FILE)
                .withLongOpt(OPT_OUT_FILE_LONG)
                .hasArg()
                .withDescription("join all transport streams to one file.")
                .create(OPT_OUT_FILE);

        Options options = new Options();

        options.addOption(help);
        options.addOption(silent);
        options.addOption(overwrite);
        options.addOption(key);
        options.addOption(outFile);
		options.addOption(lPlaylist);
		
        try {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption(OPT_HELP) || (commandLine.getArgs().length < 1)) {
                new HelpFormatter().printHelp(CLI_SYNTAX, options);
                System.exit(0);
            }
			
			if (commandLine.hasOption(OPT_PLAYLIST)) {
				String localPlaylist = commandLine.getOptionValue(OPT_PLAYLIST);
				File fp = new File(localPlaylist);
				if( !fp.isFile() ){
					System.out.println("File " + localPlaylist + " does exit or invalid file. \nExample -file D:\\playlist.m3u8");
					System.exit(1);
				}
			}
			
            if (commandLine.hasOption(OPT_KEY)) {
                String optKey = commandLine.getOptionValue(OPT_KEY);

                if (!optKey.matches("[0-9a-fA-F]{32}")) {
                    System.out.printf("Bad key format: \"%s\". Expected 32-character hex format.\nExample: -key 12ba7f70db4740dec4aab4c5c2c768d9", optKey);
                    System.exit(1);
                }
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp(CLI_SYNTAX, options);
            System.exit(1);
        }

        return commandLine;
    }
}

